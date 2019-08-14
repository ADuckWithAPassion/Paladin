package apallo.savage.savageclasses;

import org.bukkit.*;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;

public class Assassin
        implements Listener
{
    HashMap<String, Integer> selectedSpell = new HashMap();
    private SavageClasses SC;
    public static HashSet<String> isStealthed = new HashSet();
    private static double stealthDamage;
    private static Cooldown stealthCD;
    private static Cooldown smokeCD;
    private static Cooldown dodgeCD;
    private static double dodgeChance;
    private static double smokeDamage;
    private static double dodgeDamageScale;
    public static int stealthDur;
    private static int dodgeSpeedDuration;
    private static int dodgeSpeedAmplitude;
    
    public Assassin(SavageClasses SC)
    {
        this.SC = SC;
        reload(SC);
    }

    public static void reload(SavageClasses SC){
        stealthDamage = SC.getClassStatsConfig().getDouble("assassin.stealthDamage");
        stealthCD = new Cooldown("assassin.stealthCD", SC);
        smokeCD = new Cooldown("assassin.smokeCD", SC);
        dodgeCD = new Cooldown("assassin.dodgeCD", SC);
        smokeDamage = SC.getClassStatsConfig().getDouble("assassin.smokeDamage");
        stealthDur = SC.getClassStatsConfig().getInt("assassin.stealthDuration");
        dodgeChance = SC.getClassStatsConfig().getDouble("assassin.dodgeChance");
        dodgeDamageScale = SC.getClassStatsConfig().getDouble("assassin.dodgeDamageScale");
        dodgeSpeedDuration = SC.getClassStatsConfig().getInt("assassin.dodgeSpeedDuration");
        dodgeSpeedAmplitude = SC.getClassStatsConfig().getInt("assassin.dodgeSpeedAmplitude");

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent evt)
    {
        Player p = evt.getPlayer();
        if (SC.isClass(p, "assassin"))
        {

            if (p.getInventory().getItemInHand().getType() == Material.GHAST_TEAR)
            {
                if (!SavageUtility.hasLeatherGear(p))
                {
                    SavageUtility.displayMessage("&cYou must wear full leather armor to cast assassin Spells.", p);
                }
                else if ((evt.getAction() == Action.LEFT_CLICK_BLOCK) || (evt.getAction() == Action.LEFT_CLICK_AIR))
                {
                    if (!stealthCD.isOnCooldown(p))
                    {
                        SavageUtility.displayMessage("You slip into the &8shadows..", p);
                        onStealth(p.getName());
                        stealthCD.addCooldown(p);
                    }
                    else
                    {
                        SavageUtility.displayMessage("You can't stealth for " + stealthCD.getRemainingTime(p) + "s", p);
                    }
                }
            }
            else if (p.getInventory().getItemInHand().getType() == Material.FIREWORK_CHARGE)
            {
                if (!SavageUtility.hasLeatherGear(p))
                {
                    SavageUtility.displayMessage("&cYou must wear full leather armor to cast Assassin Spells.", p);
                }
                else if ((evt.getAction() == Action.LEFT_CLICK_BLOCK) || (evt.getAction() == Action.LEFT_CLICK_AIR))
                {
                    if (!smokeCD.isOnCooldown(p))
                    {
                        SavageUtility.displayMessage("You throw a smoke screen!", p);
                        Snowball sb = (Snowball)p.launchProjectile(Snowball.class);
                        sb.setCustomName(p.getName() + "'s SmokeScreen");
//                        sb.setCustomNameVisible(true);
                        smokeCD.addCooldown(p);
                    }
                    else
                    {
                        SavageUtility.displayMessage("You can't smoke for " + smokeCD.getRemainingTime(p) + "s", p);
                    }
                }
            }
        }
    }

    public void onStealth(String name)
    {
        Player player = Bukkit.getPlayer(name);
        isStealthed.add(name);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if ((!p.equals(player)) && (p.canSee(player)))
            {
                p.hidePlayer(player);
            }
        }

        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 2));
        }

        new AssassinStealth(SC,this, player);
    }

    public void unStealth(String name)
    {
        Player player = Bukkit.getPlayer(name);
        if(player == null) return;
        if (isStealthed.contains(name))
        {
            isStealthed.remove(name);

            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if ((!p.equals(player)) && (!p.canSee(player))) {
                    p.showPlayer(player);
                }
            }
            if (player.isOnline()) {
                SavageUtility.displayMessage("&cYou are no longer stealthed!", player);
            }
        }
    }

    @EventHandler
    public void onDamageTaken(EntityDamageEvent event) {
        Entity ent = event.getEntity();
        if ((ent instanceof Player))
        {
            Player player = (Player)ent;
            if(Math.random() <= dodgeChance && !dodgeCD.isOnCooldown(player))
        	{
        		SavageUtility.displayMessage("You dodge an attack.", player);
        		if(event.getCause().equals(DamageCause.ENTITY_ATTACK)){
        			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
        			Entity damager = entityEvent.getDamager();
        			if(damager instanceof LivingEntity) {
            			SavageUtility.displayMessage(player.getName()+" dodges your attack.",(LivingEntity)damager);
            			event.setDamage(event.getDamage()*dodgeDamageScale);
            			dodgeCD.addCooldown(player);
            			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*dodgeSpeedDuration, dodgeSpeedAmplitude-1));
            			return; // if you dodge, you don't reveal
        			}
        		}
        	}
            if (isStealthed.contains(player.getName()))
            {
                player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You have taken damage while stealthed!");
                unStealth(player.getName());
            }
        }
    }



    @EventHandler
    public void onQuit(PlayerQuitEvent evt)
    {
        Player player = evt.getPlayer();
        if (isStealthed.contains(player.getName())) {
            unStealth(player.getName());
        }
    }

    @EventHandler
    public void onDamageDone(EntityDamageByEntityEvent event)
    {
        if ((event.getEntity() instanceof LivingEntity))
        {
            LivingEntity target = (LivingEntity)event.getEntity();
            if ((event.getDamager() instanceof Player))
            {
                Player player = (Player)event.getDamager();
                if (SC.isClass(player.getName(), "assassin") && SavageUtility.isPVPAllowed(player))
                {
                    if (isStealthed.contains(player.getName()))
                    {
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 1));
                        event.setDamage(event.getDamage() * stealthDamage);
                        SavageUtility.displayMessage("You sneak attack your target for a critical hit!", player);
                        if(target instanceof Player) {
                            SavageUtility.displayMessage("You have been sneak attacked!", target);
                        }
                        unStealth(player.getName());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        Entity entity = event.getEntity();
        if(!(entity instanceof Player)) {
        	return;
        }
        EntityDamageEvent damageEvent = entity.getLastDamageCause();
        if(damageEvent.getCause().equals(DamageCause.ENTITY_ATTACK)){
			EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) damageEvent;
			Entity damager = entityEvent.getDamager();
			if(damager instanceof Player) {
                if (SC.isClass((Player)damager, "assassin"))
                {
                    stealthCD.resetCooldown((Player)damager);
                    smokeCD.resetCooldown((Player)damager);
                    SavageUtility.displayMessage(ChatColor.GREEN + "Your kill resets your cooldowns!", (Player)damager);
                }
			}
        }   		
    }

    @EventHandler
    public void onProjectileLand(ProjectileHitEvent evt)
    {
        if ((evt.getEntity().getCustomName() != null) && (evt.getEntity().getCustomName().endsWith("SmokeScreen")))
        {

            Player shooter = (Player)evt.getEntity().getShooter();

            Location l = evt.getEntity().getLocation();
            int x = l.getBlockX() - 4;
            int y = l.getBlockY() - 4;
            int z = l.getBlockZ() - 4;
            for (int i = x; i < x + 8; i++)
            {
                for (int j = y; j < y + 8; j++)
                {
                    for (int k = z; k < z + 8; k++)
                    {
                        Location newL = new Location(l.getWorld(), i, j, k);
                        l.getWorld().playEffect(newL, Effect.SMOKE, 4);
                        for (Player p : SavageUtility.getPlayersWithin(l,6))
                        {
                            if (!SavageUtility.isPVPAllowed(p))
                            {
                                return;
                            }
                            if (p != shooter)
                            {
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 2));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 2));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 2));
                                p.damage(smokeDamage);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent ev)
    {
        if (isStealthed.contains(ev.getPlayer().getName())) {
            unStealth(ev.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event){
        if(isStealthed.contains(event.getPlayer().getName())){
            SavageUtility.displayMessage(ChatColor.RED + "You may not use commands while stealthed!", event.getPlayer());
            event.setCancelled(true);
        }
    }
}
