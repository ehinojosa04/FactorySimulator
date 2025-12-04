package factory.agents;

import core.agents.AgentState;
import factory.Factory;
import factory.production.ProductOrder;

import core.agents.AgentLocation;
import core.agents.AgentType;
import core.agents.BaseAgent;

public class ManagerAgent extends BaseAgent {
    Factory factory;

    public ManagerAgent(Factory factory) {
        super(AgentType.MANAGER, "MANAGER", AgentLocation.FACTORY);
        this.factory = factory;
    }

    @Override
    protected void processNextState() {
        if (factory.productOrders.peek() == null){
            state = AgentState.WORKING;
        } else {
            state = random.nextInt(100) > 30 ? AgentState.IDLE : AgentState.ON_BREAK;
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (state) {
            case IDLE:
                stateDescriptor = "Supervising factory";
                sleepTime = 2000;
                shiftsSinceBreak++;
                break;

            case WORKING:
                stateDescriptor = "Generating orders " + factory.orderBatchSize + " items";
                shiftsSinceBreak += 2;

                try {
                    Thread.sleep(200L * factory.orderBatchSize);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int i = 1; i < factory.orderBatchSize; i++){
                    factory.productOrders.add(new ProductOrder(random.nextInt(factory.productsOffered)+1, random.nextInt(9)+1));
                }

                break;

            case ON_BREAK:
                stateDescriptor = "Taking a break on his phone";
                sleepTime = 5000;
                break;
        }
    }


    private void hireWorkers() {
        System.out.println("Hiring");
        stateDescriptor = "Hiring inventory agent";

        factory.inventoryAgent = new InventoryAgent("INVENTORY", AgentLocation.WAREHOUSE, factory.warehouse, factory.truckMaxCapacity, factory.deliveryAgents, factory.requestTime);

        for (int i = 0; i < factory.nFactoryWorkers; i++) {
            factory.workerAgents.add(new WorkerAgent("WORKER-"+i, AgentLocation.FACTORY, factory.warehouse, factory.productOrders, factory.productionTime, factory.inventoryAgent, factory.requestTime, factory.zones));
        }

        System.out.println("Hired " + factory.workerAgents.size() + " factory workers");

        for (int i = 0; i < factory.nDeliveryWorkers; i++) {
            factory.deliveryAgents.add(new DeliveryAgent("DELIVERY-"+i, AgentLocation.LOADING_DECK, factory.warehouse, factory.truckMaxCapacity));
        }
        System.out.println("Hiring done");
    }

    private void startShift() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        factory.inventoryAgent.start();

        for (int i = 0; i < factory.workerAgents.size(); i++) {
            stateDescriptor = "Hiring " + i + "/" +factory.nFactoryWorkers + " workers agents";
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            factory.workerAgents.get(i).start();
        }

        for (int i = 0; i < factory.deliveryAgents.size(); i++) {
            stateDescriptor = "Hiring " + i + "/" +factory.nDeliveryWorkers + " delivery agents";
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            factory.deliveryAgents.get(i).start();
        }
    }

    @Override
    public void run() {
        state = AgentState.WORKING;
        hireWorkers();
        startShift();
        System.out.println("All workers started");

        state = AgentState.IDLE;

        super.run();

        System.out.println("Manager is done");
    }
}