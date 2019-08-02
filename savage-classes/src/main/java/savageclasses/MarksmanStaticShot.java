package savageclasses;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

class MarksmanStaticShot implements Runnable {
    LivingEntity target;
    LivingEntity shooter;
    int x;
    int taskID;

    public MarksmanStaticShot(SavageClasses SC, LivingEntity target, Player shooter) {
        this.target = target;
        this.shooter = shooter;
        this.x = 0;
        this.taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(SC, this, 20L);
    }

    public void run() {
        int entities = 0;
        Iterator var2 = SavageUtility.getLivingEntitiesWithin(this.target.getLocation().getBlock(), 10).iterator();

        while (var2.hasNext()) {
            LivingEntity l = (LivingEntity) var2.next();
            if (l != this.shooter) {
                if (l instanceof Player && !SavageUtility.isPVPAllowed(l)) {
                    return;
                }

                l.getWorld().strikeLightningEffect(l.getLocation());
                if (!(l instanceof Player)) {
                    l.damage(6.0D, this.target);
                } else {
                    l.damage(4.0D);
                }
            }
        }

    }
}

