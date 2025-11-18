package core.agents;

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
    
    public BaseAgent(AgentType type, String threadID, AgentLocation location) {
        this.type = type;
        this.threadID = threadID;
        this.location = location;
        this.state = AgentState.IDLE;
        this.shiftsSinceBreak = 0;
        this.breaksSinceShift = 0;
        this.sleepTime = 0;

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

    public AgentState getAgentState(){
        return state;
    }

    public String getThreadID(){
        return threadID;
    }

    public AgentType getAgentType(){
        return type;
    }
}
