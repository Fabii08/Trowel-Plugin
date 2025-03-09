/*
Copyright © 2025 https://github.com/Fabii08?tab=repositories  
All rights reserved.  
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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TrowelPlugin extends JavaPlugin implements Listener, TabExecutor {

    private final HashMap<Player, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TICKS = 5; // 5 ticks cooldown

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
            meta.setCustomModelData(12345); // Custom model data ID
            List<Component> lore = new ArrayList<>();
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Places a random block from your hotbar.</gray>"));
            meta.lore(lore);
            trowel.setItemMeta(meta);
        }
        return trowel;
    }

    @EventHandler
    public void onPlayerUseTrowel(PlayerInteractEvent event) {
        // Only proceed if the action is from the main hand
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return; // Ignore actions from the off-hand
        }

        // Check that the action is a right-click on a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Ignore other actions (e.g., left clicks, air clicks)
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Verify that the item is the custom trowel
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 12345) {
            // Cancel default shovel behavior (like creating dirt paths)
            event.setCancelled(true);

            // Check cooldown
            long currentTime = System.currentTimeMillis();
            if (cooldowns.containsKey(player)) {
                long lastUseTime = cooldowns.get(player);
                if (currentTime - lastUseTime < COOLDOWN_TICKS * 50) { // Convert ticks to milliseconds
                    return; // Player is still on cooldown
                }
            }

            PlayerInventory inventory = player.getInventory();
            Random random = new Random();
            ItemStack[] hotbar = inventory.getContents();

            // Collect all block materials from the hotbar
            List<Integer> blockSlots = new ArrayList<>();
            for (int i = 0; i < 9; i++) { // Only loop through hotbar slots (0–8)
                if (hotbar[i] != null && hotbar[i].getType().isBlock()) {
                    blockSlots.add(i);
                }
            }

            // If there are no blocks in the hotbar, stop here
            if (blockSlots.isEmpty()) {
                return;
            }

            // Randomly select a block from the hotbar
            int randomSlot = blockSlots.get(random.nextInt(blockSlots.size()));
            Material randomBlock = hotbar[randomSlot].getType();

            // Get the block face the player clicked on
            Block targetBlock = event.getClickedBlock();
            if (targetBlock != null) {
                Block blockToPlace = targetBlock.getRelative(event.getBlockFace()); // Adjacent block
                if (blockToPlace.isEmpty()) { // Only place if the adjacent block is air
                    blockToPlace.setType(randomBlock);

                    // Consume one block from the hotbar slot
                    ItemStack blockStack = hotbar[randomSlot];
                    blockStack.setAmount(blockStack.getAmount() - 1);
                    if (blockStack.getAmount() <= 0) {
                        inventory.setItem(randomSlot, null); // Remove the stack if it's empty
                    }

                    // Update cooldown
                    cooldowns.put(player, currentTime);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakWithTrowel(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
                && item.getItemMeta().getCustomModelData() == 12345) {
            event.setCancelled(true);

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
