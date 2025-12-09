package client;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import common.Paddle;
import common.Echequier;

public class GameFrame extends JFrame {
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private Paddle leftPaddle = new Paddle(50, 250);
    private Paddle rightPaddle = new Paddle(750, 250);
    private String mySide;

    private GamePanel gamePanel;

    public GameFrame() {
        setTitle("Chess Pong - Raquette Temps Réel");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectToServer();

        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);

        setupKeyListeners();

        startReceivingThread();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 5555);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String sideMessage = in.readLine();
            if ("FULL".equals(sideMessage)) {
                JOptionPane.showMessageDialog(this, "Serveur plein (2 joueurs max)");
                System.exit(1);
            }
            if (sideMessage.startsWith("SIDE:")) {
                mySide = sideMessage.substring(5);
                System.out.println("Je suis du côté: " + mySide);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion au serveur");
            System.exit(1);
        }
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (mySide == null) return;
                if (mySide.equals("LEFT")) {
                    if (e.getKeyCode() == KeyEvent.VK_Z) {
                        out.println("MOVE:UP");
                    } else if (e.getKeyCode() == KeyEvent.VK_S) {
                        out.println("MOVE:DOWN");
                    }
                } else if (mySide.equals("RIGHT")) {
                    if (e.getKeyCode() == KeyEvent.VK_UP) {
                        out.println("MOVE:UP");
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        out.println("MOVE:DOWN");
                    }
                }
            }
        });
        setFocusable(true);
        requestFocus();
    }

    private void startReceivingThread() {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    processServerMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Déconnecté du serveur");
            }
        }).start();
    }

    private void processServerMessage(String message) {
        if (message.startsWith("STATE:")) {
            String[] parts = message.substring(6).split(",");
            leftPaddle.setY(Integer.parseInt(parts[0]));
            rightPaddle.setY(Integer.parseInt(parts[1]));
            gamePanel.repaint();
        }
    }

    class GamePanel extends JPanel {
        private final int panelWidth = 700;
        private final int panelHeight = 400;
        private final int boardY = 40;
        private final int boardSpacing = 40;
        private final int boardWidth = 8 * 40;
        private final int boardHeight = 2 * 40;

        private Echequier leftBoard = new Echequier(40, boardY, 40, 40);
        private Echequier rightBoard = new Echequier(40 + boardWidth + boardSpacing, boardY, 40, 40);

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(panelWidth, panelHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Fond blanc
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Dessiner les échiquiers
            leftBoard.draw(g);
            rightBoard.draw(g);

            // Espace blanc entre les deux échiquiers (déjà blanc, donc rien à dessiner)

            // Dessiner les raquettes sous chaque échiquier
            g.setColor(Color.BLUE);
            g.fillRect(leftPaddle.getX(), leftPaddle.getY(), leftPaddle.getWidth(), leftPaddle.getHeight());
            g.setColor(Color.RED);
            g.fillRect(rightPaddle.getX(), rightPaddle.getY(), rightPaddle.getWidth(), rightPaddle.getHeight());

            // Infos joueur
            g.setColor(Color.BLACK);
            g.drawString("Vous contrôlez: " + mySide + " (Z/S ou ↑/↓)", 20, 20);
            g.drawString("Raquette Gauche Y: " + leftPaddle.getY(), 20, 35);
            g.drawString("Raquette Droite Y: " + rightPaddle.getY(), 20, 50);
        }
    }
}