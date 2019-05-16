package unitedeffort;

import java.util.Collection;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public interface DatabaseConnection {

	public Collection<Role> getRoles();
	public void createRole(String name);
	public void removeRole(String name);
	public void setRoleColor(String name, ChatColor color);
	public void setRoleName(String name, String newName);
	public Role getRoleByName(String name);
	public Role getUserRole(Player player);
	public boolean setUserRole(Player player, String roleName);
	public void clearUserRole(Player player);
	public void close();
}
