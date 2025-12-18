package client;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import common.Paddle;
import common.Echequier;
import common.GameConfig;
import common.ChessPiece;
import common.PieceType;
import java.util.Properties;
import java.util.*;
import javax.swing.Timer;

public class GameFrame extends JFrame {
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public Paddle topPaddle;
    public Paddle bottomPaddle;

    private String mySide;

    private GamePanel gamePanel;

    // Position et dimensions des échiquiers
    private int boardX;
    private int topBoardY;
    private int bottomBoardY;
    private int cellSize;

    private Echequier topBoard;
    private Echequier bottomBoard;

    // Variables pour le formulaire
    private JTextField ipField;
    private JTextField portField;
    private JTextField colsField;
    private JButton connectButton;
    private JButton updateColsButton;
    private JButton vieConfigButton; // Ajoute ce bouton
    private boolean connected = false;

    private int ballX;
    private int ballY;


    private boolean localMode = false;
    private PrintWriter outLeft, outRight;
    private BufferedReader inLeft, inRight;
    private Socket socketLeft, socketRight;

    private Set<Integer> pressedKeys = new HashSet<>();
private Timer localKeyTimer;
    
private int progressBar = 0;
private int progressBarMax = 10;
private boolean superShotActive = false;
private int superShotLeft = 0;
private int superShotDamage = 3; // Ajoute cette ligne avec la valeur par défaut
    public GameFrame() {
        setTitle("Échec Pong - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Taille par défaut (avant d'avoir la vraie config)
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        setupConnectionForm();
        setupKeyListeners();

        // Initialisation temporaire (sera écrasée après réception de la config)
        boardX = 0;
        topBoardY = 0;
        bottomBoardY = 0;
        cellSize = 60;
        topBoard = new Echequier(boardX, topBoardY, cellSize, cellSize, 8);
        bottomBoard = new Echequier(boardX, bottomBoardY, cellSize, cellSize, 8);
        topBoard.setRowOwners("J2", "J2");
        bottomBoard.setRowOwners("J1", "J1");

        // Paddles et balles temporaires (seront recréés après config)
        topPaddle = new Paddle(0, 0, 100, 15);
        bottomPaddle = new Paddle(0, 0, 100, 15);
        ballX = 0;
        ballY = 0;
    }


    private void connectLocalPlayers() {
    try {
        String host = "localhost";
        int port = Integer.parseInt(portField.getText().trim());

        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String sideMsg = in.readLine();
        if (!sideMsg.startsWith("SIDE:")) throw new IOException("Serveur plein ou erreur");
        mySide = sideMsg.substring(5); // Peut-être "LEFT" ou "RIGHT", mais on s'en fiche ici

        // === AJOUT ICI ===
        out.println("LOCAL_MODE");

        // Thread de réception unique
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    processServerMessage(message);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Connexion perdue avec le serveur");
                });
            }
        }).start();

        connected = true;
        connectButton.setEnabled(false);
        ipField.setEnabled(false);
        portField.setEnabled(false);
        connectButton.setText("Connecté ✓");

        requestFocusInWindow();

        JOptionPane.showMessageDialog(this, "Mode local activé : 2 joueurs sur ce PC !\nFlèches = J1, S/D = J2");

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Erreur connexion mode local : " + ex.getMessage());
    }
}

    private void setupConnectionForm() {
        ipField = new JTextField("localhost", 15);
        portField = new JTextField("5555", 5);
        colsField = new JTextField("8", 5);
        connectButton = new JButton("Se connecter");
        updateColsButton = new JButton("Mettre a jour");
        vieConfigButton = new JButton("Configurer les vies");
        vieConfigButton.setBounds(400, 25, 180, 25);
        gamePanel.add(vieConfigButton);

        ipField.setBounds(10, 25, 150, 25);
        portField.setBounds(10, 55, 150, 25);
        connectButton.setBounds(10, 85, 150, 25);

        colsField.setBounds(200, 25, 100, 25);
        updateColsButton.setBounds(200, 55, 150, 25);

        gamePanel.setLayout(null);
        gamePanel.add(ipField);
        gamePanel.add(portField);
        gamePanel.add(connectButton);
        gamePanel.add(colsField);
        gamePanel.add(updateColsButton);
        gamePanel.add(vieConfigButton);

        connectButton.addActionListener(e -> connectToServer());
        updateColsButton.addActionListener(e -> updateColumns());
        vieConfigButton.addActionListener(e -> openVieConfigDialog());

        ActionListener connectAction = e -> connectToServer();
        ipField.addActionListener(connectAction);
        portField.addActionListener(connectAction);

        colsField.addActionListener(e -> updateColumns());

        JCheckBox localModeBox = new JCheckBox("Mode local (2 joueurs sur ce PC)");
        localModeBox.setBounds(10, 115, 250, 25);
        gamePanel.add(localModeBox);

        localModeBox.addActionListener(e -> {
            localMode = localModeBox.isSelected();
            if (localMode) {
                ipField.setEnabled(false);
                portField.setEnabled(false);
                connectButton.setEnabled(false);
                connectLocalPlayers();
            } else {
                ipField.setEnabled(true);
                portField.setEnabled(true);
                connectButton.setEnabled(true);
            }
        });
    }

    private void openVieConfigDialog() {
        VieConfigDialog dialog = new VieConfigDialog(this);
        dialog.setVisible(true);
    }

    // ✨ Centre les raquettes et la balle dans le panel de jeu
    private void centerPaddlesAndBall(int cols) {
        int boardWidth = cols * cellSize;
        int paddleWidth = boardWidth / 2; // ou ce que tu veux

        topPaddle.setWidth(paddleWidth);
        bottomPaddle.setWidth(paddleWidth);

        topPaddle.setX((boardWidth - paddleWidth) / 2);
        bottomPaddle.setX((boardWidth - paddleWidth) / 2);

        ballX = boardWidth / 2;
    }

    private void updateColumns() {
        String colsText = colsField.getText().trim();
        try {
            int cols = Integer.parseInt(colsText);
            if (cols < 2 || cols > 8) {
                JOptionPane.showMessageDialog(this, "Le nombre de colonnes doit être entre 2 et 8 !");
                requestFocus();
                return;
            }
            if (cols % 2 != 0) {
                JOptionPane.showMessageDialog(this, "Le nombre de colonnes doit être pair (2, 4, 6, 8) !");
                requestFocus();
                return;
            }

            topBoard.setCols(cols);
            bottomBoard.setCols(cols);

            // topBoard.initializeDefaultPieces(false);
            // bottomBoard.initializeDefaultPieces(true);

            resizeWindow(cols);

            if (connected && out != null) {
                out.println("COLS:" + cols);
            }

            gamePanel.repaint();
            requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un nombre valide !");
            requestFocus();
        }
    }

    // ✨ Redimensionner la fenêtre selon le nombre de colonnes
    private void resizeWindow(int cols) {
        int boardWidth = cols * cellSize;
        gamePanel.setPreferredSize(new Dimension(boardWidth, GameConfig.WINDOW_HEIGHT));
        gamePanel.revalidate();
        gamePanel.repaint();
        // NE PAS changer la taille de la fenêtre ici !
        System.out.println("Panel redimensionné : " + boardWidth + "x" + GameConfig.WINDOW_HEIGHT);
    }

    private void connectToServer() {
        if (connected) {
            JOptionPane.showMessageDialog(this, "Déjà connecté !");
            return;
        }

        String host = ipField.getText().trim();
        String portText = portField.getText().trim();

        if (host.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer une adresse IP");
            return;
        }

        try {
            int port = Integer.parseInt(portText);

            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String sideMessage = in.readLine();
            if ("FULL".equals(sideMessage)) {
                JOptionPane.showMessageDialog(this, "Serveur plein (2 joueurs max)");
                socket.close();
                return;
            }
            if (sideMessage.startsWith("SIDE:")) {
                mySide = sideMessage.substring(5);
                System.out.println("Je suis du côté: " + mySide);
            }

            connected = true;
            connectButton.setEnabled(false);
            ipField.setEnabled(false);
            portField.setEnabled(false);
            connectButton.setText("Connecté ✓");

            JOptionPane.showMessageDialog(this,
                "Connecté au serveur !\nVous êtes: " + (mySide.equals("LEFT") ? "TOP (J2)" : "BOTTOM (J1)"));

            out.println("COLS:" + topBoard.getCols());
            requestFocus();
            startReceivingThread();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Port invalide ! Utilisez un nombre (ex: 5555)");
        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(this, "Adresse IP invalide : " + host);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Erreur de connexion au serveur\n" + host + ":" + portText +
                "\n\nVérifiez que le serveur est démarré.");
        }
    }

private void setupKeyListeners() {
    // Utilise un Set pour suivre les touches pressées
    addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            pressedKeys.add(e.getKeyCode());
        }
        @Override
        public void keyReleased(KeyEvent e) {
            pressedKeys.remove(e.getKeyCode());
        }
    });
    setFocusable(true);

    // Timer pour envoyer les commandes tant que les touches sont pressées
    localKeyTimer = new Timer(30, evt -> {
        if (localMode) {
            // Joueur 1 (flèches) → LEFT
            if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
                if (out != null) out.println("MOVE:LEFT:LEFT"); // MOVE:<side>:<direction>
            }
            if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                if (out != null) out.println("MOVE:LEFT:RIGHT");
            }
            // Joueur 2 (S/D) → RIGHT
            if (pressedKeys.contains(KeyEvent.VK_S)) {
                if (out != null) out.println("MOVE:RIGHT:LEFT");
            }
            if (pressedKeys.contains(KeyEvent.VK_D)) {
                if (out != null) out.println("MOVE:RIGHT:RIGHT");
            }
        } else {
            if (!connected || mySide == null) return;
            if (mySide.equals("LEFT")) {
                if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
                    out.println("MOVE:LEFT");
                }
                if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
                    out.println("MOVE:RIGHT");
                }
            } else if (mySide.equals("RIGHT")) {
                if (pressedKeys.contains(KeyEvent.VK_S)) {
                    out.println("MOVE:LEFT");
                }
                if (pressedKeys.contains(KeyEvent.VK_D)) {
                    out.println("MOVE:RIGHT");
                }
            }
        }
    });
    localKeyTimer.start();
}

    private void startReceivingThread() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    processServerMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.out.println("Déconnecté du serveur");
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Connexion perdue avec le serveur");
                        connected = false;
                        connectButton.setEnabled(true);
                        ipField.setEnabled(true);
                        portField.setEnabled(true);
                        connectButton.setText("Se connecter");
                    });
                }
            }
        }).start();
    }

    private void recreateComponentsWithConfig() {
        // Met à jour les variables de position/dimension
        boardX = GameConfig.BOARD_X;
        topBoardY = GameConfig.TOP_BOARD_Y;
        bottomBoardY = GameConfig.BOTTOM_BOARD_Y;
        cellSize = GameConfig.CELL_SIZE;

        // Recrée paddles
        topPaddle = new Paddle(
            (GameConfig.WINDOW_WIDTH - GameConfig.PADDLE_WIDTH) / 2,
            GameConfig.GAME_AREA_MIN_Y + 2 * 60 + 25,
            GameConfig.PADDLE_WIDTH,
            GameConfig.PADDLE_HEIGHT
        );
        bottomPaddle = new Paddle(
            (GameConfig.WINDOW_WIDTH - GameConfig.PADDLE_WIDTH) / 2,
            GameConfig.BOTTOM_BOARD_Y - 30,
            GameConfig.PADDLE_WIDTH,
            GameConfig.PADDLE_HEIGHT
        );
        // Recrée boards
        topBoard = new Echequier(GameConfig.BOARD_X, GameConfig.TOP_BOARD_Y, GameConfig.CELL_SIZE, GameConfig.CELL_SIZE, 8);
        bottomBoard = new Echequier(GameConfig.BOARD_X, GameConfig.BOTTOM_BOARD_Y, GameConfig.CELL_SIZE, GameConfig.CELL_SIZE, 8);
        topBoard.setRowOwners("J2", "J2");
        bottomBoard.setRowOwners("J1", "J1");

        // Met à jour la balle
        ballX = GameConfig.BALL_START_X;
        ballY = GameConfig.BALL_START_Y;

        // Redimensionne la fenêtre
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        gamePanel.revalidate();
        gamePanel.repaint();
    }

    private void processServerMessage(String message) {
        if (message.startsWith("CONFIG:")) {
            // 1. Appliquer la config reçue
            String[] params = message.substring(7).split(",");
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    String key = kv[0];
                    String value = kv[1];
                    try {
                        switch (key) {
                            case "NORMAL_SPEED": GameConfig.NORMAL_SPEED = Integer.parseInt(value); break;
                            case "BOOST_SPEED": GameConfig.BOOST_SPEED = Integer.parseInt(value); break;
                            case "PADDLE_WIDTH": GameConfig.PADDLE_WIDTH = Integer.parseInt(value); break;
                            case "PADDLE_HEIGHT": GameConfig.PADDLE_HEIGHT = Integer.parseInt(value); break;
                            case "BALL_RADIUS": GameConfig.BALL_RADIUS = Integer.parseInt(value); break;
                            case "BALL_INITIAL_SPEED": GameConfig.BALL_INITIAL_SPEED = Double.parseDouble(value); break;
                            case "BALL_START_X": GameConfig.BALL_START_X = Integer.parseInt(value); break;
                            case "BALL_START_Y": GameConfig.BALL_START_Y = Integer.parseInt(value); break;
                            case "WINDOW_WIDTH": GameConfig.WINDOW_WIDTH = Integer.parseInt(value); break;
                            case "WINDOW_HEIGHT": GameConfig.WINDOW_HEIGHT = Integer.parseInt(value); break;
                            case "GAME_AREA_MIN_X": GameConfig.GAME_AREA_MIN_X = Integer.parseInt(value); break;
                            case "GAME_AREA_MAX_X": GameConfig.GAME_AREA_MAX_X = Integer.parseInt(value); break;
                            case "GAME_AREA_MIN_Y": GameConfig.GAME_AREA_MIN_Y = Integer.parseInt(value); break;
                            case "GAME_AREA_MAX_Y": GameConfig.GAME_AREA_MAX_Y = Integer.parseInt(value); break;
                            case "TICK_RATE": GameConfig.TICK_RATE = Integer.parseInt(value); break;
                            case "TICK_DELAY": GameConfig.TICK_DELAY = Integer.parseInt(value); break;
                            case "BOARD_X": GameConfig.BOARD_X = Integer.parseInt(value); break;
                            case "TOP_BOARD_Y": GameConfig.TOP_BOARD_Y = Integer.parseInt(value); break;
                            case "BOTTOM_BOARD_Y": GameConfig.BOTTOM_BOARD_Y = Integer.parseInt(value); break;
                            case "CELL_SIZE": GameConfig.CELL_SIZE = Integer.parseInt(value); break;
                        }
                    } catch (Exception ignored) {}
                }
            }
            // 2. Recréer les paddles et boards avec la bonne config
            recreateComponentsWithConfig();
            return;
        }

        if (message.startsWith("STATE:")) {
            String[] mainParts = message.split(";PIECES:");
            String[] parts = mainParts[0].substring(6).split(",");
            topPaddle.setX(Integer.parseInt(parts[0]));
            bottomPaddle.setX(Integer.parseInt(parts[1]));
            if (parts.length >= 4) {
                ballX = Integer.parseInt(parts[2]);
                ballY = Integer.parseInt(parts[3]);
            }

            if (mainParts.length > 1) {
                String[] piecesData = mainParts[1].split("\\|");
                int cols = topBoard.getCols();
                int perBoard = 2 * cols;

                // Synchronise sur les deux boards pendant toute la reconstruction
                synchronized (topBoard.getPieces()) {
                    synchronized (bottomBoard.getPieces()) {
                        topBoard.getPieces().clear();
                        bottomBoard.getPieces().clear();

                        for (int i = 0; i < piecesData.length; i++) {
                            String pdata = piecesData[i];
                            String[] fields = pdata.split(",");
                            if (fields.length < 5) continue;
                            String type = fields[0];
                            int row = Integer.parseInt(fields[1]);
                            int col = Integer.parseInt(fields[2]);
                            int hp = Integer.parseInt(fields[3]);
                            boolean alive = "1".equals(fields[4]);
                            PieceType pt = PieceType.valueOf(type);
                            boolean isWhite = (row == 1);

                            ChessPiece piece = new ChessPiece(pt, null, null, row, col, isWhite);
                            piece.setCurrentHP(hp);
                            piece.setAlive(alive);

                            if (i < perBoard) {
                                topBoard.getPieces().add(piece);
                            } else {
                                bottomBoard.getPieces().add(piece);
                            }
                        }
                    }
                }
            }
            gamePanel.repaint();
        } else if (message.startsWith("COLS:")) {
            int cols = Integer.parseInt(message.substring(5));
            topBoard.setCols(cols);
            bottomBoard.setCols(cols);
            colsField.setText(String.valueOf(cols));
            SwingUtilities.invokeLater(() -> resizeWindow(cols));
            gamePanel.repaint();
        }
        else if (message.startsWith("GAMEOVER:WINNER:")) {
            String winnerSide = message.substring("GAMEOVER:WINNER:".length());
            String winnerLabel;
            if ("LEFT".equals(winnerSide)) {
                winnerLabel = "Joueur 2 (haut)";
            } else if ("RIGHT".equals(winnerSide)) {
                winnerLabel = "Joueur 1 (bas)";
            } else {
                winnerLabel = winnerSide;
            }

            String msg;
            if (mySide != null && mySide.equals(winnerSide)) {
                msg = "Félicitations ! Vous avez gagné !\n(" + winnerLabel + ")";
            } else {
                msg = "Dommage, vous avez perdu.\nLe gagnant est : " + winnerLabel;
            }
            JOptionPane.showMessageDialog(this, "Game Over\n" + msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (message.startsWith("HP:")) {
            String[] parts = message.substring(3).split(",");
            for (String part : parts) {
                if (part.contains("=")) {
                    String[] kv = part.split("=");
                    try {
                        PieceType type = PieceType.valueOf(kv[0]);
                        int hp = Integer.parseInt(kv[1]);
                        type.setMaxHP(hp);
                    } catch (Exception ignored) {}
                }
            }
            // Réinitialise les pièces avec les bons HP max
            // topBoard.initializeDefaultPieces(false, null);
            // bottomBoard.initializeDefaultPieces(true, null);
            gamePanel.repaint();
        }
if (message.contains(";BAR:")) {
    String[] barParts = message.split(";BAR:");
    if (barParts.length > 1) {
        String[] barAndRest = barParts[1].split(";", 2);
        String barValue = barAndRest[0]; // ex: "7/10"
        String[] vals = barValue.split("/");
        if (vals.length == 2) {
            try {
                progressBar = Integer.parseInt(vals[0]);
                progressBarMax = Integer.parseInt(vals[1]);
            } catch (Exception ignored) {}
        }
    }
}
if (message.contains(";SUPER:")) {
    String[] superParts = message.split(";SUPER:");
    if (superParts.length > 1) {
        String[] superAndRest = superParts[1].split(";", 2);
        String[] vals = superAndRest[0].split(",");
        if (vals.length == 2) {
            superShotActive = "1".equals(vals[0]);
            try {
                superShotLeft = Integer.parseInt(vals[1]);
                // Met à jour la variable locale pour le formulaire
                superShotDamage = Math.max(superShotDamage, superShotLeft);
            } catch (Exception ignored) {}
        }
    }
}
    }

    class GamePanel extends JPanel {
        @Override
        public Dimension getPreferredSize() {
            int dynamicWidth = topBoard.getCols() * cellSize;
            return new Dimension(dynamicWidth, GameConfig.WINDOW_HEIGHT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // System.out.println("Client - topPaddle X: " + topPaddle.getX() + ", Y: " + topPaddle.getY()
            //     + " | bottomPaddle X: " + bottomPaddle.getX() + ", Y: " + bottomPaddle.getY());

//             System.out.println("Client - Ball: x=" + ballX + ", y=" + ballY);
// System.out.println("Client - topPaddle: x=" + topPaddle.getX() + ", y=" + (topBoardY + (2 * cellSize) + 25));
// System.out.println("Client - bottomPaddle: x=" + bottomPaddle.getX() + ", y=" + (bottomBoardY - 30));
            // Fond beige clair
            g.setColor(new Color(245, 245, 220));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Zone d'information en haut
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), 140);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.drawString("Adresse IP:", 10, 15);
            g.drawString("Port:", 10, 40);

            if (connected) {
                g.setColor(new Color(0, 150, 0));
                g.drawString("Connecte - Vous etes: " +
                    (mySide != null && mySide.equals("LEFT") ? "TOP (J2)" : "BOTTOM (J1)"),
                    200, 50);
                g.setColor(Color.BLACK);
                g.drawString("Utilisez LEFT/RIGHT pour bouger", 200, 70);
            } else {
                g.setColor(Color.RED);
                g.drawString("Non connecte", 200, 50);
            }

            g.setColor(Color.GRAY);
            g.drawString("Nombre de colonnes (pair, max 8)", 10, 110);

            // Dessiner les échiquiers
            topBoard.draw(g);
            bottomBoard.draw(g);

            // Dessiner les raquettes horizontales
            int paddleTopY = topBoardY + (2 * cellSize) + 25;
            int paddleBottomY = bottomBoardY - 30;

            g.setColor(Color.BLUE);
            g.fillRect(topPaddle.getX(), paddleTopY, topPaddle.getWidth(), topPaddle.getHeight());

            g.setColor(Color.RED);
            g.fillRect(bottomPaddle.getX(), paddleBottomY, bottomPaddle.getWidth(), bottomPaddle.getHeight());

            // Dessiner la balle
            g.setColor(Color.WHITE);
            g.fillOval(ballX - GameConfig.BALL_RADIUS,
                       ballY - GameConfig.BALL_RADIUS,
                       GameConfig.BALL_RADIUS * 2,
                       GameConfig.BALL_RADIUS * 2);

            g.setColor(Color.BLACK);
            g.drawOval(ballX - GameConfig.BALL_RADIUS,
                       ballY - GameConfig.BALL_RADIUS,
                       GameConfig.BALL_RADIUS * 2,
                       GameConfig.BALL_RADIUS * 2);

            // Affichage de la barre de progression dans l'espace blanc en haut à droite
            int barWidth = 180;
            int barHeight = 18;
            int barX = getWidth() - barWidth - 30; // Décalé à droite
            int barY = 20; // Dans la zone blanche

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(barX, barY, barWidth, barHeight);

            int filled = (int) ((progressBar / (double) progressBarMax) * barWidth);
            g.setColor(superShotActive ? Color.ORANGE : new Color(60, 180, 60));
            g.fillRect(barX, barY, filled, barHeight);

            g.setColor(Color.BLACK);
            g.drawRect(barX, barY, barWidth, barHeight);

            String barText = superShotActive
                ? "SUPER TIR ! (" + superShotLeft + " dégats)"
                : "Progression: " + progressBar + " / " + progressBarMax;
            g.setFont(new Font("Arial", Font.BOLD, 13));
            int textWidth = g.getFontMetrics().stringWidth(barText);
            g.drawString(barText, barX + (barWidth - textWidth) / 2, barY + barHeight - 5);
        }
    }

    // === Fenêtre de configuration des vies ===
    class VieConfigDialog extends JDialog {
        private Map<PieceType, JTextField> fields = new HashMap<>();

private JTextField progressBarMaxField;
private JTextField superShotDamageField;

public VieConfigDialog(Frame parent) {
    super(parent, "Configurer les vies et capacités", true);
    setLayout(new GridLayout(PieceType.values().length + 3, 2, 10, 5));
    setSize(350, 300);
    setLocationRelativeTo(parent);

    // Champs pour les pièces
    for (PieceType type : PieceType.values()) {
        add(new JLabel(type.getDisplayName()));
        JTextField tf = new JTextField(String.valueOf(type.getMaxHP()), 5);
        fields.put(type, tf);
        add(tf);
    }

    // Champs pour la barre et le super tir
    add(new JLabel("Barre progression max"));
    progressBarMaxField = new JTextField(String.valueOf(progressBarMax), 5);
    add(progressBarMaxField);

    add(new JLabel("Dégât super tir"));
    superShotDamageField = new JTextField(String.valueOf(superShotDamage), 5);
    add(superShotDamageField);

    JButton saveBtn = new JButton("Enregistrer");
    saveBtn.addActionListener(e -> saveVieConfig());
    add(saveBtn);

    JButton cancelBtn = new JButton("Annuler");
    cancelBtn.addActionListener(e -> dispose());
    add(cancelBtn);
}

        private void saveVieConfig() {
            Properties props = new Properties();
            for (PieceType type : PieceType.values()) {
                try {
                    int hp = Integer.parseInt(fields.get(type).getText().trim());
                    type.setMaxHP(hp);
                    props.setProperty(type.name(), String.valueOf(hp));
                    // Envoie la nouvelle valeur au serveur (EJB)
                    if (GameFrame.this.out != null) {
                        GameFrame.this.out.println("SET_HP:" + type.name() + ":" + hp);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valeur invalide pour " + type.getDisplayName());
                    return;
                }
            }
            // Valeurs pour la barre de progression et le super tir
            try {
                int newMax = Integer.parseInt(progressBarMaxField.getText().trim());
                GameFrame.this.progressBarMax = newMax;
                // Informe le serveur de la nouvelle valeur
                if (GameFrame.this.out != null) {
                    GameFrame.this.out.println("SET_PROGRESS_BAR_MAX:" + newMax);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valeur invalide pour la barre de progression");
                return;
            }
            try {
                int newDamage = Integer.parseInt(superShotDamageField.getText().trim());
                GameFrame.this.superShotDamage = newDamage;
                // Informe le serveur de la nouvelle valeur
                if (GameFrame.this.out != null) {
                    GameFrame.this.out.println("SET_SUPER_SHOT_DAMAGE:" + newDamage);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valeur invalide pour le dégât du super tir");
                return;
            }
                try {
                    int newBarMax = Integer.parseInt(progressBarMaxField.getText().trim());
                    int newSuperDmg = Integer.parseInt(superShotDamageField.getText().trim());
                    if (GameFrame.this.out != null) {
                        GameFrame.this.out.println("SET_CONFIG:PROGRESS_BAR_MAX:" + newBarMax);
                        GameFrame.this.out.println("SET_CONFIG:SUPER_SHOT_DAMAGE:" + newSuperDmg);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valeur invalide pour la barre ou le super tir");
                    return;
                }

            dispose();
            GameFrame.this.requestFocus();
        }
    }
}