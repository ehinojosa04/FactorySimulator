package factory.agents;

import java.util.LinkedList;

import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.AgentType;
import core.agents.BaseAgent;
import factory.production.ProductOrder;
import factory.warehouse.Warehouse;

public class WorkerAgent extends BaseAgent{
    LinkedList<ProductOrder> productOrders;
    ProductOrder currentProductOrder;
    Warehouse warehouse;
    InventoryAgent inventoryAgent;

    int orderProgress, sourceIndex, targetIndex, materialsNeeded, shiftsNeeded;
    boolean hasMaterial;

    public WorkerAgent(String threadID, AgentLocation location, Warehouse warehouse, LinkedList<ProductOrder> productOrders, InventoryAgent inventoryAgent) {
        super(AgentType.WORKER, threadID, location);
        this.productOrders = productOrders;
        this.warehouse = warehouse;
        this.orderProgress = -1;
        this.hasMaterial = false;
        this.inventoryAgent = inventoryAgent;
    }

    @Override
    protected void processNextState() {
        switch (state) {
            case ON_BREAK:
                if (random.nextInt(100) < 20 + 20*breaksSinceShift){
                //if (true){

                    state = AgentState.WORKING;
                    breaksSinceShift = 0;
                    return;
                }
                breaksSinceShift++;
                break;
        
            case WORKING:
            case IDLE:

            state = orderProgress > 0 ? AgentState.WORKING : AgentState.IDLE;
            
            if (shiftsSinceBreak > 1 && random.nextInt(100) < 3 + 3*shiftsSinceBreak){
                //if (false){
                    state = AgentState.ON_BREAK;
                    shiftsSinceBreak = 0;
                    return;
                }
                shiftsSinceBreak++;
                break;
            default:
                System.out.println("Unimplemented state " + state.toString());
                break;
        }
    }

    @Override
    protected void performLocationBehavior() {
        switch (location) {
            case FACTORY:
                if (state == AgentState.WORKING || state == AgentState.IDLE) {
                    if (currentProductOrder == null) {
                        currentProductOrder = productOrders.poll();
                        System.out.println(currentProductOrder == null ?"No orders available." : "Fetched new product order: id " + currentProductOrder.product_id+", n "+currentProductOrder.quantity);
                    }
                    
                    if (currentProductOrder != null) {
                        if (orderProgress == -1) {
                            sourceIndex = currentProductOrder.getSourceMaterialIndex();
                            targetIndex = currentProductOrder.getTargetProductIndex();

                            materialsNeeded = currentProductOrder.getRequiredMaterials(targetIndex);
                            orderProgress++;
                            System.out.println("Starting new order: source=" + sourceIndex + ", target=" + targetIndex + ", materials=" + materialsNeeded);
                            inventoryAgent.requestMaterials(materialsNeeded);
                        }

                        if (!hasMaterial) {
                            hasMaterial = warehouse.getMaterial(sourceIndex);
                        }

                        if (!hasMaterial) {
                            System.out.println("Waiting materials");
                        }

                        if (hasMaterial && orderProgress < currentProductOrder.quantity) {
                            orderProgress++;
                            System.out.println("Processing order... progress=" + orderProgress + "/" + currentProductOrder.quantity);
                            sleepTime = 1000 * targetIndex;
                            System.out.println("Processed");
                            hasMaterial = false;
                            break;
                        }

                        if (orderProgress == currentProductOrder.quantity) {
                            orderProgress = -1;
                            warehouse.AddMaterials(targetIndex, currentProductOrder.quantity);
                            currentProductOrder = null;
                            System.out.println("Completed order. Added materials to target index " + targetIndex);
                            break;
                        }
                    } else {
                        sleepTime = 1000;
                    }
                } 
                else if (state == AgentState.ON_BREAK) {
                    location = random.nextInt(100) > 80 ? AgentLocation.BATHROOM : AgentLocation.BREAKROOM;
                    System.out.println("Going on break to " + location);
                    sleepTime = 500;
                }
                break;

            case BATHROOM:
            case BREAKROOM:
                if (state == AgentState.ON_BREAK) {
                    System.out.println("On break at " + location);
                    sleepTime = 2500;
                } else if (state == AgentState.WORKING || state == AgentState.IDLE) {
                    location = AgentLocation.FACTORY;
                    System.out.println("Returning to FACTORY from " + location);
                    sleepTime = 500;
                }
                break;

            default:
                System.out.println("Undefined location behavior");
                sleepTime = 200;
                break;
        }
    }

}
