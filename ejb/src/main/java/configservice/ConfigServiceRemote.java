package configservice;

import java.util.Map;

import jakarta.ejb.Remote;

@Remote
public interface ConfigServiceRemote {
    Map<String, String> getGameConfig();
    Map<String, Integer> getPieceHP();
    
    // Méthodes de mise à jour
    void updateGameConfigValue(String key, String value);
    void updatePieceHP(String pieceType, int hp);
    void updateAllPieceHP(Map<String, Integer> hpMap);
}