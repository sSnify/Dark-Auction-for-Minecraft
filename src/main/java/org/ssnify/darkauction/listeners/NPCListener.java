package org.ssnify.darkauction.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.ssnify.darkauction.DarkAuction;

public class NPCListener implements Listener {

    private final DarkAuction plugin;

    public NPCListener(DarkAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) return;

        Villager villager = (Villager) event.getRightClicked();

        if (!plugin.getNPCManager().isAuctionNPC(villager.getUniqueId())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (plugin.getAuctionManager().isDisplayActive()) {
            player.performCommand("da displaymenu");
        } else if (plugin.getAuctionManager().isBidAuctionActive()) {
            player.performCommand("da bidmenu");
        } else {
            player.sendMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §cNo auction is currently active!");
        }
    }
}