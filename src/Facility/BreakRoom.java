package Facility;

import core.agents.AgentState;

public class BreakRoom extends Facility {
    public BreakRoom() {
        super(1);
    }

    @Override
    protected void onEnter(String agentId, ClientChannel channel) throws InterruptedException {
        channel.sendState(agentId, AgentState.MOVING);
        Thread.sleep(1000);
    }

    @Override
    protected void onUse(String agentId, ClientChannel channel) throws InterruptedException {
        channel.sendState(agentId, AgentState.ON_BREAK);
        Thread.sleep(5000);
    }

    @Override
    protected void onExit(String agentId, ClientChannel channel) throws InterruptedException {
        channel.sendState(agentId, AgentState.MOVING);
        Thread.sleep(1000);
        channel.sendEvent(agentId, "FINISHED");
    }
}
