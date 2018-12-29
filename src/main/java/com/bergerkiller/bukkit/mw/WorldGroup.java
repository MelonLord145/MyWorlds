package com.bergerkiller.bukkit.mw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.config.FileConfiguration;

public class WorldGroup {
    private static final HashMap<String, WorldGroup> groups = new HashMap<String, WorldGroup>();
    
    public static WorldGroup create(String groupName, String defaultWorld) {
    	WorldGroup group = new WorldGroup(defaultWorld);
    	groups.put(groupName, group);
        return group;
    }
    
    public static void delete(String groupName) {
    	groups.remove(groupName);
    }
    
    public static WorldGroup get(String groupName) {
    	return groups.get(groupName);
    }
    
	public static HashMap<String, WorldGroup> getAll() {
        return groups;
    }
	
	public static Set<WorldGroup> getGroupsContainingWorld(String world) {
		Set<WorldGroup> groupSet = new HashSet<WorldGroup>();
		for (WorldGroup group : groups.values()) {
			if (group.hasWorld(world)) {
				groupSet.add(group);
			}
		}
		return groupSet;
	}
	
	public static void savePlayerWorld(Player player) {
		String currentWorld = player.getWorld().getName();
		for (WorldGroup group : getGroupsContainingWorld(currentWorld)) {
			group.setLastWorld(player.getUniqueId(), currentWorld);
		}
	}

    public static void load() {
        groups.clear();
        FileConfiguration config = new FileConfiguration(MyWorlds.plugin, "worldgroups.yml");
        config.load();
        for (ConfigurationNode node : config.getNodes()) {
        	String defaultWorld = node.get("defaultWorld", String.class);
            List<String> worlds = node.getList("worlds", String.class);
            Map<String, String> playerData = node.getNode("playerData").getValues(String.class);
            
            if (defaultWorld == null) {
                continue;
            }
            
            WorldGroup group = create(node.getName(), defaultWorld);
            
            for (String world : worlds) {
                group.add(world);
            }
            
            for (Entry<String, String> entry : playerData.entrySet()) {
            	String uuidString = entry.getKey();
            	String world = entry.getValue();
            	UUID player;
            	
            	try {
            		player = UUID.fromString(uuidString);
            	} 
            	catch (IllegalArgumentException e) {
            		continue;
            	}
            	
            	group.setLastWorld(player, world);
            }
        }
    }

    public static void save() {
        FileConfiguration config = new FileConfiguration(MyWorlds.plugin, "worldgroups.yml");
        for (Entry<String, WorldGroup> entry : groups.entrySet()) {
            String name = entry.getKey();
            WorldGroup group = entry.getValue();
            
            ConfigurationNode node = config.getNode(name);
            node.set("worlds", new ArrayList<String>(group.worlds));
            node.set("defaultWorld", group.getDefaultWorld());
            
            ConfigurationNode playerData = node.getNode("playerData");
            
            for (Entry<UUID, String> playerEntry : group.playerData.entrySet()) {
            	String uuidString = playerEntry.getKey().toString();
            	String lastWorld = playerEntry.getValue();
            	
            	playerData.set(uuidString, lastWorld);
            }
        }
        config.save();
    }
    
    private String defaultWorld;
    private final Set<String> worlds = new HashSet<String>();
    private final HashMap<UUID, String> playerData = new HashMap<UUID, String>();
    
    private WorldGroup(String defaultWorld) {
    	this.defaultWorld = defaultWorld;
    }
    
    public void add(String world) {
    	this.worlds.add(world);
    }
    
    public void remove(String world) {
    	this.worlds.remove(world);
    	
    	Iterator<String> iterator = playerData.values().iterator();
    	
    	while(iterator.hasNext()) {
    		if (iterator.next().equals(world)) {
    			iterator.remove();
    		}
    	}
    }
    
    public void setLastWorld(UUID player, String world) {
    	this.playerData.put(player, world);
    }
    
    public void removePlayer(UUID player) {
    	this.playerData.remove(player);
    }
    
    public boolean hasWorld(String world) {
    	return this.worlds.contains(world);
    }
    
    public String getDefaultWorld() {
    	return this.defaultWorld;
    }
    
    public void setDefaultWorld(String defaultWorld) {
    	this.defaultWorld = defaultWorld;
    }
    
    public String getWorldForPlayer(UUID player) {
    	if (this.playerData.containsKey(player)) {
    		return this.playerData.get(player);
    	}
    	else {
    		return this.getDefaultWorld();
    	}
    }
}
