package apallo.savage.savageclasses;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WaterTNT implements Listener {

    double radius = 2;
    int dura = 1;
    SavageClasses SC;

    private HashMap<Block, Integer> blocks = new HashMap<Block, Integer>();

//    private HashMap<Entity, Faction> tntPrimed = new HashMap<>();

    private static float tntPower;
    private Cooldown durabilityCheck = new Cooldown(.15);

    WaterTNT(SavageClasses SC){
        this.SC = SC;
        reload(SC);
    }

    public static void reload(SavageClasses SC) {
        tntPower = (float)SC.getClassStatsConfig().getDouble("tnt.power");
    }

//    @EventHandler
//    public void onTNTPrimed(EntitySpawnEvent event){
//        if(event.getEntityType() == EntityType.PRIMED_TNT){
//            Faction faction = SavageUtility.getFactionFromLocation(event.getLocation());
//            SC.getLogger().info("Primed TNT added to hashmap: " + faction.getId());
//            tntPrimed.put(event.getEntity(), faction);
//        }
//    }

//    @EventHandler
//    public void onPlayerInteract(PlayerInteractEvent event){
//        if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
//            if(event.getPlayer().getInventory().getItemInHand().getType() == Material.GUNPOWDER) {
//                if(!durabilityCheck.isOnCooldown(event.getPlayer())){
//                    if (blocks.containsKey(event.getClickedBlock())) {
//                        SavageUtility.displayMessage("This block has " + ChatColor.AQUA + blocks.get(event.getClickedBlock()).toString() + " durability remaining", event.getPlayer());
//                    } else {
//                        SavageUtility.displayMessage("This block is at full durability", event.getPlayer());
//                    }
//                    durabilityCheck.addCooldown(event.getPlayer());
//                }
//            }
//        }
//    }
//
//    @EventHandler
//    public void onEntityExplode(EntityExplodeEvent event){
//        if(event.getEntityType() == EntityType.PRIMED_TNT){
//            TNTPrimed tnt = (TNTPrimed)event.getEntity();
//            Location loc = event.getLocation();
//            Faction faction = SavageUtility.getFactionFromLocation(loc);
//
//            SC.getLogger().info(faction.getId());
//
//            // Prevent block damage if not in the wilderness and the tnt
//            if(!faction.getId().equals("0") && faction == tntPrimed.get(event.getEntity())){
//                tntPrimed.remove(event.getEntity());
//                SC.getLogger().info("TNT in same territory, event cancelled");
//                loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), tntPower, false, false);
//                event.setCancelled(true);
//                return;
//            }
//
//            if(loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.STATIONARY_WATER || loc.getBlock().getType() == Material.LAVA || loc.getBlock().getType() == Material.STATIONARY_LAVA) {
//                for (double x = loc.getX() - radius; x < loc.getX() + radius; x++) {
//                    for (double y = loc.getY() - radius; y < loc.getY() + radius; y++) {
//                        for (double z = loc.getZ() - radius; z < loc.getZ() + radius; z++) {
//                            Block block = loc.getWorld().getBlockAt((int)x, (int)y, (int)z);
//                            if(blocks.containsKey(block)){
//                                int curDura = blocks.get(block) - 1;
//                                blocks.put(block, curDura - 1);
//                                if(curDura <= 0){
//                                    if(block.getType() != Material.BEDROCK) {
//                                        blocks.remove(block);
//                                        block.breakNaturally();
//                                    }
//                                }
//                            }
//                            else{
//                                if(block.getType() == Material.OBSIDIAN){
//                                    blocks.put(block, 10);
//                                }
//                                else if(block.getType() == Material.ENDER_STONE){
//                                    blocks.put(block, 3);
//                                }
//                                else {
//                                    blocks.put(block, dura);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }


}
