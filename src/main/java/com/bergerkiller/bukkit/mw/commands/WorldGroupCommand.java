package com.bergerkiller.bukkit.mw.commands;

import org.bukkit.ChatColor;

import com.bergerkiller.bukkit.mw.Permission;
import com.bergerkiller.bukkit.mw.WorldGroup;
import com.bergerkiller.bukkit.mw.WorldManager;

public class WorldGroupCommand extends Command {

	public WorldGroupCommand() {
		super(Permission.COMMAND_GROUP, "world.group");
	}

	public void execute() {
		if (args.length > 2) {
			if (args[0].equalsIgnoreCase("create")) {
				if (WorldGroup.get(args[1]) == null) {
					String world = WorldManager.matchWorld(args[2]);
					if (world != null) {
						WorldGroup.create(args[1], world);
						message(ChatColor.GREEN + "Created world group '" + args[1] + "' with default world '" + world + "'");
					} else {
						message(ChatColor.YELLOW + "World: '" + args[2] + "' doesn't exist!");
					}
				} else {
					message(ChatColor.YELLOW + "World group: '" + args[1] + "' already exists!");
				}
				return;
			} else if (args[0].equalsIgnoreCase("setdefaultworld")) {
				WorldGroup group = WorldGroup.get(args[1]);
				if (group != null) {
					String world = WorldManager.matchWorld(args[2]);
					if (world != null) {
						group.setDefaultWorld(world);
						message(ChatColor.GREEN + "Set default world to '" + world + "' in group '" + args[1] + "'");
					} else {
						message(ChatColor.YELLOW + "World: '" + args[2] + "' doesn't exist!");
					}
				} else {
					message(ChatColor.YELLOW + "World group: '" + args[1] + "' doesn't exist!");
				}
				return;
			} else if (args[0].equalsIgnoreCase("add")) {
				WorldGroup group = WorldGroup.get(args[1]);
				if (group != null) {
					String world = WorldManager.matchWorld(args[2]);
					if (world != null) {
						if (!group.hasWorld(world)) {
							group.add(world);
							message(ChatColor.GREEN + "Added world '" + world + "' to group '" + args[1] + "'");
						} else {
							message(ChatColor.YELLOW + "World '" + world + "' is already in group '" + args[1] + "'");
						}
					} else {
						message(ChatColor.YELLOW + "World: '" + args[2] + "' doesn't exist!");
					}
				} else {
					message(ChatColor.YELLOW + "World group: '" + args[1] + "' doesn't exist!");
				}
				return;
			} else if (args[0].equalsIgnoreCase("remove")) {
				WorldGroup group = WorldGroup.get(args[1]);
				if (group != null) {
					if (group.hasWorld(args[2])) {
						if (!group.getDefaultWorld().equals(args[2])) {
							group.remove(args[2]);
							message(ChatColor.GREEN + "Removed world '" + args[2] + "' from group '" + args[1] + "'");
						} else {
							message(ChatColor.YELLOW + "Cannot remove world '" + args[2]
									+ "' as it is the default world for the group '" + args[1]
									+ "' set a new one with /world group setdefaultworld " + args[1]
									+ " [world] then remove this world");
						}
					} else {
						message(ChatColor.YELLOW + "World '" + args[2] + "' isn't in group '" + args[1] + "'");
					}
				} else {
					message(ChatColor.YELLOW + "World group: '" + args[1] + "' doesn't exist!");
				}
				return;
			}
		} else if (args.length > 1) {
			if (args[0].equalsIgnoreCase("delete")) {
				if (WorldGroup.get(args[1]) != null) {
					WorldGroup.delete(args[1]);
					message(ChatColor.GREEN + "Deleted world group '" + args[1] + "'");
				} else {
					message(ChatColor.YELLOW + "World group: '" + args[1] + "' doesn't exist!");
				}
				return;
			}
		}
		// usage
		message(ChatColor.YELLOW + "/world group create [name] [default world]");
		message(ChatColor.YELLOW + "/world group delete [name]");
		message(ChatColor.YELLOW + "/world group add [name] [world]");
		message(ChatColor.YELLOW + "/world group remove [name] [world]");
		message(ChatColor.YELLOW + "/world group setdefaultworld [name] [default world]");
	}

}
