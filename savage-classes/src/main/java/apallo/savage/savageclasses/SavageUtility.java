package apallo.savage.savageclasses;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.gmail.nossr50.api.PartyAPI;

import sun.security.krb5.SCDynamicStoreConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if ((chest == Material.IRON_CHESTPLATE) && legs == Material.IRON_LEGGINGS && helm == Material.GOLD_HELMET && boots == Material.GOLD_BOOTS){
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
            if ((chest == Material.GOLD_CHESTPLATE) && legs == Material.GOLD_LEGGINGS && helm == Material.GOLD_HELMET && boots == Material.GOLD_BOOTS){
                return true;
            }
        }
        return false;
    }
    public static boolean hasShadowKnightGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            List<Material> acceptable_helm = Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLD_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET);
            List<Material> acceptable_boots = Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLD_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS);

            if ((chest == Material.IRON_CHESTPLATE) && legs == Material.IRON_LEGGINGS && acceptable_helm.contains(helm) && acceptable_boots.contains(boots)){
                return true;
            }
        }
        return false;
    }
    public static boolean hasMartyrGear(Player player){
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null) && (player.getInventory().getHelmet() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            Material helm = player.getInventory().getHelmet().getType();
            if ((chest == Material.GOLD_CHESTPLATE) && legs == Material.GOLD_LEGGINGS && helm == Material.LEATHER_HELMET && boots == Material.LEATHER_BOOTS){
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasWarlockGear(Player player) {
        if ((player.getInventory().getChestplate() != null) && (player.getInventory().getLeggings() != null) &&
                (player.getInventory().getBoots() != null)){

            Material chest = player.getInventory().getChestplate().getType();
            Material legs = player.getInventory().getLeggings().getType();
            Material boots = player.getInventory().getBoots().getType();
            ItemStack helm = player.getInventory().getHelmet();
            if ((helm == null && chest == Material.GOLD_CHESTPLATE) && legs == Material.GOLD_LEGGINGS && boots == Material.GOLD_BOOTS){
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
            List<Material> acceptable_helm = Arrays.asList(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.GOLD_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET);
            List<Material> acceptable_boots = Arrays.asList(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLD_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS);

            if ((chest == Material.LEATHER_CHESTPLATE) && legs == Material.LEATHER_LEGGINGS && acceptable_helm.contains(helm) && acceptable_boots.contains(boots)){
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

    public static Entity getTarget(Player caster, double range, boolean allowTargetParty){
        Vector l = caster.getEyeLocation().toVector();
        Vector n = caster.getLocation().getDirection().normalize();
        double cos45 = Math.cos(0.7853981633974483D);

        List<Entity> entities = caster.getNearbyEntities(range*1.5, range*1.5, range*1.5);
        for (Entity e : entities)
        {
            if (e != caster)
            {
                Vector t = e.getLocation().add(0.0D, 1.0D, 0.0D).toVector().subtract(l);
                if ((n.clone().crossProduct(t).lengthSquared() < 1.0D) && (t.normalize().dot(n) >= cos45))
                {
                    if(getTargetDistance(caster, e) <= range) {
                         if(e instanceof Player && !allowTargetParty && PartyAPI.inSameParty(caster, (Player) e)) {
                            SavageUtility.displayMessage(ChatColor.RED + "This target is in your party!", caster);
                            return null;
                         }
                         else{
                             return e;
                         }
                    }
                    else{
                        SavageUtility.displayMessage("Target is out of range", caster);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static double getTargetDistance(Player caster, Entity target){
        return target.getLocation().distance(caster.getLocation());
    }
//
//    public static boolean isInOwnTerritory(Player target){
//        if(FPlayers.getInstance().getByPlayer(target).isInOwnTerritory()){
//            return true;
//        }
//        else{
//            return false;
//        }
//
//    }
//
//    public static Faction getFactionFromLocation(Location loc){
//        Faction faction = null;
//        FLocation floc = new FLocation(loc);
//
//        faction = Board.getInstance().getFactionAt(floc);
//
//        return faction;
//    }

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
    
    static List<Player> getPartyMembers(Player player) {
    	if(PartyAPI.inParty(player)) {
    		return(PartyAPI.getOnlineMembers(player));
    	}
    	return(Arrays.asList(player));
    }
    static boolean isPVPAllowed(LivingEntity player) {
    	return true;
    }

	public static boolean isInOwnTerritory(Player target) {
		return false;
	}
    
//    static boolean isPVPAllowed(LivingEntity player)
//    {
//        RegionContainer container =WorldGuardPlugin.inst().getRegionContainer();
//        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer((Player)player);
//        RegionQuery query = container.createQuery();
//        ApplicableRegionSet set = query.getApplicableRegions(player.getLocation());
//        if(!set.testState(localPlayer, DefaultFlag.PVP)){
//            return false;
//        }
//        else{
//            return true;
//        }
//    }
}
