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
    public void updateGameConfigValue(String key, String value) {
        em.createNativeQuery("UPDATE game_config SET value = ?1 WHERE key = ?2")
            .setParameter(1, value)
            .setParameter(2, key)
            .executeUpdate();
        System.out.println("[EJB] Mise à jour: " + key + " = " + value);
    }

    @Override
    public void updatePieceHP(String pieceType, int hp) {
        em.createNativeQuery("UPDATE piece_hp SET hp = ?1 WHERE piece_type = ?2")
            .setParameter(1, hp)
            .setParameter(2, pieceType)
            .executeUpdate();
        System.out.println("[EJB] Mise à jour HP: " + pieceType + " = " + hp);
    }

    @Override
    public void updateAllPieceHP(Map<String, Integer> hpMap) {
        for (Map.Entry<String, Integer> entry : hpMap.entrySet()) {
            updatePieceHP(entry.getKey(), entry.getValue());
        }
        System.out.println("[EJB] Tous les HP mis à jour");
    }
}