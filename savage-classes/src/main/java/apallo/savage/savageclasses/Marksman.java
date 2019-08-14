package apallo.savage.savageclasses;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Marksman implements Listener
{
    private SavageClasses SC;
    private HashMap<String, Float> forceHash = new HashMap();
    private static Cooldown leapCD;
    private static Cooldown arrowToTheKneeCD;
    private static Cooldown staticShotCD;
    private static Cooldown poisonShotCD;
    private static double leapVelocity;
    private static double leapYVelocity;
    private static Cooldown rotateCD;
    private static double extraDamage; // per piece of equipment

    public static HashSet<Projectile> arrows = new HashSet();
    public static HashSet<Projectile> cripplearrow = new HashSet();
    public static HashSet<Projectile> poisonArrow = new HashSet();

    public static HashMap<Player, String> leftclick = new HashMap();


    public static HashSet<String> isLeaping = new HashSet();

    Marksman(SavageClasses SC)
    {
        this.SC = SC;
        reload(SC);
    }

    public static void reload(SavageClasses SC){
        leapCD = new Cooldown("marksman.leapCD",SC);
        arrowToTheKneeCD = new Cooldown("marksman.arrowToTheKneeCD",SC);
        staticShotCD = new Cooldown("marksman.staticShotCD",SC);
        poisonShotCD = new Cooldown("marksman.poisonShotCD",SC);
        leapVelocity = SC.getClassStatsConfig().getDouble("marksman.leapVelocity");
        leapYVelocity = SC.getClassStatsConfig().getDouble("marksman.leapYVelocity");
        rotateCD = new Cooldown("marksman.rotateCD",SC);
        extraDamage = SC.getClassStatsConfig().getDouble("marksman.extraDamage");
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity))
        {
            return;
        }

        LivingEntity hit = (LivingEntity)event.getEntity();

        if ((hit instanceof Player))
        {
            Player player = (Player)hit;

            // Prevent damage from hitting ground after leaping.
            if (isLeaping.contains(player.getName()) && SC.isClass(player, "marksman"))
            {
                if (event.getCause() == EntityDamageEvent.DamageCause.FALL)
                {
                    event.setCancelled(true);
                    isLeaping.remove(player.getName());
                    player.playSound(player.getLocation(), Sound.FALL_SMALL, 25.0F, 25.0F);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2));
                    return;
                }
            }
        }


        // Damager is the marksman shooting
        // All of this is from marksman damage
        if (!(event instanceof EntityDamageByEntityEvent)) return;
        EntityDamageByEntityEvent event_EE = (EntityDamageByEntityEvent)event;
        if (!(event_EE.getDamager() instanceof Arrow)) { return;
        }


        Projectile projectile = (Projectile)event_EE.getDamager();

        // Check if a player shot the projectile
        if (!(projectile.getShooter() instanceof Player))
        {
            return;
        }

        Player attacker = (Player)projectile.getShooter();
        if (arrows.remove(projectile))
        {
            SavageUtility.displayMessage("&cYou've been hit by a &eStatic Shot", hit);
            SavageUtility.displayMessage("&cYou hit &e" + hit.getName() + " &cwith a &eStatic shot!", attacker);

            if ((hit instanceof Player))
            {
                hit.damage(.5D, attacker);
            }
            else
            {
                hit.damage(hit.getHealth() * 0.25D, attacker);
            }
            MarksmanStaticShot localStatic = new MarksmanStaticShot(SC, hit, attacker);
        }

        if (cripplearrow.remove(projectile))
        {
            SavageUtility.displayMessage("&cYou've been hit by an &9Arrow to the knee!", hit);
            SavageUtility.displayMessage("&cYou hit " + hit.getName() + " &cwith an &9Arrow to the Knee!", attacker);

            hit.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 3));
            hit.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 80, -2));
        }

        if(poisonArrow.remove(projectile)){
            SavageUtility.displayMessage("&cYou've been hit by a &2Poison Arrow!", hit);
            SavageUtility.displayMessage("&cYou hit " + hit.getName() + " &cwith a &2Poison Arrow!", attacker);

            hit.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 1));
        }

        if (!(projectile.getShooter() instanceof Player))
        {
            return;
        }





        if (!(attacker instanceof Player)) { return;
        }
        if (SC.isClass(attacker, "marksman"))
        {

            PlayerInventory inv = attacker.getInventory();
            ItemStack helm = inv.getHelmet();
            ItemStack chest = inv.getChestplate();
            ItemStack legs = inv.getLeggings();
            ItemStack boots = inv.getBoots();


            int multiplier = 1;

            double helmdamage = 0.0D;
            double chestdamage = 0.0D;
            double legdamage = 0.0D;
            double bootdamage = 0.0D;

            float newForce = 0.0F;

            if (forceHash.containsKey(attacker.getName()))
            {
                newForce = ((Float)forceHash.get(attacker.getName())).floatValue();
                forceHash.remove(attacker.getName());
            }

            if (attacker.getInventory().getHelmet() != null)
            {
                if (helm.getType() == Material.LEATHER_HELMET)
                {
                    if ((newForce > 0.0F) && (newForce <= 0.15D))
                    {
                        helmdamage = 0.0D;
                    }
                    if ((newForce > 0.15D) && (newForce <= 0.25D))
                    {
                        helmdamage = 0.0D;
                    }
                    if ((newForce > 0.25D) && (newForce <= 0.4D))
                    {
                        helmdamage = 0.0D;
                    }
                    if ((newForce > 0.4D) && (newForce <= 0.6D))
                    {
                        helmdamage = 0.0D;
                    }
                    if ((newForce > 0.6D) && (newForce <= 0.8D))
                    {
                        helmdamage = 0.0D;
                    }
                    if ((newForce > 0.8D) && (newForce < 1.0D))
                    {
                        helmdamage = .15D;
                    }
                    if (newForce == 1.0D)
                    {
                        helmdamage = extraDamage;
                    }
                }
            }

            if (chest != null)
            {

                if (chest.getType() == Material.LEATHER_CHESTPLATE)
                {
                    if ((newForce > 0.0F) && (newForce <= 0.15D))
                    {
                        chestdamage = 0.0D;
                    }
                    if ((newForce > 0.15D) && (newForce <= 0.25D))
                    {
                        chestdamage = 0.0D;
                    }
                    if ((newForce > 0.25D) && (newForce <= 0.4D))
                    {
                        chestdamage = 0.0D;
                    }
                    if ((newForce > 0.4D) && (newForce <= 0.6D))
                    {
                        chestdamage = 0.0D;
                    }
                    if ((newForce > 0.6D) && (newForce <= 0.8D))
                    {
                        chestdamage = .10D;
                    }
                    if ((newForce > 0.8D) && (newForce < 1.0D))
                    {
                        chestdamage = .15D;
                    }
                    if (newForce == 1.0D)
                    {
                        chestdamage = extraDamage;
                    }
                }
            }

            if (legs != null)
            {
                if (legs.getType() == Material.LEATHER_LEGGINGS)
                {
                    if ((newForce > 0.0F) && (newForce <= 0.15D))
                    {
                        legdamage = 0.0D;
                    }
                    if ((newForce > 0.15D) && (newForce <= 0.25D))
                    {
                        legdamage = 0.0D;
                    }
                    if ((newForce > 0.25D) && (newForce <= 0.4D))
                    {
                        legdamage = 0.0D;
                    }
                    if ((newForce > 0.4D) && (newForce <= 0.6D))
                    {
                        legdamage = 0.0D;
                    }
                    if ((newForce > 0.6D) && (newForce <= 0.8D))
                    {
                        legdamage = .10D;
                    }
                    if ((newForce > 0.8D) && (newForce < 1.0D))
                    {
                        legdamage = .15D;
                    }
                    if (newForce == 1.0D)
                    {
                        legdamage = extraDamage;
                    }
                }
            }


            if (attacker.getInventory().getBoots() != null)
            {

                if (boots.getType() == Material.LEATHER_BOOTS)
                {
                    if ((newForce > 0.0F) && (newForce <= 0.15D))
                    {
                        bootdamage = 0.0D;
                    }
                    if ((newForce > 0.15D) && (newForce <= 0.25D))
                    {
                        bootdamage = 0.0D;
                    }
                    if ((newForce > 0.25D) && (newForce <= 0.4D))
                    {
                        bootdamage = 0.0D;
                    }
                    if ((newForce > 0.4D) && (newForce <= 0.6D))
                    {
                        bootdamage = 0.0D;
                    }
                    if ((newForce > 0.6D) && (newForce <= 0.8D))
                    {
                        bootdamage = 0.0D;
                    }
                    if ((newForce > 0.8D) && (newForce < 1.0D))
                    {
                        bootdamage = .15D;
                    }
                    if (newForce == 1.0D)
                    {
                        bootdamage = extraDamage;
                    }
                }
            }

            double totalBonus = helmdamage + chestdamage + legdamage + bootdamage;

            double totalDamage = Math.round(event.getDamage() + totalBonus);
            event.setDamage(totalDamage);
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event)
    {
        Projectile projectile = (Projectile)event.getProjectile();

        if (!(projectile.getShooter() instanceof Player))
        {
            return;
        }
        Player player = (Player)projectile.getShooter();
        if(!SC.isClass(player, "marksman")) return;

        Vector boostedArrowVelocity = calculateMarksmanVelocityBoost(player, event, projectile);
        projectile.setVelocity(boostedArrowVelocity);



        if (SavageUtility.hasLeatherGear(player))
        {
            if (!player.isSneaking())
            {
                return;
            }

            String whichattack2 = leftclick.get(player);

            if (whichattack2 == null || whichattack2.equalsIgnoreCase("static")) // Static shot
            {
                if(!staticShotCD.isOnCooldown(player)){
                    arrows.add(projectile);
                    staticShotCD.addCooldown(player);
                }
                else{
                    SavageUtility.displayMessage("&cStatic shot &7 on cooldown for: " + staticShotCD.getRemainingTime(player) + " seconds.", player);
                }
            }
            else if(whichattack2.equalsIgnoreCase("cripple")) // Arrow to the knee
            {
                if(!arrowToTheKneeCD.isOnCooldown(player)) {
                    cripplearrow.add(projectile);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1));
                    arrowToTheKneeCD.addCooldown(player);
                }
                else {
                    SavageUtility.displayMessage("&cArrow to the knee &7 on cooldown for: " + arrowToTheKneeCD.getRemainingTime(player) + " seconds.", player);
                }
            }
            else if(whichattack2.equalsIgnoreCase("poison")){
                if(!poisonShotCD.isOnCooldown(player)) {
                    poisonArrow.add(projectile);
                    poisonShotCD.addCooldown(player);
                }
                else {
                    SavageUtility.displayMessage("&2Poison Shot &7is on cooldown for: " + poisonShotCD.getRemainingTime(player) + " seconds.", player);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();


        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            if (player.getInventory().getItemInHand().getType() == Material.BOW && !rotateCD.isOnCooldown(player))
            {
                if (SC.isClass(player, "marksman"))
                {
                    rotateCD.addCooldown(player);
                    String whichattack = (String)leftclick.get(player);

                    if (whichattack == null || "Static".equals(whichattack))
                    {
                        leftclick.put(player, "Cripple");
                        SavageUtility.displayMessage("&7You ready your &cArrow to the Knee.", player);
                    }
                    if ("Cripple".equals(whichattack))
                    {
                        leftclick.put(player, "Poison");
                        SavageUtility.displayMessage("&7You ready your &2Poison Arrow.", player);
                    }
                    if ("Poison".equals(whichattack))
                    {
                        leftclick.put(player, "Static");
                        SavageUtility.displayMessage("&7You ready your &eStatic Shot.", player);
                    }
                    
//                    if ("Static".equals(whichattack))
//                    {
//                        leftclick.put(player, "Nothing");
//
//                        SavageUtility.displayMessage("&7You decide not to shoot.", player);
//                    }
//
//                    if ("Nothing".equals(whichattack))
//                    {
//                        leftclick.put(player, "Cripple");
//
//                        SavageUtility.displayMessage("&7You ready your &cArrow to the Knee.", player);
//                    }
                }
            }


            if (player.getInventory().getItemInHand().getType() == Material.FEATHER)
            {
                if (SC.isClass(player, "marksman"))
                {
                    if(SavageUtility.hasLeatherGear(player)){
                        if (!leapCD.isOnCooldown(player))
                        {
                            SavageUtility.broadcastMessage(player.getName() + "&6 leaps away!", player);
                            Vector v = player.getLocation().getDirection();
                            v.setY(0).normalize().multiply(leapVelocity).setY(leapYVelocity);
                            player.setVelocity(v);

                            player.playSound(player.getLocation(), Sound.FALL_SMALL, 25.0F, 25.0F);
                            player.playEffect(EntityEffect.WOLF_SMOKE);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, 2));

                            isLeaping.add(player.getName());
                            leapCD.addCooldown(player);
                        }
                        else{
                            SavageUtility.displayMessage("&7You must wait &4" + leapCD.getRemainingTime(player) + "&7 seconds to use &cLeap&7 again", player);
                        }
                    }
                    else{
                        SavageUtility.displayMessage("You must wear full leather armor to use marksman abilities.", player);
                    }
                }
            }
        }
    }

    Vector calculateMarksmanVelocityBoost(Player player, EntityShootBowEvent event, Projectile projectile){
            forceHash.put(player.getName(), event.getForce());

            float armorbonus = 0;
            if (player.getInventory().getChestplate() != null)
            {
                if (player.getInventory().getChestplate().getType().equals(Material.LEATHER_CHESTPLATE))
                {
                    armorbonus += 7;
                }
            }

            if (player.getInventory().getLeggings() != null)
            {
                if (player.getEquipment().getLeggings().getType().equals(Material.LEATHER_LEGGINGS))
                {
                    armorbonus += 5;
                }
            }
            if (player.getInventory().getHelmet() != null)
            {
                if (player.getInventory().getHelmet().getType().equals(Material.LEATHER_HELMET))
                {
                    armorbonus += 3;
                }
            }
            if (player.getInventory().getBoots() != null)
            {
                if (player.getInventory().getBoots().getType().equals(Material.LEATHER_BOOTS))
                {
                    armorbonus += 2;
                }
            }

            float draw = event.getForce();
            Vector newVelocity = projectile.getVelocity().multiply(1.0D + 0.5D * (armorbonus * 5 / 100) * draw);

            return newVelocity;
    }
}
