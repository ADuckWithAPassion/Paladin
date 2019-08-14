package apallo.savage.savageclasses;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
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
    public void onPlayerPlaceBlock(BlockPlaceEvent evt){
        Material bType = evt.getBlock().getType();
        if(bType == Material.DISPENSER){
            evt.setCancelled(true);
            SavageUtility.displayMessage("You may not place this block.", evt.getPlayer());
        }
    }

}
