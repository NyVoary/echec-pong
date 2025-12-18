# 📚 Explication Générale du Projet Échec-Pong

## 🎮 Vue d'ensemble

**Échec-Pong** est un jeu multijoueur innovant qui combine les mécaniques d'échecs et de pong. Deux joueurs s'affrontent en temps réel : ils contrôlent des raquettes pour renvoyer une balle tout en protégeant leurs pièces d'échecs positionnées sur un échiquier.

---

## 🏗️ Architecture Technique

### Architecture Client-Serveur

```
┌─────────────────┐         TCP/IP        ┌─────────────────┐
│   Client 1      │◄──────────────────────►│                 │
│  (Affichage)    │                        │     Serveur     │
└─────────────────┘                        │  (Logique Jeu)  │
                                           │                 │
┌─────────────────┐         TCP/IP        │                 │
│   Client 2      │◄──────────────────────►│                 │
│  (Affichage)    │                        └────────┬────────┘
└─────────────────┘                                 │
                                                    │ EJB
                                          ┌─────────▼────────┐
                                          │    WildFly       │
                                          │  (Serveur App)   │
                                          └─────────┬────────┘
                                                    │ JPA
                                          ┌─────────▼────────┐
                                          │   PostgreSQL     │
                                          │  (Base Données)  │
                                          └──────────────────┘
```

### Composants Principaux

| Composant | Rôle | Technologie |
|-----------|------|-------------|
| **Client** | Interface graphique, capture des inputs | Java Swing |
| **Serveur** | Logique de jeu, synchronisation | Java SE, Sockets |
| **EJB** | Configuration centralisée | Jakarta EE 10, WildFly 37 |
| **Base de données** | Stockage des paramètres et HP | PostgreSQL |

---

## 📂 Structure du Code

### Organisation des Dossiers

```
echec-pong/
├── client/              # Code client
│   ├── ClientMain.java       # Point d'entrée client
│   └── GameFrame.java        # Interface graphique Swing
│
├── server/              # Code serveur
│   ├── ServerMain.java       # Point d'entrée serveur
│   ├── GameEngine.java       # Moteur de jeu
│   ├── ClientHandler.java    # Gestion des connexions
│   └── Player.java           # Modèle joueur
│
├── common/              # Code partagé client/serveur
│   ├── Ball.java             # Logique de la balle
│   ├── Paddle.java           # Raquettes
│   ├── Echequier.java        # Échiquier
│   ├── ChessPiece.java       # Pièces d'échecs
│   ├── PieceType.java        # Types de pièces (enum)
│   ├── GameConfig.java       # Configuration globale
│   └── Carre.java            # Cases de l'échiquier
│
├── ejb/                 # Module EJB
│   ├── src/main/java/configservice/
│   │   ├── ConfigServiceBean.java       # Service EJB
│   │   └── ConfigServiceRemote.java     # Interface distante
│   ├── src/main/resources/META-INF/
│   │   └── persistence.xml              # Config JPA
│   ├── script.sql                       # Script base de données
│   └── pom.xml                          # Configuration Maven
│
├── pieces/              # Images des pièces d'échecs
├── config/              # Fichiers de configuration
└── *.bat                # Scripts de compilation/lancement
```

---

## 💾 Base de Données

### Schéma PostgreSQL

#### Table `game_config`
Stocke tous les paramètres de configuration du jeu.

| Colonne | Type | Description |
|---------|------|-------------|
| `key` | VARCHAR (PK) | Nom du paramètre |
| `value` | VARCHAR | Valeur du paramètre |

**Exemples de paramètres :**
- `WINDOW_WIDTH`, `WINDOW_HEIGHT` : Dimensions fenêtre
- `BALL_RADIUS`, `BALL_INITIAL_SPEED` : Propriétés balle
- `PADDLE_WIDTH`, `PADDLE_HEIGHT` : Dimensions raquettes
- `CELL_SIZE`, `BOARD_X`, `BOARD_Y` : Position échiquier
- `TICK_RATE` : Fréquence de mise à jour (60 FPS)

#### Table `piece_hp`
Définit les points de vie de chaque type de pièce.

| Colonne | Type | Description |
|---------|------|-------------|
| `piece_type` | VARCHAR (PK) | Type de pièce (PAWN, ROOK...) |
| `hp` | INT | Points de vie |

**Pièces disponibles :**
- PAWN (Pion), ROOK (Tour), KNIGHT (Cavalier)
- BISHOP (Fou), QUEEN (Dame), KING (Roi)

---

## 🎨 Affichage Graphique

### Interface Client (Swing)

#### Palette de Couleurs

| Élément | Couleur | Code RGB |
|---------|---------|----------|
| Fond général | Gris-bleu foncé | `(45, 52, 70)` |
| Header | Turquoise | `(0, 180, 180)` |
| Paddle TOP | Bleu cyan | `(0, 180, 255)` |
| Paddle BOTTOM | Orange vif | `(255, 100, 50)` |
| Balle | Jaune vif | `(255, 230, 0)` |
| Case claire échiquier | Beige doré | `(240, 217, 181)` |
| Case foncée échiquier | Marron chocolat | `(181, 136, 99)` |

#### Zones de l'Interface

```
┌──────────────────────────────────────┐
│  HEADER (Turquoise)                  │ ← Zone de connexion
│  IP: [____]  Port: [____] [Connecter]│   et configuration
│  Colonnes: [__] [Mettre à jour]      │
├──────────────────────────────────────┤
│           ÉCHIQUIER TOP              │ ← Joueur 2 (J2)
│  ┌──────────────────────────────┐   │
│  │ ♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜             │   │
│  │ ♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟             │   │
│  └──────────────────────────────┘   │
│         ═══════════════              │ ← Paddle TOP
│                                      │
│              ● ← Balle               │ ← Zone de jeu
│                                      │
│         ═══════════════              │ ← Paddle BOTTOM
│  ┌──────────────────────────────┐   │
│  │ ♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙             │   │
│  │ ♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖             │   │
│  └──────────────────────────────┘   │
│           ÉCHIQUIER BOTTOM           │ ← Joueur 1 (J1)
└──────────────────────────────────────┘
```

---

## ⚙️ Fonctions Essentielles

### 🎯 Côté Serveur

#### 1. **Moteur de Jeu (GameEngine.java)**

```java
// Fonctions principales :
- loadConfigFromEJB()        // Charge config depuis PostgreSQL
- startGameLoop()             // Boucle principale 60 FPS
- updateGameState()           // Met à jour positions/collisions
- broadcastState()            // Envoie l'état à tous les clients
- checkWinCondition()         // Vérifie conditions de victoire
```

**Logique de collision :**
- Balle rebondit sur les raquettes
- Balle touche une pièce → La pièce perd des HP
- Pièce à 0 HP → Devient inactive (grisée)
- Tous les rois d'un joueur morts → Défaite

#### 2. **Gestion des Clients (ClientHandler.java)**

```java
- handleClientInput()         // Traite commandes (MOVE, COLS, SET_HP)
- sendGameState()             // Envoie état du jeu au client
- handleDisconnection()       // Gère déconnexions
```

**Protocole de communication :**
```
Client → Serveur:
  - MOVE:LEFT / MOVE:RIGHT    → Déplacement raquette
  - COLS:8                     → Change nombre de colonnes
  - SET_HP:PAWN:10            → Modifie HP d'une pièce

Serveur → Client:
  - CONFIG:key=value,...       → Configuration initiale
  - STATE:x,y,ballX,ballY;...  → État du jeu
  - HP:PAWN=5,ROOK=10,...      → HP des pièces
  - GAMEOVER:WINNER:LEFT       → Fin de partie
```

### 🖥️ Côté Client

#### 1. **Interface Graphique (GameFrame.java)**

```java
- connectToServer()           // Connexion TCP au serveur
- setupKeyListeners()         // Capture touches clavier
- processServerMessage()      // Traite messages serveur
- paintComponent()            // Dessine le jeu
- updateColumns()             // Change taille échiquier (2-8 colonnes)
```

**Contrôles :**
- **Joueur 1 (BOTTOM)** : ⬅️ Flèche Gauche / ➡️ Flèche Droite
- **Joueur 2 (TOP)** : S (gauche) / D (droite)

#### 2. **Configuration des Vies (VieConfigDialog.java)**

Interface pour modifier dynamiquement les HP de chaque pièce.
- Les changements sont envoyés au serveur
- Sauvegardés dans PostgreSQL via EJB
- Synchronisés entre tous les joueurs

### 🔄 Synchronisation en Temps Réel

```
[Serveur] Boucle de jeu (60 FPS):
  1. Lit les inputs de tous les clients
  2. Met à jour positions (paddles, balle)
  3. Calcule les collisions
  4. Met à jour HP des pièces
  5. Vérifie conditions de victoire
  6. Envoie nouvel état → Tous les clients
  
[Client] À réception d'un STATE:
  1. Parse les données (positions, HP)
  2. Met à jour modèle local
  3. Redessine l'interface
```

---

## 🚀 Compilation et Exécution

### 1. **Démarrer WildFly + Déployer EJB**
```batch
.\wildfly.bat
```
- Démarre WildFly
- Compile le module EJB avec Maven
- Déploie `configservice.jar`

### 2. **Compiler le Jeu**
```batch
.\compile.bat
```
- Compile les classes Java (client, serveur, common)
- Crée `echec-pong-server.jar` et `echec-pong-client.jar`

### 3. **Lancer le Serveur**
```batch
.\serveur.bat
```
- Lance le serveur sur `0.0.0.0:5555`
- Charge la config depuis l'EJB/PostgreSQL
- Attend connexions de 2 joueurs max

### 4. **Lancer les Clients** (x2)
```batch
.\client.bat
```
- Ouvrir 2 terminaux pour 2 joueurs
- Saisir IP serveur (ex: `192.168.x.x` ou `localhost`)
- Port : `5555`

---

## 🎲 Règles du Jeu

### Objectif
Détruire toutes les pièces adverses en renvoyant la balle avec sa raquette.

### Déroulement
1. La balle rebondit entre les deux camps
2. Les joueurs déplacent leurs raquettes pour renvoyer la balle
3. Quand la balle touche une pièce :
   - La pièce perd **1 HP**
   - La balle rebondit
4. Une pièce à **0 HP** est détruite (ne rebondit plus)
5. **Victoire** : Quand toutes les pièces adverses sont détruites

### Stratégie
- **Protéger ses pièces** : Renvoyer la balle avant qu'elle touche l'échiquier
- **Attaquer** : Viser les pièces adverses pour les affaiblir
- **Adapter l'échiquier** : Modifier le nombre de colonnes (2, 4, 6, 8)

---

## 🔧 Technologies Utilisées

| Technologie | Version | Utilisation |
|-------------|---------|-------------|
| **Java SE** | 8+ | Logique client/serveur |
| **Swing** | Built-in | Interface graphique |
| **Jakarta EE** | 10.0.0 | EJB, JPA |
| **WildFly** | 37.0.1 | Serveur d'applications |
| **PostgreSQL** | 12+ | Base de données |
| **Maven** | 3.x | Build EJB |
| **TCP Sockets** | Built-in | Communication réseau |

---

## 📝 Points Clés de l'Architecture

### ✅ Avantages

1. **Séparation des Responsabilités**
   - Client = Affichage uniquement
   - Serveur = Logique métier
   - EJB = Configuration centralisée

2. **Pas de Triche**
   - Toute la logique côté serveur
   - Le client ne peut pas modifier les règles

3. **Synchronisation Parfaite**
   - Un seul état de jeu (serveur)
   - Tous les clients voient la même chose

4. **Configuration Dynamique**
   - Paramètres modifiables sans recompilation
   - Stockés en base de données
   - Chargés au démarrage

5. **Scalabilité**
   - Facile d'ajouter des modes de jeu
   - Support multi-joueurs (actuellement 2)

### 📊 Flux de Données

```
PostgreSQL → EJB → Serveur → Clients
    ↓         ↓       ↓         ↓
  Config   Cache  Logique  Affichage
```

---

## 🐛 Debugging

### Logs Serveur
```
Client - Ball: x=240, y=312
Client - topPaddle: x=190, y=265
Client - bottomPaddle: x=190, y=475
```

### Vérifications
- **WildFly** : http://localhost:8080
- **PostgreSQL** : `psql -U postgres -d echecpong`
- **Connexion EJB** : Vérifier `jboss-client.jar` dans classpath

---

## 👥 Auteurs & Cours

**Projet réalisé dans le cadre du cours :**  
Architecture Distribuée - Mr Tahina

---

**Version** : 1.0  
**Date** : Décembre 2025
