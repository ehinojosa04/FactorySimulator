package factory.production;


import core.Zones.BufferZone;

public class Workstations extends BufferZone {
    public Workstations(int capacity) {
        super(capacity, "Workstations");
    }
}
