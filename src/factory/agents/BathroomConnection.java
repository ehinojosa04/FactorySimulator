package factory.agents;

import java.io.*;
import java.net.Socket;

import core.agents.AgentLocation;
import core.agents.AgentState;

public class BathroomConnection {
    private final WorkerAgent agent;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    public BathroomConnection(String host, int port, WorkerAgent agent) throws IOException {
        this.agent = agent;
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send HELLO so server knows our ID
        out.println("HELLO " + agent.getThreadID());

        // Start listener thread for server messages
        Thread listener = new Thread(this::listenLoop, "BathroomListener-" + agent.getThreadID());
        listener.setDaemon(true);
        listener.start();
    }

    public void requestBathroomBreak() {
        out.println("REQUEST_BATHROOM");
    }

    public void close() throws IOException {
        out.println("QUIT");
        socket.close();
    }

    private void listenLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                handleServerLine(line.trim());
            }
        } catch (IOException e) {
            System.out.println("[" + agent.getThreadID() + "] Bathroom server connection closed: " + e.getMessage());
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
                    agent.handleBathroomEventFromServer(eventType);
                }
                break;
            }
            default:
                // ignore unknown cmds
        }
    }
}
