package com.bergerkiller.bukkit.mw;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import com.bergerkiller.bukkit.common.collections.EntityMap;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import com.bergerkiller.bukkit.common.utils.MaterialUtil;
import com.bergerkiller.bukkit.common.utils.StringUtil;

/**
 * Handles events to manage plugin-defined permissions and messages.
 * Basically, everything that happens after performing a command
 * or using portals is dealt with here.
 */
public class MWListenerPost implements Listener {
    private static EntityMap<Player, PortalInfo> lastEnteredPortal = new EntityMap<Player, PortalInfo>();

    public static Portal getLastEntered(Player player, boolean remove) {
        PortalInfo info = remove ? lastEnteredPortal.remove(player) : lastEnteredPortal.get(player);
        if (info == null) {
            return null;
        }
        // Information must be from the same tick, otherwise it is invalid!
        if (player.getTicksLived() != info.tickStamp) {
            lastEnteredPortal.remove(player);
            return null;
        }
        // Done
        return info.portal;
    }

    public static void setLastEntered(Player player, Portal portal) {
        PortalInfo info = new PortalInfo();
        info.portal = portal;
        info.tickStamp = player.getTicksLived();
        lastEnteredPortal.put(player, info);
    }

    public static boolean handleTeleportPermission(Player player, Location to) {
        // World can be entered?
        if (Permission.canEnter(player, to.getWorld())) {
            // If applicable, Portal can be entered?
            Portal lastPortal = getLastEntered(player, false);
            if (lastPortal != null) {
                String name = lastPortal.getName();
                if (name != null && !Permission.canEnterPortal(player, name)) {
                    Localization.PORTAL_NOACCESS.message(player);
                    return false;
                }
            }
        } else {
            Localization.WORLD_NOACCESS.message(player);
            return false;
        }
        return true;
    }

    public static void handleTeleportMessage(Player player, Location to) {
        // We are handling this at the very end, so we can remove the portal as we get it
        Portal lastPortal = getLastEntered(player, true);
        if (lastPortal != null) {
            // Show the portal name (disabled for this branch)
            // Localization.PORTAL_ENTER.message(player, lastPortal.getDestinationDisplayName());
        } else if (to.getWorld() != player.getWorld()) {
            // Show world enter message (disabled for this branch)
            // Localization.WORLD_ENTER.message(player, to.getWorld().getName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleportPerm(PlayerTeleportEvent event) {
        // Sometimes TO is NULL, this fixes that. After that check the teleport permission is enforced.
        if (event.getTo() == null || event.getTo().getWorld() == null || !handleTeleportPermission(event.getPlayer(), event.getTo())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportMsg(PlayerTeleportEvent event) {
        handleTeleportMessage(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Result.DENY || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (MaterialUtil.ISBUCKET.get(event.getItem())) {
            if (!Permission.canBuild(event.getPlayer())) {
                Localization.WORLD_NOBUILD.message(event.getPlayer());
                event.setUseInteractedBlock(Result.DENY);
                event.setCancelled(true);
            }
        } else if (MaterialUtil.ISINTERACTABLE.get(event.getClickedBlock())) {
            if (!Permission.canUse(event.getPlayer())) {
                Localization.WORLD_NOUSE.message(event.getPlayer());
                event.setUseInteractedBlock(Result.DENY);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!Permission.canBuild(event.getPlayer())) {
            Localization.WORLD_NOBREAK.message(event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.canBuild()) {
            if (!Permission.canBuild(event.getPlayer())) {
                Localization.WORLD_NOBUILD.message(event.getPlayer());
                event.setBuild(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        if (!MyWorlds.debugLogGMChanges) {
            return;
        }

        ArrayList<StackTraceElement> elements = new ArrayList<StackTraceElement>(Arrays.asList(Thread.currentThread().getStackTrace()));
        while (!elements.isEmpty() && !elements.get(0).toString().startsWith("org.bukkit.plugin.SimplePluginManager.callEvent")) {
            elements.remove(0);
        }

        System.out.println("Game Mode of " + event.getPlayer().getName() + " changed from " +
                event.getPlayer().getGameMode() + " to " + event.getNewGameMode());
        ArrayList<String> pluginNames = new ArrayList<String>();
        for (Plugin plugin : CommonUtil.findPlugins(elements)) {
            pluginNames.add(plugin.getName());
        }
        if (pluginNames.isEmpty()) {
            System.out.println("This was likely initiated by the server. Stack trace:");
        } else {
            System.out.println("This was likely initiated by " + StringUtil.join(" OR ", pluginNames) + ". Stack trace:");
        }
        for (StackTraceElement element : elements) {
            System.out.println("  at " + element);
        }
    }

    private static class PortalInfo {
        public Portal portal;
        public int tickStamp;
    }
}
