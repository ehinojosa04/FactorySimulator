package factory.agents;

public class BreakroomConnection extends FacilityConnection {
    public BreakroomConnection(String host, int port, WorkerAgent agent) {
        super(host, port, agent);
    }

    public void requestBreak() {
        sendLine("REQUEST_BREAKROOM");
    }

    @Override
    protected void handleEventFromServer(String eventType, String[] parts) {
        agent.handleBathroomEventFromServer(eventType);
    }
}