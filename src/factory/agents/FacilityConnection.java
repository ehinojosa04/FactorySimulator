package factory.agents;

import java.io.*;
import java.net.Socket;

import core.agents.AgentLocation;
import core.agents.AgentState;
import core.agents.BaseAgent;

public abstract class FacilityConnection {
    protected final BaseAgent agent;
    private final String host;
    private final int port;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Thread listenerThread;
    private volatile boolean running = false;

    protected FacilityConnection(String host, int port, BaseAgent agent) {
        this.host = host;
        this.port = port;
        this.agent = agent;
    }

    protected synchronized void ensureConnected() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        socket = new Socket(host, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        sendHello();

        running = true;
        listenerThread = new Thread(this::listenLoop,
                getClass().getSimpleName() + "-Listener-" + agent.getThreadID());
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    protected void sendHello() {
        sendLine("HELLO " + agent.getThreadID());
    }

    protected void sendLine(String line) {
        try {
            ensureConnected();
            out.println(line);
        } catch (IOException e) {
            System.err.println("[" + agent.getThreadID() + "] Failed to send line to facility: " + e.getMessage());
        }
    }

    public synchronized void close() {
        System.out.println("Closing server connection");
        running = false;

        try {
            if (out != null && socket != null && !socket.isClosed() && socket.isConnected()) {
                out.println("QUIT");
                out.flush();
            }
        } catch (Exception e) {
            System.out.println("[" + agent.getThreadID() + "] Could not send QUIT: " + e.getMessage());
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        socket = null;
        out = null;
        in = null;

        if (listenerThread != null && listenerThread.isAlive()) {
            try {
                listenerThread.join(1000);
            } catch (InterruptedException ignored) {
            }
        }
        listenerThread = null;
    }

    private void listenLoop() {
        try {
            String line;
            while (running && in != null && (line = in.readLine()) != null) {
                handleServerLine(line.trim());
            }
        } catch (IOException e) {
            if (running) {
                System.out
                        .println("[" + agent.getThreadID() + "] Facility server connection closed: " + e.getMessage());
            }
        } finally {
            // Ensure resources are cleaned if server disconnects
            close();
        }
    }

    private void handleServerLine(String line) {
        if (line.isEmpty())
            return;

        String[] parts = line.split("\\s+");
        String cmd = parts[0];

        switch (cmd) {
            case "STATE": {
                if (parts.length >= 3 && parts[1].equals(agent.getThreadID())) {
                    AgentState newState = AgentState.valueOf(parts[2]);
                    agent.updateStateFromServer(newState);
                }
                break;
            }
            case "LOCATION": {
                if (parts.length >= 3 && parts[1].equals(agent.getThreadID())) {
                    AgentLocation newLoc = AgentLocation.valueOf(parts[2]);
                    agent.updateLocationFromServer(newLoc);
                }
                break;
            }
            case "EVENT": {
                if (parts.length >= 3 && parts[1].equals(agent.getThreadID())) {
                    String eventType = parts[2];
                    handleEventFromServer(eventType, parts);
                }
                break;
            }
            default:
                handleUnknownCommand(cmd, parts);
        }
    }

    /**
     * Called when an EVENT line is received for this agent.
     * Subclasses should interpret eventType and react accordingly.
     */
    protected abstract void handleEventFromServer(String eventType, String[] parts);

    /**
     * Optional hook for subclasses if they want to handle custom commands.
     */
    protected void handleUnknownCommand(String cmd, String[] parts) {
        // default: ignore
    }
}
