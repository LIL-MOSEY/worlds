package me.speednt.worlds;

import me.speednt.worlds.command.WorldCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class Worlds extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(Level.INFO, "Enabled " + this.getName());
        this.getCommand("world").setExecutor(new WorldCommand());

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(Level.INFO, "Disabled " + this.getName());
    }
}
