package apallo.savage.savageclasses;

import com.sk89q.worldedit.world.block.BlockType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class EnforceEnchants implements Listener {

    SavageClasses SC;

    EnforceEnchants(SavageClasses SC){
        this.SC  = SC;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event){

        Map<Enchantment, Integer> enchants = event.getEnchantsToAdd();

        for(Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()){
            if(enchant.getKey().equals(Enchantment.DAMAGE_ALL) && enchant.getValue() > 3){
                SavageUtility.displayMessage("The max sharpness level is 3. Setting sharpness to 3.", event.getEnchanter());
                enchants.put(Enchantment.DAMAGE_ALL, 3);
            }
            if(enchant.getKey().equals(Enchantment.PROTECTION_ENVIRONMENTAL) && enchant.getValue() > 2){
                SavageUtility.displayMessage("The max protection level is 2. Setting protection to 2.", event.getEnchanter());
                enchants.put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            }
            if(enchant.getKey().equals(Enchantment.ARROW_DAMAGE) && enchant.getValue() > 3){
                SavageUtility.displayMessage("The max power level is 3. Setting power to 3.", event.getEnchanter());
                enchants.put(Enchantment.ARROW_DAMAGE, 3);
            }
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if( !(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(event.getClickedBlock().getType() == Material.ANVIL || event.getClickedBlock().getType() == Material.CHIPPED_ANVIL || event.getClickedBlock().getType() == Material.DAMAGED_ANVIL){
            SavageUtility.displayMessage("Anvils are disabled. Use McMMO anvils (iron block) to repair your items.", event.getPlayer());
            event.setCancelled(true);
        }
    }

}
