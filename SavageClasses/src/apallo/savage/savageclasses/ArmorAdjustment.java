package apallo.savage.savageclasses;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArmorAdjustment implements Listener {
    SavageClasses SC;
    
	private double goldHelmMultiplier;
	private double goldChestMultiplier;
	private double goldLegsMultiplier;
	private double goldBootsMultiplier;
	
	private double leatherHelmMultiplier;
	private double leatherChestMultiplier;
	private double leatherLegsMultiplier;
	private double leatherBootsMultiplier;

    ArmorAdjustment(SavageClasses SC){
    	this.SC  = SC;
    	goldHelmMultiplier = SC.getClassStatsConfig().getDouble("gold.helm");
    	goldChestMultiplier = SC.getClassStatsConfig().getDouble("gold.chest");
    	goldLegsMultiplier = SC.getClassStatsConfig().getDouble("gold.legs");
    	goldBootsMultiplier = SC.getClassStatsConfig().getDouble("gold.boots");
    	
    	leatherHelmMultiplier = SC.getClassStatsConfig().getDouble("leather.helm");
    	leatherChestMultiplier = SC.getClassStatsConfig().getDouble("leather.chest");
    	leatherLegsMultiplier = SC.getClassStatsConfig().getDouble("leather.legs");
    	leatherBootsMultiplier = SC.getClassStatsConfig().getDouble("leather.boots");
    	
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            event.setDamage(event.getDamage() * calculateLeatherArmorDamageReduction(player));
            event.setDamage(event.getDamage() * calculateGoldArmorDamageReduction(player));
            increaseWeaponDurability(player);
            increaseArmorDurability(player);
        }

    }

    double calculateGoldArmorDamageReduction(Player player){
        double percentage = 1;
        if (player.getInventory().getHelmet() != null){
            if (player.getInventory().getHelmet().getType() == Material.GOLDEN_HELMET){
                percentage *= goldHelmMultiplier;
            }
        }
        if (player.getInventory().getChestplate() != null){
            if (player.getInventory().getChestplate().getType() == Material.GOLDEN_CHESTPLATE){
                percentage *= goldChestMultiplier;
            }
        }
        if (player.getInventory().getLeggings() != null){
            if (player.getInventory().getLeggings().getType() == Material.GOLDEN_LEGGINGS){
                percentage *= goldLegsMultiplier;
            }
        }
        if (player.getInventory().getBoots() != null){
            if (player.getInventory().getBoots().getType() == Material.GOLDEN_BOOTS){
                percentage *= goldBootsMultiplier;
            }
        }
        return percentage;
    }

    void increaseWeaponDurability(Player player){
        // amount of dura to give back;
        int duraAmount = 3;
        // one out of x chance to get dura back
        int duraChance = 2;

        if(player.getInventory().getItemInMainHand() != null){
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();

            if(item.getType() == Material.GOLDEN_SWORD) {
                meta.setDisplayName("Paladin's Sword");
                if(getRandomInt(duraChance) == 0) {
                    ((Damageable) meta).setDamage(((Damageable) meta).getDamage() - duraAmount);
                }
            }
            item.setItemMeta(meta);
        }
    }

    void increaseArmorDurability(Player player){

        // amount of dura to give back;
        int duraAmount = 3;
        // one out of x chance to get dura back
        int duraChance = 4;
        if(player.getInventory().getHelmet() != null){
            ItemStack item = player.getInventory().getHelmet();
            ItemMeta meta = item.getItemMeta();

            if(item.getType() == Material.LEATHER_HELMET || item.getType() == Material.GOLDEN_HELMET) {
                meta.setLore(Arrays.asList("Durable Armor"));
                    if(getRandomInt(duraChance) == 0) {
                        ((Damageable) meta).setDamage(((Damageable) meta).getDamage() - duraAmount);
                    }
            }
            item.setItemMeta(meta);
        }
        if(player.getInventory().getChestplate() != null){
            ItemStack item = player.getInventory().getChestplate();
            ItemMeta meta = item.getItemMeta();

            if(item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.GOLDEN_CHESTPLATE) {
                meta.setLore(Arrays.asList("Durable Armor"));
                if(getRandomInt(duraChance) == 0) {
                    ((Damageable) meta).setDamage(((Damageable) meta).getDamage() - duraAmount);
                }
            }
            item.setItemMeta(meta);
        }
        if(player.getInventory().getLeggings() != null){
            ItemStack item = player.getInventory().getLeggings();
            ItemMeta meta = item.getItemMeta();

            if(item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.GOLDEN_LEGGINGS) {
                meta.setLore(Arrays.asList("Durable Armor"));
                if(getRandomInt(duraChance) == 0) {
                    ((Damageable) meta).setDamage(((Damageable) meta).getDamage() - duraAmount);
                }
            }
            item.setItemMeta(meta);
        }
        if(player.getInventory().getBoots() != null){
            ItemStack item = player.getInventory().getBoots();
            ItemMeta meta = item.getItemMeta();

            if(item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.GOLDEN_BOOTS) {
                meta.setLore(Arrays.asList("Durable Armor"));
                if(getRandomInt(duraChance) == 0) {
                    ((Damageable) meta).setDamage(((Damageable) meta).getDamage() - duraAmount);
                }
            }
            item.setItemMeta(meta);
        }

    }


    double calculateLeatherArmorDamageReduction(Player player){
        double percentage = 1;

        if (player.getInventory().getHelmet() != null){
            if (player.getInventory().getHelmet().getType() == Material.LEATHER_HELMET){
                percentage *= leatherHelmMultiplier;
            }
        }
        if (player.getInventory().getChestplate() != null){
            if (player.getInventory().getChestplate().getType() == Material.LEATHER_CHESTPLATE){
                percentage *= leatherChestMultiplier;
            }
        }
        if (player.getInventory().getLeggings() != null){
            if (player.getInventory().getLeggings().getType() == Material.LEATHER_LEGGINGS){
                percentage *= leatherLegsMultiplier;
            }
        }
        if (player.getInventory().getBoots() != null){
            if (player.getInventory().getBoots().getType() == Material.LEATHER_BOOTS){
                percentage *= leatherBootsMultiplier;
            }
        }
        return percentage;
    }

    int getRandomInt(int max) {
        return (int)Math.floor(Math.random() * max);
    }

}
