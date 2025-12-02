package core.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import core.agents.BaseAgent;

public class ThreadStatesWindow extends JFrame implements Runnable {
    private final ArrayList<BaseAgent> agents;
    private final DefaultTableModel tableModel;
    private volatile boolean running = true;

    public ThreadStatesWindow(ArrayList<BaseAgent> agents) {
        this.agents = agents;

        setTitle("Thread State Summary");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] columns = {"Agent Type", "RUNNABLE", "WAITING","TIMED_WAITING", "BLOCKED", "TERMINATED"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JLabel title = new JLabel("Thread State Summary per Agent", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        setVisible(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                SwingUtilities.invokeLater(this::updateTable);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    private void updateTable() {
        Map<String, EnumMap<Thread.State, Integer>> typeStats = new HashMap<>();

        for (BaseAgent agent : agents) {
            String type = agent.getAgentType().toString();
            Thread.State state = agent.getState();

            typeStats.putIfAbsent(type, new EnumMap<>(Thread.State.class));
            EnumMap<Thread.State, Integer> map = typeStats.get(type);
            map.put(state, map.getOrDefault(state, 0) + 1);
        }

        tableModel.setRowCount(0);
        for (String type : typeStats.keySet()) {
            EnumMap<Thread.State, Integer> map = typeStats.get(type);

            int runnable = map.getOrDefault(Thread.State.RUNNABLE, 0);
            int waiting = map.getOrDefault(Thread.State.WAITING, 0);
            int timed = map.getOrDefault(Thread.State.TIMED_WAITING, 0);
            int blocked = map.getOrDefault(Thread.State.BLOCKED, 0);
            int terminated = map.getOrDefault(Thread.State.TERMINATED, 0);

            tableModel.addRow(new Object[]{type, runnable, waiting, timed, blocked, terminated});
        }
    }

    public void stop() {
        running = false;
    }
}
