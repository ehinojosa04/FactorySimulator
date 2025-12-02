package Facility;

import java.util.concurrent.Semaphore;

import core.agents.AgentState;

public abstract class Facility {
    protected final int capacity;
    protected final Semaphore semaphore;

    public Facility(int capacity) {
        this.capacity = capacity;
        this.semaphore = new Semaphore(capacity, true);
    }

    public void handleAccessRequest(String agentId, ClientChannel channel) {
        new Thread(() -> {
            try {
                channel.sendState(agentId, AgentState.WAITING);

                semaphore.acquire();
                try {
                    onEnter(agentId, channel);
                    onUse(agentId, channel);
                } finally {
                    onExit(agentId, channel);
                    semaphore.release();
                }
            } catch (InterruptedException e) {
                channel.sendEvent(agentId, "INTERRUPTED");
                Thread.currentThread().interrupt();
            }
        }, "BathroomRequest-" + agentId).start();
    }

    protected abstract void onEnter(String agentId, ClientChannel channel) throws InterruptedException;

    protected abstract void onUse(String agentId, ClientChannel channel) throws InterruptedException;

    protected abstract void onExit(String agentId, ClientChannel channel) throws InterruptedException;
}
