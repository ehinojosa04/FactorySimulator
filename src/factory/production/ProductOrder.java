package factory.production;

public class ProductOrder {
    public int product_id;
    public int quantity;
    public int conversionRatio = 1;
    
    public ProductOrder(int product_id, int quantity) {
        this.product_id = product_id;
        this.quantity = quantity;
    }

    public int getRequiredMaterials(int targetIndex) {
        return quantity;
    }
    
    public int getSourceMaterialIndex() {
        return 0;
    }
    
    public int getTargetProductIndex() {
        return product_id; // Product 1 goes to index 1, Product 2 to index 2, etc.
    }
}
