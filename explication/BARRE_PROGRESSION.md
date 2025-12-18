# Barre de Progression avec Super Pouvoir - Documentation

## Vue d'ensemble

Nouvelle fonctionnalité qui ajoute une barre de progression partagée entre les 2 joueurs et un système de super pouvoir pour la balle.

## Caractéristiques principales

### 1. Barre de Progression (B)
- **Partagée** : Une seule barre pour les deux joueurs
- **Configurable** : Capacité réglable de 1 à n (par défaut : 10)
- **Position** : Affichée au-dessus du jeu, centrée
- **Incrémentation** : +1 à chaque fois qu'un joueur touche un pion d'échecs (n'importe lequel)

### 2. Super Pouvoir de la Balle

#### Activation
- Se déclenche quand la barre atteint sa capacité maximale (exemple : 10/10)
- Pouvoir de dégâts : **-3**

#### Mécaniques

##### Mode Normal (sans super pouvoir)
- La balle rebondit sur les pions
- Inflige **1 point de dégâts** par touche
- Comportement classique

##### Mode Super Pouvoir (actif)
- **Transperce les pions** au lieu de rebondir
- Inflige **3 points de dégâts** par défaut
- **Capacité résiduelle** : Si un pion a moins de 3 PV :
  - Le tue instantanément (ou inflige les dégâts disponibles)
  - Continue avec le reste du pouvoir
  - La balle réduit son pouvoir des **dégâts réellement infligés**, pas des HP totaux
  - Exemple 1 : Pion avec 1 PV → meurt avec 1 dégât, balle garde -2 de pouvoir
  - Exemple 2 : Pion avec 5 PV → perd 3 HP, balle épuise son pouvoir (-0)
  - Peut tuer plusieurs pions d'affilée tant qu'il reste du pouvoir

#### Désactivation du Super Pouvoir
Le super pouvoir s'arrête dans 3 cas :

1. **Pouvoir épuisé** : Après avoir infligé tous ses dégâts aux pions
2. **Mur touché** : Si la balle touche le fond du mur sans toucher de pions
3. **Balle sortie** : Si la balle sort du terrain

Après désactivation : La barre recommence à 0 et le cycle reprend.

## Interface Utilisateur

### Configuration
- **Emplacement** : À côté des autres boutons de configuration
- **Composants** :
  - Champ de texte pour entrer la capacité
  - Bouton "MAJ Barre" pour valider
- **Label** : "Capacité barre de progression"

### Affichage de la Barre
- **Dimensions** : 300x25 pixels
- **Couleurs** :
  - Fond : Gris foncé (60, 60, 80)
  - Remplissage normal : Jaune doré (255, 215, 0)
  - Remplissage plein : Vert éclatant (0, 255, 100)
  - Super pouvoir actif : Rouge flamboyant (255, 50, 50)

### Affichage de la Balle
- **Normale** : Jaune vif (255, 230, 0)
- **Super Pouvoir** : Rouge avec aura (255, 50, 50 + aura transparente)

## Modifications Techniques

### Fichiers modifiés

#### 1. `common/GameConfig.java`
```java
public static int PROGRESS_BAR_CAPACITY; // Nouvelle config
```

#### 2. `common/Ball.java`
- Ajout de variables pour le super pouvoir
- Méthodes : `activateSuperPower()`, `deactivateSuperPower()`, `reduceSuperPowerDamage()`

#### 3. `common/Echequier.java`
- Modification de `bounceBallOnPiece()` pour gérer le mode super pouvoir
- Mode transperce : pas de rebond, dégâts multiples
- **Calcul correct** : Le pouvoir de la balle diminue des dégâts réellement infligés (min entre pouvoir et HP du pion)

#### 4. `server/GameEngine.java`
- Variables : `progressBarCurrent`, `progressBarCapacity`
- Logique d'augmentation de la barre
- Activation/désactivation du super pouvoir
- Broadcast de l'état de la barre via `getGameState()`
- Méthode `setProgressBarCapacity()` pour configuration

#### 5. `server/ClientHandler.java`
- Gestion de la commande `PROGRESS_CAPACITY:n`
- Envoi de la configuration initiale

#### 6. `client/GameFrame.java`
- Champ de texte et bouton de configuration
- Variables locales pour l'état de la barre
- Parsing de l'état `PROGRESS:` dans `processServerMessage()`
- Dessin de la barre de progression dans `GamePanel`
- Changement visuel de la balle en mode super pouvoir

#### 7. `ejb/script.sql`
- Ajout de `PROGRESS_BAR_CAPACITY` dans la table `game_config`

## Protocole Réseau

### Messages Serveur → Client
```
STATE:...:...;PROGRESS:current,capacity,hasSuperPower,damage;PIECES:...
```
- `current` : Valeur actuelle de la barre (0 à capacity)
- `capacity` : Capacité maximale
- `hasSuperPower` : 1 si actif, 0 sinon
- `damage` : Dégâts restants du super pouvoir

```
PROGRESS_CAPACITY:n
```
- Broadcast quand la capacité est modifiée

### Messages Client → Serveur
```
PROGRESS_CAPACITY:n
```
- Demande de changement de capacité de la barre

## Exemple de Cycle de Jeu

1. **Début** : Barre à 0/10
2. **Joueur 1 touche un pion** : Barre à 1/10
3. **Joueur 2 touche un pion** : Barre à 2/10
4. **...**
5. **Barre pleine** : 10/10 → Super pouvoir activé (-3 dégâts)
6. **Balle touche pion (2 PV)** :
   - Pion perd 2 PV (meurt)
   - Super pouvoir réduit de 2 (reste -1)
7. **Balle touche pion (3 PV)** :
   - Pion perd 1 PV (reste 2 PV)
   - Super pouvoir épuisé (0)
8. **Retour au mode normal** : Barre à 0/10, cycle recommence

## Notes de Conception

- Le super pouvoir est **coopératif** : les deux joueurs contribuent à le remplir
- La transperce empêche les rebonds en mode super pouvoir
- La barre se reset toujours à 0 après activation ou perte du pouvoir
- Les configurations se synchronisent entre le serveur et tous les clients
- L'état visuel est mis à jour en temps réel via le protocole réseau
