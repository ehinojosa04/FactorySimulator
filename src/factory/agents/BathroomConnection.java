package factory.agents;

public class BathroomConnection extends FacilityConnection {
    public BathroomConnection(String host, int port, WorkerAgent agent) {
        super(host, port, agent);
    }

    public void requestBreak() {
        sendLine("REQUEST_BATHROOM");
    }

    @Override
    protected void handleEventFromServer(String eventType, String[] parts) {
        agent.handleServerEvent(eventType);
    }
}
