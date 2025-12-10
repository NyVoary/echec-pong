package common;

import java.io.*;
import java.util.*;

public class GameConfig {
    public static int NORMAL_SPEED;
    public static int BOOST_SPEED;
    public static int PADDLE_WIDTH;
    public static int PADDLE_HEIGHT;
    public static int BALL_RADIUS;
    public static double BALL_INITIAL_SPEED;
    public static int BALL_START_X;
    public static int BALL_START_Y;
    public static int WINDOW_WIDTH;
    public static int WINDOW_HEIGHT;
    public static int GAME_AREA_MIN_X;
    public static int GAME_AREA_MAX_X;
    public static int GAME_AREA_MIN_Y;
    public static int GAME_AREA_MAX_Y;
    public static String DEFAULT_HOST;
    public static int DEFAULT_PORT;
    public static int TICK_RATE;
    public static int TICK_DELAY;
    public static int BOARD_X;
    public static int TOP_BOARD_Y;
    public static int BOTTOM_BOARD_Y;
    public static int CELL_SIZE;

    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/config.txt")) {
            props.load(fis);
            NORMAL_SPEED = Integer.parseInt(props.getProperty("NORMAL_SPEED", "10"));
            BOOST_SPEED = Integer.parseInt(props.getProperty("BOOST_SPEED", "20"));
            PADDLE_WIDTH = Integer.parseInt(props.getProperty("PADDLE_WIDTH", "100"));
            PADDLE_HEIGHT = Integer.parseInt(props.getProperty("PADDLE_HEIGHT", "15"));
            BALL_RADIUS = Integer.parseInt(props.getProperty("BALL_RADIUS", "8"));
            BALL_INITIAL_SPEED = Double.parseDouble(props.getProperty("BALL_INITIAL_SPEED", "3.0"));
            BALL_START_X = Integer.parseInt(props.getProperty("BALL_START_X", "240"));
            BALL_START_Y = Integer.parseInt(props.getProperty("BALL_START_Y", "312"));
            WINDOW_WIDTH = Integer.parseInt(props.getProperty("WINDOW_WIDTH", "480"));
            WINDOW_HEIGHT = Integer.parseInt(props.getProperty("WINDOW_HEIGHT", "650"));
            GAME_AREA_MIN_X = Integer.parseInt(props.getProperty("GAME_AREA_MIN_X", "0"));
            GAME_AREA_MAX_X = Integer.parseInt(props.getProperty("GAME_AREA_MAX_X", "480"));
            GAME_AREA_MIN_Y = Integer.parseInt(props.getProperty("GAME_AREA_MIN_Y", "140"));
            GAME_AREA_MAX_Y = Integer.parseInt(props.getProperty("GAME_AREA_MAX_Y", "625"));
            DEFAULT_HOST = props.getProperty("DEFAULT_HOST", "localhost");
            DEFAULT_PORT = Integer.parseInt(props.getProperty("DEFAULT_PORT", "5555"));
            TICK_RATE = Integer.parseInt(props.getProperty("TICK_RATE", "60"));
            TICK_DELAY = Integer.parseInt(props.getProperty("TICK_DELAY", "16"));
            BOARD_X = Integer.parseInt(props.getProperty("BOARD_X", "0"));
            TOP_BOARD_Y = Integer.parseInt(props.getProperty("TOP_BOARD_Y", "145"));
            BOTTOM_BOARD_Y = Integer.parseInt(props.getProperty("BOTTOM_BOARD_Y", "505"));
            CELL_SIZE = Integer.parseInt(props.getProperty("CELL_SIZE", "60"));
        } catch (IOException e) {
            System.out.println("Config file not found, using defaults.");
            NORMAL_SPEED = 10;
            BOOST_SPEED = 20;
            PADDLE_WIDTH = 100;
            PADDLE_HEIGHT = 15;
            BALL_RADIUS = 8;
            BALL_INITIAL_SPEED = 3.0;
            BALL_START_X = 240;
            BALL_START_Y = 312;
            WINDOW_WIDTH = 480;
            WINDOW_HEIGHT = 650;
            GAME_AREA_MIN_X = 0;
            GAME_AREA_MAX_X = 480;
            GAME_AREA_MIN_Y = 140;
            GAME_AREA_MAX_Y = 625;
            DEFAULT_HOST = "localhost";
            DEFAULT_PORT = 5555;
            TICK_RATE = 60;
            TICK_DELAY = 1000 / TICK_RATE;
            BOARD_X = 0;
            TOP_BOARD_Y = 145;
            BOTTOM_BOARD_Y = 505;
            CELL_SIZE = 60;
        }
    }
}