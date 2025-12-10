package common;

public class GameConfig {
    // === VITESSES ===
    public static final int NORMAL_SPEED = 10;
    public static final int BOOST_SPEED = 20;
    
    // === PADDLE ===
    public static final int PADDLE_WIDTH = 100;
    public static final int PADDLE_HEIGHT = 15;
    
    // === BALLE ===
    public static final int BALL_RADIUS = 8;
    public static final double BALL_INITIAL_SPEED = 3.0;
    public static final int BALL_START_X = 240;  // Centre horizontal
    public static final int BALL_START_Y = 312;  // Centre vertical
    
    // === FENÊTRE ===
    public static final int WINDOW_WIDTH = 480;
    public static final int WINDOW_HEIGHT = 650;
    
    // === ZONE DE JEU ===
    public static final int GAME_AREA_MIN_X = 0;
    public static final int GAME_AREA_MAX_X = 480;
    public static final int GAME_AREA_MIN_Y = 140;  // Après la zone de formulaire
    public static final int GAME_AREA_MAX_Y = 625;
    
    // === RÉSEAU (par défaut) ===
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 5555;
    
    // === GAME LOOP ===
    public static final int TICK_RATE = 60;  // 60 FPS
    public static final int TICK_DELAY = 1000 / TICK_RATE;  // ~16ms
    
    // === ÉCHIQUIER ===
    public static final int BOARD_X = 0;
    public static final int TOP_BOARD_Y = 145;
    public static final int BOTTOM_BOARD_Y = 505;
    public static final int CELL_SIZE = 60;
}