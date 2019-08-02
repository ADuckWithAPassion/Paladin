package savageclasses;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class Stun implements Listener {
    static HashMap<String,Long> stunned = new HashMap<String,Long>();
    SavageClasses SC;
    
    
    public Stun(SavageClasses SC) {
        this.SC = SC;
    }

    public static void stunPlayer(Player player, int duration) {
        stunned.put((player).getName(),System.currentTimeMillis()+duration);
    }
    public static HashMap<String, Long> getStunned(){
    	return stunned;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        Player player = evt.getPlayer();
        if(stunned.containsKey(player.getName())) {
            if(stunned.get(player.getName()) < System.currentTimeMillis()) {
                stunned.remove(player.getName());
                SavageUtility.displayMessage("You are no longer stunned", player);
            }
            else {
                Location f = evt.getFrom();
                Location t = evt.getTo();

                if(f.getX() != t.getX() || f.getZ() != t.getZ()) {
                    evt.setCancelled(true);
                	//player.teleport(f); added check to allow player to jump (otherwise glitching in the air out is more likely)
                    String remainingTime = Long.toString((stunned.get(player.getName()) - System.currentTimeMillis())/1000 + 1);
                    SavageUtility.displayMessage("You are stunned for " +ChatColor.AQUA+remainingTime+ChatColor.GRAY+" seconds", player);
                }   
            }
        }
    }
}
//    @EventHandler
//    public void onPlayerMove(PlayerMoveEvent evt) {
//        Player player = evt.getPlayer();
//        if(stunned.containsKey(player.getName())) {
//            if(stunned.get(player.getName()) < System.currentTimeMillis()) {
//                stunned.remove(player.getName());
//                SavageUtility.displayMessage("You are no longer stunned", player);
//            }
//            else {
//                evt.setCancelled(true);
//                SavageUtility.displayMessage("You are stunned for " + ChatColor.AQUA + (this.stunned.get(player.getName()) - System.currentTimeMillis())/1000 + 1 + ChatColor.GRAY + " seconds.", player);
//            }
//        }
//    }
//}