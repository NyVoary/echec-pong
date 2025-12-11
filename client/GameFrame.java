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

public class GameFrame extends JFrame {
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public Paddle topPaddle = new Paddle(
        (GameConfig.WINDOW_WIDTH - GameConfig.PADDLE_WIDTH) / 2,
        GameConfig.GAME_AREA_MIN_Y + 2 * 60 + 25,
        GameConfig.PADDLE_WIDTH,
        GameConfig.PADDLE_HEIGHT
    );

public Paddle bottomPaddle = new Paddle(
    (GameConfig.WINDOW_WIDTH - GameConfig.PADDLE_WIDTH) / 2,
    GameConfig.BOTTOM_BOARD_Y - 30,
    GameConfig.PADDLE_WIDTH,
    GameConfig.PADDLE_HEIGHT
);
    
    private String mySide;

    private GamePanel gamePanel;

    // Position et dimensions des échiquiers
    private final int boardX = GameConfig.BOARD_X;
    private final int topBoardY = GameConfig.TOP_BOARD_Y;
    private final int bottomBoardY = GameConfig.BOTTOM_BOARD_Y;
    private final int cellSize = GameConfig.CELL_SIZE;

    private Echequier topBoard = new Echequier(boardX, topBoardY, cellSize, cellSize, 8);
    private Echequier bottomBoard = new Echequier(boardX, bottomBoardY, cellSize, cellSize, 8);

    // Variables pour le formulaire
    private JTextField ipField;
    private JTextField portField;
    private JTextField colsField;
    private JButton connectButton;
    private JButton updateColsButton;
    private JButton vieConfigButton; // Ajoute ce bouton
    private boolean connected = false;

    private int ballX = GameConfig.BALL_START_X;
    private int ballY = GameConfig.BALL_START_Y;

    public GameFrame() {
        setTitle("Échec Pong - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        topBoard.setRowOwners("J2", "J2");
        bottomBoard.setRowOwners("J1", "J1");

        // Utilise BorderLayout pour centrer le panel de jeu
        setLayout(new BorderLayout());
        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        // Taille fixe de la fenêtre
        setSize(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        setupConnectionForm();
        setupKeyListeners();

        // Suppression de l'initialisation locale des pièces
        // topBoard.initializeDefaultPieces(false);
        // bottomBoard.initializeDefaultPieces(true);

        // centerPaddlesAndBall(topBoard.getCols());
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
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        out.println("MOVE:LEFT");
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        out.println("MOVE:RIGHT");
                    }
                } else if (mySide.equals("RIGHT")) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        out.println("MOVE:LEFT");
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
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

    private void processServerMessage(String message) {
        if (message.startsWith("STATE:")) {
            String[] mainParts = message.split(";PIECES:");
            String[] parts = mainParts[0].substring(6).split(",");
            topPaddle.setX(Integer.parseInt(parts[0]));
            bottomPaddle.setX(Integer.parseInt(parts[1]));
            if (parts.length >= 4) {
                ballX = Integer.parseInt(parts[2]);
                ballY = Integer.parseInt(parts[3]);
            }

            // Synchroniser les PV des pièces PAR POSITION ET TYPE
            if (mainParts.length > 1) {
                String[] piecesData = mainParts[1].split("\\|");
                // topBoard
                for (ChessPiece piece : topBoard.getPieces()) {
                    for (String pdata : piecesData) {
                        String[] fields = pdata.split(",");
                        if (fields.length < 5) continue;
                        String type = fields[0];
                        int row = Integer.parseInt(fields[1]);
                        int col = Integer.parseInt(fields[2]);
                        if (piece.getType().name().equals(type) && piece.getRow() == row && piece.getCol() == col) {
                            piece.setCurrentHP(Integer.parseInt(fields[3]));
                            piece.setAlive("1".equals(fields[4]));
                            break;
                        }
                    }
                }
                // bottomBoard
                for (ChessPiece piece : bottomBoard.getPieces()) {
                    for (String pdata : piecesData) {
                        String[] fields = pdata.split(",");
                        if (fields.length < 5) continue;
                        String type = fields[0];
                        int row = Integer.parseInt(fields[1]);
                        int col = Integer.parseInt(fields[2]);
                        if (piece.getType().name().equals(type) && piece.getRow() == row && piece.getCol() == col) {
                            piece.setCurrentHP(Integer.parseInt(fields[3]));
                            piece.setAlive("1".equals(fields[4]));
                            break;
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

            topBoard.initializeDefaultPieces(false);
            bottomBoard.initializeDefaultPieces(true);

            // NE PAS recalculer la largeur ou centrer les paddles ici !
            SwingUtilities.invokeLater(() -> resizeWindow(cols));
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

            System.out.println("Client - topPaddle X: " + topPaddle.getX() + ", Y: " + topPaddle.getY()
                + " | bottomPaddle X: " + bottomPaddle.getX() + ", Y: " + bottomPaddle.getY());
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
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valeur invalide pour " + type.getDisplayName());
                    return;
                }
            }
            // Sauvegarde dans le fichier vie.txt
            try (FileWriter fw = new FileWriter("config/vie.txt")) {
                for (PieceType type : PieceType.values()) {
                    fw.write(type.name() + "=" + type.getMaxHP() + "\n");
                }
                fw.flush();
                JOptionPane.showMessageDialog(this, "Vies enregistrées !");
                // ✨ Correction : utiliser la référence du parent
                if (GameFrame.this.out != null) {
                    GameFrame.this.out.println("RELOAD_HP");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'écriture dans vie.txt");
            }
            dispose();
            GameFrame.this.requestFocus();
        }
    }
}