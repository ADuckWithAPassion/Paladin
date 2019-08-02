package savageclasses;

import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.gmail.nossr50.api.PartyAPI;

public class Paladin
        implements Listener
{

    private static Cooldown holyRemedyCD;
    private static Cooldown handOfLightCD;
    private static Cooldown holyAuraCD;
    private static Cooldown shieldSlamCD;
    private static Cooldown rotateCD;

    static double holyRemedyPotency; // (number of half hearts to heal)
    static double handOfLightPotency; // (number of half hearts to heal)
    static long holyAuraDuration; // (duration of holy aura)
    static int slamRange;
    static int slamDuration;

//    private Cooldown holyRemedyCD = new Cooldown(12*1000);
//    private Cooldown handOfLightCD = new Cooldown(10*1000);
//    private Cooldown holyAuraCD = new Cooldown(300*1000);
//    private Cooldown shieldSlamCD = new Cooldown(20*1000);
//    private Cooldown rotateCD = new Cooldown(50);
//
//    static double holyRemedyPotency = 12; // (number of half hearts to heal)
//    static double handOfLightPotency = 10; // (number of half hearts to heal)
//    static long holyAuraDuration = 8*20; // (duration of holy aura)
//    static int slamRange = 5;
//    static int slamDuration = 3*1000;


    HashMap<String, Integer> selectedSpell = new HashMap<String, Integer>();
    private SavageClasses SC;
    String[] spells;

    Random rand = new Random();

    Paladin(SavageClasses SC)
    {
        this.SC = SC;
        reload(SC);
        spells = new String[] { "Holy Remedy", "Hand Of Light", "Holy Aura" };
    }

    public static void reload(SavageClasses SC){
        holyRemedyCD = new Cooldown("paladin.holyRemedyCD",SC);
        handOfLightCD = new Cooldown("paladin.handOfLightCD",SC);
        holyAuraCD = new Cooldown("paladin.holyAuraCD",SC);
        shieldSlamCD = new Cooldown("paladin.shieldSlamCD",SC);
        rotateCD = new Cooldown("paladin.rotateCD",SC);

        holyRemedyPotency = SC.getClassStatsConfig().getDouble("paladin.holyRemedyPotency");
        handOfLightPotency = SC.getClassStatsConfig().getDouble("paladin.handOfLightPotency");
        holyAuraDuration = SC.getClassStatsConfig().getInt("paladin.holyAuraDuration")*20;
        slamRange = SC.getClassStatsConfig().getInt("paladin.slamRange");
        slamDuration = SC.getClassStatsConfig().getInt("paladin.slamDuration")*1000;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent evt)
    {
        Player p = evt.getPlayer();
        if (SC.isClass(p, "paladin"))
        {
            Material mainHand = p.getInventory().getItemInMainHand().getType();
            Material offHand = p.getInventory().getItemInOffHand().getType();
            Action action = evt.getAction();
            if (mainHand == Material.GOLDEN_SHOVEL)
            {
                boolean doRotate = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
                {
                    if(!(SavageUtility.hasGoldGear(p)))
                    {
                        SavageUtility.displayMessage("&cYou must be wearing full gold armor to cast spells", p); // amour spelt as armor to appease Americans.
                        return;
                    }
                    if (selectedSpell.containsKey(p.getName()))
                    {
                        int spell = selectedSpell.get(p.getName());
                        // Holy Remedy spell
                        if (spell == 0)
                        {
                            if (!holyRemedyCD.isOnCooldown(p))
                            {
                                remedy(p);
                                SavageUtility.broadcastMessage(p.getName() + " &6used &bHoly Remedy!", p);

                                holyRemedyCD.addCooldown(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage("You can't holy remedy for " + holyRemedyCD.getRemainingTime(p) + "s", p);
                            }
                        }
                        // Hand Of Light spell
                        else if (spell == 1)
                        {
                            if (!handOfLightCD.isOnCooldown(p))
                            {
                                light(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage("You can't hand of light for " + handOfLightCD.getRemainingTime(p) + "s", p);
                            }


                        }
                        // Holy Aura spell
                        else
                        {
                            if(!holyAuraCD.isOnCooldown(p)) {
                                aura(p);
                                SavageUtility.broadcastMessage(p.getName() + " &6used &bHoly Aura!", p);

                                holyAuraCD.addCooldown(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage("You can't holy aura for " + holyAuraCD.getRemainingTime(p) + "s", p);
                            }
                        }


                    }

                }
                else if (doRotate && !rotateCD.isOnCooldown(p))
                {
                    rotateCD.addCooldown(p);
                    if (selectedSpell.containsKey(p.getName()))
                    { // Replaced unnecessary if statements with modulo
                        int ss = (selectedSpell.get(p.getName()));
                        selectedSpell.put(p.getName(),(ss + 1) % spells.length); // (0,1,2,0,1,2, ... )
                        SavageUtility.displayMessage("You are now using the &c" + spells[(ss + 1) % spells.length] + " &7spell", p);
                    }
                    else
                    { // If cycling through spells for the first time
                        selectedSpell.put(p.getName(), 0);
                        SavageUtility.displayMessage("You are now using the &cHoly Remedy &7Spell.", p);
                    }
                }
            }
            else if(offHand == Material.SHIELD && mainHand == Material.GOLDEN_SWORD)
            {
                if (p.isSneaking() && !rotateCD.isOnCooldown(p) && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK))
                {
                	rotateCD.addCooldown(p);
                    if(!(SavageUtility.hasGoldGear(p)))
                    {
                        SavageUtility.displayMessage("&cYou must be wearing full gold armor to cast spells", p); // amour spelt as armor to appease Americans.
                        return;
                    }
                    if(!SavageUtility.isPVPAllowed(p)){
                        p.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "PvP is disabled in this area.");
                        return;
                    }
                    if(!shieldSlamCD.isOnCooldown(p))
                    {
                        slam(p);
                        SavageUtility.broadcastMessage(p.getName() + " &6used &bShield Slam!", p);
                        shieldSlamCD.addCooldown(p);
                    }
                    else
                    {
                        SavageUtility.displayMessage("You can't shield slam for " + shieldSlamCD.getRemainingTime(p) + "s", p);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent evt) // Golden swords only
    {
        if (evt.getDamager() instanceof Player)
        {
            Player p = (Player)evt.getDamager();
            if (SC.isClass(p, "Paladin"))
            {
                if ((evt.getEntity() instanceof LivingEntity && evt.getCause().equals(DamageCause.ENTITY_ATTACK)))
                {
                    if(!(p.getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_SWORD))) {
                        evt.setDamage(0);
                        SavageUtility.displayMessage(ChatColor.RED+"Paladins can only attack with a golden sword", p);
                    }
                    else {
                        evt.setDamage(evt.getDamage() + 2);    // Add sword damage modifiers here, if required (Don't use multipliers because they could do insane damage with enchants; instead use a flat addition).
                    }
                }
            }
        }
    }

    void light(Player player) // copied vector maths from Gladiator
    { // https://imgur.com/a/TzVLsbr (visual demonstration, ignore letters)
        // What essentially goes on here is we check that the the target is within the red lines, and then check they are within the purple lines
        // Essentially a check to see if the player is looking at another player

        assert (player != null);
        Entity target = null;
        Player target2 = null;

        double targetDistanceSquared = 0.0D;
        Vector l = player.getEyeLocation().toVector();Vector n = player.getLocation().getDirection().normalize();
        double cos45 = Math.cos(0.7853981633974483D); // represents cos of angle Beta in the visual demonstration

        for (LivingEntity other : player.getWorld().getEntitiesByClass(LivingEntity.class))
        {
            if (other != player && other instanceof Player)
            {
                if(!(PartyAPI.inSameParty(player, (Player)other))) {
                    continue;
                }
                if ((target == null) || (targetDistanceSquared > other.getLocation().distanceSquared(player.getLocation()))) // Only affect the closest entity
                {
                    Vector t = other.getLocation().add(0.0D, 1.0D, 0.0D).toVector().subtract(l);
                    if ((n.clone().crossProduct(t).lengthSquared() < 1.0D) && (t.normalize().dot(n) >= cos45)) // checks entity is within red and purple lines on visual demonstration
                    {
                        target = other;
                        targetDistanceSquared = target.getLocation().distanceSquared(player.getLocation());
                    }
                }
            }
        }
        if (target != null) // if an entity has been found by the ability
        {
            if (!(target instanceof Player))
            {
                return;
            }

            target2 = (Player)target;

            Location ploc = player.getLocation();
            Location tloc = target2.getLocation();

            if (ploc.distance(tloc) > 10.0D)
            {
                player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "Target is out of range.");
                return;
            }
            // Apply actually effect to target
            double health = target2.getHealth();
            health += handOfLightPotency;
            double maxHealth = target2.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(); // non depreciated way of getting max health
            target2.setFoodLevel(20);
            target2.setSaturation(5);

            target2.setHealth(Math.min(maxHealth, health)); // no overflow

            handOfLightCD.addCooldown(player);
            SavageUtility.broadcastMessage(player.getName() + " &6used &bHand Of Light&6 on "+target2.getName()+".", player);

        }
        else
        {

            handOfLightCD.resetCooldown(player);
            player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You must have a target in your party.");
            return;
        }
    }



    void remedy(Player player)
    {
        for(PotionEffectType effectType : SavageUtility.negativeEffects) { // remove any negative effects specified
            if(player.hasPotionEffect(effectType)) {
                player.removePotionEffect(effectType);
                player.addPotionEffect(new PotionEffect(effectType, 0, 0));
            }
        }
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setSaturation(5);
        double health = player.getHealth();
        health += handOfLightPotency;
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(); // non depreciated way of getting max health

        player.setHealth(Math.min(maxHealth, health)); // no overflow

    }

    private void aura(final Player player)
    {
        player.setInvulnerable(true);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SC, new Runnable() {
            public void run() {
                player.setInvulnerable(false);
                SavageUtility.broadcastMessage(player.getName()+" is no longer immune to damage.",player);
            }
        }, Paladin.holyAuraDuration);
    }

    void slam(Player player)
    {
        for(Entity nearby : player.getNearbyEntities(slamRange, slamRange, slamRange)) {
            if(nearby instanceof Player) {
                if(((Player)nearby).equals(player) || (PartyAPI.inSameParty(player, (Player)nearby))) {
                    continue;
                }
                SavageUtility.displayMessage("You are slammed back by "+player.getName(), (Player)nearby);
            }
            Vector vec = nearby.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(2).setY(0.1);
            nearby.setVelocity(vec);

            if(nearby instanceof Player) {
                Stun.stunPlayer((Player)nearby, slamDuration);
            }

        }
    }
}