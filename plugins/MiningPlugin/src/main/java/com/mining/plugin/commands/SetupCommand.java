package com.mining.plugin.commands;

import com.mining.plugin.MiningPlugin;
import com.mining.plugin.managers.NpcManager;
import com.mining.plugin.managers.ZoneManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    private final MiningPlugin plugin;
    private final NpcManager npcManager = new NpcManager();

    public SetupCommand(MiningPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.isOp()) return true;

        org.bukkit.World world = player.getWorld();

        // 1. DÜNYA AYARLARI
        world.setTime(1000);
        world.setStorm(false);
        world.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(org.bukkit.GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(org.bukkit.GameRule.DO_MOB_SPAWNING, false);

        // 2. HAFIZAYI VE ESKİ YAZILARI SİL
        plugin.getCrystalManager().clearAllCrystals();
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand && entity.getCustomName() != null && entity.getCustomName().contains("[")) {
                entity.remove();
            }
        }

        // 3. ADALARI İNŞA ET
        plugin.getZoneManager().buildAllIslands();
        npcManager.spawnSetupNPCs();

        // 4. TAŞLARI BEKLEMEDEN ANINDA DOĞUR (2 saniyelik görevi bekleme)
        for (ZoneManager.ZoneInfo zone : plugin.getZoneManager().getZones()) {
            if (zone.crystalName == null) continue;

            int count = 0;
            while (count < 20) {
                org.bukkit.Location loc = plugin.getZoneManager().getRandomLocationInZone(zone);
                if (loc != null) {
                    plugin.getCrystalManager().spawnCrystal(loc, zone.crystalName, zone.crystalLevel, zone.crystalMaxHp, zone.id);
                    count++;
                }
            }
        }

        player.teleport(new org.bukkit.Location(world, 0, 102, 0));
        player.sendMessage("§a[Sistem] Dünya sıfırlandı, 18 ada ve tüm kristaller hazır!");
        return true;
    }
}