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

    // START DISTRIBUTED
    private final BathroomConnection bathroomConnection;
    private final BreakroomConnection breakroomConnection;
    private volatile boolean breakRequestInProgress = false;
    // END DISTRIBUTED

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
        this.targetLocation = location;
        this.holdingWorkstation = false;

        this.bathroomConnection = new BathroomConnection("localhost", 5000, this);
        this.breakroomConnection = new BreakroomConnection("localhost", 5001, this);
    }

    public void updateStateFromServer(AgentState newState) {
        this.state = newState;
        System.out.println("[" + threadID + "] State from bathroom server: " + newState);
    }

    public void updateLocationFromServer(AgentLocation newLocation) {
        System.out.println("[" + threadID + "] Location from bathroom server: " + newLocation);

        if (newLocation == AgentLocation.FACTORY) {
            if (location == AgentLocation.BATHROOM) this.bathroomConnection.close();
            else if (location == AgentLocation.BREAKROOM) this.breakroomConnection.close();
        }

        this.location = newLocation;
    }

    public void handleBathroomEventFromServer(String eventType) {
        System.out.println("[" + threadID + "] Bathroom event: " + eventType);
        if ("EXITED_BUFFER".equals(eventType)) {
            breakRequestInProgress = false;
            System.out.println("[" + threadID + "] Finished bathroom break, back to work.");
        }
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

                        System.out.println(threadID + ": Requesting " + totalMaterialsNeeded + " materials from Inventory.");
                        inventoryAgent.requestMaterials(totalMaterialsNeeded);

                        startMovingTo(AgentLocation.WAREHOUSE);
                    }
                }
                break;

            case MOVING:
                if (location == targetLocation) {
                    arriveAtDestination();
                }
                break;

            case WAITING:
                if (location == AgentLocation.WAREHOUSE && currentProductOrder != null) {
                    if (materialsCarried >= totalMaterialsNeeded) {
                        System.out.println(threadID + ": Collected all " + materialsCarried + " items. Returning to Factory.");
                        startMovingTo(AgentLocation.FACTORY);
                    }
                }
                break;

            case WORKING:
                if (shouldTakeBreak()) {
                    System.out.println(threadID + ": Needs a break. Heading to Breakroom.");
                    startMovingTo(random.nextBoolean() ? AgentLocation.BREAKROOM : AgentLocation.BATHROOM);
                    return;
                }

                if (orderProgress >= currentProductOrder.quantity) {
                    completeOrder();
                }
                break;

            case ON_BREAK:
                if (!breakRequestInProgress) {
                    System.out.println(threadID + ": Break over. Returning to Factory.");
                    startMovingTo(AgentLocation.FACTORY);
                }
                break;
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (state) {
            case MOVING:
                stateDescriptor = "Moving to " + targetLocation;
                sleepTime = 1000;
                location = targetLocation;
                break;

            case WAITING:
                if (location == AgentLocation.WAREHOUSE && materialsCarried < totalMaterialsNeeded) {
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
                        System.out.println(threadID + ": Building... " + orderProgress + "/" + currentProductOrder.quantity);
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
                stateDescriptor = "Taking a break in " + location.toString();
                sleepTime = 2000;
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
                breakRequestInProgress = true;
                System.out.println("[" + threadID + "] Requesting breakroom break through server...");
                breakroomConnection.requestBreak();
                shiftsSinceBreak = 0;
                break;
            case BATHROOM:
                breakRequestInProgress = true;
                System.out.println("[" + threadID + "] Requesting bathroom break through server...");
                bathroomConnection.requestBreak();
                shiftsSinceBreak = 0;
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
        if (shiftsSinceBreak > 5) {
            return random.nextInt(100) < (shiftsSinceBreak * 2);
        }
        return false;
    }
}