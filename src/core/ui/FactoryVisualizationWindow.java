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

        setTitle("Factory 2D Visualization - Proyecto Final");
        
        setSize(1450, 850); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        
        for (BaseAgent agent : agents) {
            Point2D startPos = getLocationCenter(agent.getLocation());
            agentVisuals.put(agent.getThreadID(), new AgentVisual(startPos));
        }

        
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.WEST);

        
        panel = new VisualizationPanel();
        add(panel, BorderLayout.CENTER);

        
        JPanel legendPanel = createLegendPanel();
        add(legendPanel, BorderLayout.EAST);

        setVisible(true);
    }

    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setPreferredSize(new Dimension(280, 0));
        infoPanel.setBackground(Color.WHITE);

        // Logo
        try {
            
            ImageIcon logoIcon = new ImageIcon("UPL.jpg");
            
            Image img = logoIcon.getImage(); 
            Image newImg = img.getScaledInstance(200, 150,  java.awt.Image.SCALE_SMOOTH); 
            JLabel logoLabel = new JLabel(new ImageIcon(newImg));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(logoLabel);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("[Logo UPL.jpg no encontrado]");
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            infoPanel.add(errorLabel);
        }

        infoPanel.add(Box.createVerticalStrut(20));

        
        JLabel infoLabel = new JLabel("<html><div style='text-align: center;'>" +
                "<h2>Universidad Panamericana</h2>" +
                "<h3>Ingeniería en Sistemas y Gráficas Computacionales</h3>" +
                "<br>" +
                "<b>Materia:</b><br>Fundamentos de Programación en Paralelo<br><br>" +
                "<b>Profesor:</b><br>Dr. Juan Carlos López Pimentel<br><br>" +
                "<b>Estudiantes:</b><br>" +
                "Diego Amín Hernández Pallares<br>" +
                "Emiliano Hinojosa Guzmán<br>" +
                "José Salcedo Uribe<br><br>" +
                "<b>Fecha de entrega:</b><br>" +
                "4 de Diciembre de 2025" +
                "</div></html>");
        
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(infoLabel);

        return infoPanel;
    }

    private JPanel createLegendPanel() {
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setBorder(BorderFactory.createTitledBorder("Legend"));
        legend.setPreferredSize(new Dimension(280, 0));

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
                Point2D targetPos;
                
                
                if (agent.getAgentType() == AgentType.MANAGER && 
                    agent.getLocation() == AgentLocation.FACTORY) {
                    targetPos = getManagerPosition();
                }
                
                else if (agent.getAgentType() == AgentType.INVENTORY && 
                         agent.getLocation() == AgentLocation.WAREHOUSE) {
                    targetPos = getInventoryPosition();
                }
                else {
                    
                    if (!visual.currentLocation.equals(agent.getLocation())) {
                        targetPos = getLocationCenter(agent.getLocation());
                        visual.setTargetPosition(targetPos);
                        visual.currentLocation = agent.getLocation();
                    } else {
                        targetPos = visual.targetPosition;
                    }
                }
                
                visual.moveTowards(targetPos, 2.0); 
            }
        }
    }

    private Point2D getLocationCenter(AgentLocation location) {
        
        double offsetX = (Math.random() - 0.5) * 120; 
        double offsetY = (Math.random() - 0.5) * 140; 
        
        
        double offsetYLimitado = (Math.random() - 0.5) * 80; 

        
        switch (location) {
            
            case FACTORY: return new Point2D.Double(570 + offsetX, 410 + offsetY);
            
            
            case WAREHOUSE: return new Point2D.Double(270 + offsetX, 410 + offsetY);
            
            
            case BATHROOM: return new Point2D.Double(270 + offsetX, 130 + offsetY);
            
            
            case BREAKROOM: return new Point2D.Double(570 + offsetX, 130 + offsetY);
            
            
            case LOADING_DECK: return new Point2D.Double(270 + offsetX, 670 + offsetYLimitado);
            
            
            case SUPPLIER: return new Point2D.Double(570 + offsetX, 670 + offsetYLimitado);
            
            default: return new Point2D.Double(570 + offsetX, 410 + offsetY);
        }
    }
    
    
    private Point2D getManagerPosition() {
        return new Point2D.Double(570, 290);
    }
    
    
    private Point2D getInventoryPosition() {
        return new Point2D.Double(270, 290);
    }

    private Color getAgentTypeColor(AgentType type) {
        switch (type) {
            case WORKER: return new Color(70, 130, 180); 
            case MANAGER: return new Color(178, 34, 34); 
            case INVENTORY: return new Color(218, 165, 32); 
            case DELIVERY: return new Color(60, 179, 113); 
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

    
    private static class AgentVisual {
        Point2D.Double currentPos;
        Point2D.Double targetPosition;
        AgentLocation currentLocation;
        
        AgentVisual(Point2D startPos) {
            this.currentPos = new Point2D.Double(startPos.getX(), startPos.getY());
            this.targetPosition = new Point2D.Double(startPos.getX(), startPos.getY());
            this.currentLocation = AgentLocation.FACTORY; 
        }
        
        void setTargetPosition(Point2D target) {
            this.targetPosition = new Point2D.Double(target.getX(), target.getY());
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

   
    private class VisualizationPanel extends JPanel {
        
        public VisualizationPanel() {
            setBackground(new Color(245, 245, 245));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
        
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            
            drawZones(g2d);
            
            
            drawWorkstationQueues(g2d);

           
            drawAgents(g2d);
        }

        private void drawZones(Graphics2D g2d) {
            
            
            
            drawZone(g2d, 130, 40, 280, 180, new Color(230, 230, 250), "BATHROOM");
            
            
            drawZone(g2d, 430, 40, 280, 180, new Color(240, 255, 240), "BREAKROOM");
            
           
            drawZone(g2d, 130, 240, 280, 340, new Color(255, 235, 205), "WAREHOUSE");
            
            
            drawZone(g2d, 430, 240, 280, 340, new Color(200, 220, 255), "FACTORY");
            
           
            drawZone(g2d, 130, 600, 280, 140, new Color(255, 250, 205), "LOADING DECK");
            
            
            drawZone(g2d, 430, 600, 280, 140, new Color(255, 228, 225), "SUPPLIER");
        }

        private void drawZone(Graphics2D g2d, int x, int y, int width, int height, Color color, String label) {
            
            g2d.setColor(color);
            g2d.fillRoundRect(x, y, width, height, 20, 20);
            
            
            g2d.setColor(color.darker());
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(x, y, width, height, 20, 20);
            
            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, x + (width - labelWidth) / 2, y + 20);
        }

        private void drawWorkstationQueues(Graphics2D g2d) {
            
            long waitingForWorkstation = agents.stream()
                .filter(a -> a.getLocation() == AgentLocation.FACTORY 
                          && a.getAgentState() == AgentState.WAITING
                          && a.getAgentType() == AgentType.WORKER)
                .count();

            if (waitingForWorkstation > 0) {
                
                int queueX = 450; 
                int queueY = 280;

                g2d.setColor(new Color(255, 100, 100, 150));
                g2d.fillOval(queueX, queueY, 35, 35);
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(queueX, queueY, 35, 35);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                String queueText = String.valueOf(waitingForWorkstation);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(queueText);
                g2d.drawString(queueText, queueX + 17 - textWidth/2, queueY + 22);
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

            
            Color agentColor = getAgentTypeColor(agent.getAgentType());
            
            
            g2d.setColor(new Color(0, 0, 0, 50));
            g2d.fillOval(x - size/2 + 2, y - size/2 + 2, size, size);

            
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

            
            Color stateColor = getStateIndicatorColor(agent.getAgentState());
            g2d.setColor(stateColor);
            g2d.fillOval(x + size/3, y - size/2, 8, 8);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x + size/3, y - size/2, 8, 8);

            
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            FontMetrics fm = g2d.getFontMetrics();
            String id = agent.getThreadID();
            int labelWidth = fm.stringWidth(id);
            
            
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRoundRect(x - labelWidth/2 - 2, y + size/2 + 2, labelWidth + 4, 12, 4, 4);
            
            g2d.setColor(Color.BLACK);
            g2d.drawString(id, x - labelWidth/2, y + size/2 + 11);
        }
    }
}