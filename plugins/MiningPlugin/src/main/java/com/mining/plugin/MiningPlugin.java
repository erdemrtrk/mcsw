package com.mining.plugin;

import com.mining.plugin.commands.AdminCommand;
import com.mining.plugin.commands.GelistirCommand;
import com.mining.plugin.commands.SetupCommand;
import com.mining.plugin.managers.*;
import com.mining.plugin.models.PlayerData;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningPlugin extends JavaPlugin {

    private ZoneManager zoneManager;
    private CrystalManager crystalManager;
    private ScoreboardManager scoreboardManager;
    private final Map<UUID, PlayerData> players = new HashMap<>();

    @Override
    public void onEnable() {
        // Eski Manager'ları tamamen devreden çıkardık
        this.zoneManager = new ZoneManager(this);
        this.crystalManager = new CrystalManager(this);
        this.scoreboardManager = new ScoreboardManager();

        getServer().getPluginManager().registerEvents(crystalManager, this);
        getServer().getPluginManager().registerEvents(new ItemManager(this), this);
        getServer().getPluginManager().registerEvents(new NpcManager(), this);

        getCommand("setupworld").setExecutor(new SetupCommand(this));
        getCommand("gelistir").setExecutor(new GelistirCommand());
        getCommand("adminesya").setExecutor(new AdminCommand());

        getLogger().info("Metin2 RPG Sistemi Aktif!");

        // --- PARLAMA EFEKTİ SİSTEMİ (Her 5 tick'te bir çalışır) ---
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();

                if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    String name = item.getItemMeta().getDisplayName();

                    // Eğer elindeki kılıç +5 veya daha üzeriyse
                    if (name.contains("+")) {
                        int level = Integer.parseInt(name.substring(name.lastIndexOf("+") + 1));

                        if (level >= 5) {
                            // Seviyeye göre parçacık miktarını hesapla
                            // +5 için 1, +6 için 2 ... +9 için 5 parça
                            int count = level - 4;

                            // Oyuncunun çevresinde beyaz yıldızlar (END_ROD) çıkart
                            player.getWorld().spawnParticle(
                                    org.bukkit.Particle.END_ROD,
                                    player.getLocation().add(0, 1, 0), // Göğüs hizasında
                                    count, // Yüzdesel miktar
                                    0.3, 0.5, 0.3, // Yayılma alanı
                                    0.05 // Hareket hızı
                            );

                            // Eğer +9 ise ekstra bir parlama efekti (FLASH) ekle
                            if (level == 9) {
                                player.getWorld().spawnParticle(
                                        Particle.INSTANT_EFFECT,
                                        player.getLocation().add(0, 1, 0),
                                        1, 0.2, 0.2, 0.2, 0
                                );
                            }
                        }
                    }
                }
            }
        }, 0L, 5L); // 5 tick (saniyenin 4'te 1'i) aralıkla kontrol et
    }

    public PlayerData getPlayerData(UUID uuid) {
        return players.computeIfAbsent(uuid, PlayerData::new);
    }

    public ZoneManager getZoneManager() { return zoneManager; }
    public CrystalManager getCrystalManager() { return crystalManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
}