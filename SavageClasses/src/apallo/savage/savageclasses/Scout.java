package apallo.savage.savageclasses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class Scout implements Listener {
    private SavageClasses SC;

    private Cooldown thatLovingFeelingCD;
    private Cooldown scoutsBraveryCD;
    private Cooldown fishCD;

    public HashMap<String, Integer> camCD = new HashMap();
    public HashMap<String, Integer> camCount = new HashMap();

    public int CAMcd;
    public int CAMcount;
    public int CAMslowdur;
    public int CAMslowamp;
    public int CAMdur;

    
    public Scout(SavageClasses SC){
        this.SC = SC;
        thatLovingFeelingCD = new Cooldown("scout.thatLovingFeelingCD", SC);
        scoutsBraveryCD = new Cooldown("scout.scoutsBraveryCD", SC);
        fishCD = new Cooldown("scout.fishCD", SC);
        
        CAMcd = SC.getClassStatsConfig().getInt("scout.CAMcd");
        CAMcount = SC.getClassStatsConfig().getInt("scout.CAMcount");
        CAMslowdur = SC.getClassStatsConfig().getInt("scout.CAMslowdur");
        CAMslowamp = SC.getClassStatsConfig().getInt("scout.CAMslowamp");
        CAMdur = SC.getClassStatsConfig().getInt("scout.CAMdur");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){

        // Prevents fall damage
        if(event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL){
            Player player = (Player)event.getEntity();

            if(SC.isClass(player, "scout")){
                if(SavageUtility.hasScoutGear(player)) {
                    if (player.getFallDistance() > 2000.0f) {
                        // Do nothing for now. Can fall infinite distance
                    }
                    event.setCancelled(true);
                }
            }
        }

        if( !(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent event_EE = (EntityDamageByEntityEvent)event;

        if(event_EE.getDamager() instanceof  Player){
            Player damager = (Player)event_EE.getDamager();

            if(SC.isClass(damager, "scout") && SavageUtility.hasScoutGear(damager)){
                LivingEntity enemy = (LivingEntity) event.getEntity();

                double random = Math.random();

                // POISON proc
                if(random > 0D && random < 0.30D){
                    enemy.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                }
                // Scout's bravery proc
                else if(random > 0.30D && random < 0.55D){
                    if(!scoutsBraveryCD.isOnCooldown(damager)){
                        damager.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 1));
                        scoutsBraveryCD.addCooldown(damager);
                        SavageUtility.displayMessage(ChatColor.AQUA + "You Proc " + ChatColor.GOLD + "Scouts Bravery " + ChatColor.AQUA + "you have increased Resistance!", damager);
                    }
                }
                // That Loving Feeling proc
                else if(random > 0.55D && random < 0.90D){
                    if(!thatLovingFeelingCD.isOnCooldown(damager)){
                        damager.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 3));
                        thatLovingFeelingCD.addCooldown(damager);
                        SavageUtility.displayMessage(ChatColor.AQUA + "You Proc " + ChatColor.GOLD + "That Loving Feeling " + ChatColor.AQUA + "you have increased Regen!", damager);
                    }
                }
                // Fish to the face proc
                else if (random > 0.90D && random < .95D){
                    if(!fishCD.isOnCooldown(damager)){
                        event.setDamage(event.getDamage() + 5.0D);
                        fishCD.addCooldown(damager);
                        SavageUtility.displayMessage(ChatColor.AQUA + "You Proc " + ChatColor.GOLD + "Fish to the FACE! " + ChatColor.AQUA + "your target takes extra damage!", damager);

                        if(enemy instanceof Player){
                            enemy.sendMessage(ChatColor.AQUA + "You were hit by " + ChatColor.GOLD + "Fish to the FACE! OUCH!");
                        }
                    }
                }

            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        if(SC.isClass(player.getName(), "scout")){
            if(SavageUtility.hasScoutGear(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 2));
            }
            else{
                removeEffects(player);
            }
        }
    }

    void removeEffects(Player player){
        if (player.hasPotionEffect(PotionEffectType.SPEED))
        {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
        }
        if (player.hasPotionEffect(PotionEffectType.JUMP))
        {
            player.removePotionEffect(PotionEffectType.JUMP);
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 0, 0));
        }
    }
}
