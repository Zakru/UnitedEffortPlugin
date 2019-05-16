package unitedeffort;

import java.util.Collection;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class SleepController {

	public static interface SleepHandler {
		public void run(Player p, double amount, double target);
	}
	
	private double limit;
	private boolean inclusive;
	private SleepHandler sleepHandler;
	
	public SleepController(double limit, boolean inclusive) {
		this.limit = limit;
		this.inclusive = inclusive;
	}
	
	public boolean test(World w, AFKManager afk, Collection<? extends Player> players) {
		int valid = 0;
		int n = 0;
		for (Player p : players) {
			if (p.getWorld() != w || (afk != null && afk.isAFK(p))) continue;
			
			valid++;
			if (p.getSleepTicks() >= 100) {
				n++;
				if (p.getSleepTicks() == 100) {
					sleepHandler.run(p, (double)n/(double)valid, limit);
				}
			}
		}
		if (valid == 0) return false;
		if (inclusive) return (double)n/(double)valid >= limit;
		return (double)n/(double)valid > limit;
	}
	
	public void setSleepHandler(SleepHandler r) {
		sleepHandler = r;
	}
}
