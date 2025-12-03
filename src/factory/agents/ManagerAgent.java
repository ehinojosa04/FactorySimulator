package factory.agents;

import factory.Factory;
import factory.production.ProductOrder;

import core.agents.AgentLocation;
import core.agents.AgentType;
import core.agents.BaseAgent;

public class ManagerAgent extends BaseAgent{
    Factory factory;

    public ManagerAgent(Factory factory) {
        super(AgentType.MANAGER, "Manager", AgentLocation.FACTORY);
        this.factory = factory;
    }

    public void planProduction(){
        
    }

    @Override
    protected void performLocationBehavior() {
        if (factory.productOrders.peek() == null){
            for (int i = 1; i < factory.orderBatchSize; i++){
                factory.productOrders.add(new ProductOrder(random.nextInt(factory.productsOffered)+1, random.nextInt(9)+1));
            }
        }
    }
    private void hireWorkers(){
        System.out.println("Hiring");
        factory.inventoryAgent = new InventoryAgent("InventoryAgent", AgentLocation.WAREHOUSE, factory.warehouse, factory.truckMaxCapacity, factory.deliveryAgents);

        for (int i = 0; i < factory.nFactoryWorkers; i++) {
            factory.workerAgents.add(new WorkerAgent("Worker-"+i, AgentLocation.FACTORY, factory.warehouse, factory.productOrders, factory.inventoryAgent, factory.zones));
        }

        System.out.println("Hired "+factory.workerAgents.size()+" factory workers");

        for (int i = 0; i < factory.nDeliveryWorkers; i++) {
            factory.deliveryAgents.add(new DeliveryAgent("Delivery-"+i, AgentLocation.LOADING_DECK, factory.warehouse, factory.truckMaxCapacity));
        }
        System.out.println("Hiring done");
    }

    private void startShift(){
        factory.inventoryAgent.start();

        for (int i = 0; i < factory.workerAgents.size(); i++) {
            factory.workerAgents.get(i).start();
        }

        for (int i = 0; i < factory.deliveryAgents.size(); i++) {
            factory.deliveryAgents.get(i).start();
        }
    }

    @Override
    protected void processNextState() {
        return;
    }

    @Override
    public void run() {
        hireWorkers();
        startShift();
        System.out.println("All workers started");

        super.run();
        System.out.println("Manager is done");
    }
}
