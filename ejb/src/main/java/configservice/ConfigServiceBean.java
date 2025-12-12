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
}