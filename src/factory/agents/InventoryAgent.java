package factory.agents;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import core.agents.AgentLocation;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.warehouse.Warehouse;

public class InventoryAgent extends BaseAgent{

    Warehouse warehouse;
    List<DeliveryAgent> deliveryAgents;
    private final ReentrantLock lock;
    private int materialsPendingOfOrder;


    public InventoryAgent(String threadID, AgentLocation location, Warehouse warehouse, List<DeliveryAgent> deliveryAgents) {
        super(AgentType.INVENTORY, threadID, location);
        this.warehouse = warehouse;
        this.deliveryAgents = deliveryAgents;
        this.lock = new ReentrantLock();
        this.materialsPendingOfOrder = 0;
    }

    @Override
    protected void performLocationBehavior() {

        System.out.println(warehouse.inventory.toString());
        sleepTime = 1000;
        
        switch (location) {
            case WAREHOUSE:
            if (materialsPendingOfOrder > 0){
                System.out.println("Requesting materials: "+materialsPendingOfOrder);
                DeliveryAgent agent = findAvailableDeliveryAgent();
                
                if (agent != null){
                    agent.setOrder(materialsPendingOfOrder);
                    resetMaterials(materialsPendingOfOrder);
                }
                sleepTime = 1000;
            }
            break;
            
            default:
                break;
        }
    }

    public void requestMaterials(int nMaterials){
        try {
            lock.lock();
            materialsPendingOfOrder += nMaterials;
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
    }

    public void resetMaterials(int nMaterials){
        try {
            lock.lock();
            materialsPendingOfOrder -= nMaterials;
        } catch (Exception e) {

        } finally {
            lock.unlock();
        }
    }

    private DeliveryAgent findAvailableDeliveryAgent() {
        for (DeliveryAgent agent : deliveryAgents) {
            if (agent.getLocation() == AgentLocation.LOADING_DECK && 
                agent.getCurrentOrder() == 0) {
                return agent;
            }
        }
        return null;
    }

    @Override
    protected void processNextState() {
        return;
    }
}
