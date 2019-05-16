package unitedeffort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class PlaintextDatabaseConnection implements DatabaseConnection {

	private String path;
	private List<Role> roles = new ArrayList<>();
	private Map<UUID, Role> userRoles = new HashMap<>();
	
	public PlaintextDatabaseConnection(String path) {
		this.path = path;
		File f = new File(path);
		f.getParentFile().mkdirs();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			
			// Read role count
			int roleCount = Integer.parseInt(reader.readLine());
			
			// Read roles
			for (int i=0;i<roleCount;i++) {
				String[] parts = reader.readLine().split(":");
				roles.add(new Role(parts[0], ChatColor.valueOf(parts[1])));
			}
			
			// Read user roles
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(":");
				userRoles.put(UUID.fromString(parts[0]), getRoleByName(parts[1]));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// Ok
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Collection<Role> getRoles() {
		return roles;
	}

	@Override
	public void createRole(String name) {
		roles.add(new Role(name, ChatColor.WHITE));
	}

	@Override
	public void removeRole(String name) {
		Role role = getRoleByName(name);
		for (Entry<UUID, Role> e : userRoles.entrySet()) {
			if (e.getValue() == role) {
				userRoles.remove(e.getKey());
			}
		}
		roles.remove(role);
	}

	@Override
	public void setRoleColor(String name, ChatColor color) {
		getRoleByName(name).color = color;
	}

	@Override
	public void setRoleName(String name, String newName) {
		getRoleByName(name).name = newName;
	}
	
	@Override
	public Role getRoleByName(String name) {
		for (Role r : roles) {
			if (r.name.equals(name)) {
				return r;
			}
		}
		return null;
	}
	
	@Override
	public Role getUserRole(Player p) {
		return userRoles.get(p.getUniqueId());
	}

	@Override
	public boolean setUserRole(Player p, String roleName) {
		Role role = getRoleByName(roleName);
		if (role == null) return false;
		userRoles.put(p.getUniqueId(), role);
		return true;
	}

	@Override
	public void clearUserRole(Player p) {
		userRoles.remove(p.getUniqueId());
	}

	@Override
	public void close() {
		File f = new File(path);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			
			// Write role count
			writer.write(String.valueOf(roles.size()));
			writer.newLine();
			
			// Write roles
			for (Role r : roles) {
				writer.write(String.format("%s:%s", r.name, r.color.name()));
				writer.newLine();
			}
			
			// Write user roles
			for (Entry<UUID, Role> e : userRoles.entrySet()) {
				writer.write(String.format("%s:%s", e.getKey().toString(), e.getValue().name));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
