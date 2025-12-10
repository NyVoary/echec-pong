package common;
import java.io.*;
import java.util.*;
public enum PieceType {
    PAWN("Pion", 10, 30),
    ROOK("Tour", 50, 100),
    KNIGHT("Cavalier", 30, 60),
    BISHOP("Fou", 30, 60),
    QUEEN("Reine", 90, 150),
    KING("Roi", 999, 200);
    
    private final String displayName;
    private int value;         // ✨ Plus final - peut être modifié
    private int maxHP;         // ✨ Plus final - peut être modifié
    
    // Valeurs par défaut (pour réinitialiser)
    private final int defaultValue;
    private final int defaultMaxHP;
    
    PieceType(String displayName, int value, int maxHP) {
        this.displayName = displayName;
        this.value = value;
        this.maxHP = maxHP;
        this.defaultValue = value;      // ✨ Sauvegarde les valeurs par défaut
        this.defaultMaxHP = maxHP;
    }
    
    public String getDisplayName() { return displayName; }
    public int getValue() { return value; }
    public int getMaxHP() { return maxHP; }
    
    // ✨ NOUVEAU : Modifier les valeurs
    public void setValue(int value) {
        if (value >= 0) {
            this.value = value;
        }
    }
    
    public void setMaxHP(int maxHP) {
        if (maxHP > 0) {
            this.maxHP = maxHP;
        }
    }
    
    // ✨ NOUVEAU : Réinitialiser aux valeurs par défaut
    public void resetToDefault() {
        this.value = defaultValue;
        this.maxHP = defaultMaxHP;
    }
    
    public int getDefaultValue() { return defaultValue; }
    public int getDefaultMaxHP() { return defaultMaxHP; }

    public static void loadHPFromFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    int hp = Integer.parseInt(parts[1].trim());
                    for (PieceType type : PieceType.values()) {
                        if (type.name().equalsIgnoreCase(name)) {
                            type.setMaxHP(hp);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lecture vie.txt : " + e.getMessage());
        }
    }
    
    // Retourne le nom du fichier image selon le type et la couleur
    public String getImageFileName(boolean isWhite) {
        String prefix = isWhite ? "w_" : "b_";
        switch (this) {
            case PAWN:   return prefix + "pawn.png";
            case ROOK:   return prefix + "rook.png";
            case KNIGHT: return prefix + "knight.png";
            case BISHOP: return prefix + "bishop.png";
            case QUEEN:  return prefix + "queen.png";
            case KING:   return prefix + "king.png";
            default:     return null;
        }
    }
    
    // ✨ NOUVEAU : Afficher toutes les stats
    public static void printAllStats() {
        System.out.println("\n=== STATS DES PIÈCES ===");
        for (PieceType type : PieceType.values()) {
            System.out.printf("%-10s | Points: %3d | PV: %3d\n", 
                type.getDisplayName(), type.getValue(), type.getMaxHP());
        }
        System.out.println("========================\n");
    }
}