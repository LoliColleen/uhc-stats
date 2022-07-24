package com.gmail.mezymc.stats.listeners;

import com.gmail.mezymc.stats.*;
import com.gmail.val59000mc.events.UhcWinEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.game.GameState;
import com.gmail.val59000mc.game.handlers.PlayerDamageHandler;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.UhcPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class UhcStatListener implements Listener{

    private StatsManager statsManager;
    private GameMode gameMode;
    private final PlayerManager playerManager;
    private final PlayerDamageHandler playerDamageHandler;

    public UhcStatListener(StatsManager statsManager, PlayerManager playerManager, PlayerDamageHandler playerDamageHandler){
        this.statsManager = statsManager;
        gameMode = statsManager.getServerGameMode();
        this.playerManager = playerManager;
        this.playerDamageHandler = playerDamageHandler;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        if (GameManager.getGameManager().getGameState().equals(GameState.WAITING)){
            return;
        }

        StatsPlayer statsPlayer = statsManager.getStatsPlayer(player);
        statsPlayer.addOneToStats(gameMode, StatType.DEATH);

        UhcPlayer uhcPlayer = playerManager.getUhcPlayer(player);
        UhcPlayer uhcLastKiller = playerDamageHandler.getLastKiller(uhcPlayer);

        if (player.getKiller() != null) {
            StatsPlayer statsKiller = statsManager.getStatsPlayer(player.getKiller());
            statsKiller.addOneToStats(gameMode, StatType.KILL);
            statsKiller.addOneToStats(gameMode, StatType.SCORE);
        } else if(uhcLastKiller != null) {
            StatsPlayer statsKiller;
            try {
                statsKiller = statsManager.getStatsPlayer(uhcLastKiller.getPlayer());
                statsKiller.addOneToStats(gameMode, StatType.KILL);
                statsKiller.addOneToStats(gameMode, StatType.SCORE);
            } catch (UhcPlayerNotOnlineException ignored) {

            }
        }
    }

    @EventHandler
    public void onGameWin(UhcWinEvent e){
        e.getWinners().forEach(uhcPlayer -> {
            StatsPlayer statsPlayer = statsManager.getStatsPlayer(uhcPlayer.getUuid(), uhcPlayer.getName());
            if (statsPlayer != null){
                statsPlayer.addOneToStats(gameMode, StatType.WIN);
                statsPlayer.addAmountToStats(gameMode, StatType.SCORE, 10);
            }else{
                Bukkit.getLogger().warning("[UhcStats] Failed to add win to stats for " + uhcPlayer.getName());
            }
        });

        // Push all stats
        Bukkit.getScheduler().runTaskAsynchronously(UhcStats.getPlugin(), () -> statsManager.pushAllStats());
    }

}