package factory;

import java.util.ArrayList;

import core.Zones.ZonesAPI;
import core.agents.BaseAgent;
import core.ui.AgentStatesWindow;
import core.ui.InventoryWindow;
import core.ui.ThreadStatesWindow;
import core.ui.ZonesWindow;

public class FactoryServer {
    public FactoryServer(int workstations, int orderBatchSize, int productsOffered, int workers, int truckMaxCapacity, int delivery) {
        ZonesAPI zones = new ZonesAPI();
        Factory factory = new Factory(workstations, orderBatchSize, productsOffered, workers,truckMaxCapacity, delivery, zones);
        new Thread(new InventoryWindow(factory.warehouse)).start();

        ArrayList<BaseAgent> agents = new ArrayList<>();
        agents.add(factory.manager);
        agents.add(factory.inventoryAgent);
        agents.addAll(factory.workerAgents);
        agents.addAll(factory.deliveryAgents);

        new Thread(new AgentStatesWindow(agents)).start();
        new Thread(new ThreadStatesWindow(agents)).start();
        new Thread(new ZonesWindow(agents)).start();
    }
}
