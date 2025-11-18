package core.ui;

import javax.swing.*;
import java.awt.*;
import factory.warehouse.Warehouse;

public class InventoryWindow extends JFrame implements Runnable {

    private final Warehouse warehouse;
    private final JLabel[] inventoryLabels;
    private volatile boolean running = true;

    public InventoryWindow(Warehouse warehouse) {
        this.warehouse = warehouse;
        this.inventoryLabels = new JLabel[warehouse.inventory.size()];

        setTitle("Factory Inventory Monitor");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // === HEADER ===
        JLabel title = new JLabel("Warehouse Inventory", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        // === INVENTORY PANEL ===
        JPanel inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new GridLayout(warehouse.inventory.size(), 2, 10, 5));

        for (int i = 0; i < warehouse.inventory.size(); i++) {
            JLabel nameLabel = new JLabel("Slot " + i + ":");
            JLabel valueLabel = new JLabel(String.valueOf(warehouse.inventory.get(i)));
            inventoryLabels[i] = valueLabel;
            inventoryPanel.add(nameLabel);
            inventoryPanel.add(valueLabel);
        }

        add(inventoryPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                SwingUtilities.invokeLater(() -> updateInventoryDisplay());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    private void updateInventoryDisplay() {
        for (int i = 0; i < warehouse.inventory.size(); i++) {
            inventoryLabels[i].setText(String.valueOf(warehouse.inventory.get(i)));
        }
    }

    public void stop() {
        running = false;
    }
}
