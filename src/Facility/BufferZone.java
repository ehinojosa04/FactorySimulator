package Facility;

import java.util.concurrent.Semaphore;

import factory.agents.WorkerAgent;

public abstract class BufferZone {
    protected final int capacity;
    protected final Semaphore semaphore;

    public BufferZone(int capacity) {
        this.capacity = capacity;
        this.semaphore = new Semaphore(capacity, true); // fair = FIFO-ish
    }

    public void requestAccess(WorkerAgent agent) throws InterruptedException {
        onQueued(agent);

        semaphore.acquire();
        try {
            onEnter(agent); // moving into the buffer
            onUse(agent); // time spent inside (bathroom/breakroom/etc.)
        } finally {
            onExit(agent); // leaving the buffer
            semaphore.release();
        }
    }

    // Hooks for subclasses to customize behavior / states / animations:

    /** Called before acquiring the semaphore (agent is queued). */
    protected void onQueued(WorkerAgent agent) throws InterruptedException {
    }

    /** Called after acquiring the semaphore, before use (agent entering). */
    protected abstract void onEnter(WorkerAgent agent) throws InterruptedException;

    /** Called while holding the semaphore: simulate "using" the resource. */
    protected abstract void onUse(WorkerAgent agent) throws InterruptedException;

    /** Called when done, just before releasing the semaphore. */
    protected abstract void onExit(WorkerAgent agent) throws InterruptedException;
}
