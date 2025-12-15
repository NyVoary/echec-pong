package common;

public class GameConfig {
    // Valeurs par défaut (peuvent être écrasées par config.txt ou serveur)
    public static int NORMAL_SPEED = 5;
    public static int BOOST_SPEED = 10;
    public static int PADDLE_WIDTH = 200;
    public static int PADDLE_HEIGHT = 15;
    public static int BALL_RADIUS = 8;
    public static double BALL_INITIAL_SPEED = 3.0;
    public static int BALL_START_X = 240;
    public static int BALL_START_Y = 300;
    public static int WINDOW_WIDTH = 480;
    public static int WINDOW_HEIGHT = 600;
    public static int GAME_AREA_MIN_X = 0;
    public static int GAME_AREA_MAX_X = 480;
    public static int GAME_AREA_MIN_Y = 140;
    public static int GAME_AREA_MAX_Y = 600;
    public static String DEFAULT_HOST = "localhost";
    public static int DEFAULT_PORT = 5555;
    public static int TICK_RATE = 60;
    public static int TICK_DELAY = 1000 / 60;
    public static int BOARD_X = 0;
    public static int TOP_BOARD_Y = 140;
    public static int BOTTOM_BOARD_Y = 460;
    public static int CELL_SIZE = 60;
}