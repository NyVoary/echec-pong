package configservice;

import jakarta.ejb.Stateless;
import jakarta.ejb.Remote;
import jakarta.persistence.*;
import java.util.*;

@Stateless
@Remote(ConfigServiceRemote.class)
public class ConfigServiceBean implements ConfigServiceRemote {
    @PersistenceContext(unitName = "pongPU")
    private EntityManager em;

    public Map<String, String> getGameConfig() {
        List<Object[]> results = em.createNativeQuery("SELECT key, value FROM game_config").getResultList();
        Map<String, String> config = new HashMap<>();
        for (Object[] row : results) {
            config.put((String) row[0], (String) row[1]);
        }
        return config;
    }

    public Map<String, Integer> getPieceHP() {
        List<Object[]> results = em.createNativeQuery("SELECT piece_type, hp FROM piece_hp").getResultList();
        Map<String, Integer> hpMap = new HashMap<>();
        for (Object[] row : results) {
            hpMap.put((String) row[0], ((Number) row[1]).intValue());
        }
        return hpMap;
    }

    @Override
    public void setPieceHP(String pieceType, int hp) {
        em.createNativeQuery("UPDATE piece_hp SET hp = ? WHERE piece_type = ?")
          .setParameter(1, hp)
          .setParameter(2, pieceType)
          .executeUpdate();
    }

    @Override
    public void setGameConfig(String key, String value) {
        em.createNativeQuery("UPDATE game_config SET value = ? WHERE key = ?")
        .setParameter(1, value)
        .setParameter(2, key)
        .executeUpdate();
    }
}