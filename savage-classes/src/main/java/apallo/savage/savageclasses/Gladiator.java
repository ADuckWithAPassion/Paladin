package apallo.savage.savageclasses;

import com.gmail.nossr50.api.PartyAPI;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Gladiator
        implements Listener
{
    private static Cooldown hasteCD;
    private static Cooldown pocketSandCD;
    private static Cooldown whipCD;
    private static Cooldown critCD;
    private static Cooldown hamstringCD;
    private static Cooldown rotateCD;

    HashMap<String, Integer> selectedSpell = new HashMap();
    private SavageClasses SC;
    String[] spells;

    Gladiator(SavageClasses SC)
    {
        this.SC = SC;
        reload(SC);
        spells = new String[] { "Haste", "Pocket Sand", "Whip" };
    }

    public static void reload(SavageClasses SC){
        hasteCD = new Cooldown("gladiator.hasteCD",SC);
        pocketSandCD = new Cooldown("gladiator.pocketSandCD",SC);
        whipCD = new Cooldown("gladiator.whipCD",SC);
        critCD = new Cooldown("gladiator.critCD",SC);
        hamstringCD = new Cooldown("gladiator.hamstringCD",SC);
        rotateCD = new Cooldown("gladiator.rotateCD",SC);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent evt)
    {
        Player p = evt.getPlayer();
        if (SC.isClass(p, "gladiator"))
        {
            Material mainHand = p.getInventory().getItemInHand().getType();
            Action action = evt.getAction();
            if (mainHand == Material.FEATHER)
            {
                boolean doRotate = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
                if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
                {
                    if (selectedSpell.containsKey(p.getName()))
                    {
                        int spell = selectedSpell.get(p.getName());
                        // Haste spell
                        if (spell == 0)
                        {
                            if (!hasteCD.isOnCooldown(p))
                            {
                                haste(p);
                                SavageUtility.broadcastMessage(p.getName() + " &6used &bHaste!", p);

                                hasteCD.addCooldown(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage(
                                        "You can't haste for " + hasteCD.getRemainingTime(p) + "s", p);
                            }
                        }
                        // Pocket Sand
                        else if (spell == 1)
                        {
                            if (!pocketSandCD.isOnCooldown(p))
                            {
                                if (!SavageUtility.isPVPAllowed(p))
                                {
                                    SavageUtility.displayMessage("&cYou can't use sand in a non pvp area!", p);
                                    return;
                                }
                                sand(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage(
                                        "You can't sand for " + pocketSandCD.getRemainingTime(p) + "s", p);
                            }


                        }
                        // Whip spell
                        else
                        {
                            if(!whipCD.isOnCooldown(p)) {
                                if (!SavageUtility.isPVPAllowed(p)) {
                                    SavageUtility.displayMessage("&cYou can't use whip in a non pvp area!", p);
                                    return;
                                }
                                whip(p);
                                SavageUtility.broadcastMessage(p.getName() + " &6used &bWhip!", p);

                                whipCD.addCooldown(p);
                            }
                            else
                            {
                                SavageUtility.displayMessage(
                                        "You can't whip for " + whipCD.getRemainingTime(p) + "s", p);
                            }
                        }


                    }

                }
                else if (doRotate && !rotateCD.isOnCooldown(p))
                {
                    rotateCD.addCooldown(p);
                    if (selectedSpell.containsKey(p.getName()))
                    {
                        int ss = (selectedSpell.get(p.getName()));
                        if (ss < 2)
                        {
                            selectedSpell.put(p.getName(),ss + 1);
                            SavageUtility.displayMessage("You are now using the &c" + spells[(ss + 1)] + " &7spell", p);
                        }
                        else
                        {
                            selectedSpell.put(p.getName(), 0);
                            SavageUtility.displayMessage("You are now using the &c" + spells[0] + " &7spell", p);
                        }
                    }
                    else
                    {
                        selectedSpell.put(p.getName(), 0);
                        SavageUtility.displayMessage("You are now using the &cHaste &7Spell.", p);
                    }
                }
            }
        }
    }









    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent evt)
    {
        if ((evt.getDamager() instanceof Player))
        {
            Player p = (Player)evt.getDamager();
            if (SC.isClass(p, "Gladiator"))
            {
                if ((evt.getEntity() instanceof LivingEntity))
                {
                    double random = Math.random();

                    LivingEntity enemy = (LivingEntity)evt.getEntity();
                    if (!critCD.isOnCooldown(p))
                    {


                        if (random <= 0.35D)
                        {
                            double dmg = evt.getDamage();
                            evt.setDamage(dmg * 2.0D);
                            if ((evt.getEntity() instanceof Player))
                            {
                                Player player = (Player)evt.getEntity();
                                SavageUtility.displayMessage("You've been &c2x &7Crit!", player);
                            }
                            SavageUtility.displayMessage("You &c2x &7Crit!", p);

                            critCD.addCooldown(p);
                        }
                    }

                    if (!hamstringCD.isOnCooldown(p))
                    {
                        if (random >= 0.85D)
                        {

                            double dmg = evt.getDamage();
                            evt.setDamage(dmg + 2.0D);
                            if ((evt.getEntity() instanceof Player))
                            {
                                Player player = (Player)evt.getEntity();
                                SavageUtility.displayMessage("You've been &9hamstrung!", player);
                            }
                            SavageUtility.displayMessage("You have &9hamstrung your target!", p);
                            enemy.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                            enemy.setVelocity(new Vector(0, 0, 0));
                        }
                    }
                }
            }
        }
    }

    void sand(Player player)
    {
        assert (player != null);
        Entity target = SavageUtility.getTarget(player, 20, false);

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

        if (targetPlayer.hasPotionEffect(PotionEffectType.SPEED))
        {
            targetPlayer.removePotionEffect(PotionEffectType.SPEED);
            targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 0, 0));
        }

        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 300, 9));
        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 4));
        targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1));

        pocketSandCD.addCooldown(player);
        SavageUtility.broadcastMessage(player.getName() + " &6used &bPocket Sand!", player);
    }



    void haste(Player player)
    {
        if (player.hasPotionEffect(PotionEffectType.SLOW))
        {
            player.removePotionEffect(PotionEffectType.SLOW);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 0, 0));
        }

        if (player.hasPotionEffect(PotionEffectType.SPEED))
        {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 140, 3));
        }
        else
        {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 140, 3));
        }
    }

    private void whip(Player player)
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (p.getWorld().equals(player.getWorld()))
            {

                if (p.getLocation().distance(player.getLocation()) < 20.0D)
                {

                    if (p != player && !PartyAPI.inSameParty(p, player))
                    {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                        Vector pl = p.getLocation().toVector();
                        Vector vec = player.getLocation().toVector().subtract(pl).normalize().multiply(3.0D);
                        p.setVelocity(vec);
                    }
                }
            }
        }
    }
}
