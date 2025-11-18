package core.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import core.agents.BaseAgent;
import core.agents.AgentType;
import core.agents.AgentLocation;

public class ZonesWindow extends JFrame implements Runnable {

    private final List<BaseAgent> agents;

    private final JTextArea typeArea;
    private final JTextArea locationArea;

    private volatile boolean running = true;

    public ZonesWindow(List<BaseAgent> agents) {
        this.agents = agents;

        setTitle("General Overview Dashboard");
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("General Overview Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        typeArea = new JTextArea();
        typeArea.setEditable(false);
        typeArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        mainPanel.add(new JScrollPane(typeArea));

        locationArea = new JTextArea();
        locationArea.setEditable(false);
        locationArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        mainPanel.add(new JScrollPane(locationArea));

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                SwingUtilities.invokeLater(this::updateDashboard);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    private void updateDashboard() {
        StringBuilder sbTypes = new StringBuilder("AGENTS BY TYPE:\n");
        StringBuilder sbLocations = new StringBuilder("AGENTS BY LOCATION:\n");

        Map<AgentType, Long> byType = agents.stream()
                .collect(Collectors.groupingBy(BaseAgent::getAgentType, Collectors.counting()));

        for (AgentType type : AgentType.values()) {
            long count = byType.getOrDefault(type, 0L);
            sbTypes.append(String.format(" • %-10s : %d%n", type, count));
        }

        Map<AgentLocation, Long> byLocation = agents.stream()
                .collect(Collectors.groupingBy(BaseAgent::getLocation, Collectors.counting()));

        for (AgentLocation loc : AgentLocation.values()) {
            long count = byLocation.getOrDefault(loc, 0L);
            sbLocations.append(String.format(" • %-15s : %d%n", loc, count));
        }

        typeArea.setText(sbTypes.toString());
        locationArea.setText(sbLocations.toString());
    }

    public void stop() {
        running = false;
    }
}
