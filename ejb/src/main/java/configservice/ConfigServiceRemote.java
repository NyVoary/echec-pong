package configservice;

import java.util.Map;
import jakarta.ejb.Remote;

@Remote
public interface ConfigServiceRemote {
    Map<String, String> getGameConfig();
    Map<String, Integer> getPieceHP();
    void setPieceHP(String pieceType, int hp); // AJOUTE CETTE LIGNE
void setGameConfig(String key, String value);
}