package com.mining.plugin.commands;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.isOp()) {
            player.sendMessage("§cBu komutu kullanmaya yetkin yok!");
            return true;
        }

        // 1. EŞYA: Üzüntü Kristali Parçası (Demirci testi için)
        ItemStack shard = new ItemStack(Material.AMETHYST_SHARD, 64);
        ItemMeta shardMeta = shard.getItemMeta();
        shardMeta.setDisplayName("§bÜzüntü Kristali Parçası");
        shard.setItemMeta(shardMeta);
        player.getInventory().addItem(shard);

        // 2. EŞYA: Makine Tüfek Kılıcı (Hasar ve 100 Hız Testi için)
        ItemStack adminSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = adminSword.getItemMeta();
        swordMeta.setDisplayName("§c§lAdmin Kılıcı +9");
        List<String> swordLore = new ArrayList<>();
        swordLore.add("§7Hasar: §c500");
        swordLore.add("§7Saldırı Hızı: §e100.0"); // Saniyede 100 vuruşa izin verir!
        swordMeta.setLore(swordLore);
        swordMeta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        swordMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        adminSword.setItemMeta(swordMeta);
        player.getInventory().addItem(adminSword);

        // 3. EŞYA: Rüzgarın Çizmeleri (Hareket Hızı Testi için)
        ItemStack windBoots = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta bootsMeta = windBoots.getItemMeta();
        bootsMeta.setDisplayName("§a§lRüzgarın Çizmeleri");
        List<String> bootsLore = new ArrayList<>();
        bootsLore.add("§7Giyildiğinde rüzgar kadar hızlı koşarsın.");
        bootsMeta.setLore(bootsLore);

        // Minecraft 1.21+ Uyumlu Hız Sistemi
        try {
            org.bukkit.attribute.AttributeModifier speedModifier = new org.bukkit.attribute.AttributeModifier(
                    org.bukkit.NamespacedKey.minecraft("admin_speed"), // 1.21 YENİLİĞİ: Artık UUID yerine özel isimli anahtar istiyor
                    0.15,
                    org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER,
                    org.bukkit.inventory.EquipmentSlotGroup.FEET // 1.21 YENİLİĞİ: EquipmentSlot yerine SlotGroup oldu
            );

            bootsMeta.addAttributeModifier(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED, speedModifier);
        } catch (Exception ignored) {
            // Olası bir hatada oyunu çökertmemesi için koruma
        }

        windBoots.setItemMeta(bootsMeta);
        player.getInventory().addItem(windBoots);

        // 4. EŞYA: Hükümdar Kaskı (10x Hasar Çarpanı Testi)
        ItemStack adminHelmet = new ItemStack(Material.NETHERITE_HELMET);
        ItemMeta helmetMeta = adminHelmet.getItemMeta();
        helmetMeta.setDisplayName("§6§lHükümdar Kaskı");
        List<String> helmetLore = new ArrayList<>();
        helmetLore.add("§7Bu kaskı takan kişinin");
        helmetLore.add("§7tüm hasarı §e10 katına §7çıkar.");
        helmetMeta.setLore(helmetLore);
        adminHelmet.setItemMeta(helmetMeta);
        player.getInventory().addItem(adminHelmet);

        player.sendMessage("§a[Sistem] Tanrı Modu eşyaları verildi! (Kristal, Kılıç, Rüzgar Botu, Hükümdar Kaskı)");

        return true;
    }
}