package org.ssnify.darkauction.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.ssnify.darkauction.DarkAuction;

public class AuctionGUI {
    private final DarkAuction plugin;

    public AuctionGUI(DarkAuction plugin) {
        this.plugin = plugin;
    }

    public void open(final Player player) {
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 54, "§5§lDark Auction");
        this.updateGUI(inv, player);
        player.openInventory(inv);
        (new BukkitRunnable() {
            public void run() {
                if (!player.getOpenInventory().getTitle().contains("Dark Auction")) {
                    this.cancel();
                } else if (!AuctionGUI.this.plugin.getAuctionManager().isBidAuctionActive()) {
                    player.closeInventory();
                    this.cancel();
                } else {
                    AuctionGUI.this.updateGUI(inv, player);
                }

            }
        }).runTaskTimer(this.plugin, 10L, 10L);
    }

    private void updateGUI(Inventory inv, Player player) {
        inv.clear();
        int[] borderSlots = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        ItemStack border = this.createItem(Material.PURPLE_STAINED_GLASS_PANE, " ", Arrays.asList());

        for(int slot : borderSlots) {
            inv.setItem(slot, border);
        }

        ItemStack currentItem = this.plugin.getAuctionManager().getCurrentItem();
        if (currentItem == null) {
            ItemStack waiting = this.createItem(Material.BARRIER, "§cWaiting for next item...", Arrays.asList("§7The auction is starting soon!"));
            inv.setItem(22, waiting);
        } else {
            long highestBid = this.plugin.getAuctionManager().getHighestBid();
            String highestBidder = this.plugin.getAuctionManager().getHighestBidderName();
            int timeLeft = this.plugin.getAuctionManager().getTimeLeft();
            Material var10001 = Material.NETHER_STAR;
            String[] var10003 = new String[5];
            String var10006 = currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName() ? currentItem.getItemMeta().getDisplayName() : currentItem.getType().name();
            var10003[0] = "§7Current Item: §f" + var10006;
            var10003[1] = "";
            var10003[2] = "§7Time Left: §e" + timeLeft + "s";
            var10006 = highestBid > 0L ? this.formatCoins(highestBid) + " coins" : "No bids";
            var10003[3] = "§7Highest Bid: §6" + var10006;
            var10003[4] = "§7Highest Bidder: §a" + highestBidder;
            ItemStack info = this.createItem(var10001, "§5§lDark Auction - Bidding", Arrays.asList(var10003));
            inv.setItem(4, info);
            long coins = this.plugin.getCoinsManager().getCoins(player);
            var10001 = Material.SUNFLOWER;
            var10003 = new String[1];
            var10006 = this.formatCoins(coins);
            var10003[0] = "§6" + var10006 + " coins";
            ItemStack purse = this.createItem(var10001, "§aPurse", Arrays.asList(var10003));
            inv.setItem(49, purse);
            ItemStack displayItem = currentItem.clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList();
                if (meta.hasLore()) {
                    lore.addAll(meta.getLore());
                }

                lore.add("");
                String var35 = highestBid > 0L ? this.formatCoins(highestBid) + " coins" : "No bids";
                lore.add("§7Highest Bid: §6" + var35);
                lore.add("§7Time Left: §e" + timeLeft + "s");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }

            inv.setItem(22, displayItem);
            long playerBid = this.plugin.getAuctionManager().getPlayerBid(player);
            long minBid = highestBid > 0L ? highestBid + 100000L : 100000L;
            long[] bidAmounts = new long[]{minBid, minBid + 500000L, minBid + 1000000L, minBid + 5000000L};
            int[] bidSlots = new int[]{29, 30, 32, 33};
            String[] bidColors = new String[]{"§a", "§e", "§6", "§c"};

            for(int i = 0; i < bidAmounts.length; ++i) {
                long bidAmount = bidAmounts[i];
                boolean canAfford = coins >= bidAmount;
                var10001 = canAfford ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                String var10002 = bidColors[i] + "Bid: " + this.formatCoins(bidAmount) + " coins";
                var10003 = new String[3];
                var10006 = this.formatCoins(bidAmount);
                var10003[0] = "§7Click to bid §6" + var10006 + " coins";
                var10003[1] = "";
                var10003[2] = canAfford ? "§aYou can afford this!" : "§cYou cannot afford this!";
                ItemStack bidButton = this.createItem(var10001, var10002, Arrays.asList(var10003));
                inv.setItem(bidSlots[i], bidButton);
            }

            if (playerBid > 0L) {
                var10001 = Material.GOLD_INGOT;
                var10003 = new String[3];
                var10006 = this.formatCoins(playerBid);
                var10003[0] = "§7Current bid: §6" + var10006 + " coins";
                var10003[1] = "";
                var10003[2] = playerBid == highestBid ? "§aYou have the highest bid!" : "§cYou've been outbid!";
                ItemStack yourBid = this.createItem(var10001, "§eYour Bid", Arrays.asList(var10003));
                inv.setItem(40, yourBid);
            }
        }

    }

    private String formatCoins(long amount) {
        return String.format("%,d", amount);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
