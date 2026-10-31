package org.ssnify.darkauction.managers;

import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.ssnify.darkauction.DarkAuction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCManager {

    private final DarkAuction plugin;
    private final Map<UUID, Villager> auctionNPCs = new HashMap<>();

    public NPCManager(DarkAuction plugin) {
        this.plugin = plugin;
    }

    public Villager createNPC(Location location) {
        Villager villager = location.getWorld().spawn(location, Villager.class, v -> {
            v.setCustomName("§5§lᴅᴀʀᴋ ᴀᴜᴄᴛɪᴏɴᴇᴇʀ");
            v.setCustomNameVisible(true);
            v.setProfession(Villager.Profession.CLERIC);
            v.setVillagerType(Villager.Type.PLAINS);
            v.setVillagerLevel(5);
            v.setInvulnerable(true);
            v.setAI(false);
            v.setCollidable(false);
            v.setCanPickupItems(false);
            v.setPersistent(true);
            v.setSilent(true);
            v.setRecipes(new java.util.ArrayList<>());
        });

        auctionNPCs.put(villager.getUniqueId(), villager);
        plugin.getLogger().info("Dark Auction Cleric NPC spawned at " + location);
        return villager;
    }

    public boolean isAuctionNPC(UUID entityId) {
        return auctionNPCs.containsKey(entityId);
    }

    public void removeNPC(UUID entityId) {
        Villager v = auctionNPCs.remove(entityId);
        if (v != null && v.isValid()) {
            v.remove();
            plugin.getLogger().info("Removed Dark Auction NPC " + entityId);
        }
    }

    public void removeAllNPCs() {
        for (Villager v : auctionNPCs.values()) {
            if (v != null && v.isValid()) v.remove();
        }
        auctionNPCs.clear();
        plugin.getLogger().info("Removed all Dark Auction NPCs.");
    }

    public Map<UUID, Villager> getAuctionNPCs() {
        return new HashMap<>(auctionNPCs);
    }
}