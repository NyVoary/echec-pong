package client;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import common.Paddle;
import common.Echequier;
import common.GameConfig;

public class GameFrame extends JFrame {
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private Paddle topPaddle = new Paddle(120, 50);
    private Paddle bottomPaddle = new Paddle(120, 550);
    private String mySide;

    private GamePanel gamePanel;

    // Position et dimensions des échiquiers
    private final int boardX = 0;
    private final int topBoardY = 145;
    private final int bottomBoardY = 505;
    private final int cellSize = 60;
    
    private Echequier topBoard = new Echequier(boardX, topBoardY, cellSize, cellSize, 8);
    private Echequier bottomBoard = new Echequier(boardX, bottomBoardY, cellSize, cellSize, 8);

    // Variables pour le formulaire
    private JTextField ipField;
    private JTextField portField;
    private JTextField colsField; // Nouveau champ pour les colonnes
    private JButton connectButton;
    private JButton updateColsButton; // Nouveau bouton pour mettre à jour
    private boolean connected = false;

    // Ajoute dans les variables de classe
    private int ballX = GameConfig.BALL_START_X;
    private int ballY = GameConfig.BALL_START_Y;

    public GameFrame() {
        setTitle("Échec Pong - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        topBoard.setRowOwners("J2", "J2");
        bottomBoard.setRowOwners("J1", "J1");

        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);

        setupConnectionForm();
        setupKeyListeners();
    }

    private void setupConnectionForm() {
        // Créer les champs de texte
        ipField = new JTextField("localhost", 15);
        portField = new JTextField("5555", 5);
        colsField = new JTextField("8", 5); // Par défaut 8 colonnes
        connectButton = new JButton("Se connecter");
        updateColsButton = new JButton("Mettre a jour");

        // Position des composants dans le GamePanel (côte à côte)
        // Colonne gauche: IP et Port
        ipField.setBounds(10, 25, 150, 25);
        portField.setBounds(10, 55, 150, 25);
        connectButton.setBounds(10, 85, 150, 25);
        
        // Colonne droite: Colonnes
        colsField.setBounds(200, 25, 100, 25);
        updateColsButton.setBounds(200, 55, 150, 25);

        // Ajouter au panel
        gamePanel.setLayout(null);
        gamePanel.add(ipField);
        gamePanel.add(portField);
        gamePanel.add(connectButton);
        gamePanel.add(colsField);
        gamePanel.add(updateColsButton);

        // Action du bouton connexion
        connectButton.addActionListener(e -> connectToServer());

        // Action du bouton mise à jour colonnes
        updateColsButton.addActionListener(e -> updateColumns());

        // Permettre de se connecter avec Enter
        ActionListener connectAction = e -> connectToServer();
        ipField.addActionListener(connectAction);
        portField.addActionListener(connectAction);
        
        // Permettre de mettre à jour avec Enter
        colsField.addActionListener(e -> updateColumns());
    }

    private void updateColumns() {
        String colsText = colsField.getText().trim();
        
        try {
            int cols = Integer.parseInt(colsText);
            
            // Validation: pair et entre 2 et 8
            if (cols < 2 || cols > 8) {
                JOptionPane.showMessageDialog(this, 
                    "Le nombre de colonnes doit être entre 2 et 8 !");
                return;
            }
            
            if (cols % 2 != 0) {
                JOptionPane.showMessageDialog(this, 
                    "Le nombre de colonnes doit être pair (2, 4, 6, 8) !");
                return;
            }
            
            // Mettre à jour les deux échiquiers
            topBoard.setCols(cols);
            bottomBoard.setCols(cols);
            
            // Envoyer au serveur si connecté
            if (connected && out != null) {
                out.println("COLS:" + cols);
            }
            
            gamePanel.repaint();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez entrer un nombre valide !");
        }
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
            
            // Tenter la connexion
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

            // Connexion réussie
            connected = true;
            connectButton.setEnabled(false);
            ipField.setEnabled(false);
            portField.setEnabled(false);
            connectButton.setText("Connecté ✓");
            
            JOptionPane.showMessageDialog(this, 
                "Connecté au serveur !\nVous êtes: " + (mySide.equals("LEFT") ? "TOP (J2)" : "BOTTOM (J1)"));

            // Envoyer le nombre de colonnes actuel
            out.println("COLS:" + topBoard.getCols());

            // Donner le focus à la fenêtre pour les touches
            requestFocus();

            // Démarrer la réception des messages
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
                
                if (mySide.equals("LEFT")) { // TOP
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        out.println("MOVE:LEFT");
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        out.println("MOVE:RIGHT");
                    }
                } else if (mySide.equals("RIGHT")) { // BOTTOM
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

    // Dans processServerMessage()
    private void processServerMessage(String message) {
        if (message.startsWith("STATE:")) {
            String[] parts = message.substring(6).split(",");
            topPaddle.setX(Integer.parseInt(parts[0]));
            bottomPaddle.setX(Integer.parseInt(parts[1]));
            
            // Recevoir position de la balle
            if (parts.length >= 4) {
                ballX = Integer.parseInt(parts[2]);
                ballY = Integer.parseInt(parts[3]);
            }
            
            gamePanel.repaint();
        } else if (message.startsWith("COLS:")) {
            int cols = Integer.parseInt(message.substring(5));
            topBoard.setCols(cols);
            bottomBoard.setCols(cols);
            colsField.setText(String.valueOf(cols));
            gamePanel.repaint();
        }
    }

    class GamePanel extends JPanel {
        private final int panelWidth = GameConfig.WINDOW_WIDTH;
        private final int panelHeight = GameConfig.WINDOW_HEIGHT;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(panelWidth, panelHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
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
            
            // Statut de connexion
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
            
            // Contour noir
            g.setColor(Color.BLACK);
            g.drawOval(ballX - GameConfig.BALL_RADIUS, 
                       ballY - GameConfig.BALL_RADIUS, 
                       GameConfig.BALL_RADIUS * 2, 
                       GameConfig.BALL_RADIUS * 2);
        }
    }
}