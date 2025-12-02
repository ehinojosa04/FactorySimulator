package factory.agents;

public class BathroomConnection extends FacilityConnection {
    public BathroomConnection(String host, int port, WorkerAgent agent) {
        super(host, port, agent);
    }

    public void requestBathroomBreak() {
        sendLine("REQUEST_BATHROOM");
    }

    @Override
    protected void handleEventFromServer(String eventType, String[] parts) {
        agent.handleBathroomEventFromServer(eventType);
    }
}
