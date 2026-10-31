package org.ssnify.darkauction.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.ssnify.darkauction.DarkAuction;

import java.util.*;

public class AuctionManager {

    private final DarkAuction plugin;

    // Display mode
    private boolean displayActive = false;
    private List<ItemStack> displayItems = new ArrayList<>(Arrays.asList(null, null, null));
    private List<Long> displayPrices = new ArrayList<>(Arrays.asList(0L, 0L, 0L));

    // Stock per slot (-1 = unlimited)
    private int[] slotStock = {-1, -1, -1};
    // Per-slot purchase limit (-1 = use global default)
    private int[] slotLimit = {-1, -1, -1};

    // Global default: how many items a player can buy total per auction (1 or 2)
    private int globalPurchaseLimit = 2;
    // Global default stock (-1 = unlimited) — applied to all slots unless overridden
    private int globalStock = -1;

    // Per-player purchase count
    private Map<UUID, Integer> playerPurchases = new HashMap<>();
    // Per-player per-slot purchase count (for per-slot limits)
    private Map<UUID, int[]> playerSlotPurchases = new HashMap<>();

    // Bid auction mode
    private boolean bidAuctionActive = false;
    private List<ItemStack> bidItems = new ArrayList<>();
    private int currentItemIndex = -1;
    private ItemStack currentItem;
    private Map<UUID, Long> bids = new HashMap<>();
    private UUID highestBidder;
    private long highestBid = 0;
    private BukkitTask auctionTask;
    private int timeLeft = 30;
    private Set<UUID> participants = new HashSet<>();

    public AuctionManager(DarkAuction plugin) {
        this.plugin = plugin;
    }

    public boolean isAuctionActive() {
        return displayActive || bidAuctionActive;
    }

    // ===== GLOBAL SETTINGS =====

    public void setGlobalPurchaseLimit(int limit) {
        this.globalPurchaseLimit = Math.max(1, Math.min(2, limit));
    }

    public int getGlobalPurchaseLimit() {
        return globalPurchaseLimit;
    }

    public void setGlobalStock(int stock) {
        this.globalStock = stock <= 0 ? -1 : stock;
    }

    public int getGlobalStock() {
        return globalStock;
    }

    // ===== PER-SLOT OVERRIDES =====

    public void setSlotStock(int slot, int stock) {
        if (slot < 0 || slot >= 3) return;
        slotStock[slot] = stock <= 0 ? -1 : stock;
    }


    public int getEffectiveStock(int slot) {
        if (slot < 0 || slot >= 3) return -1;
        return slotStock[slot] != -1 ? slotStock[slot] : globalStock;
    }

    public void setSlotLimit(int slot, int limit) {
        if (slot < 0 || slot >= 3) return;
        slotLimit[slot] = limit <= 0 ? -1 : limit;
    }

    public int getEffectiveLimit(int slot) {
        if (slot < 0 || slot >= 3) return globalPurchaseLimit;
        return slotLimit[slot] != -1 ? slotLimit[slot] : globalPurchaseLimit;
    }

    // ===== DISPLAY MODE METHODS =====

    public boolean isDisplayActive() {
        return displayActive;
    }

    public void setDisplayItem(int slot, ItemStack item, long price) {
        if (slot < 0 || slot >= 3) return;
        if (displayItems.size() <= slot) return;
        displayItems.set(slot, item);
        displayPrices.set(slot, price);
    }

    public List<ItemStack> getDisplayItems() {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : displayItems) {
            if (item != null) items.add(item);
        }
        return items;
    }

    public ItemStack getDisplayItem(int slot) {
        if (slot < 0 || slot >= 3) return null;
        return displayItems.get(slot);
    }

    public long getDisplayPrice(int slot) {
        if (slot < 0 || slot >= 3) return 0;
        return displayPrices.get(slot);
    }

    public void clearDisplayItems() {
        displayItems = new ArrayList<>(Arrays.asList(null, null, null));
        displayPrices = new ArrayList<>(Arrays.asList(0L, 0L, 0L));
        slotStock = new int[]{-1, -1, -1};
        slotLimit = new int[]{-1, -1, -1};
    }

    public void startDisplay() {
        if (displayActive) return;
        displayActive = true;
        playerPurchases.clear();
        playerSlotPurchases.clear();

        Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §dThe Dark Auction is now open!");
        Bukkit.broadcastMessage("§7Right-click the Dark Auction NPC to view items!");
    }

    public void stopDisplay() {
        if (!displayActive) return;
        displayActive = false;
        playerPurchases.clear();
        playerSlotPurchases.clear();
        Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §cThe Dark Auction has been closed!");
    }

    public boolean canPurchase(Player player, int slot) {
        UUID uuid = player.getUniqueId();

        // Check global total purchases
        int totalPurchases = playerPurchases.getOrDefault(uuid, 0);
        if (totalPurchases >= globalPurchaseLimit) return false;

        // Check per-slot purchases against slot's effective limit
        int slotPurchases = getPlayerSlotPurchases(uuid, slot);
        if (slotPurchases >= getEffectiveLimit(slot)) return false;

        // Check stock
        int stock = getEffectiveStock(slot);
        if (stock == 0) return false;

        return true;
    }

    public boolean canPurchase(Player player) {
        int purchases = playerPurchases.getOrDefault(player.getUniqueId(), 0);
        return purchases < globalPurchaseLimit;
    }

    public int getPurchaseCount(Player player) {
        return playerPurchases.getOrDefault(player.getUniqueId(), 0);
    }

    private int getPlayerSlotPurchases(UUID uuid, int slot) {
        int[] slotCounts = playerSlotPurchases.get(uuid);
        if (slotCounts == null) return 0;
        return slotCounts[slot];
    }

    private void incrementPlayerSlotPurchases(UUID uuid, int slot) {
        int[] slotCounts = playerSlotPurchases.computeIfAbsent(uuid, k -> new int[3]);
        slotCounts[slot]++;
    }

    public boolean purchaseItem(Player player, int slot) {
        if (!displayActive) return false;
        if (!canPurchase(player, slot)) return false;

        ItemStack item = getDisplayItem(slot);
        long price = getDisplayPrice(slot);

        if (item == null) return false;
        if (!plugin.getCoinsManager().hasCoins(player, price)) return false;

        plugin.getCoinsManager().removeCoins(player, price);
        player.getInventory().addItem(item.clone());

        UUID uuid = player.getUniqueId();

        playerPurchases.put(uuid, playerPurchases.getOrDefault(uuid, 0) + 1);

        incrementPlayerSlotPurchases(uuid, slot);

        if (slotStock[slot] > 0) {
            slotStock[slot]--;
        } else if (slotStock[slot] == -1 && globalStock > 0) {
            globalStock--;
        }

        int total = getPurchaseCount(player);
        player.sendMessage("§aᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §fPurchase successful! §7(" + total + "/" + globalPurchaseLimit + " items purchased)");
        return true;
    }

    // ===== BID AUCTION MODE METHODS =====

    public boolean isBidAuctionActive() { return bidAuctionActive; }

    public void setBidItems(List<ItemStack> items) { this.bidItems = new ArrayList<>(items); }

    public List<ItemStack> getBidItems() { return new ArrayList<>(bidItems); }

    public void startBidAuction() {
        if (bidAuctionActive) return;
        if (bidItems.isEmpty()) {
            Bukkit.broadcastMessage("§cᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ No items set for bid auction!");
            return;
        }
        bidAuctionActive = true;
        currentItemIndex = -1;
        participants.clear();
        Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §dThe Dark Auction (Bidding) has started!");
        Bukkit.broadcastMessage("§7Right-click the Dark Auction NPC to participate!");
        nextItem();
    }

    private void nextItem() {
        currentItemIndex++;
        if (currentItemIndex >= bidItems.size()) { endBidAuction(); return; }
        currentItem = bidItems.get(currentItemIndex);
        bids.clear();
        highestBidder = null;
        highestBid = 0;
        timeLeft = 30;
        Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §dNow auctioning: §f" +
                (currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()
                        ? currentItem.getItemMeta().getDisplayName()
                        : currentItem.getType().name()));
        startTimer();
    }

    private void startTimer() {
        if (auctionTask != null) auctionTask.cancel();
        auctionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            timeLeft--;
            if (timeLeft <= 0) {
                finishCurrentItem();
            } else if (timeLeft % 10 == 0 || timeLeft <= 5) {
                Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §7Time left: §e" + timeLeft + "s §7| Highest bid: §6" +
                        (highestBid > 0 ? formatCoins(highestBid) + " coins" : "No bids"));
            }
        }, 20L, 20L);
    }

    private void finishCurrentItem() {
        if (auctionTask != null) auctionTask.cancel();
        if (highestBidder != null) {
            Player winner = Bukkit.getPlayer(highestBidder);
            if (winner != null) {
                winner.getInventory().addItem(currentItem);
                plugin.getCoinsManager().removeCoins(winner, highestBid);
                Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §a" + winner.getName() + " §dwon the item for §6" + formatCoins(highestBid) + " coins§d!");
            }
            for (Map.Entry<UUID, Long> entry : bids.entrySet()) {
                if (entry.getKey().equals(highestBidder)) continue;
                Player bidder = Bukkit.getPlayer(entry.getKey());
                if (bidder != null) {
                    plugin.getCoinsManager().addCoins(bidder, entry.getValue());
                    bidder.sendMessage("§eᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Your bid of §6" + formatCoins(entry.getValue()) + " coins §ehas been refunded.");
                }
            }
        } else {
            Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §cNo bids placed! Item withdrawn.");
        }
        Bukkit.getScheduler().runTaskLater(plugin, this::nextItem, 60L);
    }

    public void stopBidAuction() {
        if (!bidAuctionActive) return;
        if (auctionTask != null) auctionTask.cancel();
        for (Map.Entry<UUID, Long> entry : bids.entrySet()) {
            Player bidder = Bukkit.getPlayer(entry.getKey());
            if (bidder != null) {
                plugin.getCoinsManager().addCoins(bidder, entry.getValue());
                bidder.sendMessage("§eᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ Auction stopped! Your bid of §6" + formatCoins(entry.getValue()) + " coins §ehas been refunded.");
            }
        }
        bidAuctionActive = false;
        participants.clear();
        Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §cThe bid auction has been stopped!");
    }

    private void endBidAuction() {
        if (auctionTask != null) auctionTask.cancel();
        bidAuctionActive = false;
        participants.clear();
        Bukkit.broadcastMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §dThe Dark Auction (Bidding) has ended!");
    }

    public boolean canJoinBidAuction(Player player) {
        if (!bidAuctionActive) return false;
        return plugin.getCoinsManager().getCoins(player) == 30_000_000L;
    }

    public void addParticipant(Player player) { participants.add(player.getUniqueId()); }

    public boolean isParticipant(Player player) { return participants.contains(player.getUniqueId()); }

    public void placeBid(Player player, long amount) {
        if (!isParticipant(player)) return;
        if (amount <= highestBid) return;
        long currentBid = bids.getOrDefault(player.getUniqueId(), 0L);
        long additionalAmount = amount - currentBid;
        if (!plugin.getCoinsManager().hasCoins(player, additionalAmount)) return;
        plugin.getCoinsManager().removeCoins(player, additionalAmount);
        bids.put(player.getUniqueId(), amount);
        highestBidder = player.getUniqueId();
        highestBid = amount;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isParticipant(p)) {
                p.sendMessage("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴ §a" + player.getName() + " §dbid §6" + formatCoins(amount) + " coins§d!");
            }
        }
        if (timeLeft < 5) timeLeft = 5;
    }

    public long getPlayerBid(Player player) { return bids.getOrDefault(player.getUniqueId(), 0L); }

    public ItemStack getCurrentItem() { return currentItem; }
    public long getHighestBid() { return highestBid; }
    public String getHighestBidderName() {
        if (highestBidder == null) return "None";
        Player p = Bukkit.getPlayer(highestBidder);
        return p != null ? p.getName() : "Unknown";
    }
    public int getTimeLeft() { return timeLeft; }

    private String formatCoins(long amount) { return String.format("%,d", amount); }
}