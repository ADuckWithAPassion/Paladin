package savageclasses;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class Consumable implements Listener {

    SavageClasses SC;

    public Consumable(SavageClasses SC) {
        this.SC = SC;
    }

    @EventHandler
    public static void onPlayerItemConsume(PlayerItemConsumeEvent event){
        if(event.getItem().getType() == Material.GOLDEN_APPLE || event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE){
            SavageUtility.displayMessage("Golden apples are disabled.", event.getPlayer());
            event.setCancelled(true);
        }
        else if(event.getItem().getType() == Material.POTION || event.getItem().getType() == Material.LINGERING_POTION){
            SavageUtility.displayMessage("Potions are disabled.", event.getPlayer());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onPlayerUse(PlayerInteractEvent event){
        if( !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SPLASH_POTION){
            SavageUtility.displayMessage("Potions are disabled.", event.getPlayer());
            event.setCancelled(true);
        }
    }
}
