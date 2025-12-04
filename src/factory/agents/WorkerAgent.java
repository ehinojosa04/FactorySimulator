package factory.agents;

import java.util.LinkedList;
import core.Zones.ZonesAPI;
import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.production.ProductOrder;
import factory.warehouse.Warehouse;

public class WorkerAgent extends BaseAgent {

    private final LinkedList<ProductOrder> productOrders;
    private final Warehouse warehouse;
    private final InventoryAgent inventoryAgent;

    private final BathroomConnection bathroomConnection;
    private final BreakroomConnection breakroomConnection;
    private volatile boolean breakRequestInProgress = false;
    private volatile boolean hasRequestedBreak = false;

    private final ZonesAPI zones;

    private ProductOrder currentProductOrder;
    private int orderProgress = 0;

    private int materialsCarried = 0;
    private int totalMaterialsNeeded = 0;

    private AgentLocation targetLocation;
    private boolean holdingWorkstation;

    public WorkerAgent(
            String threadID,
            AgentLocation location,
            Warehouse warehouse,
            LinkedList<ProductOrder> productOrders,
            InventoryAgent inventoryAgent,
            ZonesAPI zones) {
        super(AgentType.WORKER, threadID, location);
        this.productOrders = productOrders;
        this.warehouse = warehouse;
        this.inventoryAgent = inventoryAgent;
        this.zones = zones;
        this.targetLocation = null; // Start with no target
        this.holdingWorkstation = false;

        this.bathroomConnection = new BathroomConnection("localhost", 5002, this);
        this.breakroomConnection = new BreakroomConnection("localhost", 5001, this);
    }

    public synchronized void updateStateFromServer(AgentState newState) {
        this.state = newState;
        System.out.println("[" + threadID + "] State from server: " + newState);
    }

    public synchronized void updateLocationFromServer(AgentLocation newLocation) {
        System.out.println("[" + threadID + "] Location from server: " + newLocation);

        AgentLocation oldLocation = this.location;
        this.location = newLocation;

        if (newLocation == AgentLocation.FACTORY) {
            if (oldLocation == AgentLocation.BATHROOM) {
                this.bathroomConnection.close();
            } else if (oldLocation == AgentLocation.BREAKROOM) {
                this.breakroomConnection.close();
            }

            state = AgentState.IDLE;
            breakRequestInProgress = false;
            hasRequestedBreak = false;
            shiftsSinceBreak = 0;
        }
    }

    public void handleServerEvent(String eventType) {
        System.out.println("[" + threadID + "] Facility event: " + eventType);
    }

    @Override
    protected void processNextState() {
        switch (state) {
            case IDLE:
                if (location == AgentLocation.FACTORY) {
                    if (currentProductOrder == null) {
                        currentProductOrder = productOrders.poll();
                    }

                    if (currentProductOrder != null) {
                        System.out.println(threadID + ": Received order. Calculating needs...");
                        this.totalMaterialsNeeded = currentProductOrder.quantity;
                        this.materialsCarried = 0;
                        System.out.println(
                                threadID + ": Requesting " + totalMaterialsNeeded + " materials from Inventory.");
                        inventoryAgent.requestMaterials(totalMaterialsNeeded);
                        startMovingTo(AgentLocation.WAREHOUSE);
                    }
                }
                break;

            case MOVING:
                // Only process arrival if we have a target and we're there
                if (targetLocation != null && location == targetLocation) {
                    arriveAtDestination();
                    targetLocation = null; // Clear to prevent re-triggering
                }
                break;

            case WAITING:
                if (location == AgentLocation.WAREHOUSE && currentProductOrder != null) {
                    if (materialsCarried >= totalMaterialsNeeded) {
                        System.out.println(
                                threadID + ": Collected all " + materialsCarried + " items. Returning to Factory.");
                        startMovingTo(AgentLocation.FACTORY);
                    }
                }
                break;

            case WORKING:
                // CRITICAL: Don't check break if we're already in a break cycle
                if (!breakRequestInProgress && !hasRequestedBreak && shouldTakeBreak()) {
                    System.out.println(threadID + ": Needs a break. Heading to facility.");
                    if (holdingWorkstation){
                        zones.getWorkstations().leave();
                        holdingWorkstation = false;
                    }

                    startMovingTo(random.nextBoolean() ? AgentLocation.BREAKROOM : AgentLocation.BATHROOM);
                    return;
                }

                if (currentProductOrder != null && orderProgress >= currentProductOrder.quantity) {
                    completeOrder();
                }
                break;

            case ON_BREAK:
                // Just wait - server controls this state
                break;
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (state) {
            case MOVING:
                // If server is controlling us (during break), don't override location
                if (breakRequestInProgress) {
                    sleepTime = 100;
                    stateDescriptor = "Server controlling movement";
                    return;
                }

                // Otherwise, simulate moving toward target
                if (targetLocation != null) {
                    stateDescriptor = "Moving to " + targetLocation;
                    sleepTime = 1000;
                    // IMPORTANT: Only update location if NOT being controlled by server
                    location = targetLocation;
                }
                break;

            case WAITING:
                if (location == AgentLocation.WAREHOUSE && currentProductOrder != null
                        && materialsCarried < totalMaterialsNeeded) {
                    int sourceIndex = currentProductOrder.getSourceMaterialIndex();
                    stateDescriptor = "Picking materials: " + materialsCarried + "/" + totalMaterialsNeeded;
                    boolean success = warehouse.getMaterial(sourceIndex);

                    if (success) {
                        materialsCarried++;
                        System.out.println(threadID + ": Picked up 1 item (" + materialsCarried + "/"
                                + totalMaterialsNeeded + ")");
                        sleepTime = 200;
                    } else {
                        stateDescriptor = "Waiting for materials " + materialsCarried + "/" + totalMaterialsNeeded;
                        sleepTime = 1000;
                    }
                } else {
                    sleepTime = 500;
                }
                break;

            case WORKING:
                if (currentProductOrder != null) {
                    if (!holdingWorkstation) {
                        state = AgentState.WAITING;
                        stateDescriptor = "Waiting for workstation";
                        zones.getWorkstations().enter();
                        state = AgentState.WORKING;
                        holdingWorkstation = true;
                    }
                    if (orderProgress < currentProductOrder.quantity) {
                        orderProgress++;
                        sleepTime = 500 * currentProductOrder.getTargetProductIndex();
                        stateDescriptor = "Building... " + orderProgress + "/" + currentProductOrder.quantity;
                        System.out.println(
                                threadID + ": Building... " + orderProgress + "/" + currentProductOrder.quantity);
                    } else {
                        stateDescriptor = "Finishing touches...";
                        sleepTime = 100;
                    }
                }
                break;

            case IDLE:
                stateDescriptor = "Waiting for orders";
                sleepTime = 500;
                break;

            case ON_BREAK:
                stateDescriptor = "Taking a break in " + (location != null ? location.toString() : "transition");
                sleepTime = 500;
                break;

            default:
                stateDescriptor = "Unknown state";
                sleepTime = 500;
                break;
        }
    }

    private void startMovingTo(AgentLocation destination) {
        this.targetLocation = destination;
        this.state = AgentState.MOVING;
    }

    private void arriveAtDestination() {
        switch (location) {
            case WAREHOUSE:
                state = AgentState.WAITING;
                break;
            case FACTORY:
                state = (currentProductOrder != null) ? AgentState.WORKING : AgentState.IDLE;
                break;
            case BREAKROOM:
                if (!hasRequestedBreak) {
                    hasRequestedBreak = true;
                    breakRequestInProgress = true;
                    System.out.println("[" + threadID + "] Requesting breakroom break through server...");
                    breakroomConnection.requestBreak();
                }
                break;
            case BATHROOM:
                if (!hasRequestedBreak) {
                    hasRequestedBreak = true;
                    breakRequestInProgress = true;
                    System.out.println("[" + threadID + "] Requesting bathroom break through server...");
                    bathroomConnection.requestBreak();
                }
                break;
        }
    }

    private void completeOrder() {
        System.out.println(threadID + ": Order Complete!");

        if (holdingWorkstation) {
            zones.getWorkstations().leave();
            holdingWorkstation = false;
        }

        warehouse.AddMaterials(currentProductOrder.getTargetProductIndex(), currentProductOrder.quantity);

        currentProductOrder = null;
        orderProgress = 0;
        materialsCarried = 0;
        state = AgentState.IDLE;
    }

    private boolean shouldTakeBreak() {
        shiftsSinceBreak++;

        // COOLDOWN: Don't take break immediately after returning
        if (shiftsSinceBreak <= 3) {
            return false;
        }

        if (shiftsSinceBreak > 3) {
            return random.nextInt(100) < (shiftsSinceBreak * 10);
        }
        return false;
    }
}