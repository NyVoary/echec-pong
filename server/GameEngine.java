package server;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import configservice.ConfigServiceRemote;
import common.Paddle;
import common.Ball;
import common.GameConfig;
import common.Echequier;
import common.ChessPiece;
import common.PieceType;

public class GameEngine {
    public Paddle topPaddle;
    public Paddle bottomPaddle;
    public Echequier topBoard;
    public Echequier bottomBoard;
    public Ball ball;
    public List<ClientHandler> clients = new ArrayList<>();
    public Map<String, Player> players = new HashMap<>();
    public Player topPlayer;      // Ajouté
    public Player bottomPlayer;   // Ajouté
    private int boardCols = 8;
    
    private boolean gameRunning = false;
    private Thread gameLoopThread;

    public GameEngine() {
        // Charger la config et les HP depuis l'EJB
        loadConfigFromEJB();

        // Initialiser les paddles et boards AVEC la bonne config
        topPaddle = new Paddle(
            GameConfig.BOARD_X + (GameConfig.CELL_SIZE * 2), // ou la formule que tu veux
            GameConfig.TOP_BOARD_Y + (2 * GameConfig.CELL_SIZE) + 25,
            GameConfig.PADDLE_WIDTH,
            GameConfig.PADDLE_HEIGHT
        );
        bottomPaddle = new Paddle(
            GameConfig.BOARD_X + (GameConfig.CELL_SIZE * 2),
            GameConfig.BOTTOM_BOARD_Y - 30,
            GameConfig.PADDLE_WIDTH,
            GameConfig.PADDLE_HEIGHT
        );
        topBoard = new Echequier(
            GameConfig.BOARD_X,
            GameConfig.TOP_BOARD_Y,
            GameConfig.CELL_SIZE,
            GameConfig.CELL_SIZE,
            8
        );
        bottomBoard = new Echequier(
            GameConfig.BOARD_X,
            GameConfig.BOTTOM_BOARD_Y,
            GameConfig.CELL_SIZE,
            GameConfig.CELL_SIZE,
            8
        );
        ball = new Ball(
            GameConfig.BALL_START_X, 
            GameConfig.BALL_START_Y, 
            GameConfig.BALL_RADIUS,
            GameConfig.GAME_AREA_MIN_X,
            GameConfig.GAME_AREA_MAX_X,
            GameConfig.GAME_AREA_MIN_Y,
            GameConfig.GAME_AREA_MAX_Y
        );
        // Utilise les objets Player pour initialiser les pièces
        topBoard.initializeDefaultPieces(false, topPlayer);
        bottomBoard.initializeDefaultPieces(true, bottomPlayer);
    }

    public void loadConfigFromEJB() { //alea 1
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
            Context ctx = new InitialContext(props);

            ConfigServiceRemote configService = (ConfigServiceRemote) ctx.lookup(
                "ejb:/configservice//ConfigServiceBean!configservice.ConfigServiceRemote"
            );

            Map<String, String> config = configService.getGameConfig();
            Map<String, Integer> hpMap = configService.getPieceHP();

            // Affiche toutes les configs récupérées
            System.out.println("=== CONFIGURATION JEU (depuis EJB) ===");
            for (Map.Entry<String, String> entry : config.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            System.out.println("=== HP DES PIECES (depuis EJB) ===");
            for (Map.Entry<String, Integer> entry : hpMap.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            System.out.println("=======================================");

            // Applique la config à GameConfig
            common.GameConfig.NORMAL_SPEED       = Integer.parseInt(config.get("NORMAL_SPEED"));
            common.GameConfig.BOOST_SPEED        = Integer.parseInt(config.get("BOOST_SPEED"));
            common.GameConfig.PADDLE_WIDTH       = Integer.parseInt(config.get("PADDLE_WIDTH"));
            common.GameConfig.PADDLE_HEIGHT      = Integer.parseInt(config.get("PADDLE_HEIGHT"));
            common.GameConfig.BALL_RADIUS        = Integer.parseInt(config.get("BALL_RADIUS"));
            common.GameConfig.BALL_INITIAL_SPEED = Double.parseDouble(config.get("BALL_INITIAL_SPEED"));
            common.GameConfig.BALL_START_X       = Integer.parseInt(config.get("BALL_START_X"));
            common.GameConfig.BALL_START_Y       = Integer.parseInt(config.get("BALL_START_Y"));
            common.GameConfig.WINDOW_WIDTH       = Integer.parseInt(config.get("WINDOW_WIDTH"));
            common.GameConfig.WINDOW_HEIGHT      = Integer.parseInt(config.get("WINDOW_HEIGHT"));
            common.GameConfig.GAME_AREA_MIN_X    = Integer.parseInt(config.get("GAME_AREA_MIN_X"));
            common.GameConfig.GAME_AREA_MAX_X    = Integer.parseInt(config.get("GAME_AREA_MAX_X"));
            common.GameConfig.GAME_AREA_MIN_Y    = Integer.parseInt(config.get("GAME_AREA_MIN_Y"));
            common.GameConfig.GAME_AREA_MAX_Y    = Integer.parseInt(config.get("GAME_AREA_MAX_Y"));
            common.GameConfig.DEFAULT_HOST       = config.get("DEFAULT_HOST");
            common.GameConfig.DEFAULT_PORT       = Integer.parseInt(config.get("DEFAULT_PORT"));
            common.GameConfig.TICK_RATE          = Integer.parseInt(config.get("TICK_RATE"));
            common.GameConfig.TICK_DELAY         = Integer.parseInt(config.get("TICK_DELAY"));
            common.GameConfig.BOARD_X            = Integer.parseInt(config.get("BOARD_X"));
            common.GameConfig.TOP_BOARD_Y        = Integer.parseInt(config.get("TOP_BOARD_Y"));
            common.GameConfig.BOTTOM_BOARD_Y     = Integer.parseInt(config.get("BOTTOM_BOARD_Y"));
            common.GameConfig.CELL_SIZE          = Integer.parseInt(config.get("CELL_SIZE"));

            // Applique les HP aux pièces via PieceType
            common.PieceType.applyHPFromMap(hpMap);

            System.out.println("Configuration chargée depuis EJB !");
        } catch (Exception e) {
            System.out.println("Erreur chargement config EJB : " + e.getMessage());
            // Optionnel : fallback sur les fichiers si besoin
        }
    }

    public void setPieceHPInEJB(String pieceType, int hp) {
        try {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
            Context ctx = new InitialContext(props);

            ConfigServiceRemote configService = (ConfigServiceRemote) ctx.lookup(
                "ejb:/configservice//ConfigServiceBean!configservice.ConfigServiceRemote"
            );
            configService.setPieceHP(pieceType, hp);
            System.out.println("HP de " + pieceType + " mis à jour à " + hp + " en base !");
        } catch (Exception e) {
            System.out.println("Erreur setPieceHPInEJB : " + e.getMessage());
        }
    }

    // === GESTION DES JOUEURS ===
    public synchronized void addPlayer(String side, ClientHandler handler) {
        String id = "PLAYER_" + side + "_" + System.currentTimeMillis();
        Player player = new Player(id, side, handler);
        players.put(side, player);
        if (side.equals("LEFT")) {
            topPlayer = player;
        } else if (side.equals("RIGHT")) {
            bottomPlayer = player;
        }
        System.out.println("✓ Joueur ajouté: " + player);
        
        // Démarrer la partie si 2 joueurs
        if (players.size() == 2 && !gameRunning) {
            startGame();
        }
    }

    public Player getPlayer(String side) {
        if (side.equals("LEFT")) return topPlayer;
        if (side.equals("RIGHT")) return bottomPlayer;
        return null;
    }

    public synchronized void removePlayer(String side) {
        Player removed = players.remove(side);
        if (side.equals("LEFT")) topPlayer = null;
        if (side.equals("RIGHT")) bottomPlayer = null;
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
            ball.update();

            System.out.println(
                "Ball: x=" + ball.getX() + ", y=" + ball.getY() +
                " | topBoard: x=" + topBoard.getX() + ", y=" + topBoard.getY() +
                " | bottomBoard: x=" + bottomBoard.getX() + ", y=" + bottomBoard.getY()
            );

            // System.out.println("Serveur - Ball: x=" + ball.getX() + ", y=" + ball.getY());
            // System.out.println("Serveur - topPaddle: x=" + topPaddle.getX() + ", y=" + topPaddle.getY());
            // System.out.println("Serveur - bottomPaddle: x=" + bottomPaddle.getX() + ", y=" + bottomPaddle.getY());

            ball.bounce(topPaddle.getX(), topPaddle.getY(), topPaddle.getWidth(), topPaddle.getHeight());
            ball.bounce(bottomPaddle.getX(), bottomPaddle.getY(), bottomPaddle.getWidth(), bottomPaddle.getHeight());

            topBoard.bounceBallOnPiece(ball);
            bottomBoard.bounceBallOnPiece(ball);
                
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

        Player winner = getWinnerIfKingDead();
        if (winner != null) {
            broadcastGameOver(winner);
            stopGame();
            return;
        }
        
        // Diffuser l'état
        broadcastState();
    }

    private void broadcastGameOver(Player winner) {
    String msg = "GAMEOVER:WINNER:" + winner.getSide();
    for (ClientHandler client : clients) {
        client.sendMessage(msg);
    }
}

    private void resetBall() {
        ball.reset(GameConfig.BALL_START_X, GameConfig.BALL_START_Y);
    }

    // === MOUVEMENT DES RAQUETTES ===
    public synchronized void movePaddle(String side, String direction) {
        Paddle paddle = side.equals("LEFT") ? topPaddle : bottomPaddle;
        int panelWidth = boardCols * GameConfig.CELL_SIZE; // largeur dynamique
        if (direction.equals("LEFT")) {
            paddle.moveLeft(GameConfig.NORMAL_SPEED, panelWidth);
        } else if (direction.equals("RIGHT")) {
            paddle.moveRight(GameConfig.NORMAL_SPEED, panelWidth);
        }
        broadcastState();
    }

    // === COLONNES ===
    public synchronized void setBoardCols(int cols) {
        this.boardCols = cols;

        int panelWidth = cols * GameConfig.CELL_SIZE;
        ball.setLimits(0, panelWidth, GameConfig.GAME_AREA_MIN_Y, GameConfig.GAME_AREA_MAX_Y);

        // ...le reste (paddle, boards, etc.)...
            // Réinitialise les échiquiers avec les bons joueurs
        topBoard.setCols(cols);
        bottomBoard.setCols(cols);
        topBoard.initializeDefaultPieces(false, topPlayer);
        bottomBoard.initializeDefaultPieces(true, bottomPlayer);

        broadcastCols();
        broadcastState();
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

    public Player getWinnerIfKingDead() {
    boolean topKingAlive = false;
    boolean bottomKingAlive = false;

    // Vérifie si le roi du haut est vivant
    for (ChessPiece piece : topBoard.getPieces()) {
        if (piece.getType() == PieceType.KING && piece.isAlive()) {
            topKingAlive = true;
            break;
        }
    }
    // Vérifie si le roi du bas est vivant
    for (ChessPiece piece : bottomBoard.getPieces()) {
        if (piece.getType() == PieceType.KING && piece.isAlive()) {
            bottomKingAlive = true;
            break;
        }
    }

    // Si le roi du haut est mort, le joueur du bas gagne
    if (!topKingAlive && bottomPlayer != null) return bottomPlayer;
    // Si le roi du bas est mort, le joueur du haut gagne
    if (!bottomKingAlive && topPlayer != null) return topPlayer;

    // Aucun gagnant pour l'instant
    return null;
}

public String getGameState() {
    StringBuilder sb = new StringBuilder();
    sb.append("STATE:");
    sb.append(topPaddle.getX()).append(",");
    sb.append(bottomPaddle.getX()).append(",");
    sb.append(ball.getX()).append(",");
    sb.append(ball.getY());

    // Ajoute l'état des pièces
    sb.append(";PIECES:");
    for (ChessPiece piece : topBoard.getPieces()) {
        sb.append(piece.getType().name()).append(",");
        sb.append(piece.getRow()).append(",");
        sb.append(piece.getCol()).append(",");
        sb.append(piece.getCurrentHP()).append(",");
        sb.append(piece.isAlive() ? "1" : "0").append("|");
    }
    for (ChessPiece piece : bottomBoard.getPieces()) {
        sb.append(piece.getType().name()).append(",");
        sb.append(piece.getRow()).append(",");
        sb.append(piece.getCol()).append(",");
        sb.append(piece.getCurrentHP()).append(",");
        sb.append(piece.isAlive() ? "1" : "0").append("|");
    }
    return sb.toString();
}
}