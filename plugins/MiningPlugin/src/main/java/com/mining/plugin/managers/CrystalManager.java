package com.mining.plugin.managers;

import com.mining.plugin.MiningPlugin;
import com.mining.plugin.models.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrystalManager implements Listener {

    private final MiningPlugin plugin;
    private final Map<Location, Integer> crystalHPs = new HashMap<>();
    private final Map<Location, ArmorStand> crystalHolograms = new HashMap<>();
    private final Map<Location, String> crystalZones = new HashMap<>();

    // YENİ: Oyuncuların son vuruş zamanlarını (milisaniye) aklında tutan hafıza
    private final Map<UUID, Long> attackCooldowns = new HashMap<>();

    public CrystalManager(MiningPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnCrystal(Location loc, String name, int level, int maxHp, String zone) {
        Block block = loc.getBlock();
        if (block.getType() != Material.AIR) loc.add(0, 1, 0);

        loc.getBlock().setType(Material.OBSIDIAN);
        Location roundedLoc = loc.getBlock().getLocation();
        crystalHPs.put(roundedLoc, maxHp);
        crystalZones.put(roundedLoc, zone);

        ArmorStand hologram = loc.getWorld().spawn(roundedLoc.clone().add(0.5, 1.5, 0.5), ArmorStand.class);
        hologram.setVisible(false); hologram.setGravity(false); hologram.setMarker(true); hologram.setCustomNameVisible(true);
        updateHologram(hologram, name, level, maxHp, maxHp);
        crystalHolograms.put(roundedLoc, hologram);
    }

    private void updateHologram(ArmorStand hologram, String name, int level, int currentHp, int maxHp) {
        hologram.setCustomName("§7[Lv." + level + "] §c" + name + " §8[§e" + currentHp + " §8/ §e" + maxHp + "§8]");
    }

    @EventHandler
    public void onCrystalHit(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) return;
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.OBSIDIAN) return;

        Location loc = clickedBlock.getLocation().getBlock().getLocation();
        Player player = event.getPlayer();

        // 1. KONTROL NOKTASI: Taş hafızada var mı?
        if (!crystalHPs.containsKey(loc)) {
            player.sendMessage("§c[Sistem] Bu taş hafızada yok! Ölü bir taş. Lütfen /setupworld yaz.");
            return;
        }

        event.setCancelled(true);

        // Kılıçtan hasar ve hız okuma işlemleri
        int damage = 50;
        double attackSpeed = 1.0;

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() != Material.AIR && handItem.hasItemMeta() && handItem.getItemMeta().hasLore()) {
            for (String line : handItem.getItemMeta().getLore()) {
                String plainLine = org.bukkit.ChatColor.stripColor(line);
                if (plainLine.contains("Hasar:")) {
                    String dmgStr = plainLine.replaceAll("[^0-9]", "");
                    try { damage = Integer.parseInt(dmgStr); } catch (Exception ignored) {}
                }
                if (plainLine.contains("Saldırı Hızı:")) {
                    String speedStr = plainLine.replaceAll("[^0-9.]", "");
                    try { attackSpeed = Double.parseDouble(speedStr); } catch (Exception ignored) {}
                }
            }
        }

        // ... (Kılıçtan hasar okuma kodlarının bittiği yer)

        // --- KASK ÇARPANI KONTROLÜ ---
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.hasItemMeta() &&
                helmet.getItemMeta().getDisplayName().equals("§6§lHükümdar Kaskı")) {

            damage *= 10; // Hasarı 10 ile çarp!
        }

        // --- HASAR UYGULAMA ---
        int currentHp = crystalHPs.get(loc) - damage;
        // ...

        // 2. KONTROL NOKTASI: Vuruş hızı limitine takılıyor muyuz?
        long now = System.currentTimeMillis();
        long cooldownMs = (long) (1000.0 / attackSpeed);
        long lastHit = attackCooldowns.getOrDefault(player.getUniqueId(), 0L);

        if (now - lastHit < cooldownMs) {
            // Spam yapıldığı için vurmasına izin verme
            return;
        }

        // VURUŞ BAŞARILI!
        attackCooldowns.put(player.getUniqueId(), now);

        // --- HASAR İŞLEMLERİ ---
        currentHp = crystalHPs.get(loc) - damage;
        if (currentHp < 0) currentHp = 0;

        loc.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0.5, 0.5, 0.5), 15);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_STONE_HIT, 1f, 1f);

        // Vurduğunu görebilmen için geçici test mesajı (Çalıştığına emin olunca bu satırı silebilirsin)
        player.sendMessage("§a[Test] Taşa başarıyla " + damage + " hasar vurdun! Kalan Can: " + currentHp);

        if (currentHp == 0) {
            destroyCrystal(loc);

            ItemStack shard = new ItemStack(Material.AMETHYST_SHARD, 1);
            ItemMeta meta = shard.getItemMeta();
            meta.setDisplayName("§bÜzüntü Kristali Parçası");
            shard.setItemMeta(meta);
            loc.getWorld().dropItemNaturally(loc, shard);

            PlayerData data = plugin.getPlayerData(player.getUniqueId());
            data.experience += 50;
            data.gold += 10.5;

            if (data.experience >= data.getRequiredXP()) {
                data.experience -= data.getRequiredXP();
                data.level++;
                player.sendMessage("§b§lTEBRİKLER! §fSeviye atladın: §e" + data.level);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
            if (plugin.getScoreboardManager() != null) plugin.getScoreboardManager().updateScoreboard(player, data);

        } else {
            crystalHPs.put(loc, currentHp);
            String zoneId = crystalZones.get(loc);
            for (ZoneManager.ZoneInfo info : plugin.getZoneManager().getZones()) {
                if (info.id.equals(zoneId)) {
                    ArmorStand holo = crystalHolograms.get(loc);
                    if (holo != null) updateHologram(holo, info.crystalName, info.crystalLevel, currentHp, info.crystalMaxHp);
                    break;
                }
            }
        }
    }
    public void destroyCrystal(Location loc) {
        loc.getBlock().setType(Material.AIR);
        crystalHPs.remove(loc);
        crystalZones.remove(loc);
        if (crystalHolograms.containsKey(loc)) crystalHolograms.remove(loc).remove();
    }

    public int getActiveCrystalCount(String zone) {
        int count = 0;
        for (String z : crystalZones.values()) if (z.equals(zone)) count++;
        return count;
    }

    // Tüm listeleri ve dünyadaki hologramları tamamen temizler
    public void clearAllCrystals() {
        // Hologramları dünyadan sil
        for (ArmorStand holo : crystalHolograms.values()) {
            if (holo != null) holo.remove();
        }

        // Listeleri boşalt
        crystalHPs.clear();
        crystalHolograms.clear();
        crystalZones.clear();
        attackCooldowns.clear();
    }
}