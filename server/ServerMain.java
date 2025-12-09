package server;
import java.io.*;
import java.net.*;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5555);
        System.out.println("Serveur démarré sur port 5555");
        
        GameEngine gameEngine = new GameEngine();
        
        while (true) {
            Socket clientSocket = serverSocket.accept();
            if (gameEngine.clients.size() >= 2) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("FULL");
                clientSocket.close();
                continue;
            }
            System.out.println("Nouveau client connecté");
            new ClientHandler(clientSocket, gameEngine).start();
        }
    }
}