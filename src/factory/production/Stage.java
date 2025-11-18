package factory.production;

import java.util.concurrent.locks.ReentrantLock;

public class Stage {
    float time_to_produce;
    ReentrantLock lock = new ReentrantLock();
    
    public Stage(float time_to_produce) {
        this.time_to_produce = time_to_produce;
    }


}
