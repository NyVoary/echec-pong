-- Créer la base de données (à exécuter en tant que superutilisateur)
CREATE DATABASE echecpong;

\c echecpong

-- Table des paramètres généraux
CREATE TABLE game_config (
    key VARCHAR PRIMARY KEY,
    value VARCHAR NOT NULL
);

INSERT INTO game_config VALUES
('NORMAL_SPEED', '10'),
('BOOST_SPEED', '20'),
('PADDLE_WIDTH', '100'),
('PADDLE_HEIGHT', '15'),
('BALL_RADIUS', '8'),
('BALL_INITIAL_SPEED', '3.0'),
('BALL_START_X', '240'),
('BALL_START_Y', '312'),
('WINDOW_WIDTH', '480'),
('WINDOW_HEIGHT', '750'),
('GAME_AREA_MIN_X', '0'),
('GAME_AREA_MAX_X', '480'),
('GAME_AREA_MIN_Y', '140'),
('GAME_AREA_MAX_Y', '625'),
('DEFAULT_HOST', 'localhost'),
('DEFAULT_PORT', '5555'),
('TICK_RATE', '60'),
('TICK_DELAY', '16'),
('BOARD_X', '0'),
('TOP_BOARD_Y', '145'),
('BOTTOM_BOARD_Y', '505'),
('CELL_SIZE', '60'),
-- bare de progression (alea) par defaut 10 jusqu'a modification
('PROGRESS_BAR_CAPACITY', '10');

-- Table des points de vie des pièces
CREATE TABLE piece_hp (
    piece_type VARCHAR PRIMARY KEY,
    hp INT NOT NULL
);

INSERT INTO piece_hp VALUES
('PAWN', 5),
('ROOK', 5),
('KNIGHT', 5),
('BISHOP', 5),
('QUEEN', 5),
('KING', 5);