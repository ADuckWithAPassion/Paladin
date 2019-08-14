package apallo.savage.savageclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class ShadowKnight implements Listener {
	static Cooldown siphonCD;
	public static int siphonRange;
	public static int siphonAOE;
	public static int siphonAmpInc;
	public static int siphonDurInc;
	public static int siphonDamage;
	public static int siphonHealMultiplier;
	
	static Cooldown cocoonCD;
	public static int cocoonRange;
	public static int cocoonDamage;
	public static int cocoonTicks;
	public static long cocoonDelay;
	public static double cocoonHealMultiplier;
	public static HashMap<String, Drain> cocoonAffected;

	static Cooldown shadowGraspCD;
	public static int shadowGraspRange;
	public static long shadowGraspSeconds;
	public static HashMap<String, Grasp> shadowGraspCaster;
	public static HashMap<String, Grasp> shadowGraspAffected;
	
	static Cooldown gatherDarknessCD;
	public static int gatherDarknessRange;
	public static int gatherDarknessAOE;
	public static int gatherDarknessSeconds;
	public static HashMap<String, Darkness> gatherDarknessAffected;
	
	SavageClasses SC;
	Random random = new Random();

	public ShadowKnight(SavageClasses SC) {
		this.SC = SC;
		reload(SC);
	}

	public static void reload(SavageClasses SC){
		// Siphon is a spell that can be cast at an enemy player not in your party by punching with a bone
		// The target player, and anyone nearby, gets a random buff 'siphoned' from them
		// This buff is then granted to the Caster with increased potency and duration
		// This also hurts the target and heals the Caster.
		siphonCD = new Cooldown("shadowknight.siphonCD",SC); // cooldown 
		siphonRange = SC.getClassStatsConfig().getInt("shadowknight.siphonRange"); // range
		siphonAOE = SC.getClassStatsConfig().getInt("shadowknight.siphonAOE"); // get any nearby enemies 
		siphonAmpInc = SC.getClassStatsConfig().getInt("shadowknight.siphonAmpInc"); // amplitude increase on siphon
		siphonDurInc = SC.getClassStatsConfig().getInt("shadowknight.siphonDurInc"); // duration increase on siphon 
		siphonDamage = SC.getClassStatsConfig().getInt("shadowknight.siphonDamage"); // damage on siphon
		siphonHealMultiplier = SC.getClassStatsConfig().getInt("shadowknight.siphonHealMultiplier"); //  multiplier of the damage dealt should be converted to health
		
		// Cocoon is a spell that can be channeled by the caster upon blocking with a sword and shifting (used to be just block on old SR, but new MC removes blocking with sword)
		// It will be maintained as long as the caster is shifting or until it expires (or death ect)
		// Every few seconds, all nearby enemies take a bit of damage
		// The Caster is healed the aggregate damage dealt (times a multipler) on every pulse
		cocoonCD = new Cooldown("shadowknight.cocoonCD",SC);  // cooldown
		cocoonRange = SC.getClassStatsConfig().getInt("shadowknight.cocoonRange"); // range
		cocoonDamage = SC.getClassStatsConfig().getInt("shadowknight.cocoonDamage"); // damage per pulse
		cocoonTicks = SC.getClassStatsConfig().getInt("shadowknight.cocoonTicks"); // number of pulses
		cocoonDelay = SC.getClassStatsConfig().getInt("shadowknight.cocoonDelay"); // delay between pulses
		cocoonHealMultiplier = SC.getClassStatsConfig().getInt("shadowknight.cocoonHealMultiplier"); // multiplier of the damage dealt should be converted to health
		cocoonAffected = new HashMap<String, Drain>(); // list of players affected by Gather Darkness

		// Shadow Grasp is a spell that can be cast at an enemy player not in your party by right clicking with a bone
		// The target can then run freely until Shadow Graph detonates
		// The target is then teleported back to where they initially were 
		shadowGraspCD = new Cooldown("shadowknight.shadowGraspCD",SC); // cooldown 
		shadowGraspRange = SC.getClassStatsConfig().getInt("shadowknight.shadowGraspRange"); // range
		shadowGraspSeconds = SC.getClassStatsConfig().getInt("shadowknight.shadowGraspSeconds"); // amount of time until Shadow Grasp detonates
		shadowGraspAffected = new HashMap<String,Grasp>(); // list of affected players (to prevent multiple grasps on one player at a time)
		shadowGraspCaster = new HashMap<String,Grasp>(); // list of casters (to prevent multiple grasps on one player at a time)
		
		// Gather Darkness is a spell that can be cast on nearby party members upon right clicking with a ghast tear
		// All party members are given the invisiblity potion effect
		// After some amount of time, Gather Darkness detonates for every party member; any nearby enemies are given blindness (might require buff)
		gatherDarknessCD = new Cooldown("shadowknight.gatherDarknessCD",SC); // cooldown
		gatherDarknessRange = SC.getClassStatsConfig().getInt("shadowknight.gatherDarknessRange"); // range of party members
		gatherDarknessAOE = SC.getClassStatsConfig().getInt("shadowknight.gatherDarknessAOE"); // denonate range
		gatherDarknessSeconds = SC.getClassStatsConfig().getInt("shadowknight.gatherDarknessSeconds"); // delay in seconds until detonation 
		gatherDarknessAffected = new HashMap<String, Darkness>(); // list of players affected by Gather Darkness
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (SC.isClass(player, "shadowknight")) {
			ItemStack itemHeld = player.getInventory().getItemInHand();
			if(itemHeld == null) {
				return;
			}
			if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
					if(itemHeld.getType().equals(Material.BONE)) {
						if(!(SavageUtility.hasShadowKnightGear(player))) {
							SavageUtility.displayMessage(ChatColor.RED+"You require an Iron Chestplate and Iron Leggings to cast spells.", player);
							return;
						}
						if(SavageUtility.isPVPAllowed(player)) {
							if(!(siphonCD.isOnCooldown(player))) {
								siphon(player);	
							}							
							else {
								SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Siphon again for another "+siphonCD.getRemainingTime(player)+" seconds.", player);
							}
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", player);
						}
					}
				}
				else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if(itemHeld.getType().equals(Material.BONE)) {
						if(!(SavageUtility.hasShadowKnightGear(player))) {
							SavageUtility.displayMessage(ChatColor.RED+"You require an Iron Chestplate and Iron Leggings to cast spells.", player);
							return;
						}
						if(SavageUtility.isPVPAllowed(player)) {
							if(!(shadowGraspCD.isOnCooldown(player))) {
								shadowGrasp(player);	
							}							
							else {
								SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Shadow Grasp again for another "+shadowGraspCD.getRemainingTime(player)+" seconds.", player);
							}
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", player);
						}
					}
					if(Arrays.asList("DIAMOND_SWORD","IRON_SWORD","GOLDEN_SWORD","STONE_SWORD","WOODEN_SWORD").contains(itemHeld.getType().toString()) && player.isSneaking()) {
						if(!(SavageUtility.hasShadowKnightGear(player))) {
							SavageUtility.displayMessage(ChatColor.RED+"You require an Iron Chestplate and Iron Leggings to cast spells.", player);
							return;
						}
						if(SavageUtility.isPVPAllowed(player)) {
							if(!(cocoonCD.isOnCooldown(player))) {
								if(!(cocoonAffected.containsKey(player.getName()))) {
									cocoon(player);										
								}
							}							
							else {
								SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Cocoon again for another "+cocoonCD.getRemainingTime(player)+" seconds.", player);
							}
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", player);
						}
					}
					if(itemHeld.getType().equals(Material.GHAST_TEAR)) {
						if(!(SavageUtility.hasShadowKnightGear(player))) {
							SavageUtility.displayMessage(ChatColor.RED+"You require an Iron Chestplate and Iron Leggings to cast spells.", player);
							return;
						}
						if(SavageUtility.isPVPAllowed(player)) {
							if(!(gatherDarknessCD.isOnCooldown(player))) {
								gatherDarkness(player);	
							}							
							else {
								SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Gather Darkness again for another "+gatherDarknessCD.getRemainingTime(player)+" seconds.", player);
							}
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", player);
						}
					}
				}
			}
		}

	private void gatherDarkness(Player player) {
		List<Player> partyMembers = SavageUtility.getPartyMembers(player);
		for(Player member: partyMembers) {
			if(member.getWorld().equals(player.getWorld())) {
				if(member.getLocation().distance(player.getLocation())<= gatherDarknessRange) {
					if(gatherDarknessAffected.containsKey(member.getName())) {
						SavageUtility.displayMessage(ChatColor.GOLD+player.getName()+"'s "+ChatColor.RED+"Gather Darkness doesn't affect you...", member);
					}
					else {
						new Darkness(player, member);
						gatherDarknessCD.addCooldown(player);
					}
				}
				else {
					SavageUtility.displayMessage(ChatColor.RED+"You are too far from "+ChatColor.GOLD+player.getName()+ChatColor.RED+" to receive Gather Darkness.", member);
				}
			}
			else {
				SavageUtility.displayMessage(ChatColor.RED+"You are in another realm from "+ChatColor.GOLD+player.getName()+ChatColor.RED+" so you don't receive Gather Darkness.", member);
			}
		}
	}
	private class Darkness implements Runnable{
		Player caster;
		Player target;
		int task;
		public Darkness(Player caster, Player target) {
			this.caster = caster;
			this.target = target;
			gatherDarknessAffected.put(target.getName(), this);
			target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, gatherDarknessSeconds*20+20, 1));
			SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+ChatColor.DARK_PURPLE+" shadows gather around you...", target);
			task = Bukkit.getScheduler().scheduleSyncDelayedTask(SC, this, gatherDarknessSeconds*20);
		}
		public void cancel() {
			Bukkit.getScheduler().cancelTask(task);
			gatherDarknessAffected.remove(target.getName());
		}
		@Override
		public void run() {
			SavageUtility.broadcastMessage(ChatColor.GOLD+target.getName()+"'s "+ChatColor.DARK_PURPLE+"Darkness erupts, blinding nearby enemies.", target);
			List<Player> partyMembers = SavageUtility.getPartyMembers(target);
			for(Player nearby: SavageUtility.getPlayersWithin(target, gatherDarknessAOE)) {
				if(!(partyMembers.contains(nearby)) && SavageUtility.isPVPAllowed(nearby)) {
					SavageUtility.displayMessage(ChatColor.DARK_GRAY+target.getName()+"'s Darkness blinds you.", nearby);
					nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, gatherDarknessSeconds*20, 1));
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_NORMAL,true, (float) (nearby.getLocation().getX()), (float) (nearby.getLocation().getY()+2), (float) (nearby.getLocation().getZ()), 0, 0, 0, 0, 1);
	                for(Player online : Bukkit.getOnlinePlayers()) {						
	                    ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
	                }
				}
			}
			gatherDarknessAffected.remove(target.getName());
		}
		public void affectedDead() {
			SavageUtility.broadcastMessage(ChatColor.GOLD+target.getName()+"'s "+ChatColor.DARK_PURPLE+"Darkness erupts upon their death, harming nearby enemies.", target);
			List<Player> partyMembers = SavageUtility.getPartyMembers(target);
			for(Player nearby: SavageUtility.getPlayersWithin(target, gatherDarknessAOE)) {
				if(!(partyMembers.contains(nearby)) && SavageUtility.isPVPAllowed(nearby)) {
					SavageUtility.displayMessage(ChatColor.DARK_GRAY+target.getName()+"'s Darkness harms you.", nearby);
					nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, gatherDarknessSeconds*20, 1));
					nearby.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, gatherDarknessSeconds*20, 0));
					nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, gatherDarknessSeconds*20, 3));
					nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, gatherDarknessSeconds*20, 3));
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_NORMAL,true, (float) (nearby.getLocation().getX()), (float) (nearby.getLocation().getY()+2), (float) (nearby.getLocation().getZ()), 0, 0, 0, 0, 1);
	                for(Player online : Bukkit.getOnlinePlayers()) {						
	                    ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
	                }				
				}
			}
			cancel();
		}
	}

	private void cocoon(Player player) {
		new Drain(player);
	}
	private class Drain implements Runnable{
		Player caster;
		int task;
		int counter = 0;
		public Drain(Player caster) {
			this.caster = caster;
			SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+ChatColor.DARK_PURPLE+" Begins to create a Cocoon", caster);
			cocoonAffected.put(caster.getName(), this);
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(SC, this, 0, cocoonDelay*20);
		}
		public void cancel() {
			Bukkit.getScheduler().cancelTask(task);
			cocoonCD.addCooldown(caster);
			cocoonAffected.remove(caster.getName());
		}
		@Override
		public void run() {
			counter++;
			if(counter < cocoonTicks && !(caster.isDead()) && caster.isSneaking() && cocoonAffected.containsKey(caster.getName())) {
				SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+"'s "+ChatColor.DARK_PURPLE+" Cocoon pulses, damaging nearby enemies and healing themself.", caster);
				caster.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int)cocoonDelay*20, 2));
				caster.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int)cocoonDelay*20, 2));
				caster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)cocoonDelay*20, 2));
				List<Player> partyMembers = SavageUtility.getPartyMembers(caster);
				for(Player nearby: SavageUtility.getPlayersWithin(caster, cocoonRange)) {
					if(!(partyMembers.contains(nearby)) && SavageUtility.isPVPAllowed(nearby)) {
						nearby.setHealth(Math.max(nearby.getHealth()-cocoonDamage, 0));
						caster.setHealth(Math.min(caster.getHealth()+cocoonDamage*cocoonHealMultiplier, caster.getMaxHealth()));
						PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.HEART,true, (float) (caster.getLocation().getX()), (float) (caster.getLocation().getY()+2), (float) (caster.getLocation().getZ()), 0, 0, 0, 0, 1);
		                for(Player online : Bukkit.getOnlinePlayers()) {						
		                    ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
		                }
						nearby.playEffect(EntityEffect.HURT);
						SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+"'s "+ChatColor.RED+"Coccon saps your health.", nearby);
						SavageUtility.displayMessage(ChatColor.GOLD+"Your Cocoon heals you.", caster);
					}
				}
			}
			else {
				SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+"'s "+ChatColor.DARK_PURPLE+"Cocoon ends.", caster);
				caster.getWorld().playSound(caster.getLocation(), Sound.ITEM_BREAK, 10, 10);
				cancel();
			}
		}
		public void affectedDead() {
			SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+"'s "+ChatColor.DARK_PURPLE+"Cocoon ends.", caster);
			caster.getWorld().playSound(caster.getLocation(), Sound.ITEM_BREAK, 10, 10);
			cancel();
		}
	}
	private void shadowGrasp(Player player) {
		Player target = (Player)SavageUtility.getTarget(player, shadowGraspRange, false);
		if(target == null) {
			return;
		}
		if(shadowGraspAffected.containsKey(target.getName())) {
			SavageUtility.displayMessage(ChatColor.RED+"Your target already has a Shadow Grasp applied.", player);
			return;
		}
		if(shadowGraspCaster.containsKey(target.getName())) {
			SavageUtility.displayMessage(ChatColor.RED+"Your shadows already have a target grasped.", player);
			return;
		}
		SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName()+"'s "+ChatColor.DARK_PURPLE+"shadows Grasp "+ChatColor.GOLD+target.getName()+ChatColor.DARK_PURPLE+"!", target);
		new Grasp(player, target);
	}
	
	private class Grasp implements Runnable{
		int task;
		Player target;
		Player caster;
		Location oldLocation;
		public Grasp(Player caster, Player target) {
			this.target = target;
			this.caster = caster;
			shadowGraspAffected.put(target.getName(), this);
			shadowGraspCaster.put(caster.getName(), this);
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0));
			target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
			oldLocation = target.getLocation();
			task = Bukkit.getScheduler().scheduleSyncDelayedTask(SC, this,shadowGraspSeconds*20);
			shadowGraspCD.addCooldown(caster);
		}
		public void casterDead() {
			SavageUtility.displayMessage(ChatColor.DARK_GRAY+"Your shadows release "+target.getName(), caster);
			SavageUtility.displayMessage(ChatColor.DARK_GRAY+caster.getName()+"'s shadows release you", target);
			cancel();
		}
		public void affectedDead() {
			SavageUtility.displayMessage(ChatColor.DARK_GRAY+"Your shadows release "+target.getName(), caster);
			SavageUtility.displayMessage(ChatColor.DARK_GRAY+caster.getName()+"'s shadows release you", target);	
			cancel();
		}
		public void cancel() {
			Bukkit.getScheduler().cancelTask(task);
			shadowGraspAffected.remove(target.getName());
			shadowGraspCaster.remove(caster.getName());
			
		}
		@Override
		public void run() {
			if(target.isDead() || !(target.isOnline()) || !(SavageUtility.isPVPAllowed(target)) || SavageUtility.getPartyMembers(caster).contains(target) || !(shadowGraspAffected.containsKey(target.getName()))) {
				return;
			}
			SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+"'s "+ChatColor.DARK_PURPLE+"Shadowy Grasp collapses in on "+ChatColor.GOLD+target.getName()+ChatColor.DARK_PURPLE+"!", target);
			shadowGraspAffected.remove(target.getName());
			shadowGraspCaster.remove(caster.getName());
			PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME,true, (float) (target.getLocation().getX()), (float) (target.getLocation().getY()+2), (float) (target.getLocation().getZ()), 0, 0, 0, 0, 1);
            for(Player online : Bukkit.getOnlinePlayers()) {						
                ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
            }
			oldLocation.setPitch(target.getLocation().getPitch());
			oldLocation.setYaw(target.getLocation().getYaw());
			target.teleport(oldLocation);
			packet = new PacketPlayOutWorldParticles(EnumParticle.SMOKE_LARGE,true, (float) (oldLocation.getX()), (float) (oldLocation.getY()+2), (float) (oldLocation.getZ()), 0, 0, 0, 0, 1);
            for(Player online : Bukkit.getOnlinePlayers()) {						
                ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
            }			oldLocation.getWorld().playSound(oldLocation, Sound.ENDERMAN_SCREAM, 10, 10);
			SavageUtility.displayMessage(ChatColor.DARK_GRAY+"The shadows pull you back...", target);
			SavageUtility.displayMessage(ChatColor.DARK_GRAY+"Your shadows pull "+target.getName()+" back...", caster);
		}
	}
	
	
	private void siphon(Player player) {
		Player target = (Player)SavageUtility.getTarget(player, siphonRange, false);
		if(target == null) {
			SavageUtility.displayMessage(ChatColor.RED+"You must have a target player not in your party.", player);
			return;
		}
		List<Player> partyMembers = SavageUtility.getPartyMembers(player);
		for(Player nearby: SavageUtility.getPlayersWithin(target, siphonAOE)){
			if(!(partyMembers.contains(nearby)) && SavageUtility.isPVPAllowed(nearby)) {
				if(!(SC.isClass(nearby, "shadowknight"))){
					Collection<PotionEffect> effectList = nearby.getActivePotionEffects();
					List<PotionEffect> effectListClone = new ArrayList<PotionEffect>(effectList);
					for(PotionEffect badEffect: effectListClone) {						
						if(SavageUtility.negativeEffects.contains(badEffect.getType()) || badEffect.getType().getName() == "HEALTH_BOOST") {
							effectList.remove(badEffect);
						}
					}
					if(effectList.size() > 0) {
						nearby.getWorld().playEffect(nearby.getLocation(), Effect.PORTAL, 4);
						int index = random.nextInt(effectList.size());
						PotionEffect effect = (PotionEffect)effectList.toArray()[index];
						PotionEffect newEffect = new PotionEffect(effect.getType(), Math.min(effect.getDuration()+siphonDurInc*20, 20*20), Math.min(effect.getAmplifier()+siphonAmpInc,2));
						player.addPotionEffect(newEffect);
						target.removePotionEffect(effect.getType());
						SavageUtility.displayMessage(ChatColor.DARK_PURPLE+"You siphon "+newEffect.getType().getName().toLowerCase().replaceAll("_", " ")+" "+String.valueOf(Math.min(effect.getAmplifier()+siphonAmpInc,2)+1)+" from "+ChatColor.GOLD+target.getName(), player);
						SavageUtility.displayMessage(ChatColor.GOLD+player.getName()+ChatColor.RED+" Siphons away your "+effect.getType().getName().toLowerCase().replaceAll("_", " ")+" "+String.valueOf(effect.getAmplifier()+1), nearby);
						nearby.setHealth(Math.max(nearby.getHealth()-siphonDamage, 0));
						player.setHealth(Math.min(player.getHealth()+siphonDamage*siphonHealMultiplier, player.getMaxHealth()));
						continue;
					}
					else {
						nearby.getWorld().playEffect(nearby.getLocation(), Effect.PORTAL, 4);
						SavageUtility.displayMessage(ChatColor.DARK_PURPLE+"You siphon no buff from "+ChatColor.GOLD+target.getName(), player);
						SavageUtility.displayMessage(ChatColor.GOLD+player.getName()+ChatColor.RED+" Siphons away no buff.", nearby);
						nearby.setHealth(Math.max(nearby.getHealth()-siphonDamage, 0));
						player.setHealth(Math.min(player.getHealth()+siphonDamage*siphonHealMultiplier, player.getMaxHealth()));
						continue;						
					}
				}
				else {
					SavageUtility.displayMessage(ChatColor.RED+"A Shadow Knight is immune to your Siphon.", player);
					nearby.getWorld().playEffect(nearby.getLocation(), Effect.PORTAL, 4);
				}
				
			}
		}
		siphonCD.addCooldown(player);
	}
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(shadowGraspCaster.containsKey(player.getName())) {
			shadowGraspCaster.get(player.getName()).casterDead();
		}
		if(shadowGraspAffected.containsKey(player.getName())) {
			shadowGraspAffected.get(player.getName()).affectedDead();
		}
		if(cocoonAffected.containsKey(player.getName())) {
			cocoonAffected.get(player.getName()).affectedDead();
		}
		if(gatherDarknessAffected.containsKey(player.getName())) {
			gatherDarknessAffected.get(player.getName()).affectedDead();
		}
	}
}