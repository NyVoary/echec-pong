package common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List; // Ajoute cet import

import javax.imageio.ImageIO;

import server.Player;

public class Echequier {
    private int x, y, cellWidth, cellHeight;
    private int cols;
    public static final int ROWS = 2;
    private String[] rowOwners = {"J2", "J2"};

    // Liste des pièces sur l'échiquier
    private List<ChessPiece> pieces = new ArrayList<>();

    private Carre[][] cases;

    public Echequier(int x, int y, int cellWidth, int cellHeight, int cols) {
        this.x = x;
        this.y = y;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.cols = cols;
        this.cases = new Carre[ROWS][cols];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                int carreX = x + col * cellWidth;
                int carreY = y + row * cellHeight;
                cases[row][col] = new Carre(row, col, carreX, carreY);
            }
        }
    }

    public List<ChessPiece> getPieces() {
    return pieces;
}

    public int getX() { return x; }
    public int getY() { return y; }

    public void setRowOwners(String ownerTop, String ownerBottom) {
        rowOwners[0] = ownerTop;
        rowOwners[1] = ownerBottom;
    }

    public void setCols(int cols) {
        this.cols = cols;
        // Recréer la grille de cases avec les nouvelles colonnes
        this.cases = new Carre[ROWS][cols];
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                int carreX = x + col * cellWidth;
                int carreY = y + row * cellHeight;
                cases[row][col] = new Carre(row, col, carreX, carreY);
            }
        }
    }

    public int getCols() {
        return cols;
    }

    // Ajoute une pièce à l'échiquier
    public void addPiece(ChessPiece piece) {
        pieces.add(piece);
        cases[piece.getRow()][piece.getCol()].setPiece(piece); // Ajoute la pièce dans le Carre
    }

    // Initialise les pièces comme un vrai échiquier (pour 8 colonnes)
    public void initializeDefaultPieces(boolean isWhite, Player joueur) {
        pieces.clear();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                cases[row][col].setPiece(null);
            }
        }
        PieceType[] layout;
        switch (cols) {
            case 8:
                layout = new PieceType[] {
                    PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                    PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
                };
                break;
            case 6:
                layout = new PieceType[] {
                    PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                    PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT
                };
                break;
            case 4:
                layout = new PieceType[] {
                    PieceType.BISHOP, PieceType.KING, PieceType.QUEEN, PieceType.BISHOP
                };
                break;
            case 2:
                layout = new PieceType[] {
                    PieceType.KING, PieceType.QUEEN
                };
                break;
            default:
                layout = new PieceType[cols];
                for (int i = 0; i < cols; i++) layout[i] = PieceType.PAWN;
        }

        System.out.print("Layout (" + cols + " cols): ");
        for (PieceType pt : layout) {
            System.out.print(pt + " ");
        }
        System.out.println();

        if (!isWhite) {
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(layout[col], false, 0, col, joueur));
            }
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(PieceType.PAWN, false, 1, col, joueur));
            }
        } else {
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(PieceType.PAWN, true, 0, col, joueur));
            }
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(layout[col], true, 1, col, joueur));
            }
        }
    }

    // Crée une pièce avec son image
    private ChessPiece createPiece(PieceType type, boolean isWhite, int row, int col, Player joueur) {
        BufferedImage img = null;
        try {
            String path = "pieces/" + type.getImageFileName(isWhite);
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Image manquante: " + type.getImageFileName(isWhite));
        }
        return new ChessPiece(type, img, joueur, row, col, isWhite);
    }

    /**
     * Fait rebondir la balle si elle touche une case contenant une pièce vivante.
     * Retourne true si collision, false sinon.
     */
    public boolean bounceBallOnPiece(Ball ball) {
        boolean collision = false;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                Carre carre = cases[row][col];
                System.out.println("Case (" + row + "," + col + ") hasPiece=" + carre.hasPiece() +
                    " | caseX=" + carre.getX() + ", caseY=" + carre.getY() +
                    " | Ball: x=" + ball.getX() + ", y=" + ball.getY());
                if (!carre.hasPiece()) continue;

                int caseX = carre.getX();
                int caseY = carre.getY();
                int ballX = ball.getX();
                int ballY = ball.getY();
                int r = ball.getRadius();

                if (ballX + r >= caseX && ballX - r <= caseX + cellWidth &&
                    ballY + r >= caseY && ballY - r <= caseY + cellHeight) {
                    ball.bounce(caseX, caseY, cellWidth, cellHeight);
                    ChessPiece piece = carre.getPiece();
                    piece.takeDamage(1); // Par exemple, 10 PV par rebond
                    collision = true;
                }
            }
        }
        return collision;
    }

    public void draw(Graphics g) {
        // Dessiner les cases avec nouvelles couleurs
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                if ((row + col) % 2 == 0) {
                    // Cases claires: beige doré
                    g.setColor(new Color(240, 217, 181));
                } else {
                    // Cases foncées: marron chocolat
                    g.setColor(new Color(181, 136, 99));
                }
                g.fillRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
            }
        }
        // Dessiner les pièces
        synchronized (pieces) {
            for (ChessPiece piece : pieces) {
                if (piece.isAlive()) {
                    int px = x + piece.getCol() * cellWidth;
                    int py = y + piece.getRow() * cellHeight;
                    if (piece.getImage() != null) {
                        g.drawImage(piece.getImage(), px + 5, py + 5, cellWidth - 10, cellHeight - 10, null);
                    } else {
                        g.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
                        g.fillOval(px + 10, py + 10, cellWidth - 20, cellHeight - 20);
                    }
                    // PV sous la pièce
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, 12));
                    String hpText = String.valueOf(piece.getCurrentHP());
                    int textWidth = g.getFontMetrics().stringWidth(hpText);
                    g.drawString(hpText, px + (cellWidth - textWidth) / 2, py + cellHeight - 5);
                }
            }
        }
    }
}