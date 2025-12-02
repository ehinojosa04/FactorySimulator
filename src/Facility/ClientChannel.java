package Facility;

import core.agents.AgentLocation;
import core.agents.AgentState;

public interface ClientChannel {
    void sendState(String agentId, AgentState state);

    void sendLocation(String agentId, AgentLocation location);

    void sendEvent(String agentId, String eventType);
}
