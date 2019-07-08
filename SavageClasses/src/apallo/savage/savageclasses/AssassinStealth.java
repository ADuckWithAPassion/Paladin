package apallo.savage.savageclasses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

class AssassinStealth implements Runnable {
    Player player;
    int x;
    int taskID;
    Assassin assassin;

    public AssassinStealth(SavageClasses SC, Assassin assassin, Player player) {
        this.assassin = assassin;
        this.player = player;
        this.x = 0;
        this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SC, this, 0L, 20L);
    }

    public void run() {
        ++this.x;
        if (this.x > this.assassin.stealthDur || !this.assassin.isStealthed.contains(this.player.getName())) {
            if (this.player.isOnline()) {
                this.assassin.unStealth(this.player.getName());
            }

            Bukkit.getScheduler().cancelTask(this.taskID);
        }

    }
}
