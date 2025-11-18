package factory.agents;

import java.util.concurrent.locks.ReentrantLock;

import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.warehouse.Warehouse;


public class DeliveryAgent extends BaseAgent {
    Warehouse warehouse;
    ReentrantLock lock;

    int cargo;
    int currentOrder;
    int MAX_CAPACITY = 10;

    public DeliveryAgent(String threadID, AgentLocation location, Warehouse warehouse) {
        super(AgentType.DELIVERY, threadID, location);
        this.warehouse = warehouse;
        this.cargo = 0;
        this.currentOrder = 0;
        this.lock = new ReentrantLock();
    }

    @Override
    protected void performLocationBehavior() {
        switch (location) {
            case LOADING_DECK:
                try {
                    waitForOrders();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }

                System.out.println("Going to supplier with order of "+currentOrder);
                location = AgentLocation.SUPPLIER;
                state = AgentState.WORKING;
                sleepTime = 10000;
                break;

            case SUPPLIER:
                cargo = Math.min(MAX_CAPACITY, currentOrder);
                currentOrder -= cargo;

                System.out.println("Going to deliver");
                state = AgentState.WORKING;
                location = AgentLocation.WAREHOUSE;
                sleepTime = 10000;
                break;

            case WAREHOUSE:
                warehouse.inventory.set(0, warehouse.inventory.get(0) + cargo);
                System.out.println("Delivered " + cargo + " items to warehouse");
                cargo = 0;
                
                if (currentOrder > 0) {
                    location = AgentLocation.SUPPLIER;
                } else {
                    location = AgentLocation.LOADING_DECK;
                }
                sleepTime = 100;
                break;

            default:
                break;
        }
    }

    @Override
    protected void processNextState() {
        state = cargo > 0 ? AgentState.WORKING : AgentState.IDLE;
        return;
    }

    public synchronized int getCurrentOrder() {
        return currentOrder;
    }


    public synchronized void waitForOrders() throws InterruptedException {
        while (currentOrder == 0) {
            wait();
        }
    }

    public synchronized void setOrder(int order) {
        if (order <= 0) {
            throw new IllegalArgumentException("Order must be positive");
        }
        currentOrder = order;
        notifyAll();
    }
}