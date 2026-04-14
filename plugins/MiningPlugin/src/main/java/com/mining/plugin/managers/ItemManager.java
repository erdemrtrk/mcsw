package com.mining.plugin.managers;

import com.mining.plugin.MiningPlugin;
import com.mining.plugin.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemManager implements Listener {

    private final MiningPlugin plugin;

    public ItemManager(MiningPlugin plugin) { this.plugin = plugin; }

    public static ItemStack getStarterSword() {
        ItemStack item = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§fKılıç +0");
        List<String> lore = new ArrayList<>();
        lore.add("§7Hasar: §c50");
        // YENİ: Başlangıç saldırı hızı saniyede 1 vuruş
        lore.add("§7Saldırı Hızı: §e1.0");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getStarterArmor() {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§fMonk Zırhı +0");
        List<String> lore = new ArrayList<>(); lore.add("§7Savunma: §910");
        meta.setLore(lore); item.setItemMeta(meta); return item;
    }

    public static ItemStack getTeleportRing() {
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bIşınlanma Yüzüğü");
        item.setItemMeta(meta); return item;
    }

    @EventHandler
    public void onRingUse(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§bIşınlanma Yüzüğü")) {
                openDynamicGUI(e.getPlayer());
            }
        }
    }

    public void openDynamicGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§8Işınlanma Yüzüğü");
        PlayerData data = plugin.getPlayerData(player.getUniqueId());

        int slot = 0;
        for (ZoneManager.ZoneInfo zone : plugin.getZoneManager().getZones()) {
            if (slot >= 27) break;
            Material icon = (zone.id.equals("ZONE_0")) ? Material.GRASS_BLOCK : Material.AMETHYST_BLOCK;
            if (data.level >= zone.reqLevel) {
                gui.setItem(slot, createGuiItem(icon, "§a" + zone.name, "§7Gerekli Lv: §e" + zone.reqLevel, "§eTıkla ve ışınlan!"));
            } else {
                gui.setItem(slot, createGuiItem(Material.BARRIER, "§c" + zone.name + " §7(Kilitli)", "§7Gerekli Lv: §c" + zone.reqLevel));
            }
            slot++;
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§8Işınlanma Yüzüğü")) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;

        Player player = (Player) e.getWhoClicked();
        String clickedName = e.getCurrentItem().getItemMeta().getDisplayName();

        if (clickedName.contains("(Kilitli)")) {
            player.sendMessage("§c[Sistem] Seviyen bu adaya henüz yetmiyor!"); return;
        }

        for (ZoneManager.ZoneInfo zone : plugin.getZoneManager().getZones()) {
            if (clickedName.contains(zone.name)) {
                player.teleport(new Location(player.getWorld(), zone.centerX, 102, 0));
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                player.closeInventory();
                return;
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material); ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name); List<String> loreList = new ArrayList<>();
        for (String l : lore) loreList.add(l);
        meta.setLore(loreList); item.setItemMeta(meta); return item;
    }
}