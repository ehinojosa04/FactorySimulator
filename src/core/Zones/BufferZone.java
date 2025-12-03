package core.Zones;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class BufferZone {
    private final Semaphore semaphore;
    private final String zoneName;
    private final int capacity;

    public BufferZone(int capacity, String zoneName) {
        this.capacity = capacity;
        this.zoneName = zoneName;
        // true = fair ordering (FIFO for waiting threads)
        this.semaphore = new Semaphore(capacity, true);
    }

    public void enter() {
        try {
            System.out.println(Thread.currentThread().getName() + " is waiting for " + zoneName + "...");
            semaphore.acquire();
            System.out.println(Thread.currentThread().getName() + " entered " + zoneName + ". (Occupancy: " + (capacity - semaphore.availablePermits()) + "/" + capacity + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for " + zoneName);
        }
    }

    public void leave() {
        semaphore.release();
        System.out.println(Thread.currentThread().getName() + " left " + zoneName + ". (Available spots: " + semaphore.availablePermits() + ")");
    }

    public int getAvailableSlots() {
        return semaphore.availablePermits();
    }
}