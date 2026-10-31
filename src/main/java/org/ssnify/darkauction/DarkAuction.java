package org.ssnify.darkauction;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.ssnify.darkauction.commands.CoinsCommand;
import org.ssnify.darkauction.commands.DarkAuctionCommand;
import org.ssnify.darkauction.listeners.AuctionListener;
import org.ssnify.darkauction.listeners.NPCListener;
import org.ssnify.darkauction.listeners.PaymentBlockerListener;
import org.ssnify.darkauction.managers.AuctionManager;
import org.ssnify.darkauction.managers.CoinsManager;
import org.ssnify.darkauction.managers.NPCManager;

public class DarkAuction extends JavaPlugin {
    private static DarkAuction instance;
    private CoinsManager coinsManager;
    private AuctionManager auctionManager;
    private NPCManager npcManager;

    public DarkAuction() {
    }

    public void onEnable() {
        instance = this;
        this.coinsManager = new CoinsManager(this);
        this.auctionManager = new AuctionManager(this);
        this.npcManager = new NPCManager(this);
        this.getCommand("darkauction").setExecutor(new DarkAuctionCommand(this));
        this.getCommand("coins").setExecutor(new CoinsCommand(this));
        Bukkit.getPluginManager().registerEvents(new NPCListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AuctionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PaymentBlockerListener(this), this);
        this.scheduleWeeklyAuction();
        this.getLogger().info("DarkAuction has been enabled!");
    }

    private void scheduleWeeklyAuction() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Bucharest"));
            if (now.getDayOfWeek() == DayOfWeek.SATURDAY && now.getHour() == 20 && now.getMinute() == 0 && now.getSecond() < 20 && !this.auctionManager.getBidItems().isEmpty()) {
                this.auctionManager.startBidAuction();
            }

        }, 0L, 200L);
    }

    public void onDisable() {
        if (this.auctionManager != null) {
            this.auctionManager.stopBidAuction();
            this.auctionManager.stopDisplay();
        }

        this.getLogger().info("DarkAuction has been disabled!");
    }

    public static DarkAuction getInstance() {
        return instance;
    }

    public CoinsManager getCoinsManager() {
        return this.coinsManager;
    }

    public AuctionManager getAuctionManager() {
        return this.auctionManager;
    }

    public NPCManager getNPCManager() {
        return this.npcManager;
    }
}