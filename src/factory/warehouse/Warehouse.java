package factory.warehouse;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Warehouse {
    public ArrayList<Integer> inventory;
    ReentrantLock lock;

    public Warehouse() {
        inventory = new ArrayList<Integer>();
        lock = new ReentrantLock();
    }

    public int getWarehouseSize() { return inventory.size(); }

    public boolean getMaterial(int index){
        boolean materialsAvailable = false;

        try {
            lock.lock();
            materialsAvailable = inventory.get(index) > 0;
            if(materialsAvailable) inventory.set(index, inventory.get(index) - 1);

        } catch (Exception e) {
            
        } finally{
            lock.unlock();
        }
        return materialsAvailable;
    }

    public void AddMaterials(int index, int quantity){
        try {
            lock.lock();
            inventory.set(index, inventory.get(index) + quantity);

        } catch (Exception e) {
            
        } finally{
            lock.unlock();
        }
    }
}
