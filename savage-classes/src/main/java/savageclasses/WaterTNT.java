package savageclasses;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WaterTNT implements Listener {

    double radius = 2;
    int dura = 1;
    SavageClasses SC;

    private HashMap<Block, Integer> blocks = new HashMap<Block, Integer>();

    WaterTNT(SavageClasses SC){
        this.SC = SC;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        if(event.getEntityType() == EntityType.PRIMED_TNT){
            Location loc = event.getLocation();

            if(loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.LAVA) {
                boolean cancelDamage = false;
                for (double x = loc.getX() - radius; x < loc.getX() + radius; x++) {
                    for (double y = loc.getY() - radius; y < loc.getY() + radius; y++) {
                        for (double z = loc.getZ() - radius; z < loc.getZ() + radius; z++) {
                            Block block = loc.getWorld().getBlockAt((int)x, (int)y, (int)z);
                            if(blocks.containsKey(block)){
                                int curDura = blocks.get(block) - 1;
                                blocks.put(block, curDura - 1);
                                SC.getLogger().info(Integer.toString(curDura));
                                if(curDura <= 0){
                                    if(block.getType() != Material.BEDROCK && SavageUtility.getPlayersWithin(loc, 8).isEmpty()) {
                                        block.breakNaturally();
                                    }
                                }
                            }
                            else{
                                blocks.put(block, dura);
                            }
                        }
                    }
                }
            }
        }
    }


}
