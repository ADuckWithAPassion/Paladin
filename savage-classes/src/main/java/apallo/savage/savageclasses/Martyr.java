package apallo.savage.savageclasses;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_8_R3.EntityPlayer;

public class Martyr implements Listener {

	private static Cooldown switchCD;

	private static Cooldown sacrificeCD;
	private static int sacrificeRange;
	private static int sacrificeDuration;
	private static double sacrificeHealthPercentage;
	private static double sacrificeShieldPercentage;
	private static HashMap<String, Shield> sacrificeAffected;
	private static Cooldown regenerationCD;
	private static int regenerationDur;
	private static int regenerationAmp;

	private static Cooldown flagellationCD;
	private static int flagellationDur;
	private static int flagellationAmp;
	private static int flagellationDamage;


	SavageClasses SC;
	private static HashMap<String,String> deathMessage;

	public Martyr(SavageClasses SC) {
		this.SC = SC;
		reload(SC);
	}

	public static void reload(SavageClasses SC){
		// Switch Now is a spell that can be cast at an enemy player not in your party by hitting them with a golden axe
		// The target player and the caster get their health switched around
		switchCD = new Cooldown("martyr.switchCD",SC); // cooldown 

		// Sacrifice is a spell that can be casted upon nearby party members. The Martyr loses 30% of their current health.
		// In exchange, party members get a shield worth 225% of the health lost
		sacrificeCD = new Cooldown("martyr.sacrificeCD",SC);  // cooldown
		sacrificeRange = SC.getClassStatsConfig().getInt("martyr.sacrificeRange"); // range
		sacrificeDuration = SC.getClassStatsConfig().getInt("martyr.sacrificeDuration"); // duration 
		sacrificeHealthPercentage = SC.getClassStatsConfig().getDouble("martyr.sacrificeHealthPercentage"); // percentage of health to be taken from caster
		sacrificeShieldPercentage = SC.getClassStatsConfig().getDouble("martyr.sacrificeShieldPercentage"); // percentage of taken health to be converted to a shield
		sacrificeAffected = new HashMap<String, Shield>();

		// Regeneration is a spell that can be cast by right clicking with an axe.
		// The caster is given a powerful regeneration effect for a few seconds.
		regenerationCD = new Cooldown("martyr.regenerationCD",SC); // cooldown 
		regenerationDur = SC.getClassStatsConfig().getInt("martyr.regenerationDur"); // duration of regen effect
		regenerationAmp = SC.getClassStatsConfig().getInt("martyr.regenerationAmp"); // amp of regen effect

		// flagellation is a spell that can be cast by attacking a living entity with a golden sword
		// it hurts the player for 1 heart and applies poison 2 on the target
		flagellationCD = new Cooldown("martyr.flagellationCD",SC); // cooldown 
		flagellationDur = SC.getClassStatsConfig().getInt("martyr.flagellationDur"); // duration of poison effect
		flagellationAmp = SC.getClassStatsConfig().getInt("martyr.flagellationAmp"); // amp of poison effect
		flagellationDamage = SC.getClassStatsConfig().getInt("martyr.flagellationDamage"); // how much to damage the caster

		// Passive to deal more damage as your health gets lower
		deathMessage = new HashMap<String,String>(); // Custom death messages
	}

	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (SC.isClass(player, "martyr")) {
			ItemStack itemHeld = player.getInventory().getItemInHand();
			if(itemHeld==null) {
				return;
			}
			if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(itemHeld.getType().equals(Material.GOLD_AXE)) {
					if(!(SavageUtility.hasMartyrGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You require a Leather Helmet, Golden Chestplate, Golden Leggings, and Leather Boots to cast spells.", player);
						return;
					}
					if(SavageUtility.isPVPAllowed(player)) {
						if(switchCD.isOnCooldown(player) && switchCD.getRemainingTime(player) != switchCD.getStunTime()) {
							SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Switch Life again for another "+switchCD.getRemainingTime(player)+" seconds.", player);
						}
					}
				}
				else if(itemHeld.getType().equals(Material.GOLD_NUGGET)) {
					if(!(SavageUtility.hasMartyrGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You require a Leather Helmet, Golden Chestplate, Golden Leggings, and Leather Boots to cast spells.", player);
						return;
					}
					if(!(sacrificeCD.isOnCooldown(player))) {
						sacrifice(player);
						sacrificeCD.addCooldown(player);
					}							
					else {
						SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Sacrifice again for another "+sacrificeCD.getRemainingTime(player)+" seconds.", player);
					}
				}
				else if(itemHeld.getType().equals(Material.GOLD_SWORD)) {
					if(!(SavageUtility.hasMartyrGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You require a Leather Helmet, Golden Chestplate, Golden Leggings, and Leather Boots to cast spells.", player);
						return;
					}
					if(flagellationCD.isOnCooldown(player) && flagellationCD.getRemainingTime(player) != flagellationCD.getStunTime()) {
						SavageUtility.displayMessage(ChatColor.RED+"You cannot use Flagellation again for another "+flagellationCD.getRemainingTime(player)+" seconds.", player);						
					}
				}
			}
			else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(itemHeld.getType().equals(Material.GOLD_AXE)) {
					if(!(SavageUtility.hasMartyrGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You require a Leather Helmet, Golden Chestplate, Golden Leggings, and Leather Boots to cast spells.", player);
						return;
					}
					if(!(regenerationCD.isOnCooldown(player))) {
						regeneration(player);	
						regenerationCD.addCooldown(player);
					}							
					else {
						SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Regeneration again for another "+regenerationCD.getRemainingTime(player)+" seconds.", player);
					}
				}
			}
		}
	}


	private void regeneration(Player player) {
		player.removePotionEffect(PotionEffectType.REGENERATION);
		player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenerationDur*20, regenerationAmp-1));
		SavageUtility.displayMessage(ChatColor.GREEN+"You use regeneration to heal your wounds.", player);
	}

	private void switchHealth(Player player, Player target) {
		double playerHealth = player.getHealth();
		double targetHealth = target.getHealth();
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,100,1));
		Log.info(player.getMaxHealth());
		Log.info(targetHealth);

		player.setHealth(Math.max(0,Math.min(player.getMaxHealth(), targetHealth)));
		target.setHealth(Math.max(0,Math.min(target.getMaxHealth(), playerHealth)));
		SavageUtility.displayMessage(ChatColor.RED +player.getName()+ ChatColor.GOLD + " used 'Switch Now!' to change life totals with you!", target);
		SavageUtility.displayMessage(ChatColor.GOLD + "You used 'Switch Now!' and changed life totals with " + ChatColor.RED + target.getName(),player);
	}

	private void sacrifice(Player player) {
		float health = (float) (player.getHealth()*sacrificeHealthPercentage);
		Log.info(player.getHealth());
		Log.info(sacrificeHealthPercentage);
		Log.info(health);
		SavageUtility.broadcastMessage(ChatColor.RED+player.getName()+ChatColor.GREEN+" Tries to Sacrifice their health to create a shield for their allies.", player);
		if(health<=1) {
			SavageUtility.broadcastMessage(ChatColor.GREEN+"The Gods are not happy with "+ChatColor.RED+player.getName()+"'s "+ChatColor.GREEN+"Sacrifice.", player);
			deathMessage.put(player.getName(), "The Gods crush "+player.getName());
			player.setHealth(0);
		}
		else {
			player.setHealth(health);
			for(Player member: SavageUtility.getPartyMembers(player)) {
				if(member != player) {
					if(member.getWorld().equals(player.getWorld())) {
						if(member.getLocation().distance(player.getLocation())<=sacrificeRange) {
							EntityPlayer craftMember = ((CraftPlayer)member).getHandle();
							if(craftMember.getAbsorptionHearts()<=0 && !(sacrificeAffected.containsKey(member.getName()))) {
								new Shield(player, member, health);
							}
							else {
								SavageUtility.displayMessage(ChatColor.RED+player.getName()+ChatColor.GREEN+" Tried to Sacrifice their health to give you a shield, but it failed...", member);
							}	
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"You are too far from "+ChatColor.GOLD+player.getName()+ChatColor.RED+" to receive their Sacrifice.", member);
						}	
					}
					else {
						SavageUtility.displayMessage(ChatColor.RED+"You are in another realm to "+ChatColor.GOLD+player.getName()+ChatColor.RED+" so you do not receive their Sacrifice.", member);
					}

				}
			}
		}
	}
	private class Shield implements Runnable{
		Player caster;
		Player member;
		int task;
		public Shield(Player caster, Player member, float hearts) {
			this.caster = caster;
			this.member = member;
			sacrificeAffected.put(member.getName(), this);
			EntityPlayer craftPlayer = ((CraftPlayer)member).getHandle();
			craftPlayer.setAbsorptionHearts(hearts*(float)sacrificeShieldPercentage);
			SavageUtility.displayMessage(ChatColor.RED+caster.getName()+ChatColor.GREEN+" Sacrifices their health to give you a shield.", member);
			task = Bukkit.getScheduler().scheduleSyncDelayedTask(SC, this, sacrificeDuration*20);
		}
		public void cancel() {
			Bukkit.getScheduler().cancelTask(task);
			sacrificeAffected.remove(member.getName());
		}
		@Override
		public void run() {
			EntityPlayer craftPlayer = ((CraftPlayer)member).getHandle();
			if(craftPlayer.getAbsorptionHearts() >= 0) {
				craftPlayer.setAbsorptionHearts(0);
				SavageUtility.displayMessage(ChatColor.RED+caster.getName()+"'s "+ChatColor.GREEN+"Sacrifice wears off...", member);
				sacrificeAffected.remove(member.getName());
			}
		}
	}

	private void flagellation(Player player, Player target) {
		double health = player.getHealth()-flagellationDamage;
		if(health<=0) {
			SavageUtility.broadcastMessage(ChatColor.GREEN+"The Gods are not happy with "+ChatColor.RED+player.getName()+"'s "+ChatColor.GREEN+"Flagellation.", player);
			deathMessage.put(player.getName(), "The Gods crush "+player.getName());
			player.setHealth(0);
		}
		else {
			player.setHealth(health);
			target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, flagellationDur*20, flagellationAmp-1));
			SavageUtility.displayMessage(ChatColor.GREEN+"Your lashes hurt "+ChatColor.RED+target.getName(), player);
			SavageUtility.displayMessage(ChatColor.RED+player.getName()+"'s "+ChatColor.GREEN+"lashes hurt you.", target);
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player damaged = (Player)event.getEntity();		
			Log.info(sacrificeAffected);
			if(sacrificeAffected.containsKey(damaged.getName())) {
				EntityPlayer craftDamaged = ((CraftPlayer)damaged).getHandle();
				if(craftDamaged.getAbsorptionHearts()<=0) {
					craftDamaged.setAbsorptionHearts(0);
					SavageUtility.displayMessage(ChatColor.RED+sacrificeAffected.get(damaged.getName()).caster.getName()+"'s "+ChatColor.GREEN+"Sacrifice is destroyed.", damaged);
					sacrificeAffected.get(damaged.getName()).cancel();
				}
			}
		}
	}

	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player damaged = (Player)event.getEntity();
			Player damager = (Player)event.getDamager();

			if(SC.isClass(damager, "martyr")) {
				ItemStack item = damager.getInventory().getItemInHand();
				if(!(SavageUtility.hasMartyrGear(damager))) {
					return;
				}
				Log.info(damager.getMaxHealth());
				Log.info(damager.getHealth());
				Log.info(damaged.getMaxHealth());
				Log.info(damaged.getHealth());
				
				event.setDamage(event.getDamage() + (damager.getMaxHealth() - damager.getHealth())/5);
				if(item == null) {
					return;
				}
				if(item.getType().equals(Material.GOLD_AXE)){
					if(switchCD.isOnCooldown(damager)) {
						return;
					}
					else {
						if(SavageUtility.isPVPAllowed(damaged) && SavageUtility.isPVPAllowed(damager)) {
							if(SavageUtility.getPartyMembers(damager).contains(damaged)) {
								SavageUtility.displayMessage(ChatColor.RED+"You cannot Switch Life with party members.", damager);
							}
							else {
								switchHealth(damager, damaged);
								switchCD.addCooldown(damager);
								event.setDamage(0);
							}
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", damager);
						}
					}	
				}
				if(item.getType().equals(Material.GOLD_SWORD)){
					if(flagellationCD.isOnCooldown(damager)) {
						return;
					}
					else {
						if(SavageUtility.isPVPAllowed(damaged) && SavageUtility.isPVPAllowed(damager)) {
							if(SavageUtility.getPartyMembers(damager).contains(damaged)) {
								SavageUtility.displayMessage(ChatColor.RED+"You cannot use Flagellation on party members.", damager);
							}
							else {
								flagellation(damager, damaged);
								flagellationCD.addCooldown(damager);
							}
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", damager);
						}
					}	
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(deathMessage.containsKey(event.getEntity().getName())) {
			event.setDeathMessage(deathMessage.get(event.getEntity().getName()));
			deathMessage.remove(event.getEntity().getName());
		}
	}
}