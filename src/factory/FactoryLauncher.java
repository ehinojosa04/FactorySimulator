package factory;

import javax.swing.*;
import java.awt.*;

public class FactoryLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Factory Simulation Launcher");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(350, 600);
            frame.setLayout(new BorderLayout());

            JLabel title = new JLabel("Factory Simulation Launcher", SwingConstants.CENTER);
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            frame.add(title, BorderLayout.NORTH);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

            JTextField workersTF = createField(panel, "Workers:", "10");
            JTextField deliveryTF = createField(panel, "Delivery:", "3");
            JTextField truckMaxCapacityTF = createField(panel, "Truck max capacity:", "20");


            JTextField orderBatchSizeTF = createField(panel, "Order batch size:", "10");
            JTextField productsTF = createField(panel, "Types of products:", "5");

            JTextField workstationCpctyTF = createField(panel, "Workstation agent capacity:", "2");

            JTextField transportTimeTF = createField(panel, "Time to start driving (ms):", "10000");
            JTextField timeToProduceTF = createField(panel, "Time to produce an item (ms):", "500");
            JTextField timeToRequestMaterialsTF = createField(panel, "Time to request materials (ms):", "500");


            JButton startButton = new JButton("Start Simulation");
            startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            startButton.setFocusPainted(false);

            panel.add(Box.createVerticalStrut(10));
            panel.add(startButton);

            frame.add(panel, BorderLayout.CENTER);

            startButton.addActionListener(e -> {
                try {
                    int workstations = Integer.parseInt(workstationCpctyTF.getText());
                    int orderBatchSize = Integer.parseInt(orderBatchSizeTF.getText());
                    int productsOffered = Integer.parseInt(productsTF.getText());
                    int workers = Integer.parseInt(workersTF.getText());
                    int delivery = Integer.parseInt(deliveryTF.getText());
                    int truckMaxCapacity = Integer.parseInt(truckMaxCapacityTF.getText());
                    int transportTime = Integer.parseInt(transportTimeTF.getText());
                    int productionTime = Integer.parseInt(timeToProduceTF.getText());
                    int requestTime = Integer.parseInt(timeToRequestMaterialsTF.getText());

                    new FactoryServer(workstations, orderBatchSize, productsOffered, productionTime, workers, truckMaxCapacity, transportTime, delivery, requestTime);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid integers.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JTextField createField(JPanel panel, String labelText, String defaultValue) {
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(defaultValue, 10);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(label);
        panel.add(textField);
        panel.add(Box.createVerticalStrut(5));
        return textField;
    }
}
