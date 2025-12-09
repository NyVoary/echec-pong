package server;
import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private GameEngine gameEngine;
    private PrintWriter out;
    private BufferedReader in;
    private String playerSide;

    public ClientHandler(Socket socket, GameEngine gameEngine) {
        this.socket = socket;
        this.gameEngine = gameEngine;
    }

    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            synchronized (gameEngine.clients) {
                gameEngine.clients.add(this);
                if (gameEngine.clients.size() == 1) {
                    playerSide = "LEFT";
                } else {
                    playerSide = "RIGHT";
                }
            }
            out.println("SIDE:" + playerSide);
            System.out.println("Client assigné côté: " + playerSide);

            gameEngine.broadcastState();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                processCommand(inputLine);
            }
        } catch (IOException e) {
            System.out.println("Client déconnecté");
        } finally {
            synchronized (gameEngine.clients) {
                gameEngine.clients.remove(this);
            }
        }
    }

    private void processCommand(String command) {
        if (command.startsWith("MOVE:")) {
            String direction = command.substring(5);
            gameEngine.movePaddle(playerSide, direction);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}