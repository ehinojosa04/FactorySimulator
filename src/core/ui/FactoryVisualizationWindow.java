package core.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import core.agents.BaseAgent;
import core.agents.AgentType;
import core.agents.AgentLocation;
import core.agents.AgentState;

public class FactoryVisualizationWindow extends JFrame implements Runnable {

    private final List<BaseAgent> agents;
    private final VisualizationPanel panel;
    private volatile boolean running = true;

    // Agent visual data (position, target, etc.)
    private final Map<String, AgentVisual> agentVisuals;

    public FactoryVisualizationWindow(List<BaseAgent> agents) {
        this.agents = agents;
        this.agentVisuals = new HashMap<>();

        setTitle("Factory 2D Visualization");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize agent visuals
        for (BaseAgent agent : agents) {
            Point2D startPos = getLocationCenter(agent.getLocation());
            agentVisuals.put(agent.getThreadID(), new AgentVisual(startPos));
        }

        panel = new VisualizationPanel();
        add(panel, BorderLayout.CENTER);

        // Legend panel
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.EAST);

        setVisible(true);
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createTitledBorder("Legend"));
        legend.setPreferredSize(new Dimension(200, 0));

        legend.add(createLegendItem("Worker", new Color(70, 130, 180)));
        legend.add(createLegendItem("Manager", new Color(178, 34, 34)));
        legend.add(createLegendItem("Inventory", new Color(218, 165, 32)));
        legend.add(createLegendItem("Delivery", new Color(60, 179, 113)));
        legend.add(Box.createVerticalStrut(20));
        
        legend.add(new JLabel("States:"));
        legend.add(createStateLegendItem("WORKING", Color.GREEN));
        legend.add(createStateLegendItem("WAITING", Color.RED));
        legend.add(createStateLegendItem("MOVING", Color.BLUE));
        legend.add(createStateLegendItem("IDLE", Color.GRAY));
        legend.add(createStateLegendItem("ON_BREAK", Color.ORANGE));

        return legend;
    }

    private JPanel createLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        item.add(colorBox);
        item.add(new JLabel(label));
        return item;
    }

    private JPanel createStateLegendItem(String label, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel colorLabel = new JLabel("●");
        colorLabel.setForeground(color);
        colorLabel.setFont(new Font("Arial", Font.BOLD, 20));
        item.add(colorLabel);
        item.add(new JLabel(label));
        return item;
    }

    @Override
    public void run() {
        while (running) {
            try {
                updateAgentPositions();
                SwingUtilities.invokeLater(() -> panel.repaint());
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                running = false;
                break;
            }
        }
    }

    private void updateAgentPositions() {
        for (BaseAgent agent : agents) {
            AgentVisual visual = agentVisuals.get(agent.getThreadID());
            if (visual != null) {
                Point2D targetPos = getLocationCenter(agent.getLocation());
                visual.moveTowards(targetPos, 2.0); // 2 pixels per frame
            }
        }
    }

    private Point2D getLocationCenter(AgentLocation location) {
        switch (location) {
            case FACTORY: return new Point2D.Double(400, 400);
            case WAREHOUSE: return new Point2D.Double(150, 400);
            case BATHROOM: return new Point2D.Double(400, 150);
            case BREAKROOM: return new Point2D.Double(650, 150);
            case LOADING_DECK: return new Point2D.Double(150, 650);
            case SUPPLIER: return new Point2D.Double(900, 650);
            default: return new Point2D.Double(400, 400);
        }
    }

    private Color getAgentTypeColor(AgentType type) {
        switch (type) {
            case WORKER: return new Color(70, 130, 180); // Steel Blue
            case MANAGER: return new Color(178, 34, 34); // Firebrick
            case INVENTORY: return new Color(218, 165, 32); // Goldenrod
            case DELIVERY: return new Color(60, 179, 113); // Medium Sea Green
            default: return Color.GRAY;
        }
    }

    private Color getStateIndicatorColor(AgentState state) {
        switch (state) {
            case WORKING: return Color.GREEN;
            case WAITING: return Color.RED;
            case MOVING: return Color.BLUE;
            case IDLE: return Color.GRAY;
            case ON_BREAK: return Color.ORANGE;
            case ENDING_SHIFT:
            case SHIFT_ENDED: return Color.DARK_GRAY;
            default: return Color.BLACK;
        }
    }

    public void stop() {
        running = false;
        dispose();
    }

    // Inner class for agent visual data
    private static class AgentVisual {
        Point2D.Double currentPos;
        
        AgentVisual(Point2D startPos) {
            this.currentPos = new Point2D.Double(startPos.getX(), startPos.getY());
        }

        void moveTowards(Point2D target, double speed) {
            double dx = target.getX() - currentPos.x;
            double dy = target.getY() - currentPos.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > speed) {
                currentPos.x += (dx / distance) * speed;
                currentPos.y += (dy / distance) * speed;
            } else {
                currentPos.x = target.getX();
                currentPos.y = target.getY();
            }
        }
    }

    // Inner class for the visualization panel
    private class VisualizationPanel extends JPanel {
        
        public VisualizationPanel() {
            setBackground(new Color(245, 245, 245));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable anti-aliasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Draw zones
            drawZones(g2d);
            
            // Draw workstation queue indicators
            drawWorkstationQueues(g2d);

            // Draw agents
            drawAgents(g2d);
        }

        private void drawZones(Graphics2D g2d) {
            // Factory
            drawZone(g2d, 250, 250, 300, 300, new Color(200, 220, 255), "FACTORY");
            
            // Warehouse
            drawZone(g2d, 50, 250, 150, 300, new Color(255, 235, 205), "WAREHOUSE");
            
            // Bathroom
            drawZone(g2d, 300, 50, 200, 150, new Color(230, 230, 250), "BATHROOM");
            
            // Breakroom
            drawZone(g2d, 550, 50, 200, 150, new Color(240, 255, 240), "BREAKROOM");
            
            // Loading Deck
            drawZone(g2d, 50, 550, 200, 150, new Color(255, 250, 205), "LOADING DECK");
            
            // Supplier (off-site)
            drawZone(g2d, 800, 550, 150, 150, new Color(255, 228, 225), "SUPPLIER");

            // Draw workstations inside factory
            drawWorkstations(g2d);
        }

        private void drawZone(Graphics2D g2d, int x, int y, int width, int height, Color color, String label) {
            // Fill zone
            g2d.setColor(color);
            g2d.fillRoundRect(x, y, width, height, 20, 20);
            
            // Border
            g2d.setColor(color.darker());
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(x, y, width, height, 20, 20);
            
            // Label
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, x + (width - labelWidth) / 2, y + 20);
        }

        private void drawWorkstations(Graphics2D g2d) {
            // Draw 4 workstation rectangles inside the factory
            int[] wsX = {280, 450, 280, 450};
            int[] wsY = {280, 280, 450, 450};
            
            g2d.setColor(new Color(150, 150, 150));
            for (int i = 0; i < 4; i++) {
                g2d.fillRect(wsX[i], wsY[i], 60, 60);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(wsX[i], wsY[i], 60, 60);
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString("WS" + (i+1), wsX[i] + 18, wsY[i] + 35);
                g2d.setColor(new Color(150, 150, 150));
            }
        }

        private void drawWorkstationQueues(Graphics2D g2d) {
            // Count agents waiting for workstations
            long waitingForWorkstation = agents.stream()
                .filter(a -> a.getLocation() == AgentLocation.FACTORY 
                          && a.getAgentState() == AgentState.WAITING
                          && a.getAgentType() == AgentType.WORKER)
                .count();

            if (waitingForWorkstation > 0) {
                g2d.setColor(new Color(255, 100, 100, 150));
                g2d.fillOval(500, 360, 30, 30);
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String queueText = String.valueOf(waitingForWorkstation);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(queueText);
                g2d.drawString(queueText, 515 - textWidth/2, 380);
            }
        }

        private void drawAgents(Graphics2D g2d) {
            for (BaseAgent agent : agents) {
                AgentVisual visual = agentVisuals.get(agent.getThreadID());
                if (visual != null) {
                    drawAgent(g2d, agent, visual.currentPos);
                }
            }
        }

        private void drawAgent(Graphics2D g2d, BaseAgent agent, Point2D pos) {
            int x = (int) pos.getX();
            int y = (int) pos.getY();
            int size = 24;

            // Draw agent shape based on type
            Color agentColor = getAgentTypeColor(agent.getAgentType());
            
            // Shadow
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(x - size/2 + 2, y - size/2 + 2, size, size);

            // Agent body
            g2d.setColor(agentColor);
            
            switch (agent.getAgentType()) {
                case WORKER:
                    g2d.fillOval(x - size/2, y - size/2, size, size);
                    break;
                case DELIVERY:
                    g2d.fillRect(x - size/2, y - size/2, size, size);
                    break;
                case MANAGER:
                    int[] xPoints = {x, x - size/2, x + size/2};
                    int[] yPoints = {y - size/2, y + size/2, y + size/2};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                    break;
                case INVENTORY:
                    g2d.fillRoundRect(x - size/2, y - size/2, size, size, 8, 8);
                    break;
            }

            // Border
            g2d.setColor(agentColor.darker());
            g2d.setStroke(new BasicStroke(2));
            
            switch (agent.getAgentType()) {
                case WORKER:
                    g2d.drawOval(x - size/2, y - size/2, size, size);
                    break;
                case DELIVERY:
                    g2d.drawRect(x - size/2, y - size/2, size, size);
                    break;
                case MANAGER:
                    int[] xPoints = {x, x - size/2, x + size/2};
                    int[] yPoints = {y - size/2, y + size/2, y + size/2};
                    g2d.drawPolygon(xPoints, yPoints, 3);
                    break;
                case INVENTORY:
                    g2d.drawRoundRect(x - size/2, y - size/2, size, size, 8, 8);
                    break;
            }

            // State indicator (small circle on top-right)
            Color stateColor = getStateIndicatorColor(agent.getAgentState());
            g2d.setColor(stateColor);
            g2d.fillOval(x + size/3, y - size/2, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x + size/3, y - size/2, 8, 8);

            // Agent ID label
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            FontMetrics fm = g2d.getFontMetrics();
            String id = agent.getThreadID();
            int labelWidth = fm.stringWidth(id);
            
            // Background for label
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRoundRect(x - labelWidth/2 - 2, y + size/2 + 2, labelWidth + 4, 12, 4, 4);
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(id, x - labelWidth/2, y + size/2 + 11);
        }
    }
}