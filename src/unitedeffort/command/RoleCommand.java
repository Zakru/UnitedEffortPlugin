package unitedeffort.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import unitedeffort.Role;
import unitedeffort.UnitedEffortPlugin;

public class RoleCommand implements CommandExecutor, TabCompleter {
	
	private static final String[] subCommands = new String[] { "clear", "set", "create", "remove", "color", "rename" };

	private UnitedEffortPlugin plugin;
	
	public RoleCommand(UnitedEffortPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0) return false;
		
		Player p = null;
		if (sender instanceof Player) p = (Player) sender;
		
		switch (args[0]) {
		case "clear":
			if (args.length > 1) p = plugin.getServer().getPlayer(args[1]);
			if (p == null) return false;
			plugin.getDb().clearUserRole(p);
			plugin.updatePlayer(p);
			break;
		case "set":
			if (args.length == 1) return false;
			if (args.length > 2) p = plugin.getServer().getPlayer(args[2]);
			if (p == null) return false;
			if (!plugin.getDb().setUserRole(p, args[1])) sender.sendMessage("That role was not found.");
			plugin.updatePlayer(p);
			break;
		case "create":
			if (args.length == 1) return false;
			plugin.getDb().createRole(args[1]);
			break;
		case "remove":
			if (args.length == 1) return false;
			plugin.getDb().removeRole(args[1]);
			plugin.updatePlayers();
			break;
		case "color":
			if (args.length < 3) return false;
			plugin.getDb().setRoleColor(args[1], ChatColor.valueOf(args[2].toUpperCase()));
			plugin.updatePlayers();
			break;
		case "rename":
			if (args.length < 3) return false;
			plugin.getDb().setRoleName(args[1], args[2]);
			plugin.updatePlayers();
			break;
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			List<String> list = new ArrayList<>();
			for (String s : subCommands) {
				if (s.startsWith(args[0])) {
					list.add(s);
				}
			}
			return list;
		} else {
			switch (args[0]) {
			case "clear":
				if (args.length == 2) {
					return null;
				}
				break;
			case "set":
				if (args.length == 2) {
					List<String> list = new ArrayList<>();
					for (Role r : plugin.getDb().getRoles()) {
						if (r.name.startsWith(args[1])) {
							list.add(r.name);
						}
					}
					return list;
				}
				if (args.length == 3) {
					return null;
				}
				break;
			case "create":
				break;
			case "remove":
				if (args.length == 2) {
					List<String> list = new ArrayList<>();
					for (Role r : plugin.getDb().getRoles()) {
						if (r.name.startsWith(args[1])) {
							list.add(r.name);
						}
					}
					return list;
				}
				break;
			case "color":
				if (args.length == 2) {
					List<String> list = new ArrayList<>();
					for (Role r : plugin.getDb().getRoles()) {
						if (r.name.startsWith(args[1])) {
							list.add(r.name);
						}
					}
					return list;
				}
				if (args.length == 3) {
					List<String> list = new ArrayList<>();
					for (ChatColor c : ChatColor.values()) {
						if (c.name().toLowerCase().startsWith(args[2])) {
							list.add(c.name().toLowerCase());
						}
					}
					return list;
				}
				break;
			case "rename":
				if (args.length == 2) {
					List<String> list = new ArrayList<>();
					for (Role r : plugin.getDb().getRoles()) {
						if (r.name.startsWith(args[1])) {
							list.add(r.name);
						}
					}
					return list;
				}
				break;
			}
		}
		
		return new ArrayList<>();
	}
}
