package savageclasses;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Triton implements Listener {

    private SavageClasses SC;
    private static Cooldown dolphin;
    private static int loyaltyLevel;

    public Triton(SavageClasses SC){
        this.SC = SC;
        reload(SC);
    }

    public static void reload(SavageClasses SC){
        dolphin = new Cooldown("triton.dolphin", SC);
        loyaltyLevel = SC.getClassStatsConfig().getInt("triton.loyaltyLevel");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        if(SC.isClass(player, "triton") && SavageUtility.hasTritonGear(player)){
            if(player.getLocation().getBlock().getType() == Material.WATER){

                addBuffs(player);

                if(player.isSwimming()) {
                    Vector velocity = new Vector();
                    velocity = player.getLocation().getDirection().multiply(1.025);
                    player.setVelocity(velocity);
                }
            }
            else{
                removeBuffs(player);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        final Player player = event.getPlayer();
        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(player.getInventory().getItemInMainHand().getType() == Material.TRIDENT && player.getLocation().getBlock().getType() != Material.WATER && !dolphin.isOnCooldown(player)){

                dolphin.addCooldown(player);
                player.setAllowFlight(true);
                player.setFlying(true);
                new BukkitRunnable(){
                    int i = 0;

                    @Override
                    public void run() {
                        i++;
                        if (i > 7){
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(SC, 0, 1);
            }
        }
    }

    private void addBuffs(Player player){
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 9999, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 9999, 0));
        player.setRemainingAir(200);

        if(player.getInventory().getHelmet() != null) {
            ItemStack helmet = player.getInventory().getHelmet();
            ItemMeta meta = helmet.getItemMeta();
            meta.setDisplayName("Triton's Helmet");
            helmet.setItemMeta(meta);
            helmet.addEnchantment(Enchantment.WATER_WORKER, 1);
        }
        if(player.getInventory().getBoots() != null) {
            ItemStack boots = player.getInventory().getBoots();
            ItemMeta meta = boots.getItemMeta();
            meta.setDisplayName("Triton's Boots");
            boots.setItemMeta(meta);
            boots.addEnchantment(Enchantment.DEPTH_STRIDER, 1);
        }

        ItemStack[] inv = player.getInventory().getContents();
        for(ItemStack item : inv){
            if(item != null && item.getType() == Material.TRIDENT){
                item.addEnchantment(Enchantment.LOYALTY, loyaltyLevel);
            }
        }
    }

    private void removeBuffs(Player player){
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);

        if(player.getInventory().getHelmet() != null) {
            player.getInventory().getHelmet().removeEnchantment(Enchantment.WATER_WORKER);
        }
        if(player.getInventory().getBoots() != null) {
            player.getInventory().getBoots().removeEnchantment(Enchantment.DEPTH_STRIDER);
        }
    }

}
