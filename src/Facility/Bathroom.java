package Facility;

import core.agents.AgentLocation;
import core.agents.AgentState;

public class Bathroom extends Facility {
    public Bathroom() {
        super(5);
    }

    @Override
    protected void onEnter(String agentId, ClientChannel channel) throws InterruptedException {
        channel.sendState(agentId, AgentState.ON_BREAK);
        System.out.println("[" + agentId + "] Moving to the bathroom");
        Thread.sleep(1000);
    }

    @Override
    protected void onUse(String agentId, ClientChannel channel) throws InterruptedException {
        System.out.println("[" + agentId + "] Taking a break...");
        Thread.sleep(5000);
        System.out.println("[" + agentId + "] Finished taking a break");
    }

    @Override
    protected void onExit(String agentId, ClientChannel channel) throws InterruptedException {
        AgentLocation returnLocation = null;

        switch (agentId.split("-")[0]) {
            case "WORKER", "MANAGER" -> returnLocation = AgentLocation.FACTORY;
            case "INVENTORY" -> returnLocation = AgentLocation.WAREHOUSE;
            case "DELIVERY" -> returnLocation = AgentLocation.LOADING_DECK;
            default -> returnLocation = AgentLocation.FACTORY;
        }

        // channel.sendState(agentId, AgentState.MOVING);
        System.out.println("[" + agentId + "] Moving towards exit...");
        Thread.sleep(1000);
        System.out.println("[" + agentId + "] Moving towards " + returnLocation);
        channel.sendState(agentId, AgentState.IDLE);
        channel.sendLocation(agentId, returnLocation);
        
        channel.sendEvent(agentId, "BREAK_COMPLETE");
    }
}