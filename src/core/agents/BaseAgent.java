package core.agents;

import factory.agents.BathroomConnection;
import factory.agents.BreakroomConnection;

import java.util.Random;


public abstract class BaseAgent extends Thread{
    protected final AgentType type;
    protected final String threadID;
    protected static final Random random = new Random();

    protected AgentLocation location;
    protected AgentState state;

    protected int shiftsSinceBreak;
    protected int breaksSinceShift;
    protected int sleepTime;

    protected String stateDescriptor;

    protected final BathroomConnection bathroomConnection;
    protected final BreakroomConnection breakroomConnection;
    protected volatile boolean breakRequestInProgress = false;
    protected volatile boolean hasRequestedBreak = false;

    protected AgentLocation preBreakLocation;

    public BaseAgent(AgentType type, String threadID, AgentLocation location) {
        this.type = type;
        this.threadID = threadID;
        this.location = location;
        this.state = AgentState.IDLE;
        this.shiftsSinceBreak = 0;
        this.breaksSinceShift = 0;
        this.sleepTime = 0;
        this.stateDescriptor = "";

        this.bathroomConnection = new BathroomConnection("localhost", 5002, this);
        this.breakroomConnection = new BreakroomConnection("localhost", 5001, this);

        System.out.println("Agent "+threadID+" ("+type+"): "+" has been started");
    }

    protected abstract void performLocationBehavior();

    @Override
    public void run() {
        while(!Thread.interrupted()){
            performLocationBehavior();
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            processNextState();
        }
    }

    protected abstract void processNextState();

    
    public AgentLocation getLocation(){
        return location;
    }

    public void setAgentLocation(AgentLocation location) {
        this.location = location;
    }
    
    public AgentState getAgentState(){
        return state;
    }

    public void setAgentState(AgentState state) {
        this.state = state;
    }

    public String getThreadID(){
        return threadID;
    }

    public AgentType getAgentType(){
        return type;
    }

    public String getStateDescriptor(){
        return stateDescriptor;
    }

    public synchronized void updateStateFromServer(AgentState newState) {
        this.state = newState;
        // System.out.println("[" + threadID + "] State from server: " + newState);
    }

    public void handleServerEvent(String eventType) {
        System.out.println("[" + threadID + "] Facility event: " + eventType);
    }

    public synchronized void updateLocationFromServer(AgentLocation newLocation) {
        System.out.println("[" + threadID + "] Location from server: " + newLocation);

        AgentLocation oldLocation = this.location;
        this.location = newLocation;

        if (newLocation == AgentLocation.FACTORY || newLocation == AgentLocation.WAREHOUSE) {
            if (oldLocation == AgentLocation.BATHROOM) {
                this.bathroomConnection.close();
            } else if (oldLocation == AgentLocation.BREAKROOM) {
                this.breakroomConnection.close();
            }

            state = AgentState.MOVING;
            breakRequestInProgress = false;
            hasRequestedBreak = false;
            shiftsSinceBreak = 0;
        }
    }
}
