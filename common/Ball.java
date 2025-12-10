package common;

public class Ball {
    private double x;           // Position X (double pour précision)
    private double y;           // Position Y
    private double speedX;      // Vitesse horizontale
    private double speedY;      // Vitesse verticale
    private final int radius;   // Rayon de la balle
    
    // Limites du terrain
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    
    public Ball(double x, double y, int radius, int minX, int maxX, int minY, int maxY) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        
        // Vitesse initiale aléatoire
        this.speedX = 3;
        this.speedY = 3;
    }
    
    // === MOUVEMENT ===
    public void update() {
        // Déplacer la balle
        x += speedX;
        y += speedY;

        System.out.println("Ball position: x=" + x + ", y=" + y);

        
        // Rebond sur les bords gauche et droit
        if (x - radius <= minX) {
            x = minX + radius; // Repositionner
            speedX = -speedX;  // Inverser direction
        } else if (x + radius >= maxX) {
            x = maxX - radius;
            speedX = -speedX;
        }
        
        // Rebond sur les bords haut et bas (temporaire, sera remplacé par paddles)
        if (y - radius <= minY) {
            y = minY + radius;
            speedY = -speedY;
        } else if (y + radius >= maxY) {
            y = maxY - radius;
            speedY = -speedY;
        }
    }

    public void bounce(int rectX, int rectY, int rectWidth, int rectHeight) {
    // Vérifie collision avec le rectangle
    boolean collisionX = x + radius >= rectX && x - radius <= rectX + rectWidth;
    boolean collisionY = y + radius >= rectY && y - radius <= rectY + rectHeight;
    if (collisionX && collisionY) {
        // Détermine le côté du rebond (vertical ou horizontal)
        double overlapLeft = Math.abs((x + radius) - rectX);
        double overlapRight = Math.abs((x - radius) - (rectX + rectWidth));
        double overlapTop = Math.abs((y + radius) - rectY);
        double overlapBottom = Math.abs((y - radius) - (rectY + rectHeight));

        // Rebond vertical si la balle touche plus le haut/bas
        if (overlapTop < overlapLeft && overlapTop < overlapRight && overlapTop < overlapBottom) {
            speedY = -Math.abs(speedY);
            y = rectY - radius;
        } else if (overlapBottom < overlapLeft && overlapBottom < overlapRight && overlapBottom < overlapTop) {
            speedY = Math.abs(speedY);
            y = rectY + rectHeight + radius;
        }
        // Rebond horizontal si la balle touche plus le côté gauche/droit
        else if (overlapLeft < overlapTop && overlapLeft < overlapBottom && overlapLeft < overlapRight) {
            speedX = -Math.abs(speedX);
            x = rectX - radius;
        } else if (overlapRight < overlapTop && overlapRight < overlapBottom && overlapRight < overlapLeft) {
            speedX = Math.abs(speedX);
            x = rectX + rectWidth + radius;
        }
    }
}
    
    // === REBOND SUR PADDLE ===
    // public void bounceOnPaddle(int paddleX, int paddleY, int paddleWidth, int paddleHeight) {
    //     // Vérifier collision avec le paddle
    //     if (x + radius >= paddleX && x - radius <= paddleX + paddleWidth) {
    //         if (Math.abs(y - paddleY) <= radius + paddleHeight / 2) {
    //             // Calculer où la balle a touché le paddle (gauche = -1, centre = 0, droite = 1)
    //             double hitPos = (x - (paddleX + paddleWidth / 2.0)) / (paddleWidth / 2.0);
                
    //             // Ajuster l'angle selon où la balle touche
    //             speedX = hitPos * 5; // Plus on touche sur les côtés, plus l'angle est prononcé
    //             speedY = -speedY;    // Inverser direction verticale
                
    //             // Repositionner pour éviter bug de collision multiple
    //             if (speedY > 0) {
    //                 y = paddleY + paddleHeight / 2 + radius;
    //             } else {
    //                 y = paddleY - paddleHeight / 2 - radius;
    //             }
    //         }
    //     }
    // }
    
    // === VÉRIFIER SI SORTIE ===
    public boolean isOutTop() {
        return y - radius < minY;
    }
    
    public boolean isOutBottom() {
        return y + radius > maxY;
    }
    
    // === RÉINITIALISER ===
    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        // Vitesse aléatoire
        this.speedX = (Math.random() > 0.5 ? 1 : -1) * 3;
        this.speedY = (Math.random() > 0.5 ? 1 : -1) * 3;
    }
    
    // === GETTERS ===
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getRadius() { return radius; }
    public double getSpeedX() { return speedX; }
    public double getSpeedY() { return speedY; }
    
    // === SETTERS ===
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setSpeedX(double speedX) { this.speedX = speedX; }
    public void setSpeedY(double speedY) { this.speedY = speedY; }
    
    @Override
    public String toString() {
        return String.format("Ball{x=%.1f, y=%.1f, vx=%.1f, vy=%.1f}", x, y, speedX, speedY);
    }
}