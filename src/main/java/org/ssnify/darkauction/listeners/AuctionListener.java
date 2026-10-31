package org.ssnify.darkauction.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ssnify.darkauction.DarkAuction;
import org.ssnify.darkauction.gui.DisplayGUI;

public class AuctionListener implements Listener {
    private final DarkAuction plugin;

    public AuctionListener(DarkAuction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.contains("Dark Auction")) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player)event.getWhoClicked();
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.hasItemMeta()) {
                    ItemMeta meta = clicked.getItemMeta();
                    if (meta != null && meta.hasDisplayName()) {
                        String displayName = meta.getDisplayName();
                        if (title.contains("Display")) {
                            if (displayName.contains("Purchase Item 1")) {
                                if (this.plugin.getAuctionManager().purchaseItem(player, 0)) {
                                    player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1.0F, 1.5F);
                                    (new DisplayGUI(this.plugin)).open(player);
                                } else {
                                    player.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Cannot purchase this item!");
                                    player.playSound(player.getLocation(), "entity.villager.no", 1.0F, 1.0F);
                                }
                            } else if (displayName.contains("Purchase Item 2")) {
                                if (this.plugin.getAuctionManager().purchaseItem(player, 1)) {
                                    player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1.0F, 1.5F);
                                    (new DisplayGUI(this.plugin)).open(player);
                                } else {
                                    player.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Cannot purchase this item!");
                                    player.playSound(player.getLocation(), "entity.villager.no", 1.0F, 1.0F);
                                }
                            } else if (displayName.contains("Purchase Item 3")) {
                                if (this.plugin.getAuctionManager().purchaseItem(player, 2)) {
                                    player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1.0F, 1.5F);
                                    (new DisplayGUI(this.plugin)).open(player);
                                } else {
                                    player.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Cannot purchase this item!");
                                    player.playSound(player.getLocation(), "entity.villager.no", 1.0F, 1.0F);
                                }
                            }
                        } else if ((displayName.startsWith("§a") || displayName.startsWith("§e") || displayName.startsWith("§6") || displayName.startsWith("§c")) && displayName.contains("Bid:") && displayName.contains("coins")) {
                            try {
                                String cleanName = displayName.replaceAll("§[0-9a-fk-or]", "");
                                String bidPart = cleanName.replace("Bid:", "").replace("coins", "").trim();
                                long bidAmount = this.parseFormattedCoins(bidPart);
                                if (!this.plugin.getCoinsManager().hasCoins(player, bidAmount)) {
                                    player.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ You don't have enough coins!");
                                    player.playSound(player.getLocation(), "entity.villager.no", 1.0F, 1.0F);
                                    return;
                                }

                                if (bidAmount <= this.plugin.getAuctionManager().getHighestBid()) {
                                    player.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Bid too low! Current highest: §6" + this.formatCoins(this.plugin.getAuctionManager().getHighestBid()) + " coins");
                                    player.playSound(player.getLocation(), "entity.villager.no", 1.0F, 1.0F);
                                    return;
                                }

                                this.plugin.getAuctionManager().placeBid(player, bidAmount);
                                player.playSound(player.getLocation(), "entity.experience_orb.pickup", 1.0F, 1.5F);
                            } catch (Exception e) {
                                player.sendMessage("§cError placing bid!");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

    }

    private long parseFormattedCoins(String formatted) {
        formatted = formatted.trim().replace(",", "");
        if (formatted.endsWith("B")) {
            return (long)(Double.parseDouble(formatted.replace("B", "")) * (double)1.0E9F);
        } else if (formatted.endsWith("M")) {
            return (long)(Double.parseDouble(formatted.replace("M", "")) * (double)1000000.0F);
        } else {
            return formatted.endsWith("k") ? (long)(Double.parseDouble(formatted.replace("k", "")) * (double)1000.0F) : Long.parseLong(formatted);
        }
    }

    private String formatCoins(long amount) {
        return String.format("%,d", amount);
    }
}
