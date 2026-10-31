package org.ssnify.darkauction.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ssnify.darkauction.DarkAuction;

import java.util.ArrayList;
import java.util.List;

public class DarkAuctionCommand implements CommandExecutor {

    private final DarkAuction plugin;

    public DarkAuctionCommand(DarkAuction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (requiresAdmin(sub)) {
            if (!sender.isOp() && !sender.hasPermission("darkauction.admin")) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }
        }

        switch (sub) {
            case "create":
                if (!(sender instanceof Player)) { sender.sendMessage("§cOnly players can create NPCs!"); return true; }
                org.bukkit.entity.Villager npc = plugin.getNPCManager().createNPC(((Player) sender).getLocation());
                if (npc != null) {
                    sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Cleric NPC spawned at your location!");
                } else {
                    sender.sendMessage("§cFailed to spawn NPC!");
                }
                break;

            // ── GLOBAL LIMIT ─────
            case "limit":
                // /da limit <1|2>
                if (args.length < 2) { sender.sendMessage("§cUsage: /da limit <1|2>"); return true; }
                try {
                    int limit = Integer.parseInt(args[1]);
                    if (limit != 1 && limit != 2) { sender.sendMessage("§cLimit must be 1 or 2!"); return true; }
                    plugin.getAuctionManager().setGlobalPurchaseLimit(limit);
                    sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Global purchase limit set to §e" + limit + " item(s) per player.");
                } catch (NumberFormatException e) { sender.sendMessage("§cInvalid number!"); }
                break;

            // ── GLOBAL STOCK ─────
            case "stock":
                // /da stock <amount|unlimited>
                if (args.length < 2) { sender.sendMessage("§cUsage: /da stock <amount|unlimited>"); return true; }
                if (args[1].equalsIgnoreCase("unlimited")) {
                    plugin.getAuctionManager().setGlobalStock(-1);
                    sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Global stock set to §eunlimited.");
                } else {
                    try {
                        int stock = Integer.parseInt(args[1]);
                        plugin.getAuctionManager().setGlobalStock(stock);
                        sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Global stock set to §e" + stock + " per item.");
                    } catch (NumberFormatException e) { sender.sendMessage("§cInvalid number!"); }
                }
                break;

            // ── SET ITEMS (item1 / item2 / item3) ────────────────────────────
            // Usage: /da item<N> <price> [stock] [limit]
            case "item1": handleSetItem(sender, args, 0); break;
            case "item2": handleSetItem(sender, args, 1); break;
            case "item3": handleSetItem(sender, args, 2); break;

            // ── DISPLAY CONTROL ──────────────────────────────────────────────
            case "start":
                if (plugin.getAuctionManager().getDisplayItems().isEmpty()) {
                    sender.sendMessage("§cNo display items set! Use /da item1, /da item2, /da item3 first.");
                    return true;
                }
                plugin.getAuctionManager().startDisplay();
                sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Display started!");
                break;

            case "stop":
                plugin.getAuctionManager().stopDisplay();
                sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Display stopped!");
                break;

            // ── BID MODE ─────────────────────────────────────────────────────
            case "bid": {
                if (!(sender instanceof Player)) { sender.sendMessage("§cOnly players can add bid items!"); return true; }
                Player p = (Player) sender;
                ItemStack held = p.getInventory().getItemInMainHand();
                if (held == null || held.getType() == Material.AIR) {
                    sender.sendMessage("§cHold an item in your hand!"); return true;
                }
                List<ItemStack> current = plugin.getAuctionManager().getBidItems();
                if (current.size() >= 3) { sender.sendMessage("§cMax 3 bid items!"); return true; }
                current.add(held.clone());
                plugin.getAuctionManager().setBidItems(current);
                sender.sendMessage("§aItem added to bid auction! Total: " + current.size());
                break;
            }

            case "startbid":
                if (plugin.getAuctionManager().getBidItems().isEmpty()) {
                    sender.sendMessage("§cNo bid items set!"); return true;
                }
                plugin.getAuctionManager().startBidAuction();
                sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Bid auction started!");
                break;

            case "stopbid":
                plugin.getAuctionManager().stopBidAuction();
                sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Bid auction stopped!");
                break;
            case "list":
                sendDisplayList(sender);
                break;

            case "listbid": {
                List<ItemStack> bidItems = plugin.getAuctionManager().getBidItems();
                if (bidItems.isEmpty()) { sender.sendMessage("§eNo bid items set."); break; }
                sender.sendMessage("§eᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Bid items (" + bidItems.size() + "):");
                for (int i = 0; i < bidItems.size(); i++) {
                    ItemStack it = bidItems.get(i);
                    sender.sendMessage("§7" + (i + 1) + ". §f" + itemName(it));
                }
                break;
            }

            case "clear":
                plugin.getAuctionManager().clearDisplayItems();
                sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ All display items cleared!");
                break;

            case "clearbid":
                plugin.getAuctionManager().setBidItems(new ArrayList<>());
                sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ All bid items cleared!");
                break;

            case "displaymenu": {
                if (!(sender instanceof Player)) { sender.sendMessage("§cOnly players!"); return true; }
                Player p = (Player) sender;
                if (!plugin.getAuctionManager().isDisplayActive()) {
                    p.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Display mode is not active!"); return true;
                }
                new org.ssnify.darkauction.gui.DisplayGUI(plugin).open(p);
                break;
            }

            case "bidmenu": {
                if (!(sender instanceof Player)) { sender.sendMessage("§cOnly players!"); return true; }
                Player p = (Player) sender;
                if (!plugin.getAuctionManager().isBidAuctionActive()) {
                    p.sendMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Bid auction is not active!"); return true;
                }
                if (plugin.getAuctionManager().isParticipant(p)) {
                    new org.ssnify.darkauction.gui.AuctionGUI(plugin).open(p); return true;
                }
                long coins = plugin.getCoinsManager().getCoins(p);
                if (coins != 30_000_000L) {
                    p.sendMessage("§cYou need exactly §630,000,000 coins §cto enter!");
                    p.sendMessage("§cYou have: §6" + formatCoins(coins) + " coins");
                    return true;
                }
                plugin.getAuctionManager().addParticipant(p);
                p.sendMessage("§aYou joined the bid auction!");
                new org.ssnify.darkauction.gui.AuctionGUI(plugin).open(p);
                break;
            }

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    // ── HELPER: set item with optional stock and limit overrides ─────────────
    private void handleSetItem(CommandSender sender, String[] args, int slot) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /da item" + (slot + 1) + " <price> [stock] [limit]");
            return;
        }
        if (!(sender instanceof Player)) { sender.sendMessage("§cOnly players can set items!"); return; }
        Player p = (Player) sender;
        ItemStack held = p.getInventory().getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) {
            sender.sendMessage("§cHold an item in your hand!"); return;
        }

        try {
            long price = Long.parseLong(args[1]);
            plugin.getAuctionManager().setDisplayItem(slot, held.clone(), price);

            // Optional stock override for this slot
            if (args.length >= 3) {
                if (args[2].equalsIgnoreCase("unlimited")) {
                    plugin.getAuctionManager().setSlotStock(slot, -1);
                    sender.sendMessage("§7Slot " + (slot + 1) + " stock: §eunlimited");
                } else {
                    int stock = Integer.parseInt(args[2]);
                    plugin.getAuctionManager().setSlotStock(slot, stock);
                    sender.sendMessage("§7Slot " + (slot + 1) + " stock: §e" + stock);
                }
            }

            // Optional limit override for this slot
            if (args.length >= 4) {
                int limit = Integer.parseInt(args[3]);
                if (limit != 1 && limit != 2) {
                    sender.sendMessage("§cSlot limit must be 1 or 2!"); return;
                }
                plugin.getAuctionManager().setSlotLimit(slot, limit);
                sender.sendMessage("§7Slot " + (slot + 1) + " limit: §e" + limit + " per player");
            }

            sender.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Item " + (slot + 1) + " set — price: §6" + formatCoins(price) + " coins");

        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number in arguments!");
        }
    }

    // ── HELPERS ────

    private void sendDisplayList(CommandSender sender) {
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§5§l  DARK AUCTION — DISPLAY ITEMS");
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§7Global limit: §e" + plugin.getAuctionManager().getGlobalPurchaseLimit() +
                " §7| Global stock: §e" + stockLabel(plugin.getAuctionManager().getGlobalStock()));
        sender.sendMessage("");
        for (int i = 0; i < 3; i++) {
            ItemStack it = plugin.getAuctionManager().getDisplayItem(i);
            if (it == null) {
                sender.sendMessage("§7" + (i + 1) + ". §8empty");
                continue;
            }
            long price = plugin.getAuctionManager().getDisplayPrice(i);
            int effStock = plugin.getAuctionManager().getEffectiveStock(i);
            int effLimit = plugin.getAuctionManager().getEffectiveLimit(i);
            sender.sendMessage("§7" + (i + 1) + ". §f" + itemName(it) +
                    " §7| §6" + formatCoins(price) + " coins" +
                    " §7| stock: §e" + stockLabel(effStock) +
                    " §7| limit: §e" + effLimit);
        }
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private String stockLabel(int stock) {
        return stock == -1 ? "unlimited" : String.valueOf(stock);
    }

    private String itemName(ItemStack it) {
        if (it.hasItemMeta() && it.getItemMeta().hasDisplayName())
            return it.getItemMeta().getDisplayName();
        return it.getType().name();
    }

    private boolean requiresAdmin(String sub) {
        switch (sub) {
            case "create": case "start": case "stop":
            case "item1": case "item2": case "item3":
            case "limit": case "stock":
            case "bid": case "startbid": case "stopbid":
            case "listbid": case "clear": case "clearbid":
                return true;
            default: return false;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§5§l       DARK AUCTION COMMANDS");
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (sender.isOp() || sender.hasPermission("darkauction.admin")) {
            sender.sendMessage("");
            sender.sendMessage("§d§l┃ §f§lDISPLAY MODE §c[ADMIN]");
            sender.sendMessage("§d┃ §e/da create §8- §7Create auction NPC");
            sender.sendMessage("§d┃ §e/da limit <1|2> §8- §7Set global purchase limit per player");
            sender.sendMessage("§d┃ §e/da stock <amount|unlimited> §8- §7Set global stock per item");
            sender.sendMessage("§d┃ §e/da item1 <price> §8[stock] [limit] §8- §7Set slot 1 (overrides global)");
            sender.sendMessage("§d┃ §e/da item2 <price> §8[stock] [limit] §8- §7Set slot 2");
            sender.sendMessage("§d┃ §e/da item3 <price> §8[stock] [limit] §8- §7Set slot 3");
            sender.sendMessage("§d┃ §e/da list §8- §7View all items, stock & limits");
            sender.sendMessage("§d┃ §e/da clear §8- §7Clear all items");
            sender.sendMessage("§d┃ §e/da start §8- §7Start display");
            sender.sendMessage("§d┃ §e/da stop §8- §7Stop display");
            sender.sendMessage("");
            sender.sendMessage("§5§l┃ §f§lBID MODE §c[ADMIN]");
            sender.sendMessage("§5┃ §e/da bid §8- §7Add bid item (max 3)");
            sender.sendMessage("§5┃ §e/da listbid §8- §7View bid items");
            sender.sendMessage("§5┃ §e/da clearbid §8- §7Clear bid items");
            sender.sendMessage("§5┃ §e/da startbid §8- §7Start bid auction");
            sender.sendMessage("§5┃ §e/da stopbid §8- §7Stop bid auction");
            sender.sendMessage("");
        }

        sender.sendMessage("§a§l┃ §f§lPLAYER COMMANDS");
        sender.sendMessage("§a┃ §e/da displaymenu §8- §7Open display GUI");
        sender.sendMessage("§a┃ §e/da bidmenu §8- §7Open bid GUI");
        sender.sendMessage("§a┃ §e/da list §8- §7View current items");
        sender.sendMessage("§5§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private String formatCoins(long amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000.0);
        if (amount >= 1_000_000)     return String.format("%.1fM", amount / 1_000_000.0);
        if (amount >= 1_000)         return String.format("%.1fk", amount / 1_000.0);
        return String.format("%,d", amount);
    }
}