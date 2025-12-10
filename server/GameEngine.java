package server;
import java.util.*;
import common.Paddle;
import common.Ball;
import common.GameConfig;

public class GameEngine {
    public Paddle topPaddle = new Paddle(
        (GameConfig.WINDOW_WIDTH - GameConfig.PADDLE_WIDTH) / 2,
        GameConfig.GAME_AREA_MIN_Y + 2 * 60 + 25 // même calcul que le client
    );
    public Paddle bottomPaddle = new Paddle(
        (GameConfig.WINDOW_WIDTH - GameConfig.PADDLE_WIDTH) / 2,
        GameConfig.GAME_AREA_MAX_Y - 120 // même calcul que le client
    );
    public Ball ball;
    public List<ClientHandler> clients = new ArrayList<>();
    public Map<String, Player> players = new HashMap<>();
    private int boardCols = 8;
    
    private boolean gameRunning = false;
    private Thread gameLoopThread;

    public GameEngine() {
        // Initialiser la balle
        ball = new Ball(
            GameConfig.BALL_START_X, 
            GameConfig.BALL_START_Y, 
            GameConfig.BALL_RADIUS,
            GameConfig.GAME_AREA_MIN_X,
            GameConfig.GAME_AREA_MAX_X,
            GameConfig.GAME_AREA_MIN_Y,
            GameConfig.GAME_AREA_MAX_Y
        );
    }

    // === GESTION DES JOUEURS ===
    public synchronized void addPlayer(String side, ClientHandler handler) {
        String id = "PLAYER_" + side + "_" + System.currentTimeMillis();
        Player player = new Player(id, side, handler);
        players.put(side, player);
        System.out.println("✓ Joueur ajouté: " + player);
        
        // Démarrer la partie si 2 joueurs
        if (players.size() == 2 && !gameRunning) {
            startGame();
        }
    }

    public Player getPlayer(String side) {
        return players.get(side);
    }

    public synchronized void removePlayer(String side) {
        Player removed = players.remove(side);
        if (removed != null) {
            System.out.println("✗ Joueur retiré: " + removed.getName());
        }
        
        // Arrêter la partie si moins de 2 joueurs
        if (players.size() < 2) {
            stopGame();
        }
    }

    // === GAME LOOP ===
    public void startGame() {
        if (gameRunning) return;
        
        gameRunning = true;
        System.out.println("🎮 Partie démarrée !");
        
        gameLoopThread = new Thread(() -> {
            while (gameRunning) {
                try {
                    updateGame();
                    Thread.sleep(GameConfig.TICK_DELAY);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        gameLoopThread.start();
    }

    public void stopGame() {
        gameRunning = false;
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
        }
        System.out.println("Partie arretee");
    }

    private synchronized void updateGame() {
        // Mettre à jour la balle
        // Mettre à jour la balle
        ball.update();

    System.out.println("Serveur - topPaddle X: " + topPaddle.getX() + ", Y: " + topPaddle.getY()
        + " | bottomPaddle X: " + bottomPaddle.getX() + ", Y: " + bottomPaddle.getY());
        
        // Vérifier collision avec les paddles
        ball.bounceOnPaddle(topPaddle.getX(), topPaddle.getY(), topPaddle.getWidth(), topPaddle.getHeight());

        // BOTTOM paddle : utilise la vraie position Y
        ball.bounceOnPaddle(bottomPaddle.getX(), bottomPaddle.getY(), bottomPaddle.getWidth(), bottomPaddle.getHeight());
        
        // Vérifier si la balle est sortie
        if (ball.isOutTop()) {
            System.out.println("Balle sortie en haut ! Point pour BOTTOM");
            Player bottomPlayer = getPlayer("RIGHT");
            if (bottomPlayer != null) {
                bottomPlayer.addScore(1);
            }
            resetBall();
        } else if (ball.isOutBottom()) {
            System.out.println("Balle sortie en bas ! Point pour TOP");
            Player topPlayer = getPlayer("LEFT");
            if (topPlayer != null) {
                topPlayer.addScore(1);
            }
            resetBall();
        }
        
        // Diffuser l'état
        broadcastState();
    }

    private void resetBall() {
        ball.reset(GameConfig.BALL_START_X, GameConfig.BALL_START_Y);
    }

    // === MOUVEMENT DES RAQUETTES ===
    public synchronized void movePaddle(String side, String direction) {
        Paddle paddle = side.equals("LEFT") ? topPaddle : bottomPaddle;
        if (direction.equals("LEFT")) {
            paddle.moveLeft(GameConfig.NORMAL_SPEED);
        } else if (direction.equals("RIGHT")) {
            paddle.moveRight(GameConfig.NORMAL_SPEED);
        }
        broadcastState();
    }

    // === COLONNES ===
    public synchronized void setBoardCols(int cols) {
        this.boardCols = cols;
        broadcastCols();
    }

    public synchronized void broadcastCols() {
        String message = "COLS:" + boardCols;
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // === ÉTAT DU JEU ===
    public synchronized void broadcastState() {
        String state = getGameState();
        for (ClientHandler client : clients) {
            client.sendMessage(state);
        }
    }

    public String getGameState() {
        return "STATE:" + 
               topPaddle.getX() + "," + 
               bottomPaddle.getX() + "," +
               ball.getX() + "," +
               ball.getY();
    }
}