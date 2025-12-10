package common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Echequier {
    private int x, y, cellWidth, cellHeight;
    private int cols;
    public static final int ROWS = 2;
    private String[] rowOwners = {"J2", "J2"};

    // Liste des pièces sur l'échiquier
    private List<ChessPiece> pieces = new ArrayList<>();

    public Echequier(int x, int y, int cellWidth, int cellHeight, int cols) {
        this.x = x;
        this.y = y;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.cols = cols;
    }

    public void setRowOwners(String ownerTop, String ownerBottom) {
        rowOwners[0] = ownerTop;
        rowOwners[1] = ownerBottom;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getCols() {
        return cols;
    }

    // Ajoute une pièce à l'échiquier
    public void addPiece(ChessPiece piece) {
        pieces.add(piece);
    }

    // Initialise les pièces comme un vrai échiquier (pour 8 colonnes)
    public void initializeDefaultPieces(boolean isWhite) {
        pieces.clear();
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
            // Haut : pièces majeures ligne 0, pions ligne 1
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(layout[col], false, 0, col));
            }
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(PieceType.PAWN, false, 1, col));
            }
        } else {
            // Bas : pions ligne 0, pièces majeures ligne 1
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(PieceType.PAWN, true, 0, col));
            }
            for (int col = 0; col < cols; col++) {
                addPiece(createPiece(layout[col], true, 1, col));
            }
        }
    }

    // Crée une pièce avec son image
    private ChessPiece createPiece(PieceType type, boolean isWhite, int row, int col) {
        BufferedImage img = null;
        try {
            String path = "pieces/" + type.getImageFileName(isWhite);
            img = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Image manquante: " + type.getImageFileName(isWhite));
        }
        return new ChessPiece(type, img, null, row, col, isWhite);
    }

    public void draw(Graphics g) {
        // Dessiner les cases
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < cols; col++) {
                if ((row + col) % 2 == 0) {
                    g.setColor(new Color(220, 220, 200));
                } else {
                    g.setColor(new Color(150, 50, 50));
                }
                g.fillRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x + col * cellWidth, y + row * cellHeight, cellWidth, cellHeight);
            }
        }
        // Dessiner les pièces
        for (ChessPiece piece : pieces) {
            if (piece.isAlive()) {
                int px = x + piece.getCol() * cellWidth;
                int py = y + piece.getRow() * cellHeight;
                if (piece.getImage() != null) {
                    g.drawImage(piece.getImage(), px + 5, py + 5, cellWidth - 10, cellHeight - 10, null);
                } else {
                    // Fallback si image manquante
                    g.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
                    g.fillOval(px + 10, py + 10, cellWidth - 20, cellHeight - 20);
                }
            }
        }
    }
}