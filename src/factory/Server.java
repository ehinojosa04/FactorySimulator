package factory;

import factory.production.ProductOrder;

public class Server {
    public static void main(String[] args) {
        Factory factory = new Factory(10, 1, 1);

        // while (true) {
        //     for (ProductOrder po : factory.productOrders) {
        //         System.out.println("Product: "+po.product_id +" Quantity: "+po.quantity);
        //     }
        //     System.out.println("\n\n");
        //     try {
        //         Thread.sleep(1000);
        //     } catch (InterruptedException e) {
        //         e.printStackTrace();
        //     }
        // }
    }
}
