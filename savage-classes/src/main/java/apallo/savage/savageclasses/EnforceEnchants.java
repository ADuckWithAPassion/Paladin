package apallo.savage.savageclasses;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EnforceEnchants implements Listener {

    SavageClasses SC;

    EnforceEnchants(SavageClasses SC){
        this.SC  = SC;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event){

        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();
        final Player enchanter = event.getEnchanter();

        for(Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()){
            if(enchant.getKey().equals(Enchantment.DAMAGE_ALL) && enchant.getValue() > 2){
                SavageUtility.displayMessage("The max sharpness level is 2. Setting sharpness to 2.", enchanter);
                enchants.put(Enchantment.DAMAGE_ALL, 2);
                continue;
            }
            if(enchant.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) && enchant.getValue() > 2){
                SavageUtility.displayMessage("The max protection level is 2. Setting protection to 2.", enchanter);
                enchants.put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                continue;
            }
            if(enchant.getKey().equals(Enchantment.ARROW_DAMAGE) && enchant.getValue() > 3){
                SavageUtility.displayMessage("The max power level is 2. Setting power to 2.", enchanter);
                enchants.put(Enchantment.ARROW_DAMAGE, 3);
                continue;
            }
            enchants.put(enchant.getKey(), enchant.getValue());
        }

        new BukkitRunnable(){
            int i = 0;

            @Override
            public void run() {
                i++;
                if (i > 7){
                    enchanter.setLevel(0);
                    SavageUtility.displayMessage("All of your exp was sucked away!", enchanter);
                    this.cancel();
                }
            }
        }.runTaskTimer(SC, 0, 1);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player)){
            return;
        }
        Player p = (Player)event.getDamager();
        List<ItemStack> armorContents = Arrays.asList(p.getInventory().getArmorContents());
        for(ItemStack armor : armorContents){
            if(armor == null || armor.getEnchantments() == null || armor.getEnchantments().isEmpty()){
                continue;
            }
            for( Map.Entry<Enchantment, Integer> enchant : armor.getEnchantments().entrySet()){
                if(enchant.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) && enchant.getValue() > 2){
                    SavageUtility.displayMessage("Max protection level is now 2. Downgrading armor to level 2", p);
                    armor.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                }
            }
        }

        ItemStack weapon = p.getInventory().getItemInHand();
        if(weapon == null || weapon.getEnchantments() == null || weapon.getEnchantments().isEmpty()){
            return;
        }
        for(Map.Entry<Enchantment, Integer> enchant : weapon.getEnchantments().entrySet()){
            if(enchant.getKey().equals(Enchantment.DAMAGE_ALL) && enchant.getValue() > 2){
                SavageUtility.displayMessage("Max sharpness level is now 2. Downgrading weapon to level 2", p);
                weapon.addEnchantment(Enchantment.DAMAGE_ALL, 2);
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if( !(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(event.getClickedBlock().getType() == Material.ANVIL){
            SavageUtility.displayMessage("Anvils are disabled. Use McMMO anvils (iron block) to repair your items.", event.getPlayer());
            event.setCancelled(true);
        }
    }

}
