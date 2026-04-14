package com.mining.plugin.managers;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NpcManager implements Listener {

    @EventHandler
    public void onNpcInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;
        if (!(e.getRightClicked() instanceof Villager)) return;

        Villager npc = (Villager) e.getRightClicked();
        Player player = e.getPlayer();

        if (npc.getCustomName() != null) {
            if (npc.getCustomName().contains("Gardiyan Burak")) {
                e.setCancelled(true);
                player.getInventory().addItem(ItemManager.getStarterSword());
                player.getInventory().addItem(ItemManager.getStarterArmor());
                player.getInventory().addItem(ItemManager.getTeleportRing());
                player.sendMessage("§a[Gardiyan Burak] Hoş geldin! İşte başlangıç ekipmanların.");
            }
            else if (npc.getCustomName().contains("Berk-Pyeong")) {
                e.setCancelled(true);
                player.sendMessage("§5[Berk-Pyeong] §fSadece +9 eşyaları kadim güçlerle dönüştürebilirim.");
            }
        }
    }

    public void spawnSetupNPCs() {
        org.bukkit.World world = org.bukkit.Bukkit.getWorld("world");
        if (world == null) return;

        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof Villager && entity.getCustomName() != null) {
                if (entity.getCustomName().contains("Gardiyan Burak") || entity.getCustomName().contains("Berk-Pyeong")) entity.remove();
            }
        }

        Villager gardiyan = world.spawn(new org.bukkit.Location(world, 5.5, 101, 0.5), Villager.class);
        gardiyan.setCustomName("§eGardiyan Burak"); gardiyan.setCustomNameVisible(true);
        gardiyan.setAI(false); gardiyan.setInvulnerable(true);

        Villager seon = world.spawn(new org.bukkit.Location(world, -4.5, 101, 0.5), Villager.class);
        seon.setCustomName("§5Berk-Pyeong"); seon.setCustomNameVisible(true);
        seon.setAI(false); seon.setInvulnerable(true);
    }
}