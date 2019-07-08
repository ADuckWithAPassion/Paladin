package apallo.savage.savageclasses;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class Restriction implements Listener {

    SavageClasses SC;

    Restriction(SavageClasses SC){
        this.SC  = SC;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if( !(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(event.getClickedBlock().getType() == Material.BREWING_STAND){
            SavageUtility.displayMessage("Brewing stands are disabled to reinforce the use of our class system.", event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        // Enforce no shield except for gladiators or paladins
        if(player.getInventory().getItemInOffHand().getType() == Material.SHIELD){
            if(!SC.isClass(player, "gladiator") && !SC.isClass(player, "paladin")){
                player.getInventory().setItemInOffHand(null);
                player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.SHIELD));
            }
        }

        enforceTritonGear(player);


    }

    // Enforce player wearing Triton gear is a Triton
    private void enforceTritonGear(Player player){

        if(player.getInventory().getBoots() != null){
            ItemStack boots = player.getInventory().getBoots();
            if(boots.getItemMeta().getDisplayName().contains("Triton's") && !SC.isClass(player, "triton")){
                if(boots.getEnchantments().containsKey(Enchantment.DEPTH_STRIDER)) {
                    boots.removeEnchantment(Enchantment.DEPTH_STRIDER);
                }
            }
        }

        if(player.getInventory().getHelmet() != null){
            ItemStack helmet = player.getInventory().getHelmet();
            if(helmet.getItemMeta().getDisplayName().contains("Triton's") && !SC.isClass(player, "triton")){
                if(helmet.getEnchantments().containsKey(Enchantment.WATER_WORKER)) {
                    helmet.removeEnchantment(Enchantment.WATER_WORKER);
                }
            }
        }

        if(player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() == Material.TRIDENT){
            if (!SC.isClass(player, "triton")) {
                player.getInventory().setItemInMainHand(null);
                player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.TRIDENT));
            }
        }
    }
}
