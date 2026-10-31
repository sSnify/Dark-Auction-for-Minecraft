package org.ssnify.darkauction.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.ssnify.darkauction.DarkAuction;

public class PaymentBlockerListener implements Listener {
    private final DarkAuction plugin;

    public PaymentBlockerListener(DarkAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        if (command.startsWith("/pay ") || command.startsWith("/money send ") || command.startsWith("/eco give ") || command.startsWith("/economy give ")) {
            String[] args = command.split(" ");
            if (args.length < 2) {
                return;
            }

            String targetName = args[1];
            Player target = this.plugin.getServer().getPlayer(targetName);
            if (target != null && this.plugin.getAuctionManager().isParticipant(target)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ You cannot send coins to " + target.getName() + " while they are in the Dark Auction!");
            }
        }

    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player clicker = event.getPlayer();
            Player target = (Player)event.getRightClicked();
            boolean clickerInAuction = this.plugin.getAuctionManager().isParticipant(clicker);
            boolean targetInAuction = this.plugin.getAuctionManager().isParticipant(target);
            if (clickerInAuction || targetInAuction) {
                event.setCancelled(true);
                if (clickerInAuction) {
                    clicker.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ You cannot trade while in the Dark Auction!");
                } else {
                    clicker.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ You cannot trade with " + target.getName() + " while they are in the Dark Auction!");
                }
            }
        }

    }
}
