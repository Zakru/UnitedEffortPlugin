package unitedeffort;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public class AFKManager {

	public static interface AFKUpdateHandler {
		public void onUpdate(Player p, boolean afk);
	}
	
	private long afkTime;
	private Map<Player, Long> players = new HashMap<>();
	private AFKUpdateHandler handler = (Player p, boolean afk)->{};
	
	public AFKManager(long afkTime) {
		this.afkTime = afkTime;
	}
	
	public void addPlayers(Collection<? extends Player> players) {
		for (Player p : players) {
			addPlayer(p);
		}
	}
	
	public void addPlayer(Player p) {
		this.players.put(p, 0L);
	}
	
	public void removePlayer(Player p) {
		this.players.remove(p);
	}
	
	public void tick() {
		for (Player p : players.keySet()) {
			long t = players.get(p) + 1;
			players.put(p, t);
			if (t == afkTime) handler.onUpdate(p, true);
		}
	}
	
	public void refresh(Player p) {
		boolean update = false;
		if (players.getOrDefault(p, 0L) >= afkTime) update = true;
		players.put(p, 0L);
		if (update) handler.onUpdate(p, false);
	}
	
	public void setAFK(Player p) {
		boolean update = false;
		if (players.getOrDefault(p, 0L) < afkTime) update = true;
		players.put(p, afkTime);
		if (update) handler.onUpdate(p, true);
	}
	
	public boolean isAFK(Player p) {
		return players.getOrDefault(p, 0L) >= afkTime;
	}
	
	public void setAFKUpdateHandler(AFKUpdateHandler h) {
		handler = h;
	}
}
