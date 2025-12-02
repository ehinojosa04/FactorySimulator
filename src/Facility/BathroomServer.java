package Facility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import core.agents.AgentLocation;
import core.agents.AgentState;

public class BathroomServer extends FacilityServer {
    public BathroomServer() {
        super(FacilityType.BATHROOM, 5000);
    }

    public static void main(String[] args) throws IOException {
        BathroomServer server = new BathroomServer();
        server.start();
    }

    @Override
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(this.facilityType + " server listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, facility)).start();
            }
        }
    }

    private class ClientHandler implements Runnable, ClientChannel {
        private final Socket socket;
        private final Facility facility;
        private String agentId = "UNKNOWN";
        private PrintWriter out;

        ClientHandler(Socket socket, Facility facility) {
            this.socket = socket;
            this.facility = facility;
        }

        @Override
        public void run() {
            System.out.println("Client connected: " + socket);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

                this.out = out;
                String line;
                while ((line = in.readLine()) != null) {
                    handleLine(line.trim());
                }
            } catch (IOException e) {
                System.out.println("Connection error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                System.out.println("Client disconnected: " + socket);
            }
        }

        private void handleLine(String line) {
            if (line.isEmpty())
                return;
            String[] parts = line.split("\\s+");
            String cmd = parts[0];

            switch (cmd) {
                case "HELLO":
                    if (parts.length >= 2) {
                        this.agentId = parts[1];
                        sendEvent(agentId, "HELLO_OK");
                    }
                    break;
                case "REQUEST_BATHROOM":
                    facility.handleAccessRequest(agentId, this);
                    break;
                case "QUIT":
                    sendEvent(agentId, "BYE");
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    break;

                default:
                    sendEvent(agentId, "UNKNOWN_COMMAND:" + cmd);
            }
        }

        @Override
        public void sendState(String agentId, AgentState state) {
            out.println("STATE " + agentId + " " + state.name());
        }

        @Override
        public void sendLocation(String agentId, AgentLocation location) {
            out.println("LOCATION " + agentId + " " + location.name());
        }

        @Override
        public void sendEvent(String agentId, String eventType) {
            out.println("EVENT " + agentId + " " + eventType);
        }
    }
}
