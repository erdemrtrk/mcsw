package com.mining.plugin.managers;

import com.mining.plugin.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {

    public void updateScoreboard(Player player, PlayerData data) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        // Tablonun başlığı (1.16+ sürümler için 'dummy' kullanımı)
        Objective obj = board.registerNewObjective("guide", "dummy", "§6§lErdem2TuttunS2");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Satırları yukarıdan aşağıya doğru sıralıyoruz (Sayılar satır numarasını belirler)
        obj.getScore("§7--- İstatistikler ---").setScore(6);
        obj.getScore("§fSeviye: §e" + data.level).setScore(5);
        obj.getScore("§fTecrübe: §a" + data.experience + " §8/ §2" + data.getRequiredXP()).setScore(4);
        obj.getScore("§fAltın: §6" + data.gold).setScore(3);
        obj.getScore("§7---------------------").setScore(2);
        obj.getScore("§bplay.Erdem2TuttunS2.com").setScore(1);

        player.setScoreboard(board);
    }
}