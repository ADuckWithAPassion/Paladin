package savageclasses;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Scout implements Listener {
    private SavageClasses SC;

    private static Cooldown thatLovingFeelingCD;
    private static Cooldown scoutsBraveryCD;
    private static Cooldown fishCD;
    private static double fishToFaceDamage;

    public Scout(SavageClasses SC){
        this.SC = SC;
        reload(SC);
    }

    public static void reload(SavageClasses SC){
        thatLovingFeelingCD = new Cooldown("scout.thatLovingFeelingCD", SC);
        scoutsBraveryCD = new Cooldown("scout.scoutsBraveryCD", SC);
        fishCD = new Cooldown("scout.fishCD", SC);
        fishToFaceDamage = SC.getClassStatsConfig().getDouble("scout.fishToFaceDamage");
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
                        event.setDamage(event.getDamage() + fishToFaceDamage);
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
