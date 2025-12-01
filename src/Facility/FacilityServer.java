package Facility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FacilityServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(5050);
        Socket clientSocket = serverSocket.accept();

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        String message = in.readLine();
        System.out.println("Server received a message: " + message);

        out.println(message);

        clientSocket.close();
        serverSocket.close();
    }
}
