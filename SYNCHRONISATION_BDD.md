# 🔄 Synchronisation Base de Données - Mode d'emploi

## 📋 Vue d'ensemble

Le jeu synchronise maintenant **automatiquement** les modifications avec la base de données PostgreSQL via l'EJB.

## 🎯 Ce qui est synchronisé

### 1️⃣ **Points de Vie des Pièces (HP)**
- **Fichier local** : `config/vie.txt`
- **Table BDD** : `piece_hp`
- **Déclencheur** : Bouton "💾 Sauvegarder" dans la fenêtre "⚔ Configuration des Points de Vie"

#### Comment ça marche :
1. Ouvrir l'interface du jeu → Cliquer sur "❤ Gérer PV"
2. Modifier les valeurs de HP
3. Cliquer sur "💾 Sauvegarder"
4. ✅ Le client envoie `SAVE_HP:PAWN=5,ROOK=10,...` au serveur
5. ✅ Le serveur appelle l'EJB : `updateAllPieceHP(hpMap)`
6. ✅ L'EJB exécute `UPDATE piece_hp SET hp = ? WHERE piece_type = ?`
7. ✅ La BDD est mise à jour !

### 2️⃣ **Configuration du Jeu**
- **Fichier local** : `config/config.txt`
- **Table BDD** : `game_config`
- **Méthode disponible** : `saveConfigValueToDatabase(key, value)`

## 🔧 Architecture

```
Client (GameFrame)
    ↓ [SAVE_HP:...]
Server (ClientHandler)
    ↓ [saveHPToDatabase()]
Server (GameEngine)
    ↓ [configService.updateAllPieceHP()]
EJB (ConfigServiceBean)
    ↓ [UPDATE SQL]
PostgreSQL Database
```

## 🆕 Nouvelles méthodes EJB

### Interface `ConfigServiceRemote`
```java
void updateGameConfigValue(String key, String value);
void updatePieceHP(String pieceType, int hp);
void updateAllPieceHP(Map<String, Integer> hpMap);
```

### Implémentation `ConfigServiceBean`
- Utilise des requêtes natives `UPDATE` pour modifier les données
- Logs de confirmation dans la console du serveur

## 📝 Protocole de communication

### Client → Serveur
- `SAVE_HP:PAWN=5,ROOK=10,KNIGHT=8,...` : Sauvegarde les HP en BDD
- `RELOAD_HP` : Recharge les HP depuis l'EJB

### Serveur → Client
- `HP:PAWN=5,ROOK=10,...` : Envoie les HP actuels

## 🧪 Comment tester

1. **Déployer l'EJB mis à jour dans Wildfly**
   ```bash
   cd ejb
   mvn clean package
   # Déployer ejb/target/configservice-1.0-SNAPSHOT.jar dans Wildfly
   ```

2. **Lancer le serveur**
   ```bash
   .\serveur.bat
   ```
   Vérifier : `Configuration chargée depuis EJB !`

3. **Lancer le client**
   ```bash
   .\client.bat
   ```

4. **Modifier les HP**
   - Cliquer sur "❤ Gérer PV"
   - Changer par exemple PAWN de 5 à 10
   - Cliquer sur "💾 Sauvegarder"

5. **Vérifier dans la console serveur**
   ```
   [EJB] Mise à jour HP: PAWN = 10
   💾 HP sauvegardés dans la BDD via EJB
   ```

6. **Vérifier dans PostgreSQL**
   ```sql
   SELECT * FROM piece_hp WHERE piece_type = 'PAWN';
   -- Doit afficher hp = 10
   ```

## ⚠️ Gestion des erreurs

- **EJB non disponible** : Le serveur utilise `config.txt` en fallback
- **BDD inaccessible** : Message d'erreur dans la console serveur
- **Connexion perdue** : Les modifications locales restent dans `vie.txt`

## 🔐 Sécurité

Les mises à jour utilisent des **paramètres préparés** pour éviter les injections SQL :
```java
em.createNativeQuery("UPDATE piece_hp SET hp = ?1 WHERE piece_type = ?2")
    .setParameter(1, hp)
    .setParameter(2, pieceType)
    .executeUpdate();
```

## 📊 Tables PostgreSQL

### `piece_hp`
```sql
piece_type VARCHAR PRIMARY KEY
hp         INT NOT NULL
```

### `game_config`
```sql
key   VARCHAR PRIMARY KEY
value VARCHAR NOT NULL
```

## ✅ Avantages

- ✅ **Persistance** : Les modifications survivent au redémarrage
- ✅ **Centralisation** : Une seule source de vérité (la BDD)
- ✅ **Multi-joueurs** : Tous les joueurs voient les mêmes paramètres
- ✅ **Traçabilité** : Les logs montrent chaque modification
- ✅ **Robustesse** : Fallback sur fichiers si EJB indisponible

---

**📅 Dernière mise à jour** : 15 décembre 2025
