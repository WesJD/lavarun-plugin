package me.gong.lavarun.plugin.game;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.ArenaCreator;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.command.Cmd;
import me.gong.lavarun.plugin.command.annotation.Command;
import me.gong.lavarun.plugin.command.annotation.SubCommand;
import me.gong.lavarun.plugin.shop.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class GameCommands implements Cmd {

    @Command(name = "game", help = "Main game command", alias = "g")
    public boolean onCmd(Player player, String[] args) {
        return !hasPerms(player);
    }

    @SubCommand(name = "createarena", alias = "ca", help = "Creates an arena", syntax = "<name>", centralCommand = "game")
    public boolean onCreate(Player player, String[] args) {
        if(hasPerms(player)) {
            if (args.length != 1) return false;
            ArenaCreator.beginPlayer(player, args[0], new ArenaCreator.CreationListener() {
                @Override
                public void onCreation(Arena arena) {
                    player.sendMessage(ChatColor.GOLD + "Arena '" + arena.getName() + "' created");
                    InManager.get().getInstance(GameManager.class).addArena(arena);
                }

                @Override
                public void onFail() {
                    player.sendMessage(ChatColor.GOLD + "Unable to create arena.");
                }
            });
            return true;
        } else return false;
    }

    @SubCommand(name = "load", alias = "l", help = "Loads an arena", syntax = "<arena>", centralCommand = "game")
    public boolean onLoad(Player player, String[] args) {
        if(hasPerms(player)) {
            if(args.length != 1) return false;
            GameManager gm = InManager.get().getInstance(GameManager.class);
            Arena a = gm.getArenas().stream()
                    .filter(ar -> ar.getName().equalsIgnoreCase(args[0]))
                    .findFirst().orElse(null);
            if(a == null) {
                player.sendMessage(ChatColor.RED+"Invalid arena. Valids: "+
                        gm.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
            } else {
                gm.loadArena(a);
                player.sendMessage(ChatColor.GREEN+"Arena '"+a.getName()+"' loaded.");
            }
            return true;
        } else return true;
    }

    @SubCommand(name = "end", alias = "e", help = "Ends the game", centralCommand = "game")
    public boolean onEnd(Player player, String[] args) {
        if(hasPerms(player)) {
            InManager.get().getInstance(GameManager.class).stopGame(player);
            return true;
        } else return false;
    }

    @SubCommand(name = "remove", alias = "r", help = "Removes an arena", syntax = "<arena>", centralCommand = "game")
    public boolean onRemove(Player player, String[] args) {
        if(hasPerms(player)) {
            if(args.length != 1) {

                return false;
            }
            GameManager gm = InManager.get().getInstance(GameManager.class);
            Arena a = gm.getArenas().stream()
                    .filter(ar -> ar.getName().equalsIgnoreCase(args[0]))
                    .findFirst().orElse(null);
            if(a == null) {
                player.sendMessage(ChatColor.RED+"Invalid arena. Valids: "+
                        gm.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
            } else gm.removeArena(player, a);
            return true;
        } else return true;
    }

    @SubCommand(name = "test", alias = "t", help = "Tests random features", centralCommand = "game")
    public boolean onTest(Player player, String[] args) {
        if(hasPerms(player)) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            if(!gm.getCurrentArena().isPlaying(player, false)) player.sendMessage(ChatColor.RED+"You are not ingame");
            else {
                ShopManager sm = InManager.get().getInstance(ShopManager.class);
                sm.setPoints(player, 1000);
            }
            return true;
        } else return true;
    }

    @Command(name = "join", help = "Makes yourself join a team or another player", syntax = "<team, player> [team]")
    public boolean onCmds(Player player, String[] args) {
        if(hasPerms(player)) {
            if(args.length < 1) return false;
            GameManager gm = InManager.get().getInstance(GameManager.class);
            if(!gm.isInGame()) player.sendMessage(ChatColor.RED+"No game is currently running");
            else if(args.length == 1) {
                //team
                Team t = gm.getCurrentArena().getTeam(args[0]);
                if(t == null) player.sendMessage(ChatColor.RED+"Invalid team name.");
                else joinTeam(player, t);
            } else {
                String pl = args[1];
                Player find = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(pl)).findFirst().orElse(null);
                if(find == null) player.sendMessage(ChatColor.RED+"No player by name of "+ChatColor.YELLOW+pl);
                else {
                    Team t = gm.getCurrentArena().getTeam(args[0]);
                    if(t == null) player.sendMessage(ChatColor.RED+"Invalid team name.");
                    else {
                        joinTeam(find, t);
                        if (!find.equals(player))
                            player.sendMessage(ChatColor.GREEN+"Joined "+ChatColor.YELLOW+find.getName()+ChatColor.GREEN+" to "+t.getColor()+t.getName()+" team");
                    }
                }
            }
            return true;
        } else return true;
    }

    public void joinTeam(Player player, Team team) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Team curTeam = gm.getCurrentArena().getTeam(player);
        if(curTeam != null) curTeam.removePlayer(player);
        team.addPlayer(player);
        gm.spawnPlayer(player, true);
        team.giveKit(player);
        player.sendMessage(ChatColor.GREEN+"You have joined "+team.getColor()+team.getName()+" team");
    }


}
