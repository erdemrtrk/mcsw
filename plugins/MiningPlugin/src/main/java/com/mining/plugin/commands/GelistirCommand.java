package com.mining.plugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GelistirCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem.getType() == Material.AIR || !handItem.getType().name().endsWith("_SWORD")) {
            player.sendMessage("§c[Demirci] Eline bir kılıç almalısın!");
            return true;
        }

        ItemMeta meta = handItem.getItemMeta();
        String name = meta.getDisplayName();
        int currentPlus = name.contains("+") ? Integer.parseInt(name.substring(name.lastIndexOf("+") + 1)) : 0;

        if (currentPlus >= 9) {
            player.sendMessage("§c[Demirci] Bu eşya zaten maksimum seviyede! Dönüşüm NPC'sine gitmelisin.");
            return true;
        }

        int cost = (currentPlus + 1) * 10;

        // --- 1. ADIM: ENVANTERDEKİ TOPLAM PARÇAYI SAY ---
        int totalShards = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.AMETHYST_SHARD &&
                    item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§bÜzüntü Kristali Parçası")) {
                totalShards += item.getAmount();
            }
        }

        // Yeterli parça yoksa işlemi iptal et
        if (totalShards < cost) {
            player.sendMessage("§c[Demirci] Yetersiz parça! Gerekli: " + cost + " Üzüntü Kristali Parçası. Sende olan: " + totalShards);
            return true;
        }

        // --- 2. ADIM: ENVANTERDEN PARÇALARI DÜŞ (64 Sınırını Aşar) ---
        int remainingCost = cost;
        for (ItemStack item : player.getInventory().getContents()) {
            if (remainingCost <= 0) break; // Borç bittiyse döngüyü kır

            if (item != null && item.getType() == Material.AMETHYST_SHARD &&
                    item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§bÜzüntü Kristali Parçası")) {

                if (item.getAmount() <= remainingCost) {
                    // Yığın, gereken maliyetten küçük veya eşitse yığını tamamen sil
                    remainingCost -= item.getAmount();
                    item.setAmount(0);
                } else {
                    // Yığın maliyetten büyükse, sadece gerekeni içinden al
                    item.setAmount(item.getAmount() - remainingCost);
                    remainingCost = 0;
                }
            }
        }

        // --- 3. ADIM: KILICI GELİŞTİR VE HASARINI ARTIR ---
        int nextPlus = currentPlus + 1;
        String baseName = name.substring(0, name.lastIndexOf("+"));
        meta.setDisplayName(baseName + "+" + nextPlus);

        // PARLAMA KONTROLÜ: +5 ve üzerinde büyü parlaması ekle
        if (nextPlus >= 5) {
            // Görünmez bir büyü ekliyoruz (DURABILITY - Kırılmazlık)
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            // Büyü yazısının kılıçta görünmemesi için gizliyoruz
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        int addedDamage = baseName.contains("Üzüntü") ? 150 : 20;

        List<String> lore = meta.getLore();
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                if (lore.get(i).contains("Hasar:")) {
                    String plainLine = ChatColor.stripColor(lore.get(i));
                    int currentDmg = Integer.parseInt(plainLine.replaceAll("[^0-9]", ""));
                    lore.set(i, "§7Hasar: §c" + (currentDmg + addedDamage));
                }
            }
        }

        meta.setLore(lore);
        handItem.setItemMeta(meta);
        player.sendMessage("§a[Demirci] Başarılı! Eşyan " + nextPlus + " seviyesine yükseldi.");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 1, 1);

        return true;
    }
}