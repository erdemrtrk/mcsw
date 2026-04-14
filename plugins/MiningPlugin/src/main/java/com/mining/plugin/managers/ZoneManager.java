package com.mining.plugin.managers;

import com.mining.plugin.MiningPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZoneManager {

    private final MiningPlugin plugin;
    private final World gameWorld;
    private final Random random = new Random();
    private final List<ZoneInfo> zones = new ArrayList<>();

    public ZoneManager(MiningPlugin plugin) {
        this.plugin = plugin;
        this.gameWorld = Bukkit.getWorld("world");
        loadZones();
        startSpawningTask();
    }

    private void loadZones() {
        zones.add(new ZoneInfo("ZONE_0", "Başlangıç Köyü", 0, null, 0, 0, 0));
        zones.add(new ZoneInfo("ZONE_1", "Üzüntü Adası", 0, "Üzüntü Kristali", 10, 10000, 1000));
        zones.add(new ZoneInfo("ZONE_2", "Dövüş Adası", 15, "Savaş Kristali", 15, 25000, 2000));
        zones.add(new ZoneInfo("ZONE_3", "Hırs Adası", 20, "Hırs Kristali", 20, 40000, 3000));
        // Dilersen diğer adaları buraya ekleyebilirsin...
    }

    public List<ZoneInfo> getZones() { return zones; }

    public Location getRandomLocationInZone(ZoneInfo zone) {
        if (gameWorld == null) return null;
        int minX = zone.centerX - 20; int maxX = zone.centerX + 20;
        int minZ = -20; int maxZ = 20;

        int x = random.nextInt((maxX - minX) + 1) + minX;
        int z = random.nextInt((maxZ - minZ) + 1) + minZ;
        int y = gameWorld.getHighestBlockYAt(x, z);

        // Obsidyen kulesi oluşmasını engelle (Üst üste binme koruması)
        if (gameWorld.getBlockAt(x, y, z).getType() == Material.OBSIDIAN) return null;
        if (y < 20) return null;

        return new Location(gameWorld, x, y, z);
    }

    private void startSpawningTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (ZoneInfo zone : zones) {
                if (zone.crystalName == null) continue;
                int currentCrystals = plugin.getCrystalManager().getActiveCrystalCount(zone.id);
                int attempts = 0;
                while (currentCrystals < 20 && attempts < 50) {
                    attempts++;
                    Location spawnLoc = getRandomLocationInZone(zone);
                    if (spawnLoc != null) {
                        plugin.getCrystalManager().spawnCrystal(spawnLoc, zone.crystalName, zone.crystalLevel, zone.crystalMaxHp, zone.id);
                        currentCrystals++;
                    }
                }
            }
        }, 100L, 40L);
    }

    public void buildAllIslands() {
        for (ZoneInfo zone : zones) buildSingleIsland(zone.centerX, 100, 0, 20);
    }

    private void buildSingleIsland(int centerX, int y, int centerZ, int radius) {
        if (gameWorld == null) return;
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                gameWorld.getBlockAt(x, y - 3, z).setType(Material.STONE);
                gameWorld.getBlockAt(x, y - 2, z).setType(Material.DIRT);
                gameWorld.getBlockAt(x, y - 1, z).setType(Material.DIRT);
                gameWorld.getBlockAt(x, y, z).setType(Material.GRASS_BLOCK);

                // YENİ: Çimenin üstündeki 5 bloğu tamamen temizle (Eski taşları yok eder)
                for (int i = 1; i <= 5; i++) {
                    gameWorld.getBlockAt(x, y + i, z).setType(Material.AIR);
                }
            }
        }
    }

    public static class ZoneInfo {
        public String id, name, crystalName;
        public int reqLevel, crystalLevel, crystalMaxHp, centerX;
        public ZoneInfo(String id, String name, int reqLevel, String crystalName, int crystalLevel, int crystalMaxHp, int centerX) {
            this.id = id; this.name = name; this.reqLevel = reqLevel;
            this.crystalName = crystalName; this.crystalLevel = crystalLevel;
            this.crystalMaxHp = crystalMaxHp; this.centerX = centerX;
        }
    }
}