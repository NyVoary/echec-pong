package server;
import java.util.*;
import common.Paddle;

public class GameEngine {
    public Paddle leftPaddle = new Paddle(50, 250);
    public Paddle rightPaddle = new Paddle(750, 250);
    public List<ClientHandler> clients = new ArrayList<>();

    public synchronized void movePaddle(String side, String direction) {
        Paddle paddle = side.equals("LEFT") ? leftPaddle : rightPaddle;
        if (direction.equals("UP")) {
            paddle.moveUp();
        } else if (direction.equals("DOWN")) {
            paddle.moveDown();
        }
        broadcastState();
    }

    public synchronized void broadcastState() {
        String state = getGameState();
        for (ClientHandler client : clients) {
            client.sendMessage(state);
        }
    }

    public String getGameState() {
        return "STATE:" + leftPaddle.getY() + "," + rightPaddle.getY();
    }
}