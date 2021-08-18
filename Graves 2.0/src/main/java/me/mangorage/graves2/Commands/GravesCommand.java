package me.mangorage.graves2.Commands;

import me.mangorage.graves2.PlayerGraveManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GravesCommand implements CommandExecutor {
    private PlayerGraveManager manager;

    public GravesCommand(PlayerGraveManager PGM) {
        manager = PGM;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player plr = (Player) sender;

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("Menu") && plr.hasPermission("Graves.menu.Use")) {
                    manager.openMenu(plr);
                }
            }
        }

        return true;
    }
}
