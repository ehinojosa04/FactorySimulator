package factory.agents;

import java.util.LinkedList;

import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.production.ProductOrder;
import factory.warehouse.Warehouse;

public class WorkerAgent extends BaseAgent {
    LinkedList<ProductOrder> productOrders;
    ProductOrder currentProductOrder;
    Warehouse warehouse;
    InventoryAgent inventoryAgent;

    private final BathroomConnection bathroomConnection;
    private volatile boolean bathroomRequestInProgress = false;

    int orderProgress, sourceIndex, targetIndex, materialsNeeded, shiftsNeeded;
    boolean hasMaterial;

    public WorkerAgent(
            String threadID,
            AgentLocation location,
            Warehouse warehouse,
            LinkedList<ProductOrder> productOrders,
            InventoryAgent inventoryAgent) {
        super(AgentType.WORKER, threadID, location);
        this.productOrders = productOrders;
        this.warehouse = warehouse;
        this.orderProgress = -1;
        this.hasMaterial = false;
        this.inventoryAgent = inventoryAgent;

        this.bathroomConnection = new BathroomConnection("localhost", 5000, this);
    }

    public void updateStateFromServer(AgentState newState) {
        this.state = newState;
        // debug:
        System.out.println("[" + threadID + "] State from bathroom server: " + newState);
    }

    public void updateLocationFromServer(AgentLocation newLocation) {
        this.location = newLocation;
        System.out.println("[" + threadID + "] Location from bathroom server: " + newLocation);

        // When we are back at the factory, we can close the bathroom connection.
        if (this.location == AgentLocation.FACTORY) {
            // The connection is lazy, so this will be a no-op if it was never opened.
            bathroomConnection.close();
        }
    }

    public void handleBathroomEventFromServer(String eventType) {
        System.out.println("[" + threadID + "] Bathroom event: " + eventType);
        if ("EXITED_BUFFER".equals(eventType)) {
            // bathroom server has finished the whole pipeline for this request
            bathroomRequestInProgress = false;
            System.out.println("[" + threadID + "] Finished bathroom break, back to work.");
        }
    }

    @Override
    protected void processNextState() {
        switch (state) {
            case WAITING:
            case MOVING:
            case ON_BREAK:
                // While on break, state is controlled by bathroom server.
                // Do not modify it here.
                break;
            case WORKING:
            case IDLE:
                state = orderProgress > 0 ? AgentState.WORKING : AgentState.IDLE;

                if (shiftsSinceBreak > 1 && random.nextInt(100) < 3 + 3 * shiftsSinceBreak) {
                    state = AgentState.ON_BREAK;
                    shiftsSinceBreak = 0;
                    return;
                }
                shiftsSinceBreak++;
                break;
            default:
                System.out.println("Unimplemented state " + state.toString());
                break;
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (location) {
            case FACTORY:
                if (state == AgentState.WORKING || state == AgentState.IDLE) {
                    if (currentProductOrder == null) {
                        currentProductOrder = productOrders.poll();
                        System.out.println(currentProductOrder == null ? "No orders available."
                                : "Fetched new product order: id " + currentProductOrder.product_id + ", n "
                                        + currentProductOrder.quantity);
                    }

                    if (currentProductOrder != null) {
                        if (orderProgress == -1) {
                            sourceIndex = currentProductOrder.getSourceMaterialIndex();
                            targetIndex = currentProductOrder.getTargetProductIndex();

                            materialsNeeded = currentProductOrder.getRequiredMaterials(targetIndex);
                            orderProgress++;
                            System.out.println("Starting new order: source=" + sourceIndex + ", target=" + targetIndex
                                    + ", materials=" + materialsNeeded);
                            inventoryAgent.requestMaterials(materialsNeeded);
                        }

                        if (!hasMaterial) {
                            hasMaterial = warehouse.getMaterial(sourceIndex);
                        }

                        if (!hasMaterial) {
                            System.out.println("Waiting materials");
                        }

                        if (hasMaterial && orderProgress < currentProductOrder.quantity) {
                            orderProgress++;
                            System.out.println("Processing order... progress=" + orderProgress + "/"
                                    + currentProductOrder.quantity);
                            sleepTime = 1000 * targetIndex;
                            System.out.println("Processed");
                            hasMaterial = false;
                            break;
                        }

                        if (orderProgress == currentProductOrder.quantity) {
                            orderProgress = -1;
                            warehouse.AddMaterials(targetIndex, currentProductOrder.quantity);
                            currentProductOrder = null;
                            System.out.println("Completed order. Added materials to target index " + targetIndex);
                            break;
                        }
                    } else {
                        sleepTime = 1000;
                    }
                } else if (state == AgentState.ON_BREAK) {
                    if (!bathroomRequestInProgress) {
                        bathroomRequestInProgress = true;
                        System.out.println("[" + threadID + "] Requesting bathroom break through server...");
                        bathroomConnection.requestBathroomBreak(); // connection opens here if needed
                    }
                    sleepTime = 200;
                }
                break;

            case BATHROOM:
            case BREAKROOM:
                if (state == AgentState.ON_BREAK) {
                    System.out.println("[" + threadID + "] On break at " + location);
                    sleepTime = 250;
                } else if (state == AgentState.WORKING || state == AgentState.IDLE) {
                    sleepTime = 200;
                }
                break;

            default:
                System.out.println("Undefined location behavior");
                sleepTime = 200;
                break;
        }
    }
}
