package factory;

import java.util.ArrayList;
import java.util.LinkedList;

import factory.agents.*;
import factory.production.ProductOrder;
import factory.production.Stage;
import factory.warehouse.Warehouse;

public class Factory {
    public Warehouse warehouse;
    ManagerAgent manager;

    public ArrayList<Stage> stages;
    public ArrayList<WorkerAgent> workerAgents;
    public InventoryAgent inventoryAgent;
    public ArrayList<DeliveryAgent> deliveryAgents;
    public int nFactoryWorkers, nDeliveryWorkers;

    public LinkedList<ProductOrder> productOrders;
    int orderBatchSize;

    public Factory(int nStages, int nFactoryWorkers, int nDeliveryWorkers) {
        warehouse = new Warehouse();
        stages = new ArrayList<Stage>();
        productOrders = new LinkedList<ProductOrder>();

        this.nFactoryWorkers = nFactoryWorkers;
        workerAgents = new ArrayList<WorkerAgent>(nFactoryWorkers);

        this.nDeliveryWorkers = nDeliveryWorkers;
        deliveryAgents = new ArrayList<DeliveryAgent>(nDeliveryWorkers);
        
        for (int i = 0; i < nStages; i++) {
            stages.add(new Stage(500 + 500 * i));
            warehouse.inventory.add(0);
        }

        manager = new ManagerAgent(this);
        manager.start();
    }
}
