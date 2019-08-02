package savageclasses;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Bandage implements Listener {

    SavageClasses SC;
    private static List<String> allowedClasses;
    private static int requiredPaper;
    private static int bandagePotency;
    private static Cooldown bandageCD;

    public Bandage(SavageClasses SC) {
        this.SC = SC;
        reload(SC);
        }

    public static void reload(SavageClasses SC){
    	bandageCD = new Cooldown("bandage.CD", SC);
        allowedClasses = SC.getClassStatsConfig().getStringList("bandage.allowedClasses");
        requiredPaper = SC.getClassStatsConfig().getInt("bandage.requiredPaper");
        bandagePotency = SC.getClassStatsConfig().getInt("bandage.potency");
    }
    
    @EventHandler
    public void onBandage(PlayerInteractEvent evt) {
        if (evt.getPlayer().getInventory().getItemInMainHand().getType() == Material.PAPER && allowedClasses.contains(this.SC.getPlayerClass(evt.getPlayer()))) {
            ItemStack i = evt.getPlayer().getInventory().getItemInMainHand();
            if (i.getAmount() >= requiredPaper) {
                if (!bandageCD.isOnCooldown(evt.getPlayer())) {
                    i.setAmount(i.getAmount() - requiredPaper);
                    SavageUtility.displayMessage("You begin to apply &9bandages.", evt.getPlayer());
                    new Bandage.Bandageapply(evt.getPlayer());
                    bandageCD.addCooldown(evt.getPlayer());
                } else {
                    SavageUtility.displayMessage("You can't &9bandage &7for " + bandageCD.getRemainingTime(evt.getPlayer()) + "s", evt.getPlayer());
                }
            } else {
                SavageUtility.displayMessage("You need 2 paper to bandage!", evt.getPlayer());
            }
        }

    }

    private class Bandageapply implements Runnable {
        Player p;
        int taskID;

        public Bandageapply(Player p) {
            this.p = p;
            this.taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(SC, this, 60L);
        }

        public void run() {
            SavageUtility.displayMessage(this.p.getName() + " bandaged his wounds.", this.p);
            double health = p.getHealth();
            health += bandagePotency;
            double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(); // non depreciated way of getting max health (works, even if the player does not have 20 health)

            p.setHealth(Math.min(maxHealth, health)); // no overflow
        }
    }
}

