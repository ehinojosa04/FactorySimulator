package Facility;

import java.io.IOException;

public abstract class FacilityServer {
    protected final Facility facility;
    protected final FacilityType facilityType;
    protected final int port;

    public FacilityServer(FacilityType facilityType, int port) {
        this.port = port;
        this.facilityType = facilityType;
        
        if (facilityType == FacilityType.BATHROOM) {
            facility = new Bathroom();
        } else {
            facility = new BreakRoom();
        }
    }

    public abstract void start() throws IOException;
}
