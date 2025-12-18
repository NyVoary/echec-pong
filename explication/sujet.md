Architecture Technique
Pourquoi cette architecture ?
Problème à résoudre : Synchroniser parfaitement l'état du jeu entre tous les clients en temps réel.

Solution choisie :

text
┌─────────────────┐    Réseau    ┌─────────────────┐
│   Client 1      │◄────────────►│    Serveur      │◄────────────►│   Client 2      │
│  (Affichage)    │   WebSocket  │ (Logique Jeu)   │   WebSocket  │  (Affichage)    │
└─────────────────┘              └─────────────────┘              └─────────────────┘
1. Serveur - "Source de vérité"
Contient TOUTE la logique métier

Calcul des collisions

Gestion des scores

Vérification des mouvements valides

Synchronisation entre joueurs

2. Client - "Interface utilisateur"
Affiche l'état du jeu reçu du serveur

Capture les inputs utilisateur

Envoie les commandes au serveur

NE calcule PAS la logique de jeu

Pourquoi cette Séparation ?
Avantages :
Pas de triche : La logique est côté serveur, impossible à modifier

Synchronisation parfaite : Tous les joueurs voient la même chose

Performance : Le serveur peut gérer les calculs complexes

Maintenance : Mettre à jour les règles sans toucher aux clients

Cross-platform : Différents clients (PC, mobile, web) peuvent se connecter

Exemple Concret :
Quand un joueur bouge sa raquette :

Client → "Je bouge ma raquette vers le haut"

Serveur → Valide le mouvement, calcule la nouvelle position

Serveur → Envoie la nouvelle position à tous les clients

Tous les clients → Affichent la raquette à la nouvelle position

Classes Métier (Orienté Objet)
1. Paddle (Raquette)
java
// Responsabilités :
- Connaître sa position (x, y)
- Connaître sa taille (largeur, hauteur)
- Savoir se déplacer (haut/bas)
- Vérifier les collisions avec la balle
2. Ball (Balle)
java
// Responsabilités :
- Connaître sa position et vitesse
- Se déplacer selon sa vitesse
- Rebondir sur les paddles/bordures
- Détecter si elle sort du terrain
3. ChessBoard (Échiquier)
java
// Responsabilités :
- Stocker les pièces à leurs positions
- Valider les mouvements d'échecs
- Gérer les captures de pièces
- Vérifier les collisions balle/pieces
4. ChessPiece (Pièce d'échecs)
java
// Responsabilités :
- Connaître son type (Pion, Tour, etc.)
- Connaître sa couleur (Blanc/Noir)
- Avoir des points de vie
- Savoir comment elle peut se déplacer
5. Player (Joueur)
java
// Responsabilités :
- Avoir un identifiant unique
- Connaître son rôle (Paddle/Chess)
- Connaître son score
- Être connecté à un client
Flux de Données
text
Événement utilisateur → Client → Réseau → Serveur → Logique métier
                                 ↓
Tous les clients ← Réseau ← Nouvel état du jeu
Exemple de Partie
Connexion :

4 joueurs se connectent

2 sont assignés "Paddle" (gauche/droite)

2 sont assignés "Chess" (gauche/droite)

Début de partie :

La balle commence au centre

Les échiquiers sont en position initiale

Chaque joueur doit cliquer "Prêt"

Action en jeu :

text
Joueur Paddle Gauche : Appuie sur ↑
→ Raquette monte
→ Balle rebondit sur raquette
→ Balle vole vers échiquier droit
→ Balle touche un pion noir
→ Pion perd 30 PV
→ Balle rebondit vers la droite
→ Joueur Paddle Droit tente de rattraper...
Fin de partie :

Soit un joueur atteint 5 points (Pong)

Soit un roi est capturé (Échecs)

Victoire annoncée à tous

Innovations de ce Jeu
Mix de genres : Action rapide (Pong) + Réflexion stratégique (Échecs)

Coopération compétitive : Deux types de joueurs doivent collaborer

Nouvelles stratégies : Positionner ses pièces pour défendre/attaquer

Accessibilité : Même les débutants aux échecs peuvent jouer

Technologies Choisies
Pour Java/Swing :

Swing : Interface graphique native Java

Socket TCP : Communication réseau fiable

Threads : Gestion du temps réel

JSON : Échange de données structurées