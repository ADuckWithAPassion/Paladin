package apallo.savage.savageclasses;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;

import org.bukkit.entity.Firework;

import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.gmail.nossr50.api.PartyAPI;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PistonExtendsChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Shaman
implements Listener
{
	private static Cooldown callOfTheWildCD;
	private static Cooldown curseCD;
	private static Cooldown healthTotemCD;

	private static int callOfTheWildDur;
	private static int callOfTheWildRange;
	private static int effectStrengthAmp;
	private static int effectSpeedAmp;

	private static int curseDur;
	private static int curseAmp;
	private static int curseRange;
	private static int curseAOE;
	private static int curseDamage;

	private static int healthTotemPulses;
	private static int healthTotemPotency;
	private static int healthTotemDelay;
	private static int healthTotemRange;	
	private static int healthTotemMaxHealth;	
	private static int healthTotemDespawnDistance;	

	private static Cooldown rotateCD;

	private String[] spells;
	HashMap<String, Integer> selectedSpell = new HashMap<String, Integer>();
	static HashMap<Block, TotemHeal> totems = new HashMap<Block, TotemHeal>();
	Random rand = new Random();

	private SavageClasses SC;

	Shaman(SavageClasses SC){
		this.SC = SC;

		reload(SC);
		spells = new String[] { "Call Of The Wild", "Curse", "Healing Totem" };

	}
	public static void reload(SavageClasses SC) {
		callOfTheWildCD = new Cooldown("shaman.callOfTheWildCD", SC);
		curseCD = new Cooldown("shaman.curseCD", SC);
		healthTotemCD = new Cooldown("shaman.healthTotemCD", SC);

		callOfTheWildDur = SC.getClassStatsConfig().getInt("shaman.callOfTheWildDur")*20;
		callOfTheWildRange = SC.getClassStatsConfig().getInt("shaman.callOfTheWildRange");
		effectStrengthAmp= SC.getClassStatsConfig().getInt("shaman.effectStrengthAmp")-1;
		effectSpeedAmp = SC.getClassStatsConfig().getInt("shaman.effectSpeedAmp")-1;

		curseDur = SC.getClassStatsConfig().getInt("shaman.curseDur")*20;
		curseAmp = SC.getClassStatsConfig().getInt("shaman.curseAmp")-1;
		curseRange = SC.getClassStatsConfig().getInt("shaman.curseRange");
		curseAOE = SC.getClassStatsConfig().getInt("shaman.curseAOE");
		curseDamage = SC.getClassStatsConfig().getInt("shaman.curseDamage");

		healthTotemPulses = SC.getClassStatsConfig().getInt("shaman.healthTotemPulses");
		healthTotemDelay = SC.getClassStatsConfig().getInt("shaman.healthTotemDelay")*20;
		healthTotemPotency = SC.getClassStatsConfig().getInt("shaman.healthTotemPotency");
		healthTotemRange = SC.getClassStatsConfig().getInt("shaman.healthTotemRange");
		healthTotemDespawnDistance= SC.getClassStatsConfig().getInt("shaman.healthTotemDespawnDistance");

		rotateCD = new Cooldown("shaman.rotateCD",SC);
	}
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent evt){
		Player p = evt.getPlayer();
		if (SC.isClass(p, "shaman")){
			Material mainHand = p.getInventory().getItemInHand().getType();
			Action action = evt.getAction();
			if (mainHand == Material.BONE){
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
					if(!SavageUtility.hasLeatherGear(p)){
						SavageUtility.displayMessage(ChatColor.RED + "Shamans must be wearing leather armor to use abilities.", p);
						return;
					}
					int spell = selectedSpell.get(p.getName());
					if(spell == 0) { // Call Of The Wild
						if(!(callOfTheWildCD.isOnCooldown(p))) {
							callOfTheWild(p);	
						}
						else {
							SavageUtility.displayMessage("Call Of The Wild is on cooldown for "+callOfTheWildCD.getRemainingTime(p)+" seconds.", p);
						}
					}
					else if(spell == 1) { // Curse
						if(!(curseCD.isOnCooldown(p))) {
							curse(p);	
						}
						else {
							SavageUtility.displayMessage("Curse is on cooldown for "+curseCD.getRemainingTime(p)+" seconds.", p);
						}
					}
					else if(spell == 2) { // Healing Totem
						if(!(healthTotemCD.isOnCooldown(p))) {
							healthTotem(p);	
						}
						else {
							SavageUtility.displayMessage("Health Totem is on cooldown for "+healthTotemCD.getRemainingTime(p)+" seconds.", p);
						}
					}
				}
				else if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK && !(rotateCD.isOnCooldown(p))) {
					rotateCD.addCooldown(p);
					if (selectedSpell.containsKey(p.getName())) { // Replaced unnecessary if statements with modulo
						int ss = (selectedSpell.get(p.getName()));
						selectedSpell.put(p.getName(),(ss + 1) % spells.length); // (0,1,2,0,1,2, ... )
						SavageUtility.displayMessage("You are now using &c" + spells[(ss + 1) % spells.length] + " &7spell", p);
					}
					else { // If cycling through spells for the first time
						selectedSpell.put(p.getName(), 0);
						SavageUtility.displayMessage("You are now using &cCall Of The Wild &7Spell.", p);
					}
				}
			}
		}
	}

	private void callOfTheWild(Player player) {
		for(Player party: SavageUtility.getPartyMembers(player)) {
			if(player.getLocation().distance(party.getLocation()) <= callOfTheWildRange) {
				party.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, callOfTheWildDur, effectStrengthAmp));
				party.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, callOfTheWildDur, effectSpeedAmp));				
				SavageUtility.displayMessage(ChatColor.AQUA+"You are buffed by "+ChatColor.GOLD+player.getName()+"'s "+ChatColor.AQUA+" Call Of The Wild!", party);
			}
		}
		callOfTheWildCD.addCooldown(player);
		SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName() + ChatColor.AQUA+" used Call Of The Wild.", player);
	}
	private void curse(Player player) {
		Player target = (Player) SavageUtility.getTarget(player,curseRange, false);
		if(target == null) {
			SavageUtility.displayMessage(ChatColor.RED + "You must have a player target not in your party.",player);			
			return;
		}

		Location l = target.getLocation();
		int x = l.getBlockX() - curseAOE;
		int y = l.getBlockY() - curseAOE;
		int z = l.getBlockZ() - curseAOE;
		for (int i = x; i < x + 2*curseAOE; i++){
			for (int j = y; j < y + 2*curseAOE; j++){
				for (int k = z; k < z + 2*curseAOE; k++){
					Location newL = new Location(l.getWorld(), i, j, k);
					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.CRIT_MAGIC,true, (float) x, (float) (y+2), (float) z, 0, 0, 0, 0, 1);
		            for(Player online : Bukkit.getOnlinePlayers()) {						
		                ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
		            }
				}
			}
		}
		for(Entity nearby: SavageUtility.getPlayersWithin(target.getLocation(), curseAOE)) {
			if(!(SavageUtility.getPartyMembers(player).contains((Player)nearby)) || player==(Player)nearby) {
				Player affected = (Player)nearby;
				if(affected==player) {
					SavageUtility.displayMessage(ChatColor.BLUE + "Cursed yourself!",player);
				}
				else {
					SavageUtility.displayMessage(ChatColor.RED + "You have been " + ChatColor.GOLD + "Cursed " + ChatColor.RED + "by " + ChatColor.GOLD + player.getName() + ".",affected);
				}
				affected.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, curseDur, curseAmp));
				affected.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, curseDur, curseAmp));
				affected.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, curseDur, curseAmp));
				affected.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, curseDur, curseAmp));
				affected.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, curseDur, curseAmp));
				affected.setHealth(Math.max(1,affected.getHealth()-curseDamage));
				affected.playSound(affected.getLocation(), Sound.LAVA, 25, 25);
			}
		}
		curseCD.addCooldown(player);
		SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName() + ChatColor.AQUA+" used Curse.", player);
	}
	private void healthTotem(Player player) {
		if(!(player.getLocation().getBlock().getType().equals(Material.AIR))) {
			SavageUtility.displayMessage(ChatColor.RED + "Not enough space for health totem.",player);			
			return;
		}
		Block totem = player.getLocation().getBlock();
		totem.setType(Material.SOUL_SAND);
		totems.put(totem,new TotemHeal(totem,player,healthTotemPulses,SC));

		healthTotemCD.addCooldown(player);
		SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName() + ChatColor.AQUA+" used Health Totem. It can be destroyed to stop its healing.", player);
	}
	@EventHandler 
	public void pistonPush(BlockPistonExtendEvent event){
		for(Block block: event.getBlocks()) {
			if(block.getType().equals(Material.SOUL_SAND)) {
				if(totems.containsKey(block)) {
					totems.get(block).destroy("Your totem is destroyed by a piston.");
					event.setCancelled(true);
				}
			}
		}
	}
	@EventHandler 
	public void pistonPull(BlockPistonRetractEvent event){
		for(Block block: event.getBlocks()) {
			if(block.getType().equals(Material.SOUL_SAND)) {
				if(totems.containsKey(block)) {
					totems.get(block).destroy("Your totem is destroyed by a piston.");
					event.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	public void breakingBlock(BlockBreakEvent event) {
		if(event.getBlock().getType().equals(Material.SOUL_SAND) && totems.containsKey(event.getBlock())) {
			Block totem = event.getBlock();
			if(SavageUtility.getPartyMembers(totems.get(totem).player).contains(event.getPlayer())) {
				event.setCancelled(true);
				SavageUtility.displayMessage(ChatColor.RED+"You cannot destroy a friendly Healthing Totem.", event.getPlayer());
				return;
			}
			for(Player nearby: SavageUtility.getPlayersWithin(totem.getLocation(), healthTotemRange)) {
				SavageUtility.displayMessage(ChatColor.GOLD + totems.get(totem).player.getName()+"'s"+ ChatColor.RED + " Healing Totem has been destroyed.",nearby);		
			}
		    totems.get(totem).destroy(null);
		}
	}
	@EventHandler
	public void onBlockExplosion(BlockExplodeEvent event) {
		for(Block block: event.blockList()) {
			if(block.getType().equals(Material.SOUL_SAND) && totems.containsKey(block)) {
				for(Player nearby: SavageUtility.getPlayersWithin(block.getLocation(), healthTotemRange)) {
					SavageUtility.displayMessage(ChatColor.GOLD + totems.get(block).player.getName()+"'s"+ ChatColor.RED + " Healing Totem has been destroyed.",nearby);		
				}
			    totems.get(block).destroy(null);
			}	
		}
	}
	public static void disable() {
		for(TotemHeal totem: totems.values()) {
			totem.destroy(ChatColor.DARK_RED+"Server is clearing all totems.");
		}
	}
	
	public class TotemHeal implements Runnable {
		private Block totem;
		private Player player;
		private int ticks;
		private int i=0;
		private int taskID;

		public TotemHeal(Block totem, Player player, int ticks, SavageClasses SC){
			this.totem = totem;
			this.player = player;
			this.ticks = ticks;
			this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SC, this, 0, healthTotemDelay);

		}

		public void destroy(String message) {
			if(message != null) {
				for(Player party: SavageUtility.getPartyMembers(player)) {
					SavageUtility.displayMessage(message,party);
				}	
			}
			totems.remove(totem);
			totem.setType(Material.AIR);
			Bukkit.getServer().getScheduler().cancelTask(taskID);
		}
		
		@Override
		public void run() {
			i++;
			if(i>=ticks && totem.getType().equals(Material.SOUL_SAND) && totems.containsKey(totem)) {
				for(Player nearby: SavageUtility.getPlayersWithin(totem.getLocation(), healthTotemRange)) {
					if(nearby.getLocation().distance(totem.getLocation()) <= healthTotemRange || SavageUtility.getPartyMembers(nearby).contains(player)) {
						SavageUtility.displayMessage(ChatColor.GOLD + totems.get(totem).player.getName()+"'s"+ ChatColor.RED + " Healing Totem has expired.",nearby);
						destroy(null);
					}
				}
			}
			else if(totem.getLocation().distance(player.getLocation())>=healthTotemDespawnDistance || totem.getWorld() != player.getWorld() || player.isDead()) {
				destroy(ChatColor.GOLD+ player.getName()+ChatColor.AQUA+" has moved too far from their totem. It has been destroyed.");
			}
			else if(!(totem.getType().equals(Material.SOUL_SAND))) {
				destroy(ChatColor.GOLD+ player.getName()+"'s "+ChatColor.AQUA+"totem has been destroyed.");				
			}
			else {
				Firework firework = player.getWorld().spawn(totem.getLocation().add(0.5, 1, 0.5), Firework.class);
				FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();
				data.addEffects(FireworkEffect.builder().withColor(Color.GRAY).with(Type.CREEPER).build());
				data.setPower(1);
				firework.setFireworkMeta(data);

				for(Player nearby: totem.getWorld().getEntitiesByClass(Player.class)) {
					if(nearby.getLocation().distance(totem.getLocation()) <= healthTotemRange) {
						//SavageUtility.displayMessage(ChatColor.AQUA+player.getName()+"'s "+ChatColor.GOLD+"health totem"+ChatColor.AQUA+" heals nearby allies.", nearby);
						if(SavageUtility.getPartyMembers(player).contains(nearby)) {
							double health = nearby.getHealth();
							health += healthTotemPotency;
							double maxHealth = nearby.getMaxHealth();
							nearby.setHealth(Math.min(maxHealth, health)); // no overflow
							SavageUtility.displayMessage(ChatColor.GOLD + player.getName()+"'s " + ChatColor.AQUA + "Healing Totem" + ChatColor.AQUA + " renews you.",nearby);
							nearby.playSound(nearby.getLocation(), Sound.WATER, 25, 25);
							PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.HEART,true, (float) (nearby.getLocation().getX()), (float) (nearby.getLocation().getY()+2), (float) (nearby.getLocation().getZ()), 0, 0, 0, 0, 1);
			                for(Player online : Bukkit.getOnlinePlayers()) {						
			                    ((CraftPlayer)online).getHandle().playerConnection.sendPacket(packet);	
			                }
						}
					}
					else if(SavageUtility.getPartyMembers(player).contains(nearby)) {
						SavageUtility.displayMessage(ChatColor.AQUA+"You are too far from "+ChatColor.GOLD+player.getName()+"'s "+ChatColor.AQUA+"health totem to recieve healing.", nearby);
					}
				}
			}
		}
	}
}
