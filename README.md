# MyWorlds
[Spigot Resource Page](https://www.spigotmc.org/resources/myworlds.39594/) | [Dev Builds](https://ci.mg-dev.eu/job/MyWorlds/)

World Management plugin for Bukkit.

# Changes from original:
* Removed unwanted (in my case (breaks immersion)) teleport messages.
* Added world groups

## World Groups
World groups are a portal destination that picks the last world of a set of worlds the player was on.

Ex. You have one portal at spawn that you want to go to your survival world. In your survival world you have the nether, the end, and the overworld each of them have remember last position on so you will return to the last place you were at, but you want your portal to also go to the last world of those three the player was in. Otherwise somebody could go to the nether, log off, and when they rejoined they would be back in the overworld, and loose their position in the nether. Not fun!

If a player has not been to a world group before the portal will send them to the default world of the group.

### Usage
* /world group create [name] [default world]  - Create a new world group with the default world specified.
* /world group delete [name]  - Delete the world group specified.
* /world group setdefaultworld [name] [default world]  - Change the default world for a group.
* /world group add [name] [world]  - Add a world to a group.
* /world group remove [name] [world]  - Remove a world from a group.

Then to use a world group just put it on the destination line of a portal sign or use /tpp [world group]
