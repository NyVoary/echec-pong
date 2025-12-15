package client;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import common.ChessPiece;
import common.Echequier;
import common.GameConfig;
import common.Paddle;
import common.PieceType;

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

    public GameFrame() {
        setTitle("♟ Chess Battle Arena - Multiplayer Edition ♟");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Taille par défaut (avant d'avoir la vraie config) - Élargie pour mieux voir
        setSize(950, 700);
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

    private void setupConnectionForm() {
        ipField = new JTextField("localhost", 15);
        portField = new JTextField("5555", 5);
        colsField = new JTextField("8", 5);
        connectButton = new JButton("Se connecter");
        updateColsButton = new JButton("Mettre a jour");
        vieConfigButton = new JButton("Configurer les vies");
        vieConfigButton.setBounds(400, 25, 180, 25);
        gamePanel.add(vieConfigButton);

        ipField.setBounds(15, 30, 170, 28);
        portField.setBounds(15, 65, 170, 28);
        connectButton.setBounds(15, 100, 170, 32);

        colsField.setBounds(220, 30, 110, 28);
        updateColsButton.setBounds(220, 65, 170, 32);

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
    addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!connected || mySide == null) return;

            if (mySide.equals("LEFT")) {
                // Joueur 1 : touches fléchées
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    out.println("MOVE:LEFT");
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    out.println("MOVE:RIGHT");
                }
            } else if (mySide.equals("RIGHT")) {
                // Joueur 2 : touches S (gauche) et D (droite)
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    out.println("MOVE:LEFT");
                } else if (e.getKeyCode() == KeyEvent.VK_D) {
                    out.println("MOVE:RIGHT");
                }
            }
        }
    });
    setFocusable(true);
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
            String msg;
            if (mySide != null && mySide.equals(winnerSide)) {
                msg = "Félicitations ! Vous avez gagné !";
            } else {
                msg = "Dommage, vous avez perdu.";
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
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            System.out.println("Client - Ball: x=" + ballX + ", y=" + ballY);
System.out.println("Client - topPaddle: x=" + topPaddle.getX() + ", y=" + (topBoardY + (2 * cellSize) + 25));
System.out.println("Client - bottomPaddle: x=" + bottomPaddle.getX() + ", y=" + (bottomBoardY - 30));
            // Fond dégradé moderne (bleu foncé vers violet)
            GradientPaint bgGradient = new GradientPaint(0, 0, new Color(25, 42, 86), 
                                                          0, getHeight(), new Color(58, 27, 82));
            g2d.setPaint(bgGradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Zone d'information en haut avec dégradé cyan/turquoise
            GradientPaint headerGradient = new GradientPaint(0, 0, new Color(0, 150, 199), 
                                                              getWidth(), 0, new Color(0, 199, 176));
            g2d.setPaint(headerGradient);
            g2d.fillRoundRect(5, 5, getWidth() - 10, 145, 20, 20);

            g2d.setColor(new Color(255, 255, 255, 230));
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
            g2d.drawString("🌐 Server IP:", 15, 20);
            g2d.drawString("🔌 Port:", 15, 55);

            if (connected) {
                g2d.setColor(new Color(0, 255, 128));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString("✓ CONNECTED - Player: " +
                    (mySide != null && mySide.equals("LEFT") ? "⬆ TOP (J2)" : "⬇ BOTTOM (J1)"),
                    220, 55);
                g2d.setColor(new Color(255, 255, 100));
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("⌨ Controls: ← LEFT / RIGHT →", 220, 80);
            } else {
                g2d.setColor(new Color(255, 80, 80));
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString("✗ DISCONNECTED", 220, 55);
            }

            g2d.setColor(new Color(220, 220, 255));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2d.drawString("🎮 Board Columns (even, max 8)", 15, 120);

            // Dessiner les échiquiers
            topBoard.draw(g);
            bottomBoard.draw(g);

            // Dessiner les raquettes horizontales avec effet 3D et dégradés
            int paddleTopY = topBoardY + (2 * cellSize) + 25;
            int paddleBottomY = bottomBoardY - 30;

            // Paddle TOP avec dégradé cyan lumineux
            GradientPaint topPaddleGradient = new GradientPaint(
                topPaddle.getX(), paddleTopY, new Color(0, 200, 255),
                topPaddle.getX(), paddleTopY + topPaddle.getHeight(), new Color(0, 120, 255));
            g2d.setPaint(topPaddleGradient);
            g2d.fillRoundRect(topPaddle.getX(), paddleTopY, topPaddle.getWidth(), topPaddle.getHeight(), 12, 12);
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(topPaddle.getX(), paddleTopY, topPaddle.getWidth(), topPaddle.getHeight(), 12, 12);

            // Paddle BOTTOM avec dégradé orange/rouge vif
            GradientPaint bottomPaddleGradient = new GradientPaint(
                bottomPaddle.getX(), paddleBottomY, new Color(255, 100, 50),
                bottomPaddle.getX(), paddleBottomY + bottomPaddle.getHeight(), new Color(255, 50, 100));
            g2d.setPaint(bottomPaddleGradient);
            g2d.fillRoundRect(bottomPaddle.getX(), paddleBottomY, bottomPaddle.getWidth(), bottomPaddle.getHeight(), 12, 12);
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.drawRoundRect(bottomPaddle.getX(), paddleBottomY, bottomPaddle.getWidth(), bottomPaddle.getHeight(), 12, 12);

            // Dessiner la balle avec effet lumineux (glow)
            RadialGradientPaint ballGradient = new RadialGradientPaint(
                ballX, ballY, GameConfig.BALL_RADIUS * 1.5f,
                new float[]{0f, 0.7f, 1f},
                new Color[]{new Color(255, 255, 100), new Color(255, 200, 0), new Color(255, 150, 0, 0)});
            g2d.setPaint(ballGradient);
            g2d.fillOval(ballX - GameConfig.BALL_RADIUS - 3,
                       ballY - GameConfig.BALL_RADIUS - 3,
                       (GameConfig.BALL_RADIUS + 3) * 2,
                       (GameConfig.BALL_RADIUS + 3) * 2);
            g2d.setColor(new Color(255, 255, 200));
            g2d.fillOval(ballX - GameConfig.BALL_RADIUS,
                       ballY - GameConfig.BALL_RADIUS,
                       GameConfig.BALL_RADIUS * 2,
                       GameConfig.BALL_RADIUS * 2);
            g2d.setColor(new Color(255, 200, 0));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(ballX - GameConfig.BALL_RADIUS,
                       ballY - GameConfig.BALL_RADIUS,
                       GameConfig.BALL_RADIUS * 2,
                       GameConfig.BALL_RADIUS * 2);
        }
    }

    // === Fenêtre de configuration des vies ===
    class VieConfigDialog extends JDialog {
        private Map<PieceType, JTextField> fields = new HashMap<>();

        public VieConfigDialog(Frame parent) {
            super(parent, "Configurer les vies des pièces", true);
            setLayout(new GridLayout(PieceType.values().length + 1, 2, 10, 5));
            setSize(350, 250);
            setLocationRelativeTo(parent);

            for (PieceType type : PieceType.values()) {
                add(new JLabel(type.getDisplayName()));
                JTextField tf = new JTextField(String.valueOf(type.getMaxHP()), 5);
                fields.put(type, tf);
                add(tf);
            }

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
            // Optionnel : sauvegarde locale (si tu veux garder un backup)
            try (FileWriter fw = new FileWriter("config/vie.txt")) {
                for (PieceType type : PieceType.values()) {
                    fw.write(type.name() + "=" + type.getMaxHP() + "\n");
                }
                fw.flush();
                JOptionPane.showMessageDialog(this, "Vies enregistrées !");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'écriture dans vie.txt");
            }
            dispose();
            GameFrame.this.requestFocus();
        }
    }
}