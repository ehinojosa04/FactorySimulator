package factory;

import java.util.ArrayList;

import core.Zones.ZonesAPI;
import core.agents.BaseAgent;
import core.ui.AgentStatesWindow;
import core.ui.FactoryVisualizationWindow;
import core.ui.InventoryWindow;
import core.ui.ThreadStatesWindow;
import core.ui.ZonesWindow;

public class FactoryServer {
    public FactoryServer(int workstations, int orderBatchSize, int productsOffered, int timeToProduce, int workers, int truckMaxCapacity, int transportTime, int delivery, int requestTime) {
        ZonesAPI zones = new ZonesAPI();
        Factory factory = new Factory(workstations, orderBatchSize, productsOffered, timeToProduce, workers,truckMaxCapacity, transportTime, delivery, requestTime, zones);
        new Thread(new InventoryWindow(factory.warehouse)).start();

        ArrayList<BaseAgent> agents = new ArrayList<>();
        agents.add(factory.manager);
        agents.add(factory.inventoryAgent);
        agents.addAll(factory.workerAgents);
        agents.addAll(factory.deliveryAgents);

        new Thread(new AgentStatesWindow(agents)).start();
        new Thread(new ThreadStatesWindow(agents)).start();
        new Thread(new ZonesWindow(agents)).start();
        new Thread(new FactoryVisualizationWindow(agents)).start();
    }
}
