package server;

public class Player {
    private String id;              // Identifiant unique
    private String side;            // "LEFT" ou "RIGHT"
    private String name;            // Nom du joueur
    private int score;              // Score actuel
    private boolean ready;          // Prêt à jouer ?
    private ClientHandler handler;  // Connexion réseau
    
    // Statistiques (pour plus tard)
    private int ballsHit;           // Nombre de balles touchées
    private int ballsMissed;        // Nombre de balles manquées
    private int piecesDestroyed;    // Nombre de pièces détruites
    
    public Player(String id, String side, ClientHandler handler) {
        this.id = id;
        this.side = side;
        this.handler = handler;
        this.name = "Joueur " + side;
        this.score = 0;
        this.ready = false;
        this.ballsHit = 0;
        this.ballsMissed = 0;
        this.piecesDestroyed = 0;
    }
    
    // === GETTERS ===
    public String getId() { return id; }
    public String getSide() { return side; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public boolean isReady() { return ready; }
    public ClientHandler getHandler() { return handler; }
    public int getBallsHit() { return ballsHit; }
    public int getBallsMissed() { return ballsMissed; }
    public int getPiecesDestroyed() { return piecesDestroyed; }
    
    // === SETTERS ===
    public void setName(String name) { this.name = name; }
    public void setReady(boolean ready) { this.ready = ready; }
    
    // === SCORE (pour plus tard) ===
    public void addScore(int points) {
        this.score += points;
        System.out.println(name + " a maintenant " + score + " points");
    }
    
    public void resetScore() {
        this.score = 0;
    }
    
    // === STATISTIQUES (pour plus tard) ===
    public void incrementBallsHit() {
        this.ballsHit++;
    }
    
    public void incrementBallsMissed() {
        this.ballsMissed++;
    }
    
    public void incrementPiecesDestroyed() {
        this.piecesDestroyed++;
    }
    
    // === MÉTHODES UTILITAIRES ===
    public double getAccuracy() {
        int total = ballsHit + ballsMissed;
        if (total == 0) return 0.0;
        return (double) ballsHit / total * 100;
    }
    
    public void sendMessage(String message) {
        if (handler != null) {
            handler.sendMessage(message);
        }
    }
    
    public String getStats() {
        return String.format(
            "Joueur: %s | Score: %d | Précision: %.1f%% | Pièces détruites: %d",
            name, score, getAccuracy(), piecesDestroyed
        );
    }
    
    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", side='" + side + '\'' +
                ", name='" + name + '\'' +
                ", score=" + score +
                ", ready=" + ready +
                '}';
    }
}