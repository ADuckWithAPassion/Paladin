package apallo.savage.savageclasses;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.gmail.nossr50.api.PartyAPI;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

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

    HashMap<String, Integer> selectedSpell = new HashMap<String, Integer>();
    public HashSet<String> isInvincible = new HashSet();
    private SavageClasses SC;
    String[] spells;

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
            Material mainHand = p.getInventory().getItemInHand().getType();
            Action action = evt.getAction();
            if (mainHand == Material.GOLD_SPADE)
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
                                SavageUtility.broadcastMessage(p.getName() + " &6used &b&LHoly Aura&b!", p);

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
            else if(mainHand == Material.IRON_DOOR)
            {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                {
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
                if ((evt.getEntity() instanceof LivingEntity && evt.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))) //untested (should prevent thorns and other effects from gaining extra damage)
                {
                    if(!(p.getInventory().getItemInHand().getType().equals(Material.GOLD_SWORD))) {
                        evt.setDamage(0);
                        SavageUtility.displayMessage(ChatColor.RED+"Paladins can only attack with a golden sword", p);
                    }
                    else {
                        evt.setDamage(evt.getDamage() + 2);    // Add sword damage modifiers here, if required (Don't use multipliers because they could do insane damage with enchants; instead use a flat addition).
                    }
                }
            }
        }
        if(evt.getEntity() instanceof  Player){
            Player p = (Player)evt.getEntity();
            if(isInvincible.contains(p) ){
                if(evt.getDamager() instanceof Player){
                    SavageUtility.displayMessage(ChatColor.RED + "This player is invincible with Holy Aura!", (Player)evt.getDamager());
                }
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event){
        if(isInvincible.contains(event.getPlayer().getName())){
            SavageUtility.displayMessage(ChatColor.RED + "You may not use commands while Holy Aura is active!", event.getPlayer());
            event.setCancelled(true);
        }
    }

    void light(Player player) // copied vector maths from Gladiator
    { // https://imgur.com/a/TzVLsbr (visual demonstration, ignore letters)
        // What essentially goes on here is we check that the the target is within the red lines, and then check they are within the purple lines
        // Essentially a check to see if the player is looking at another player

        assert (player != null);
        Entity target = SavageUtility.getTarget(player, 20, true);

        if(target == null){
            SavageUtility.displayMessage(ChatColor.RED + "You must have a target to use this spell.", player);
            return;
        }

        if (!(target instanceof Player))
        {
            SavageUtility.displayMessage("This may only be used on players", player);
            return;
        }

        Player targetPlayer = (Player)target;

        if(!PartyAPI.inSameParty(player, targetPlayer)){
            SavageUtility.displayMessage("This may only be used on party members", player);
            return;
        }

        // Apply actually effect to target
        double health = targetPlayer.getHealth();
        health += handOfLightPotency;
        double maxHealth = targetPlayer.getMaxHealth();
        targetPlayer.setFoodLevel(20);
        targetPlayer.setSaturation(5);

        targetPlayer.setHealth(Math.min(maxHealth, health)); // no overflow

        handOfLightCD.addCooldown(player);
        SavageUtility.broadcastMessage(player.getName() + " &6used &bHand Of Light&6 on "+target.getName()+".", player);


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
        double maxHealth = player.getMaxHealth();

        player.setHealth(Math.min(maxHealth, health)); // no overflow

    }

    private void aura(final Player player)
    {
        double maxHealth = player.getMaxHealth();
        player.setHealth(maxHealth);
        isInvincible.add(player.getName());
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SC, new Runnable() {
            public void run() {
                isInvincible.remove(player.getName());
                SavageUtility.broadcastMessage(ChatColor.RED+player.getName()+"'s "+ChatColor.GOLD + (ChatColor.BOLD+"Holy Aura")+ChatColor.stripColor("")+ChatColor.GOLD+" fades.",player);
            }
        }, Paladin.holyAuraDuration);
    }

    void slam(Player player)
    {
        for(Entity nearby : player.getNearbyEntities(slamRange, slamRange, slamRange)) {
            if(nearby instanceof Player) {
                if(((Player)nearby).equals(player) || (PartyAPI.inSameParty(player, (Player)nearby)) || Stun.stunImmunityDuration.isOnCooldown((Player)nearby)) {
                    continue;
                }
                SavageUtility.displayMessage("You are slammed back by "+player.getName(), (Player)nearby);
                Vector vec = nearby.getLocation().subtract(player.getLocation()).toVector().normalize().multiply(2).setY(0.1);
                nearby.setVelocity(vec);
                Stun.stunPlayer((Player)nearby, slamDuration);
            }

        }

    }
}
