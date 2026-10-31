package org.ssnify.darkauction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ssnify.darkauction.DarkAuction;

import java.util.Arrays;
import java.util.List;

public class DisplayGUI {

    private final DarkAuction plugin;

    public DisplayGUI(DarkAuction plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§5§lDark Auction - Display");
        updateGUI(inv, player);
        player.openInventory(inv);
    }

    private void updateGUI(Inventory inv, Player player) {
        inv.clear();

        // Border
        int[] borderSlots = {0,1,2,3,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,50,51,52,53};
        ItemStack border = createItem(Material.PURPLE_STAINED_GLASS_PANE, " ", Arrays.asList());
        for (int slot : borderSlots) inv.setItem(slot, border);

        // Purse
        long coins = plugin.getCoinsManager().getCoins(player);
        inv.setItem(4, createItem(Material.SUNFLOWER, "§aPurse", Arrays.asList(
                "§6" + formatCoins(coins) + " coins"
        )));

        // Purchase info
        int purchases = plugin.getAuctionManager().getPurchaseCount(player);
        int globalLimit = plugin.getAuctionManager().getGlobalPurchaseLimit();
        inv.setItem(49, createItem(Material.BOOK, "§ePurchase Info", Arrays.asList(
                "§7Items purchased: §a" + purchases + "/" + globalLimit,
                purchases >= globalLimit ? "§cYou've reached the maximum!" : "§7You can buy §a" + (globalLimit - purchases) + " §7more item(s)"
        )));

        // Slot positions: display row and buy button row
        int[] displaySlots  = {20, 22, 24};
        int[] buySlots      = {29, 31, 33};

        for (int i = 0; i < 3; i++) {
            ItemStack item = plugin.getAuctionManager().getDisplayItem(i);

            if (item == null) continue;

            long price      = plugin.getAuctionManager().getDisplayPrice(i);
            int  effStock   = plugin.getAuctionManager().getEffectiveStock(i);
            int  effLimit   = plugin.getAuctionManager().getEffectiveLimit(i);
            boolean outOfStock  = (effStock == 0);
            boolean canAfford   = coins >= price;
            boolean canPurchase = plugin.getAuctionManager().canPurchase(player, i);

            if (outOfStock) {
                // ── SOLD OUT placeholder ──
                inv.setItem(displaySlots[i], createItem(
                        Material.BARRIER,
                        "§c§lSOLD OUT",
                        Arrays.asList(
                                "§7This item has sold out.",
                                "§7Check back next week!"
                        )
                ));
                inv.setItem(buySlots[i], createItem(
                        Material.RED_STAINED_GLASS_PANE,
                        "§cSOLD OUT",
                        Arrays.asList("§7No stock remaining.")
                ));

            } else {
                // ── Normal item display ───
                String stockLine = effStock == -1 ? "§7Stock: §eUnlimited" : "§7Stock: §e" + effStock + " remaining";
                String limitLine = "§7Max per player: §e" + effLimit;

                String statusLine;
                if (!canPurchase) {
                    statusLine = "§cPurchase limit reached!";
                } else if (!canAfford) {
                    statusLine = "§cYou cannot afford this!";
                } else {
                    statusLine = "§aClick to purchase!";
                }

                ItemStack display = item.clone();
                ItemMeta meta = display.getItemMeta();
                if (meta != null) {
                    List<String> lore = new java.util.ArrayList<>();
                    if (meta.hasLore()) lore.addAll(meta.getLore());
                    lore.add("");
                    lore.add("§7Price: §6" + formatCoins(price) + " coins");
                    lore.add(stockLine);
                    lore.add(limitLine);
                    lore.add("");
                    lore.add(statusLine);
                    meta.setLore(lore);
                    display.setItemMeta(meta);
                }
                inv.setItem(displaySlots[i], display);

                boolean buyable = canAfford && canPurchase;
                inv.setItem(buySlots[i], createItem(
                        buyable ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                        (buyable ? "§a" : "§c") + "Purchase Item " + (i + 1),
                        Arrays.asList(
                                "§7Price: §6" + formatCoins(price) + " coins",
                                stockLine,
                                "",
                                !canPurchase ? "§cPurchase limit reached!" :
                                        !canAfford ? "§cInsufficient coins!" : "§eClick to buy!"
                        )
                ));
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