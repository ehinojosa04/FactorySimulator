package core.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import core.agents.BaseAgent;

public class AgentStatesWindow extends JFrame implements Runnable {

    private final List<BaseAgent> agents;
    private final DefaultTableModel tableModel;
    private volatile boolean running = true;

    public AgentStatesWindow(List<BaseAgent> agents) {
        this.agents = agents;

        setTitle("Agent State Monitor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columnNames = {"Agent ID", "Type", "State", "Location", "Descriptor"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JLabel title = new JLabel("Agent States", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        setVisible(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                SwingUtilities.invokeLater(this::updateTable);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (BaseAgent agent : agents) {
            tableModel.addRow(new Object[]{
                agent.getThreadID(),
                agent.getAgentType(),
                agent.getAgentState(),
                agent.getLocation(),
                agent.getStateDescriptor(),
            });
        }
    }

    public void stop() {
        running = false;
    }
}
