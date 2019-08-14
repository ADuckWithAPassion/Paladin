package apallo.savage.savageclasses;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.gmail.nossr50.api.PartyAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Berserker
        implements Listener
{
    static private Cooldown rageCD;
    static private Cooldown roarCD;
    static private Cooldown headbuttCD;
    static private Cooldown rotateCD;

    static private Cooldown stunImmunityDuration;

    static int roarDuration;
    static int roarRange;
    static int headbuttDuration;
    static int rageStrength;

    HashMap<String, Integer> selectedSpell = new HashMap<String, Integer>();
    private SavageClasses SC;
    String[] spells;
    String[] negativeEffects;
    List<String> readiedHeadbutt = new ArrayList<String>();

    Berserker(SavageClasses SC)
    {
        reload(SC);
        this.SC = SC;
        spells = new String[] { "Roar", "Rage" };
        negativeEffects = new String[] { "BLINDNESS","CONFUSION","HUNGER","POISON","SLOW","SLOW_DIGGING","WEAKNESS","WITHER" };
    }

    public static void reload(SavageClasses SC){
        rageCD = new Cooldown("berserker.rageCD",SC);
        roarCD = new Cooldown("berserker.roarCD",SC);
        headbuttCD = new Cooldown("berserker.headbuttCD",SC);
        rotateCD = new Cooldown("berserker.rotateCD",SC);
        stunImmunityDuration = new Cooldown("berserker.stunImmunityDuration", SC);
        roarDuration = SC.getClassStatsConfig().getInt("berserker.roarDuration")*1000;
        roarRange = SC.getClassStatsConfig().getInt("berserker.roarRange");
        rageStrength = SC.getClassStatsConfig().getInt("berserker.rageStrength");
        headbuttDuration = SC.getClassStatsConfig().getInt("berserker.headbuttDuration")*1000;
}

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent evt)
    {
        Player p = evt.getPlayer();
        if (SC.isClass(p, "berserker"))
        {
            Material mainHand = p.getInventory().getItemInHand().getType();
            Action action = evt.getAction();
            if (mainHand == Material.ROTTEN_FLESH)
            {
                boolean doRotate = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
                {
                    if(!(SavageUtility.hasBerserkerGear(p)))
                    {
                        SavageUtility.displayMessage("&cYou must be wearing the berserker's armor to cast spells", p); // armour spelt as armor to appease Americans.
                        return;
                    }
                    if (selectedSpell.containsKey(p.getName()))
                    {
                        int spell = selectedSpell.get(p.getName());
                        // Roar
                        if (spell == 0)
                        {
                            if (!roarCD.isOnCooldown(p))
                            {
                                if(!SavageUtility.isPVPAllowed(p)){
                                    p.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "PvP is disabled in this area.");
                                    return;
                                }
                                roar(p);
                                SavageUtility.broadcastMessage(p.getName() + " &6used &bRoar!", p);

                                roarCD.addCooldown(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage("You can't roar for " + roarCD.getRemainingTime(p) + "s", p);
                            }
                        }
                        // Rage
                        else if (spell == 1)
                        {
                            if (!rageCD.isOnCooldown(p))
                            {
                                rage(p);
                                SavageUtility.broadcastMessage(p.getName() + " &6used &bRage!", p);

                                rageCD.addCooldown(p);

                            }
                            else
                            {
                                SavageUtility.displayMessage("You can't rage for " + rageCD.getRemainingTime(p) + "s", p);
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
                        selectedSpell.put(p.getName(),(ss + 1) % spells.length); // (0,1,0,1, ... )
                        SavageUtility.displayMessage("You are now using &c" + spells[(ss + 1) % spells.length], p);
                    }
                    else
                    { // If cycling through spells for the first time
                        selectedSpell.put(p.getName(), 0);
                        SavageUtility.displayMessage("You are now using &cRoar.", p);
                    }
                }
            }
            else if(mainHand == Material.DIAMOND_AXE)
            {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                {
                    if(!(SavageUtility.hasBerserkerGear(p)))
                    {
                        SavageUtility.displayMessage("&cYou must be wearing the berserker's armor to cast spells", p); // armour spelt as armor to appease Americans.
                        return;
                    }
                    if(!SavageUtility.isPVPAllowed(p)){
                        SavageUtility.displayMessage("PVP is not allowed in this area.", p);
                        return;
                    }
                    if(!headbuttCD.isOnCooldown(p))
                    {
                        headbutt(p);
                    }
                    else
                    {
                        SavageUtility.displayMessage("You can't headbutt for " + headbuttCD.getRemainingTime(p) + "s", p);
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
            if (SC.isClass(p, "Berserker"))
            {
                if ((evt.getEntity() instanceof LivingEntity))
                {
                    List<Material>acceptable_weapons = Arrays.asList(Material.WOOD_AXE,Material.STONE_AXE,Material.IRON_AXE,Material.DIAMOND_AXE);
                    if(!(acceptable_weapons.contains(p.getInventory().getItemInHand().getType()))) {
                        evt.setDamage(0);
                        SavageUtility.displayMessage(ChatColor.RED+"Berserkers can only attack with an axe.", p);
                    }
                    else {
//                        evt.setDamage(evt.getDamage() + 1);    // Add axe damage modifiers here, if required (Don't use multipliers because they could do insane damage with enchants; instead use a flat addition).
                        if(evt.getEntity() instanceof Player && readiedHeadbutt.contains(p.getName())) {
                            if( Stun.stunImmunityDuration.isOnCooldown( (Player)evt.getEntity()) ) {
                                SavageUtility.displayMessage("This player cannot be stunned again for " + Stun.stunImmunityDuration.getRemainingTime((Player)evt.getEntity()), p);
                                return;
                            }
                            Stun.stunPlayer((Player)evt.getEntity(), headbuttDuration);
                            SavageUtility.broadcastMessage(evt.getEntity().getName()+" is stunned by "+p.getName()+"'s headbutt.", (Player)evt.getEntity());
                            readiedHeadbutt.remove(p.getName());
                            headbuttCD.addCooldown(p);
                        }
                    }
                }
            }
        }
    }

    void rage(Player player)
    {
        for(String effectName : negativeEffects) { // remove any negative effects specified
            PotionEffectType potion = PotionEffectType.getByName(effectName);
            player.removePotionEffect(potion);
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6*20, rageStrength));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6*20, 3));
    }

    void roar(Player player)
    {
        for(Entity nearby : player.getNearbyEntities(roarRange, roarRange, roarRange)) {
            if(nearby instanceof Player) {
                if(((Player)nearby).equals(player) || (PartyAPI.inSameParty(player, (Player)nearby)) || Stun.stunImmunityDuration.isOnCooldown((Player)nearby)) {
                    continue;
                }
                SavageUtility.displayMessage("You are petrified by "+player.getName()+"'s mighty roar.", (Player)nearby);
                Stun.stunPlayer((Player)nearby, roarDuration);
            }
        }
    }

    void headbutt(Player player) {
        if(readiedHeadbutt.contains(player.getName())) {
            SavageUtility.displayMessage("Headbutt is already ready", player);
            return;
        }
        readiedHeadbutt.add(player.getName());
        SavageUtility.displayMessage("You have readied headbutt", player);
    }
}
