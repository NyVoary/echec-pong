package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import common.GameConfig;
import common.PieceType;

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
            
            // Créer le joueur dans le moteur de jeu
            gameEngine.addPlayer(playerSide, this);
            
            out.println("SIDE:" + playerSide);
            System.out.println("→ Client assigné côté: " + playerSide);

            // Envoie la configuration générale au client
            StringBuilder configMsg = new StringBuilder("CONFIG:");
            configMsg.append("WINDOW_WIDTH=").append(GameConfig.WINDOW_WIDTH).append(",");
            configMsg.append("WINDOW_HEIGHT=").append(GameConfig.WINDOW_HEIGHT).append(",");
            configMsg.append("PADDLE_WIDTH=").append(GameConfig.PADDLE_WIDTH).append(",");
            configMsg.append("PADDLE_HEIGHT=").append(GameConfig.PADDLE_HEIGHT).append(",");
            configMsg.append("BALL_RADIUS=").append(GameConfig.BALL_RADIUS).append(",");
            configMsg.append("BALL_INITIAL_SPEED=").append(GameConfig.BALL_INITIAL_SPEED).append(",");
            configMsg.append("BALL_START_X=").append(GameConfig.BALL_START_X).append(",");
            configMsg.append("BALL_START_Y=").append(GameConfig.BALL_START_Y).append(",");
            configMsg.append("GAME_AREA_MIN_X=").append(GameConfig.GAME_AREA_MIN_X).append(",");
            configMsg.append("GAME_AREA_MAX_X=").append(GameConfig.GAME_AREA_MAX_X).append(",");
            configMsg.append("GAME_AREA_MIN_Y=").append(GameConfig.GAME_AREA_MIN_Y).append(",");
            configMsg.append("GAME_AREA_MAX_Y=").append(GameConfig.GAME_AREA_MAX_Y).append(",");
            configMsg.append("TICK_RATE=").append(GameConfig.TICK_RATE).append(",");
            configMsg.append("TICK_DELAY=").append(GameConfig.TICK_DELAY).append(",");
            configMsg.append("BOARD_X=").append(GameConfig.BOARD_X).append(",");
            configMsg.append("TOP_BOARD_Y=").append(GameConfig.TOP_BOARD_Y).append(",");
            configMsg.append("BOTTOM_BOARD_Y=").append(GameConfig.BOTTOM_BOARD_Y).append(",");
            configMsg.append("CELL_SIZE=").append(GameConfig.CELL_SIZE).append(",");
            configMsg.append("PROGRESS_BAR_CAPACITY=").append(GameConfig.PROGRESS_BAR_CAPACITY);
            out.println(configMsg.toString());

            // Envoie les HP max de chaque type au client
            StringBuilder hpMsg = new StringBuilder("HP:");
            for (PieceType type : PieceType.values()) {
                hpMsg.append(type.name()).append("=").append(type.getMaxHP()).append(",");
            }
            out.println(hpMsg.toString());

            gameEngine.broadcastState();

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                processCommand(inputLine);
            }
        } catch (IOException e) {
            System.out.println("✗ Client déconnecté (" + playerSide + ")");
        } finally {
            synchronized (gameEngine.clients) {
                gameEngine.clients.remove(this);
                if (playerSide != null) {
                    gameEngine.removePlayer(playerSide);
                }
            }
        }
    }

    private void processCommand(String command) {
        if (command.startsWith("MOVE:")) {
            String direction = command.substring(5);
            gameEngine.movePaddle(playerSide, direction);
        } else if (command.startsWith("COLS:")) {
            int cols = Integer.parseInt(command.substring(5));
            System.out.println("📊mise à jour colonnes: " + cols);
            gameEngine.setBoardCols(cols);
        } else if (command.startsWith("PROGRESS_CAPACITY:")) {
            int capacity = Integer.parseInt(command.substring("PROGRESS_CAPACITY:".length()));
            System.out.println("📊 Mise à jour capacité barre: " + capacity);
            gameEngine.setProgressBarCapacity(capacity);
        }
        else if (command.equals("RELOAD_HP")) {
            gameEngine.loadConfigFromEJB();
        } else if (command.startsWith("SET_HP:")) {
            // Format: SET_HP:PIECE_TYPE:HP
            String[] parts = command.split(":");
            if (parts.length == 3) {
                String pieceType = parts[1];
                int hp = Integer.parseInt(parts[2]);
                gameEngine.setPieceHPInEJB(pieceType, hp);
                gameEngine.loadConfigFromEJB(); // recharge la config pour tous
                gameEngine.broadcastState();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}