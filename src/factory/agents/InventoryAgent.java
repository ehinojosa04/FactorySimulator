package factory.agents;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.warehouse.Warehouse;

public class InventoryAgent extends BaseAgent {

    private Warehouse warehouse;
    private List<DeliveryAgent> deliveryAgents;
    private final ReentrantLock lock;
    private int materialsPendingOfOrder, truckMaxCapacity;

    public InventoryAgent(String threadID, AgentLocation location, Warehouse warehouse, int truckMaxCapacity, List<DeliveryAgent> deliveryAgents) {
        super(AgentType.INVENTORY, threadID, location);
        this.warehouse = warehouse;
        this.deliveryAgents = deliveryAgents;
        this.lock = new ReentrantLock();
        this.materialsPendingOfOrder = 0;
        this.truckMaxCapacity = truckMaxCapacity;
    }

    @Override
    protected void processNextState() {
        // Logic remains similar, but we check if ANY agent is available
        boolean anyAgentAvailable = false;
        for(DeliveryAgent da : deliveryAgents) {
            if(da.getLocation() == AgentLocation.LOADING_DECK && da.getCurrentOrder() == 0) {
                anyAgentAvailable = true;
                break;
            }
        }

        if (materialsPendingOfOrder > 0) {
            state = anyAgentAvailable ? AgentState.WORKING : AgentState.WAITING;
        } else {
            // Flavor logic
            if (state == AgentState.IDLE && random.nextInt(100) < 10) {
                state = AgentState.WORKING;
            } else {
                state = AgentState.IDLE;
            }
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (state) {
            case WORKING:
                // Scenario A: Real Work (Distributing Orders)
                if (materialsPendingOfOrder > 0) {

                    // Iterate through ALL agents to distribute the load
                    for (DeliveryAgent agent : deliveryAgents) {

                        // Stop if we ran out of work
                        if (materialsPendingOfOrder <= 0) break;

                        // Check if THIS specific agent is available
                        if (agent.getLocation() == AgentLocation.LOADING_DECK && agent.getCurrentOrder() == 0) {

                            // LOGIC: How much can this truck take?

                            int amountToAssign = Math.min(materialsPendingOfOrder, truckMaxCapacity);

                            // Dispatch
                            agent.setOrder(amountToAssign);
                            resetMaterials(amountToAssign); // Decrement our local counter

                            stateDescriptor = "Assigned " + amountToAssign + " items to " + agent.getThreadID();
                            System.out.println(threadID + ": Assigned " + amountToAssign + " to " + agent.getThreadID() + ". Remaining: " + materialsPendingOfOrder);
                        }
                    }

                    sleepTime = 1000;
                }
                // Scenario B: Busy Work
                else {
                    stateDescriptor = "Processing administrative paperwork";
                    sleepTime = 2000;
                }
                break;

            case WAITING:
                stateDescriptor = "BOTTLENECK: Need " + materialsPendingOfOrder + " units (No Agents!)";
                sleepTime = 200;
                break;

            case IDLE:
                stateDescriptor = "Monitoring inventory levels";
                sleepTime = 500;
                break;

            default:
                stateDescriptor = "Unknown State";
                sleepTime = 500;
                break;
        }
    }

    // --- Standard Methods ---

    public void requestMaterials(int nMaterials){
        try {
            lock.lock();
            materialsPendingOfOrder += nMaterials;
            System.out.println(threadID + ": Request received: " + nMaterials + ". Total Pending: " + materialsPendingOfOrder);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void resetMaterials(int nMaterials){
        try {
            lock.lock();
            materialsPendingOfOrder -= nMaterials;
            if (materialsPendingOfOrder < 0) materialsPendingOfOrder = 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}