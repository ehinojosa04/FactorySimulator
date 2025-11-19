package factory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import core.agents.BaseAgent;
import core.ui.AgentStatesWindow;
import core.ui.InventoryWindow;
import core.ui.ThreadStatesWindow;
import core.ui.ZonesWindow;

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

            JTextField stagesTF = createField(panel, "Stages:", "10");
            JTextField workersTF = createField(panel, "Workers:", "5");
            JTextField deliveryTF = createField(panel, "Delivery:", "1");

            JTextField workstationCpctyTF = createField(panel, "Workstation agent capacity:", "2");
            JTextField BthrmCpctyTF = createField(panel, "Bathroom agent capacity:", "5");
            JTextField BrkrmCpctyTF = createField(panel, "Breakroom  agent apacity:", "10");

            JTextField pcrTF = createField(panel, "Production Conversion Ratio:", "1");
            JTextField deliveryTimeTF = createField(panel, "Time for delivery (ms):", "10000");
            JTextField timeToProduceTF = createField(panel, "Time to produce (ms):", "1000");
            JTextField timeToRequestMaterialsTF = createField(panel, "Time to request materials (ms):", "500");
            JTextField breakTimeTF = createField(panel, "Time inside break (ms):", "1000");


            JButton startButton = new JButton("Start Simulation");
            startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            startButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            startButton.setFocusPainted(false);

            panel.add(Box.createVerticalStrut(10));
            panel.add(startButton);

            frame.add(panel, BorderLayout.CENTER);

            startButton.addActionListener(e -> {
                try {
                    int stages = Integer.parseInt(stagesTF.getText());
                    int workers = Integer.parseInt(workersTF.getText());
                    int delivery = Integer.parseInt(deliveryTF.getText());

                    startSimulation(stages, workers, delivery);
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

    private static void startSimulation(int stages, int workers, int delivery) {
        Factory factory = new Factory(stages, workers, delivery);

        new Thread(new InventoryWindow(factory.warehouse)).start();

        ArrayList<BaseAgent> agents = new ArrayList<>();
        agents.add(factory.manager);
        agents.add(factory.inventoryAgent);
        agents.addAll(factory.workerAgents);
        agents.addAll(factory.deliveryAgents);

        new Thread(new AgentStatesWindow(agents)).start();
        new Thread(new ThreadStatesWindow(agents)).start();
        new Thread(new ZonesWindow(agents)).start();

        System.out.println("Factory simulation started with: " + stages + ", " + workers + ", " + delivery);
    }
}
