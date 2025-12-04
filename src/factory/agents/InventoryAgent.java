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
    private int requestTime;

    public InventoryAgent(String threadID, AgentLocation location, Warehouse warehouse, int truckMaxCapacity, List<DeliveryAgent> deliveryAgents, int requestTime) {
        super(AgentType.INVENTORY, threadID, location);
        this.warehouse = warehouse;
        this.deliveryAgents = deliveryAgents;
        this.lock = new ReentrantLock();
        this.materialsPendingOfOrder = 0;
        this.truckMaxCapacity = truckMaxCapacity;
        this.requestTime = requestTime;
    }

    @Override
    protected void processNextState() {
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
                if (materialsPendingOfOrder > 0) {
                    for (DeliveryAgent agent : deliveryAgents) {
                        if (materialsPendingOfOrder <= 0) break;
                        if (agent.getLocation() == AgentLocation.LOADING_DECK && agent.getCurrentOrder() == 0) {
                            int amountToAssign = Math.min(materialsPendingOfOrder, truckMaxCapacity);
                            stateDescriptor = "Assigning " + amountToAssign + " items to " + agent.getThreadID();

                            try {
                                Thread.sleep(requestTime);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            agent.setOrder(amountToAssign);
                            resetMaterials(amountToAssign);

                            System.out.println(threadID + ": Assigned " + amountToAssign + " to " + agent.getThreadID() + ". Remaining: " + materialsPendingOfOrder);
                        }
                    }

                    sleepTime = 1000;
                } else {
                    stateDescriptor = "Processing administrative paperwork";
                    sleepTime = 2000;
                }
                break;

            case WAITING:
                stateDescriptor = "Need to order " + materialsPendingOfOrder + " units, no drivers";
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