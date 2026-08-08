package com.foolsmagic.customitems.effects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Handles the Timekeeper's Watch freeze effect: immobilizes (but does not make
 * invulnerable) every living entity caught in it for a fixed duration.
 *
 * Players: walk speed set to 0 for the duration (movement effectively locked).
 * Mobs: AI turned off for the duration (won't move or act).
 *
 * Damage still applies normally to frozen entities from any source - freezing only
 * stops THEM from acting, it doesn't protect them from being hit.
 */
public class FreezeManager {

    private record FrozenState(Float originalWalkSpeed, Boolean originalAi) {}

    private final Plugin plugin;
    private final Map<UUID, FrozenState> frozen = new HashMap<>();

    public FreezeManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isFrozen(UUID entityId) {
        return frozen.containsKey(entityId);
    }

    public void freeze(LivingEntity entity, long durationTicks) {
        UUID id = entity.getUniqueId();

        if (entity instanceof Player player) {
            if (!frozen.containsKey(id)) {
                frozen.put(id, new FrozenState(player.getWalkSpeed(), null));
            }
            player.setWalkSpeed(0f);
        } else if (entity instanceof Mob mob) {
            if (!frozen.containsKey(id)) {
                frozen.put(id, new FrozenState(null, mob.hasAI()));
            }
            mob.setAI(false);
        } else {
            return;
        }

        // Ambient particles for the duration, ticking every 5 ticks.
        BukkitTask particleTask = plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!entity.isValid() || !isFrozen(id)) {
                return;
            }
            Location loc = entity.getLocation().add(0, 1, 0);
            entity.getWorld().spawnParticle(Particle.END_ROD, loc, 3, 0.3, 0.5, 0.3, 0.01);
        }, 0L, 5L);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            unfreeze(entity);
        }, durationTicks);

        // Safety: cancel the particle loop shortly after the unfreeze runs too.
        plugin.getServer().getScheduler().runTaskLater(plugin, particleTask::cancel, durationTicks + 1L);
    }

    private void unfreeze(LivingEntity entity) {
        UUID id = entity.getUniqueId();
        FrozenState state = frozen.remove(id);
        if (state == null || !entity.isValid()) return;

        if (entity instanceof Player player && state.originalWalkSpeed() != null) {
            player.setWalkSpeed(state.originalWalkSpeed());
        } else if (entity instanceof Mob mob && state.originalAi() != null) {
            mob.setAI(state.originalAi());
        }
    }
}
