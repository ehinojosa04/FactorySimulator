package core.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;
import core.agents.BaseAgent;
import core.agents.AgentState; // Ensure you have this import

public class AgentStatesWindow extends JFrame implements Runnable {

    private final List<BaseAgent> agents;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private volatile boolean running = true;

    public AgentStatesWindow(List<BaseAgent> agents) {
        this.agents = agents;

        setTitle("Factory Simulation Monitor");
        setSize(1000, 500); // Made it slightly wider for descriptors
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Setup Table Data Structure
        String[] columnNames = {"Agent ID", "Type", "State", "Location", "Activity Description"};

        // We make cells non-editable
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30); // Taller rows for readability
        table.setFillsViewportHeight(true);
        table.setGridColor(Color.LIGHT_GRAY);

        // 2. Formatting & Colors
        setupTableFormatting();

        // 3. Layout
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JLabel title = new JLabel("Real-Time Agent Status", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        setVisible(true);
    }

    private void setupTableFormatting() {
        TableColumnModel columnModel = table.getColumnModel();

        // A. Set Column Widths
        columnModel.getColumn(0).setPreferredWidth(100); // ID
        columnModel.getColumn(1).setPreferredWidth(100); // Type
        columnModel.getColumn(2).setPreferredWidth(100); // State
        columnModel.getColumn(3).setPreferredWidth(150); // Location
        columnModel.getColumn(4).setPreferredWidth(450); // Descriptor (Give this the most space)

        // B. Center Align Text for ID, Type, Location
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);
        columnModel.getColumn(1).setCellRenderer(centerRenderer);
        columnModel.getColumn(3).setCellRenderer(centerRenderer);

        // C. Custom Color Renderer for STATE
        columnModel.getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Center the text
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));

                // Color Logic based on AgentState
                if (value != null) {
                    String stateStr = value.toString();
                    switch (stateStr) {
                        case "WORKING":
                            c.setForeground(new Color(0, 150, 0)); // Dark Green
                            break;
                        case "WAITING":
                            c.setForeground(Color.RED); // Alert Red
                            break;
                        case "MOVING":
                            c.setForeground(Color.BLUE);
                            break;
                        case "IDLE":
                            c.setForeground(Color.GRAY);
                            break;
                        case "FIXING":
                            c.setForeground(new Color(200, 100, 0)); // Orange
                            break;
                        default:
                            c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });
    }

    @Override
    public void run() {
        while (running) {
            try {
                SwingUtilities.invokeLater(this::updateTable);
                // 100ms is smoother for reading text than 50ms
                Thread.sleep(100);
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    private void updateTable() {
        // IMPROVEMENT: Don't delete rows! Update existing ones to prevent flickering.

        // 1. If table is empty, fill it first
        if (tableModel.getRowCount() != agents.size()) {
            tableModel.setRowCount(0);
            for (BaseAgent agent : agents) {
                tableModel.addRow(new Object[]{
                        agent.getThreadID(),
                        agent.getAgentType(),
                        agent.getAgentState(),
                        agent.getLocation(),
                        agent.getStateDescriptor()
                });
            }
        }
        // 2. Update existing rows
        else {
            for (int i = 0; i < agents.size(); i++) {
                BaseAgent agent = agents.get(i);

                // Only update specific cells
                tableModel.setValueAt(agent.getThreadID(), i, 0);
                tableModel.setValueAt(agent.getAgentType(), i, 1);
                tableModel.setValueAt(agent.getAgentState(), i, 2);
                tableModel.setValueAt(agent.getLocation(), i, 3);
                tableModel.setValueAt(agent.getStateDescriptor(), i, 4);
            }
        }
    }

    public void stop() {
        running = false;
        dispose(); // Close window properly
    }
}