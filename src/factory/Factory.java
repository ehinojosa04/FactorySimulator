package factory;

import java.util.ArrayList;
import java.util.LinkedList;

import core.Zones.ZonesAPI;
import factory.agents.*;
import factory.production.ProductOrder;
import factory.production.Workstations;
import factory.warehouse.Warehouse;

public class Factory {
    public Warehouse warehouse;
    ManagerAgent manager;

    public ArrayList<WorkerAgent> workerAgents;
    public ArrayList<DeliveryAgent> deliveryAgents;
    
    public InventoryAgent inventoryAgent;
    public int nFactoryWorkers, nDeliveryWorkers;

    public LinkedList<ProductOrder> productOrders;
    public ZonesAPI zones;
    public int orderBatchSize, productsOffered, truckMaxCapacity;
    public int transportTime, productionTime, requestTime, nWorkstation;

    public Factory(int nWorkstation, int orderBatchSize, int productsOffered, int productionTime, int nFactoryWorkers, int truckMaxCapacity, int transportTime, int nDeliveryWorkers, int requestTime, ZonesAPI zones) {
        this.zones = zones;
        this.nWorkstation = nWorkstation;
        warehouse = new Warehouse();
        zones.setWorkstations(new Workstations(nWorkstation));
        productOrders = new LinkedList<>();

        this.nFactoryWorkers = nFactoryWorkers;
        workerAgents = new ArrayList<>(nFactoryWorkers);

        this.nDeliveryWorkers = nDeliveryWorkers;
        deliveryAgents = new ArrayList<>(nDeliveryWorkers);

        this.orderBatchSize = orderBatchSize;
        this.productsOffered = productsOffered;
        this.truckMaxCapacity = truckMaxCapacity;

        this.transportTime = transportTime;
        this.productionTime = productionTime;
        this.requestTime = requestTime;

        for (int i = 0; i < productsOffered; i++) {
            warehouse.inventory.add(0);
        }

        manager = new ManagerAgent(this);
        manager.start();
    }
}
