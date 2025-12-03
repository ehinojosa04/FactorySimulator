package factory.agents;

import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.warehouse.Warehouse;

public class DeliveryAgent extends BaseAgent {

    private final Warehouse warehouse;

    private int maxCapacity;
    private int currentOrderTotal;
    private int cargo;
    private AgentLocation targetLocation;

    public DeliveryAgent(String threadID, AgentLocation location, Warehouse warehouse, int maxCapacity) {
        super(AgentType.DELIVERY, threadID, location);
        this.warehouse = warehouse;
        this.cargo = 0;
        this.currentOrderTotal = 0;
        this.targetLocation = location;
        this.state = AgentState.WAITING;
        this.maxCapacity = maxCapacity;
    }

    public synchronized void setOrder(int order) {
        if (order > 0) {
            this.currentOrderTotal = order;
            System.out.println(threadID + ": Received order for " + order + " units.");
        }
    }

    public synchronized int getCurrentOrder() {
        return currentOrderTotal;
    }

    @Override
    protected void processNextState() {
        switch (state) {
            case WAITING:
                // Trigger: If we are parked and get an order, go.
                if (location == AgentLocation.LOADING_DECK && currentOrderTotal > 0) {
                    System.out.println(threadID + ": Heading to Supplier.");
                    startMovingTo(AgentLocation.SUPPLIER);
                }
                break;

            case MOVING:
                if (location == targetLocation) {
                    arriveAtDestination();
                }
                break;

            case WORKING:
                // Transitions handled in performLocationBehavior
                break;

            default:
                break;
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (state) {
            case WAITING:
                stateDescriptor = "Parked at Loading Deck (Ready)";
                sleepTime = 500;
                break;

            case MOVING:
                stateDescriptor = "Driving to " + targetLocation + " Cargo: " + cargo;
                sleepTime = 5000;
                location = targetLocation;
                break;

            case WORKING:
                if (location == AgentLocation.SUPPLIER) {
                    if (cargo < maxCapacity && currentOrderTotal > 0) {

                        cargo++;
                        currentOrderTotal--;

                        stateDescriptor = "Loading from Supplier (" + cargo + "/" + maxCapacity + ")";
                        System.out.println(threadID + ": Loading item... (Cargo: " + cargo + ")");

                        sleepTime = 500;

                    } else {
                        // --- LOADING COMPLETE ---
                        stateDescriptor = "Loading complete. Securing cargo.";
                        System.out.println(threadID + ": Truck full or Order filled. Heading to Warehouse.");
                        startMovingTo(AgentLocation.WAREHOUSE);
                    }

                } else if (location == AgentLocation.WAREHOUSE) {
                    if (cargo > 0) {
                        stateDescriptor = "Unloading cargo (" + cargo + " items remaining)";

                        cargo--;
                        warehouse.AddMaterials(0, 1);

                        System.out.println(threadID + ": Unloading... (" + cargo + " left on truck)");
                        sleepTime = 500; // Time to carry box to shelf

                    } else {
                        // --- UNLOADING COMPLETE ---
                        stateDescriptor = "Unloading complete. Checking manifest...";
                        System.out.println(threadID + ": Unloading complete.");

                        if (currentOrderTotal > 0) {
                            System.out.println(threadID + ": Order incomplete. Returning to Supplier.");
                            startMovingTo(AgentLocation.SUPPLIER);
                        } else {
                            System.out.println(threadID + ": Job done. Parking at Loading Deck.");
                            startMovingTo(AgentLocation.LOADING_DECK);
                        }
                    }
                }
                // SAFETY CATCH
                else {
                    state = AgentState.IDLE;
                }
                break;

            default:
                stateDescriptor = "Idle";
                sleepTime = 500;
                break;
        }
    }

    // --- Helpers ---

    private void startMovingTo(AgentLocation destination) {
        this.targetLocation = destination;
        this.state = AgentState.MOVING;
        stateDescriptor = "Starting engine: Destination " + destination;
    }

    private void arriveAtDestination() {
        // BUG FIX: Always park (WAITING) when arriving at Loading Deck.
        // This ensures the processNextState logic triggers cleanly to restart the loop if needed.
        if (location == AgentLocation.LOADING_DECK) {
            state = AgentState.WAITING;
        } else {
            state = AgentState.WORKING;
        }
    }
}