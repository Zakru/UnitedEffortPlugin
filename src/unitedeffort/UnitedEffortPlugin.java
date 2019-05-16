package unitedeffort;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import unitedeffort.command.RoleCommand;

public class UnitedEffortPlugin extends JavaPlugin implements Listener {

	private DatabaseConnection db;
	private AFKManager afk;
	private SleepController sleep;
	
	private Map<Player, String> fullNames = new HashMap<>();
	
	private final Role fdsa = new Role("fdsa", ChatColor.AQUA);
	private static final String afkPrefix = ChatColor.GRAY + "[AFK] " + ChatColor.RESET;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		FileConfiguration config = getConfig();
		
		db = new PlaintextDatabaseConnection("unitedeffort/roles");
		
		if (config.getLong("afkTime", 12000L) > 0) {
			afk = new AFKManager(config.getLong("afkTime", 12000L));
			String afkBegin = config.getString("afkBeginMessage", "%player% is now AFK");
			String afkEnd = config.getString("afkEndMessage", "%player% is no longer AFK");
			afk.setAFKUpdateHandler((Player p, boolean afk)->{
				if (afk && !afkBegin.equals("")) {
					Bukkit.broadcastMessage(afkBegin.replace("%player%", fullNames.get(p)));
				}
				updatePlayer(p);
				if (!afk && !afkEnd.equals("")) {
					Bukkit.broadcastMessage(afkEnd.replace("%player%", fullNames.get(p)));
				}
			});
			afk.addPlayers(getServer().getOnlinePlayers());
		}
		
		if (config.getDouble("sleepLimit", 1d) < 1) {
			sleep = new SleepController(config.getDouble("sleepLimit"), config.getBoolean("sleepInclusive", true));
			String sleepMessage = config.getString("sleepMessage", "%player% is now sleeping (%percentage%)");
			String sleepSkipMessage = config.getString("sleepSkipMessage", "Good morning!");
			if (!sleepMessage.equals(""))
				sleep.setSleepHandler((Player p, double amount, double target)->{
					Bukkit.broadcastMessage(sleepMessage
							.replace("%player%", fullNames.get(p))
							.replace("%percentage%", 
									String.format("%.0f/%.0f%%", amount*100, target*100)));
				});
			World w = getServer().getWorlds().get(0);
			Bukkit.getScheduler().runTaskTimer(this, ()->{
				if (sleep.test(w, afk, getServer().getOnlinePlayers())) {
					if (w.getTime() >= 12541 && w.getTime() <= 23458) {
						w.setTime(0);
						if (!sleepSkipMessage.equals("")) Bukkit.broadcastMessage(sleepSkipMessage);
					}
				}
			}, 0, 1);
		}
		
		updatePlayers();
		
		PluginCommand roleCommand = getCommand("role");
		RoleCommand roleHandler = new RoleCommand(this);
		roleCommand.setExecutor(roleHandler);
		roleCommand.setTabCompleter(roleHandler);
		
		getCommand("afk").setExecutor((CommandSender sender, Command command, String alias, String args[])->{
			if (!(sender instanceof Player)) return false;
			afk.setAFK((Player)sender);
			return true;
		});
		
		getServer().getPluginManager().registerEvents(this, this);
		
		Bukkit.getScheduler().runTaskTimer(this, afk::tick, 0, 1);
	}
	
	@Override
	public void onDisable() {
		getDb().close();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		afk.addPlayer(e.getPlayer());
		updatePlayer(e.getPlayer());
		e.setJoinMessage(String.format("%s joined the game", fullNames.get(e.getPlayer())));
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		afk.removePlayer(e.getPlayer());
		updatePlayer(e.getPlayer());
		e.setQuitMessage(String.format("%s left the game", fullNames.get(e.getPlayer())));
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		afk.refresh(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		e.setCancelled(true);
		Bukkit.broadcastMessage(String.format("%s :  %s",
				fullNames.get(e.getPlayer()),
				e.getMessage()));
	}
	
	public void updatePlayer(Player p) {
		Role r = getDb().getUserRole(p);
		if (p.getUniqueId().toString().equals("81b98fae-1282-4a2e-8df1-72c1741a2e6d")) r = fdsa;
		String name;
		if (r != null) {
			name = String.format("%s[%s]%s %s",
					r.color,
					r.name,
					ChatColor.RESET,
					p.getName());
		} else {
			name = p.getName();
		}
		
		if (afk != null && afk.isAFK(p)) {
			name = afkPrefix + name;
		}
		
		fullNames.put(p, name);
		p.setPlayerListName(name);
	}
	
	public void updatePlayers() {
		for (Player p : getServer().getOnlinePlayers()) {
			updatePlayer(p);
		}
	}

	public DatabaseConnection getDb() {
		return db;
	}
}
