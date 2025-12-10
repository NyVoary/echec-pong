package server;
import java.io.*;
import java.net.*;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        // Récupérer l'adresse IP locale
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        int port = 5555;
        
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("=================================");
        System.out.println("Serveur démarré !");
        System.out.println("Adresse IP : " + serverIP);
        System.out.println("Port       : " + port);
        System.out.println("=================================");
        System.out.println("En attente de connexions...\n");
        
        GameEngine gameEngine = new GameEngine();
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            if (gameEngine.clients.size() >= 2) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("FULL");
                clientSocket.close();
                System.out.println("Client refusé (serveur plein)");
                continue;
            }
            System.out.println("Nouveau client connecté depuis: " + clientSocket.getInetAddress());
            new ClientHandler(clientSocket, gameEngine).start();
        }
    }
}