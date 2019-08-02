package savageclasses;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.nossr50.api.PartyAPI;

public class Bard implements Listener {
    public HashMap<String, Hymn> selectedHymn = new HashMap();
    public HashMap<String, Spell> selectedSpell = new HashMap();
    static Cooldown hymnCD;
    static Cooldown inspireCD;
    static Cooldown melodyCD;
    static Cooldown cleanseCD;
    public static double hymnRange;
    public static double inspireAmount;
    public static double inspirePartyAmount;
    static int hymnDuration;
    static int hymnLevel;

    enum Hymn {
        STRENGTH, SPEED, REGENERATION, PROTECTION, FIRE_RESISTANCE
    }

    enum Spell {
        INSPIRE, MELODY, CLEANSE
    }

    SavageClasses SC;

    public Bard(SavageClasses SC) {
        this.SC = SC;
        reload(SC);
    }

    public static void reload(SavageClasses SC){
        hymnCD = new Cooldown("bard.hymnCD",SC);
        inspireCD = new Cooldown("bard.inspireCD",SC);
        melodyCD = new Cooldown("bard.melodyCD",SC);
        cleanseCD = new Cooldown("bard.cleanseCD",SC);

        hymnRange = SC.getClassStatsConfig().getDouble("bard.hymnRange");
        hymnDuration = SC.getClassStatsConfig().getInt("bard.hymnDuration");
        hymnLevel = SC.getClassStatsConfig().getInt("bard.hymnLevel");
        inspireAmount = SC.getClassStatsConfig().getDouble("bard.inspirePartyAmount");
        inspirePartyAmount = SC.getClassStatsConfig().getDouble("bard.inspirePartyAmount");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        if (SC.isClass(player, "Bard")) {
            EquipmentSlot e = event.getHand();
            if (e == null) {
                return;
            }

            if (e.equals(EquipmentSlot.HAND)) {
                ItemStack itemHeld;
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    itemHeld = player.getInventory().getItemInMainHand();

                    if (itemHeld.getType() == Material.GLOWSTONE_DUST) {
                        Hymn hymn;
                        if (selectedHymn.containsKey(player.getName().toLowerCase())) {
                            hymn = selectedHymn.get(player.getName().toLowerCase());
                            hymn = Hymn.values()[(hymn.ordinal() + 1) % Hymn.values().length];
                        }
                        else {
                            hymn = Hymn.STRENGTH;
                        }
                        SavageUtility.displayMessage("You ready your &aHymn of &c" + hymn.toString(), player);

                        selectedHymn.put(player.getName().toLowerCase(), hymn);
                    }
                    else if (itemHeld.getType() == Material.CLOCK) {
                        Spell spell;
                        if (selectedSpell.containsKey(player.getName().toLowerCase())) {
                            spell = selectedSpell.get(player.getName().toLowerCase());
                            spell = Spell.values()[(spell.ordinal() + 1) % Spell.values().length];
                        }
                        else {
                            spell = Spell.INSPIRE;
                        }

                        if (spell == Spell.INSPIRE) {
                            SavageUtility.displayMessage("You ready your &aInspiration&7 spell.", player);
                        }

                        if (spell == Spell.MELODY) {
                            SavageUtility.displayMessage("You ready your &cChilling Melody&7 spell.", player);
                        }

                        if (spell == Spell.CLEANSE) {
                            SavageUtility.displayMessage("You ready your &bCleanse&7 spell.", player);
                        }

                        selectedSpell.put(player.getName().toLowerCase(), spell);
                    }
                }

                if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {

                    if (player.getInventory().getItemInMainHand().getType() == Material.GLOWSTONE_DUST) {
                        Hymn hymn;
                        if (SavageUtility.hasIronGear(player)) {
                            if (hymnCD.isOnCooldown(player)) {
                                SavageUtility.displayMessage("You cannot cast a hymn for another &b" + hymnCD.getRemainingTime(player) + "&c seconds.", player);
                            }
                            else {
                                if (selectedHymn.containsKey(player.getName().toLowerCase())) {
                                    hymn = selectedHymn.get(player.getName().toLowerCase());
                                } else {
                                    hymn = Hymn.STRENGTH;
                                }

                                PotionEffectType potionEffect = PotionEffectType.INCREASE_DAMAGE;
                                String effectname = ChatColor.GREEN + "";
                                if (hymn == Hymn.STRENGTH) {
                                    potionEffect = PotionEffectType.INCREASE_DAMAGE;
                                    effectname = ChatColor.GREEN + "Hymn of Strength.";
                                }

                                else if (hymn == Hymn.SPEED) {
                                    potionEffect = PotionEffectType.SPEED;
                                    effectname = ChatColor.GREEN + "Hymn of Speed.";
                                }

                                else if (hymn == Hymn.REGENERATION) {
                                    potionEffect = PotionEffectType.REGENERATION;
                                    effectname = ChatColor.GREEN + "Hymn of Regeneration.";
                                }

                                else if (hymn == Hymn.PROTECTION) {
                                    potionEffect = PotionEffectType.DAMAGE_RESISTANCE;
                                    effectname = ChatColor.GREEN + "Hymn of Protection.";
                                }

                                else if (hymn == Hymn.FIRE_RESISTANCE) {
                                    potionEffect = PotionEffectType.FIRE_RESISTANCE;
                                    effectname = ChatColor.GREEN + "Hymn of Fire Resistance.";
                                }

                                hymnCD.addCooldown(player);
                                if (player.hasPotionEffect(potionEffect)) {
                                    player.removePotionEffect(potionEffect);
                                    player.addPotionEffect(new PotionEffect(potionEffect, 600, 1));
                                } else {
                                    player.addPotionEffect(new PotionEffect(potionEffect, 600, 1));
                                }

                                SavageUtility.displayMessage("You buff yourself with " + effectname, player);

                                if (PartyAPI.inParty(player)) {
                                    Location bardloc = player.getLocation();
                                    World playerWorld = player.getWorld();
                                    Iterator partyMembers = PartyAPI.getOnlineMembers(player).iterator();

                                    while(partyMembers.hasNext()) {
                                        Player partyPlayer = (Player)partyMembers.next();
                                        if (partyPlayer != player) {
                                            Location partyloc = partyPlayer.getLocation();
                                            if (partyPlayer.getWorld().equals(playerWorld)) {
                                                if (bardloc.distance(partyloc) <= hymnRange) {
                                                    if (partyPlayer.hasPotionEffect(potionEffect)) {
                                                        partyPlayer.removePotionEffect(potionEffect);
                                                        partyPlayer.addPotionEffect(new PotionEffect(potionEffect, 600, 1));
                                                    } else {
                                                        partyPlayer.addPotionEffect(new PotionEffect(potionEffect, 600, 1));
                                                    }

                                                    SavageUtility.displayMessage("You have been buffed by &6" + player.getDisplayName() + "&6's " + effectname, partyPlayer);
                                                } else {
                                                    SavageUtility.displayMessage("You were too far away to recieve &6" + player.getDisplayName() + "&6's " + effectname, partyPlayer);
                                                }
                                            } else {
                                                SavageUtility.displayMessage("Due to being in another world, you did not recieve " + player.getDisplayName() + "&6's " + effectname, partyPlayer);
                                            }
                                        }
                                    }
                                }
                            }
                    } else {
                            SavageUtility.displayMessage("&cYou must be wearing full iron armor to cast spells", player);
                        }
                    }

                    else if (player.getInventory().getItemInMainHand().getType() == Material.CLOCK) {
                        if (SavageUtility.hasIronGear(player)) {
                            Spell spell;

                            if (selectedSpell.containsKey(player.getName().toLowerCase())) {
                                spell = selectedSpell.get(player.getName().toLowerCase());
                            } else {
                                spell = Spell.INSPIRE;
                            }

                            if (spell == Spell.INSPIRE) {
                                if (inspireCD.isOnCooldown(player)) {
                                    player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You cannot cast " + ChatColor.GOLD + "Inspiration " + ChatColor.RED + "again for " + ChatColor.AQUA + inspireCD.getRemainingTime(player) + ChatColor.RED + " seconds.");
                                } else {
                                    inspireCD.addCooldown(player);
                                    double playerHealth = player.getHealth() + this.inspireAmount;
                                    if (playerHealth > 20) {
                                        playerHealth = 20;
                                    }

                                    player.setHealth(playerHealth);
                                    SavageUtility.displayMessage("&aYou heal yourself for &e" + inspireAmount / 2.0D + " &ahearts.", player);
                                    if (PartyAPI.inParty(player)) {
                                        SavageUtility.displayMessage("&aYou &eInpsire&a your party, healing them for &e" + inspirePartyAmount / 2.0D + " &ahearts.", player);
                                        Location bardloc = player.getLocation();
                                        World playerWorld = player.getWorld();
                                        Iterator partyMembers = PartyAPI.getOnlineMembers(player).iterator();

                                        while (partyMembers.hasNext()) {
                                            Player partyplayer = (Player) partyMembers.next();
                                            if (partyplayer != player) {
                                                Location partyloc = partyplayer.getLocation();
                                                if (partyplayer.getWorld().equals(playerWorld)) {
                                                    if (bardloc.distance(partyloc) < 25.0D) {
                                                        double partyhealth = partyplayer.getHealth() + this.inspirePartyAmount;
                                                        if (partyhealth > 20) {
                                                            partyhealth = 20;
                                                        }

                                                        partyplayer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 0));
                                                        partyplayer.setHealth(partyhealth);
                                                        partyplayer.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.GREEN + "You have been " + ChatColor.GOLD + "Inspired " + ChatColor.GREEN + "by " + ChatColor.GOLD + player.getDisplayName() + ".");
                                                    } else {
                                                        partyplayer.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You were too far away to receive " + ChatColor.GOLD + player.getDisplayName() + ChatColor.RED + "'s " + ChatColor.GOLD + "Inspiration!");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else if (spell == Spell.MELODY) {

                                if(!SavageUtility.isPVPAllowed(player)){
                                    player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "PvP is disabled in this area.");
                                }
                                else {
                                    if (melodyCD.isOnCooldown(player)) {
                                            player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You cannot cast " + ChatColor.GOLD + "Chilling Melody " + ChatColor.RED + "again for " + ChatColor.AQUA + (melodyCD.getRemainingTime(player)) + ChatColor.RED + " seconds.");
                                    }
                                    else {
                                        melodyCD.addCooldown(player);
                                        World playerWorld = player.getWorld();
                                        Location bardLocation = player.getLocation();
                                        player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.GREEN + "You cast " + ChatColor.GOLD + "Chilling Melody" + ChatColor.GREEN + ", slowing all nearby enemies.");
                                        Iterator allPlayers = SC.getServer().getOnlinePlayers().iterator();

                                        while (allPlayers.hasNext()) {
                                            Player p = (Player) allPlayers.next();
                                            Location pLocation = p.getLocation();
                                            if (p.getWorld().equals(playerWorld) && bardLocation.distance(pLocation) < 16.0D && !PartyAPI.inSameParty(p, player) && !p.equals(player)) {
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 4));
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 120, 4));
                                                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 0));
                                                p.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You have been snared by " + ChatColor.GOLD + player.getDisplayName() + ChatColor.RED + "'s " + ChatColor.GOLD + "Chilling Melody.");
                                            }
                                        }
                                    }
                                }
                            }
                            else if (spell == Spell.CLEANSE) {
                                if (cleanseCD.isOnCooldown(player)) {
                                        player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You cannot cast " + ChatColor.GOLD + "Cleanse " + ChatColor.RED + "again for " + ChatColor.AQUA + cleanseCD.getRemainingTime(player) + ChatColor.RED + " seconds.");
                                }
                                else {
                                    cleanseCD.addCooldown(player);
                                    SavageUtility.displayMessage("You &acleanse&7 yourself of all negative effects.", player);

                                    player.setFireTicks(0);
                                    Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
                                    for(PotionEffect potionEffect : potionEffects){
                                        if(player.hasPotionEffect(potionEffect.getType()) && SavageUtility.negativeEffects.contains(potionEffect.getType())){
                                            player.removePotionEffect(potionEffect.getType());
                                            player.addPotionEffect(new PotionEffect(potionEffect.getType(), 0, 0));
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            SavageUtility.displayMessage("&cYou must be wearing full iron armor to cast spells", player);
                        }
                    }
                }
            }
        }
    }
}

