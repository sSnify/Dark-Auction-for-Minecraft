package org.ssnify.darkauction.managers;

import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class CoinsManager {
    private final Plugin plugin;
    private Economy economy;

    public CoinsManager(Plugin plugin) {
        this.plugin = plugin;
        this.setupEconomy();
    }

    private boolean setupEconomy() {
        if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.plugin.getLogger().warning("Vault not found! CoinsManager will not work.");
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                this.plugin.getLogger().warning("No economy plugin found! Please install an economy plugin like EssentialsX.");
                return false;
            } else {
                this.economy = (Economy)rsp.getProvider();
                Logger var10000 = this.plugin.getLogger();
                String var10001 = this.economy.getName();
                var10000.info("Hooked into " + var10001 + " via Vault!");
                return this.economy != null;
            }
        }
    }

    public long getCoins(Player player) {
        return this.economy == null ? 0L : (long)this.economy.getBalance(player);
    }

    public void setCoins(Player player, long amount) {
        if (this.economy != null) {
            double currentBalance = this.economy.getBalance(player);
            double difference = (double)amount - currentBalance;
            if (difference > (double)0.0F) {
                this.economy.depositPlayer(player, difference);
            } else if (difference < (double)0.0F) {
                this.economy.withdrawPlayer(player, Math.abs(difference));
            }
        }

    }

    public void addCoins(Player player, long amount) {
        if (this.economy != null) {
            this.economy.depositPlayer(player, (double)amount);
        }

    }

    public void removeCoins(Player player, long amount) {
        if (this.economy != null) {
            this.economy.withdrawPlayer(player, (double)amount);
        }

    }

    public boolean hasCoins(Player player, long amount) {
        return this.economy == null ? false : this.economy.has(player, (double)amount);
    }
}
