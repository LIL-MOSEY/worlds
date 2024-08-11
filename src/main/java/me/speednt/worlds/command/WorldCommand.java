package me.speednt.worlds.command;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class WorldCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You don't have the required permissions to run this command.");
        }

        if (args.length < 1) {
            return false;
        }

        String action = args[0];

        switch (action.toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    sender.sendMessage("/world create <overworld|nether|end> <name>");
                } else {
                    createWorld(sender, args[1], args[2]);
                }
                break;

            case "delete":
                if (args.length < 3) {
                    sender.sendMessage("/world delete <overworld|nether|end> <name>");
                } else {
                    deleteWorld(sender, args[1], args[2]);
                }
                break;

            case "teleport":
            case "tp":
                if (args.length < 2) {
                    sender.sendMessage("/world teleport <name> [x y z]");
                } else {
                    teleportToWorld(sender, args[1], args);
                }
                break;

            case "info":
                displayWorldInfo(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Invalid action. Use 'create', 'delete', 'teleport', or 'info'.");
                break;
        }

        return true;
    }

    private void createWorld(CommandSender sender, String type, String name) {
        WorldCreator creator = new WorldCreator(name);

        switch (type.toLowerCase()) {
            case "overworld":
                creator.environment(World.Environment.NORMAL);
                creator.type(WorldType.NORMAL);
                break;
            case "nether":
                creator.environment(World.Environment.NETHER);
                break;
            case "end":
                creator.environment(World.Environment.THE_END);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Invalid world type. Use 'overworld', 'nether', or 'end'.");
                return;
        }

        World world = creator.createWorld();
        if (world != null) {
            sender.sendMessage(ChatColor.GRAY + "World '" + name + "' created successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to create world '" + name + "'.");
        }
    }

    private void deleteWorld(CommandSender sender, String type, String name) {
        World world = Bukkit.getWorld(name);

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World '" + name + "' does not exist.");
            return;
        }

        // Check if the sender is in the world they want to delete
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.getWorld().equals(world)) {
                sender.sendMessage(ChatColor.RED + "You cannot delete the world you are currently in. Please teleport to another world first.");
                return;
            }
        }

        // Check if any other players are in the world
        if (!world.getPlayers().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There are players in the world. Please ensure all players leave the world before deleting it.");
            return;
        }

        // Unload the world
        Bukkit.unloadWorld(world, false);

        // Delete the world folder
        File worldFolder = new File(Bukkit.getServer().getWorldContainer(), world.getName());
        if (deleteWorldFolder(worldFolder)) {
            sender.sendMessage(ChatColor.GRAY + "World '" + name + "' deleted successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete world '" + name + "'.");
        }
    }


    private boolean deleteWorldFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteWorldFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return path.delete();
    }

    private void teleportToWorld(CommandSender sender, String worldName, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "World '" + worldName + "' does not exist. Please recheck the world names.");
            return;
        }

        Location location;

        if (args.length == 2) {
            // Teleport to the world's spawn point
            location = world.getSpawnLocation();
        } else if (args.length == 5) {
            // Parse the coordinates and teleport to them
            try {
                double x = Double.parseDouble(args[2]);
                double y = Double.parseDouble(args[3]);
                double z = Double.parseDouble(args[4]);
                location = new Location(world, x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid coordinates. Please enter valid numbers for x, y, and z.");
                return;
            }
        } else {
            sender.sendMessage("/world teleport <name> [x y z]");
            return;
        }

        player.teleport(location);
        sender.sendMessage(ChatColor.GRAY + "Teleported to world '" + location.getWorld().getName() + "' at " + location.getX() + ", " + location.getY() + ", " + location.getZ());
    }

    private void displayWorldInfo(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        Location loc = player.getLocation();

        String worldName = world.getName();
        String dimension = world.getEnvironment().toString();
        if (dimension.equalsIgnoreCase("NORMAL")) {
            dimension = "Overworld";
        } else if (dimension.equalsIgnoreCase("NETHER")) {
            dimension = "Nether";
        } else if (dimension.equalsIgnoreCase("THE_END")) {
            dimension = "The End";
        }

        double x = Double.parseDouble(String.valueOf(loc.getX()));
        double y = Double.parseDouble(String.valueOf(loc.getY()));
        double z = Double.parseDouble(String.valueOf(loc.getZ()));
        long time = world.getTime();
        String difficulty = world.getDifficulty().toString();
        if (difficulty.equalsIgnoreCase("PEACEFUL")) {
            difficulty = "Peaceful";
        } else if (difficulty.equalsIgnoreCase("EASY")) {
            difficulty = "Easy";
        } else if (difficulty.equalsIgnoreCase("NORMAL")) {
            difficulty = "Normal";
        } else if (difficulty.equalsIgnoreCase("HARD")) {
            difficulty = "Hard";
        }
        int playerCount = world.getPlayers().size();

        sender.sendMessage(ChatColor.GRAY + "World info for '" + ChatColor.GREEN + worldName + ChatColor.GRAY + "'" +
                "\nDimension: " + ChatColor.GREEN + dimension + ChatColor.GRAY +
                "\nCoordinates: X=" + ChatColor.GREEN + x + ChatColor.GRAY + " Y=" + ChatColor.GREEN + y + ChatColor.GRAY + " Z=" + ChatColor.GREEN + z + ChatColor.GRAY +
                "\nTime: " + ChatColor.GREEN + time + ChatColor.GRAY +
                "\nDifficulty: " + ChatColor.GREEN + difficulty + ChatColor.GRAY +
                "\nPlayers in world: " + ChatColor.GREEN + playerCount);
    }
}