package apallo.savage.savageclasses;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SavageUtility {

    public SavageUtility() {};

    public static ArrayList<PotionEffectType> negativeEffects = new ArrayList<PotionEffectType>(Arrays.asList(PotionEffectType.BLINDNESS,PotionEffectType.CONFUSION,PotionEffectType.HUNGER,PotionEffectType.POISON,PotionEffectType.SLOW,PotionEffectType.SLOW_DIGGING,PotionEffectType.WEAKNESS,PotionEffectType.WITHER));

    public static void displayMessage(String s, LivingEntity p){
        if(p instanceof Player) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&9SR&8]&7 " + s));
        }
    }

    public static void broadcastMessage(String s, Player p)
    {
        for (Player x : getPlayersWithin(p, 20)) {
            displayMessage(s, x);
        }
    }

    public static void broadcastMessage(String s, Player p, Integer distance)
    {
        for (Player x : getPlayersWithin(p, distance)) {
            displayMessage(s, x);
        }
    }

    public static void removePotionEffects(Player player){
        if (player.hasPotionEffect(PotionEffectType.SPEED))
        {
            player.removePotionEffect(PotionEffectType.SPEED);
        }
        if (player.hasPotionEffect(PotionEffectType.JUMP))
        {
            player.removePotionEffect(PotionEffectType.JUMP);
        }
    }

    public static boolean hasScoutGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            if ((chest == Material.IRON_CHESTPLATE) && legs == Material.IRON_LEGGINGS && helm == Material.GOLDEN_HELMET && boots == Material.GOLDEN_BOOTS){
                return true;
            }
        }
        return false;
    }

    public static boolean hasLeatherGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            if ((chest == Material.LEATHER_CHESTPLATE) && legs == Material.LEATHER_LEGGINGS && helm == Material.LEATHER_HELMET && boots == Material.LEATHER_BOOTS){
                return true;
            }
        }
        return false;
    }

    public static boolean hasIronGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            if ((chest == Material.IRON_CHESTPLATE) && legs == Material.IRON_LEGGINGS && helm == Material.IRON_HELMET && boots == Material.IRON_BOOTS){
                return true;
            }
        }
        return false;
    }

    public static boolean hasGoldGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            if ((chest == Material.GOLDEN_CHESTPLATE) && legs == Material.GOLDEN_LEGGINGS && helm == Material.GOLDEN_HELMET && boots == Material.GOLDEN_BOOTS){
                return true;
            }
        }
        return false;
    }

	public static boolean hasBerserkerGear(Player player) {
		if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
				(player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

			Material chest = player.getInventory().getChestplate().getType();
			Material legs = player.getInventory().getLeggings().getType();
			Material boots = player.getInventory().getBoots().getType();
			Material helm = player.getInventory().getHelmet().getType();
			List<Material> acceptable_helm = Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLDEN_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET);
			List<Material> acceptable_boots = Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS);

			if ((chest == Material.LEATHER_CHESTPLATE) && legs == Material.LEATHER_LEGGINGS && acceptable_helm.contains(helm) && acceptable_boots.contains(boots)){
				return true;
			}
		}
		return false;
	}
    public static boolean hasTritonGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            if ((chest == Material.GOLDEN_CHESTPLATE) && helm == Material.TURTLE_HELMET){
                return true;
            }
        }
        return false;
    }

    public static List<Player> getPlayersWithin(Player player, int distance)
    {
        List<Player> res = new ArrayList();
        int d2 = distance * distance;
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if ((p.getWorld() == player.getWorld()) &&
                    (p.getLocation().distanceSquared(player.getLocation()) <= d2)) {
                res.add(p);
            }
        }
        return res;
    }

    public static List<Player> getPlayersWithin(Location l, int distance)
    {
        List<Player> res = new ArrayList();
        int d2 = distance * distance;
        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (p.getWorld() == l.getWorld() && p.getLocation().distanceSquared(l) <= d2){
                res.add(p);
            }
        }
        return res;
    }

    public static void hitEntityMessage(String s, String s2, LivingEntity e, Player g)
    {
        if ((e instanceof Player))
        {
            Player p = (Player)e;
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&9SR&8]&7 " + s));

        }
        else
        {
            g.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&9SR&8]&7 " + s2));
        }
    }

    static Iterable<LivingEntity> getLivingEntitiesWithin(Block b, int distance)
    {
        List<LivingEntity> res = new ArrayList();
        int d2 = distance * distance;

        for (LivingEntity p : b.getWorld().getEntitiesByClass(LivingEntity.class))
        {

            if ((p.getWorld() == b.getWorld()) &&
                    (p.getLocation().distanceSquared(b.getLocation()) <= d2)) {
                res.add(p);
            }
        }
        return res;
    }

    static boolean isPVPAllowed(LivingEntity player)
    {
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        return set.testState(null, Flags.PVP);
    }
}
