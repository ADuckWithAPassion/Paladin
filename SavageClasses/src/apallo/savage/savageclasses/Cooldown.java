package apallo.savage.savageclasses;

import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Cooldown {
    private HashMap<String, Double> cd = new HashMap();
    private double time;

    public Cooldown(String location, SavageClasses SC){
    	try {
    	    this.time = SC.getClassStatsConfig().getDouble(location)*1000;		
    	    Log.info(this.time);
    	    Log.info(location);
    	}
    	catch(Error error){
    		error.getStackTrace();
    	}
    }      


    public boolean isOnCooldown(Player p){
        if(cd.containsKey(p.getName())){
            if(System.currentTimeMillis() < cd.get(p.getName()).longValue()){
                return true;
            }
        }
        return false;
    }

    public void addCooldown(Player p){
        cd.put(p.getName(), Double.valueOf(System.currentTimeMillis() + time));
    }

    public void resetCooldown(Player p){
        cd.remove(p.getName());
    }

    public int getRemainingTime(Player p){
        return (int)(((cd.get(p.getName())).longValue() - System.currentTimeMillis()) / 1000L);
    }
}
