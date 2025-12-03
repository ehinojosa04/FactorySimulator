package Facility;

import core.agents.AgentLocation;
import core.agents.AgentState;

public class Bathroom extends Facility {
    public Bathroom() {
        super(5);
    }

    @Override
    protected void onEnter(String agentId, ClientChannel channel) throws InterruptedException {
        channel.sendLocation(agentId, AgentLocation.BATHROOM);
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
        channel.sendState(agentId, AgentState.IDLE);
        channel.sendLocation(agentId, AgentLocation.FACTORY);
    }
}
