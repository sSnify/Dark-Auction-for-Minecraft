package org.ssnify.darkauction.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ssnify.darkauction.DarkAuction;

public class CoinsCommand implements CommandExecutor {
    private final DarkAuction plugin;

    public CoinsCommand(DarkAuction plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("darkauction.coins")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        } else if (args.length < 3) {
            sender.sendMessage("§cUsage: /coins <give|set|take> <player> <amount>");
            return true;
        } else {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            } else {
                long amount;
                try {
                    amount = Long.parseLong(args[2]);
                } catch (NumberFormatException var16) {
                    sender.sendMessage("§cInvalid amount!");
                    return true;
                }

                switch (args[0].toLowerCase()) {
                    case "give":
                        this.plugin.getCoinsManager().addCoins(target, amount);
                        String var12 = String.format("%,d", amount);
                        sender.sendMessage("§aGave " + var12 + " coins to " + target.getName());
                        Object[] var14 = new Object[]{amount};
                        target.sendMessage("§aYou received " + String.format("%,d", var14) + " coins!");
                        break;
                    case "set":
                        this.plugin.getCoinsManager().setCoins(target, amount);
                        String var11 = target.getName();
                        sender.sendMessage("§aSet " + var11 + "'s coins to " + String.format("%,d", amount));
                        Object[] var13 = new Object[]{amount};
                        target.sendMessage("§aYour coins have been set to " + String.format("%,d", var13));
                        break;
                    case "take":
                        this.plugin.getCoinsManager().removeCoins(target, amount);
                        String var10001 = String.format("%,d", amount);
                        sender.sendMessage("§aTook " + var10001 + " coins from " + target.getName());
                        Object[] var10002 = new Object[]{amount};
                        target.sendMessage("§c" + String.format("%,d", var10002) + " coins were taken from you!");
                        break;
                    default:
                        sender.sendMessage("§cUsage: /coins <give|set|take> <player> <amount>");
                }

                return true;
            }
        }
    }
}
