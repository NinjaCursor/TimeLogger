# TimeLogger

A simple, yet expandable, logger for logging in and out of Spigot, Bukkit, or Paper servers. 

# What is required?

1. Minecraft Server
2. Access to a MySQL Server

# API

The API, detailed [here](https://github.com/NinjaCursor/TimeLogger/blob/6cdbe190bb7479941bb9dfd8fc8d1c60685415c4/src/main/java/TimeSheet/Main/PluginInterfacePublic.java#L6-L31), allows
other plugins to get summary (e.g., total time, first time, how many logins total) and detailed logs (i.e., every logged event for a given player and given event) for individual players.

To use the API in another plugin, add the following to the onEnable method.

```
TimeSheet timeSheet = (TimeSheet) Bukkit.getPluginManager().getPlugin("TimeSheet");
CompletableFuture<TimeSheetAPI> api_future = timeSheet.getAPI();
api_future.thenAccept((api) -> { CODE_USING_API; });
```
