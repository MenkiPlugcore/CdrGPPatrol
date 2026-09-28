package id.menkiplugcore.cdrgppatrol.service;

import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PatrolSessionService {
    private final Map<UUID, PatrolState> states = new HashMap<>();

    public PatrolState state(Player player) {
        return states.computeIfAbsent(player.getUniqueId(), ignored -> new PatrolState());
    }

    public void remove(Player player) {
        states.remove(player.getUniqueId());
    }

    public void clear() {
        states.clear();
    }
}
