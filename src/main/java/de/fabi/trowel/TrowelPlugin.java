/*
Copyright © 2025 https://github.com/Fabii08?tab=repositories
All rights reserved.

Unauthorized copying, modification, or distribution of this file,
via any medium, is strictly prohibited.

DO NOT DISTRIBUTE.
*/
package de.fabi.trowel;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrowelPlugin extends JavaPlugin implements Listener, TabExecutor {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("givetrowel").setExecutor(this);
        createTrowelRecipe();
    }

    private void createTrowelRecipe() {
        ItemStack trowel = createTrowel();
        NamespacedKey key = new NamespacedKey(this, "trowel");
        ShapedRecipe recipe = new ShapedRecipe(key, trowel);
        recipe.shape("S  ", " II", "   ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    private ItemStack createTrowel() {
        ItemStack trowel = new ItemStack(Material.IRON_SHOVEL);
        ItemMeta meta = trowel.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize("<gold><bold>Trowel</bold></gold>"));
            meta.setCustomModelData(12345);
            List<Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Places a random block from your hotbar.</gray>"));
            meta.lore(lore);
            trowel.setItemMeta(meta);
        }
        return trowel;
    }

    @EventHandler
    public void onPlayerUseTrowel(PlayerInteractEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() &&
                item.getItemMeta().getCustomModelData() == 12345) {

            event.setCancelled(true);
            PlayerInventory inventory = player.getInventory();
            ItemStack[] hotbar = inventory.getContents();
            List<Integer> blockSlots = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < 9; i++) {
                if (hotbar[i] != null && hotbar[i].getType().isBlock()) {
                    blockSlots.add(i);
                }
            }

            if (blockSlots.isEmpty()) return;

            int randomSlot = blockSlots.get(random.nextInt(blockSlots.size()));
            Material randomBlock = hotbar[randomSlot].getType();
            Block targetBlock = event.getClickedBlock();

            if (targetBlock != null) {
                Block blockToPlace = targetBlock.getRelative(event.getBlockFace());
                if (blockToPlace.isEmpty()) {
                    blockToPlace.setType(randomBlock);
                    ItemStack blockStack = hotbar[randomSlot];
                    blockStack.setAmount(blockStack.getAmount() - 1);
                    if (blockStack.getAmount() <= 0) {
                        inventory.setItem(randomSlot, null);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("givetrowel")) {
            if (sender instanceof Player player) {
                player.getInventory().addItem(createTrowel());
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>You have been given a <gold>Trowel</gold>!</green>"));
            } else if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage("This command can only be used by a player!");
            }
            return true;
        }
        return false;
    }
}
