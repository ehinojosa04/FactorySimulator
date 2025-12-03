package core.Zones;

import Facility.Facility;
import factory.production.Workstations;

public class ZonesAPI {
    Facility bathroom;
    Facility breakroom;
    Workstations workstations;

    public ZonesAPI() {
        this.workstations = null;
        this.bathroom = null;
        this.breakroom = null;
    }

    public void setBathroom(Facility bathroom) {
        this.bathroom = bathroom;
    }

    public void setBreakroom(Facility breakroom) {
        this.breakroom = breakroom;
    }

    // ADD THIS
    public void setWorkstations(Workstations workstations) {
        this.workstations = workstations;
    }

    public Workstations getWorkstations() {
        return workstations;
    }
}
