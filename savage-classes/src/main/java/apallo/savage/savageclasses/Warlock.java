package apallo.savage.savageclasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntitySkeleton;

public class Warlock implements Listener {
	static Cooldown leechCD;
	public static int leechRange;
	public static double leechDamage;
	public static double leechTicks;
	public static int leechDelay;
	public static HashMap<String, Drain> leechAffected;
	public static HashMap<String, Drain> leechCaster;

	static Cooldown sickenCD;
	public static int sickenRange;
	public static int sickenPoisonLevel;
	public static int sickenDur;
	public static double sickenDamage;

	static Cooldown boneShieldCD;
	public static int boneShieldHearts;
	public static double boneShieldDur;
	public static List<String> boneShieldList;

	static Cooldown fadeCD;
	public static int fadeRange;

	static Cooldown imprisonCD;
	public static double imprisonRange;
	public static int imprisonDur;
	public static List<String> imprisonAffected;

	SavageClasses SC;

	public Warlock(SavageClasses SC) {
		this.SC = SC;
		reload(SC);
	}

	public static void reload(SavageClasses SC){
		leechCD = new Cooldown("warlock.leechLifeCD",SC);
		leechRange = SC.getClassStatsConfig().getInt("warlock.leechRange");
		leechDamage = SC.getClassStatsConfig().getInt("warlock.leechDamage");
		leechTicks = SC.getClassStatsConfig().getInt("warlock.leechTicks");
		leechDelay = SC.getClassStatsConfig().getInt("warlock.leechDelay");
		leechAffected = new HashMap<String, Drain>();
		leechCaster= new HashMap<String, Drain>();

		sickenCD = new Cooldown("warlock.sickenCD",SC);
		sickenRange = SC.getClassStatsConfig().getInt("warlock.sickenRange");
		sickenPoisonLevel = SC.getClassStatsConfig().getInt("warlock.sickenPoisonLevel");
		sickenDur = SC.getClassStatsConfig().getInt("warlock.sickenDur");
		sickenDamage = SC.getClassStatsConfig().getInt("warlock.sickenDamage");

		boneShieldCD = new Cooldown("warlock.boneShieldCD",SC);
		boneShieldHearts = SC.getClassStatsConfig().getInt("warlock.boneShieldHearts");
		boneShieldDur = SC.getClassStatsConfig().getInt("warlock.boneShieldDur");
		boneShieldList = new ArrayList<String>();

		fadeCD = new Cooldown("warlock.fadeCD",SC);
		fadeRange = SC.getClassStatsConfig().getInt("warlock.fadeRange");

		imprisonCD = new Cooldown("warlock.imprisonCD",SC);
		imprisonRange = SC.getClassStatsConfig().getInt("warlock.imprisonRange");
		imprisonDur = SC.getClassStatsConfig().getInt("warlock.imprisonDur");
		imprisonAffected = new ArrayList<String>();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (SC.isClass(player, "Warlock")) {
			ItemStack itemHeld = player.getInventory().getItemInHand();
			if(itemHeld == null) {
				return;
			}
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (itemHeld.getType() == Material.SLIME_BALL) {
					if(!(SavageUtility.hasWarlockGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You must be wearing Gold Chest/Legs/Boots to cast spells.", player);
						return;
					}
					if(SavageUtility.isPVPAllowed(player)) {
						if(!(sickenCD.isOnCooldown(player))) {
							sicken(player);	
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Sicken again for another "+sickenCD.getRemainingTime(player)+" seconds.", player);
						}
					}
					else {
						SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", player);
					}
				}
				else if(itemHeld.getType() == Material.CLAY_BALL) {
					if(!(SavageUtility.hasWarlockGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You must be wearing Gold Chest/Legs/Boots to cast spells.", player);
						return;
					}
					if(!(boneShieldCD.isOnCooldown(player))) {
						boneShield(player);							
					}
					else {
						SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Bone Shield again for another "+boneShieldCD.getRemainingTime(player)+" seconds.", player);
					}
				}
				else if(itemHeld.getType() == Material.BOW) {
					SavageUtility.displayMessage(ChatColor.RED+"Warlocks cannot use bows.", player);
					event.setCancelled(true);
				}
			}
			if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (itemHeld.getType() == Material.SLIME_BALL) {
					if(!(SavageUtility.hasWarlockGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You must be wearing Gold Chest/Legs/Boots to cast spells.", player);
						return;
					}
					if(SavageUtility.isPVPAllowed(player)) {
						if(!(leechCD.isOnCooldown(player))) {
							leechLife(player);	
						}							
						else {
							SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Leech Life again for another "+leechCD.getRemainingTime(player)+" seconds.", player);
						}
					}
					else {
						SavageUtility.displayMessage(ChatColor.RED+"PVP is disabled in this area.", player);
					}
				}
				else if(itemHeld.getType() == Material.CLAY_BALL) {
					if(!(SavageUtility.hasWarlockGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You must be wearing Gold Chest/Legs/Boots to cast spells.", player);
						return;
					}
					if(!(fadeCD.isOnCooldown(player))) {
						if(!(player.getLocation().getBlock().getType().isSolid() || player.getLocation().add(0, 1, 0).getBlock().getType().isSolid()) ) {
							fade(player);
						}
						else {
							SavageUtility.displayMessage(ChatColor.RED+"You cannot Fade whilst in a block.", player);
							Log.info(player.getLocation().getBlock().getType());
							Log.info(player.getLocation().add(0,1,0).getBlock().getType());

						}
					}
					else {
						SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Fade again for another "+fadeCD.getRemainingTime(player)+" seconds.", player);
					}
				}
				else if(itemHeld.getType() == Material.EYE_OF_ENDER) {
					if(!(SavageUtility.hasWarlockGear(player))) {
						SavageUtility.displayMessage(ChatColor.RED+"You must be wearing Gold Chest/Legs/Boots to cast spells.", player);
						return;
					}
					if(!(imprisonCD.isOnCooldown(player))) {
						imprison(player);							
					}
					else {
						SavageUtility.displayMessage(ChatColor.RED+"You cannot cast Imprison again for another "+imprisonCD.getRemainingTime(player)+" seconds.", player);
					}
				}
			} 
		}
	}


	private void fade(Player player) {
		BlockIterator iter = new BlockIterator(player, fadeRange);
		Block block = null;
		List<Block> blockList = new ArrayList<Block>();
		blockList.add(player.getLocation().getBlock());

		while(iter.hasNext()) {
			block = iter.next();
			blockList.add(block);
			if((block.getType().isSolid())) {
				Location loc = block.getLocation();
				if(!loc.clone().add(0, 1, 0).getBlock().getType().isSolid() && !loc.clone().add(0, 2, 0).getBlock().getType().isSolid()) {
					List<Integer> offset = Arrays.asList(-1,0,1);
					for(int x:offset) {
						for(int z:offset) {
							if(!loc.clone().add(x, 0, z).getBlock().getType().isSolid()) {
								loc.setYaw(player.getLocation().getYaw());
								loc.setPitch(player.getLocation().getPitch());
								player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 4);
								player.teleport(loc.add(0.5, 1, 0.5));
								player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 4);
								player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 2));
								SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName()+ChatColor.DARK_AQUA+" fades into the distance.", player);
								fadeCD.addCooldown(player);
								return;
							}
						}
					}
				}
				Location prevLoc = blockList.get(blockList.size()-2).getLocation().add(0.5, 0, 0.5);
				prevLoc.setYaw(player.getLocation().getYaw());
				prevLoc.setPitch(player.getLocation().getPitch());
				player.teleport(prevLoc);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 2));
				SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName()+ChatColor.DARK_AQUA+" fades into the distance.", player);
				fadeCD.addCooldown(player);
				return;
			}
		}
		SavageUtility.displayMessage(ChatColor.RED+"Target block is out of range.", player);
	}

	private void boneShield(Player player) {
		if(boneShieldList.contains(player.getName())) {
			SavageUtility.displayMessage(ChatColor.RED+"You already have a Bone Shield.", player);
			return;
		}
		boneShieldList.add(player.getName());
		(((CraftPlayer)player).getHandle()).setAbsorptionHearts(boneShieldHearts*2);
		new Shield(player, SC);
	}

	private void sicken(Player player) {
		Player target = (Player)SavageUtility.getTarget(player, sickenRange, false);
		if(target == null){
			return;
		}
		target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, sickenDur*20, sickenPoisonLevel-1));
		target.setHealth(Math.max(target.getHealth()-sickenDamage, 0));
		SavageUtility.displayMessage(ChatColor.RED+"You have been Sickened by "+ChatColor.GOLD+player.getName(), target);
		SavageUtility.displayMessage(ChatColor.AQUA+"You have Sickened "+ChatColor.GOLD+target.getName(), player);
		sickenCD.addCooldown(player);
	}

	private void leechLife(Player player) {
		Player target = (Player)SavageUtility.getTarget(player, leechRange, false);
		if(target == null){
			SavageUtility.displayMessage(ChatColor.RED+"You must have a player target not in your party.", player);
			return;
		}
		if(leechAffected.containsKey(target.getName())) {
			SavageUtility.displayMessage(ChatColor.RED+"Target is already affected by a Leech Life.", player);
			return;
		}
		if(leechCaster.containsKey(player.getName())) {
			SavageUtility.displayMessage(ChatColor.RED+"You cannot apply Leech Life to mulitple targets", player);
			return;
		}
		new Drain(SC, player, target);
	}
	private class Drain implements Runnable{
		int task;
		int counter = 1;
		Player caster;
		Player target;

		public Drain(SavageClasses SC, Player caster, Player target){
			this.caster = caster;
			this.target = target;
			leechAffected.put(target.getName(), this);
			leechCaster.put(caster.getName(), this);
			SavageUtility.displayMessage(ChatColor.RED + "Your life is being siphoned by " +ChatColor.GOLD+ caster.getName() +ChatColor.RED+ "!", target);
			SavageUtility.displayMessage(ChatColor.GOLD + "You begin leeching " + target.getName()+"'s life.", caster);
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(SC, this, 0, leechDelay*20);
			leechCD.addCooldown(caster);
		}
		public void cancel() {
			Bukkit.getScheduler().cancelTask(task);
			leechAffected.remove(target.getName());
			leechCaster.remove(caster.getName());
		}
		@Override
		public void run() {
			if(counter > leechTicks) {
				SavageUtility.displayMessage(ChatColor.GOLD + caster.getName() + " stops Leeching life from you, as it expires.", target);
				SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " +ChatColor.GOLD+ target.getName()+ChatColor.RED+", as it expires.", caster);
				cancel();
				return;
			}
			counter++;
			if(caster.isOnline() == false) {
				SavageUtility.displayMessage(ChatColor.GOLD + caster.getName()+ChatColor.AQUA+" has disconnected, lifting their Leeching Life curse from you.", target);
				cancel();
				return;				
			}
			if(target.isOnline() == false) {
				SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as they have gone offline.", caster);
				cancel();
				return;
			}
			if(caster.isDead()) {
				cancel();
				return;
			}
			if(target.isDead()) {
				cancel();
				return;				
			}
			if(SavageUtility.getPartyMembers(caster).contains(target)) {
				SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as they are in your party.", caster);
				SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+ "stops Leeching life from you, as they are in your party.", target);
				Bukkit.getScheduler().cancelTask(task);
				return;
			}
			if(!(SavageUtility.isPVPAllowed(target))) {
				SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as they are in a safezone.", caster);
				SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+ "stops Leeching life from you, as you are in a safezone.", target);
				cancel();
				return;
			}
			if(Assassin.isStealthed.contains(target.getName())) {
				SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as they have gone invisible.", caster);
				SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+ "stops Leeching life from you, as you are invisible.", target);
				cancel();
				return;				
			}
			if(caster.getLocation().distance(target.getLocation())<=leechRange*3 && caster.getWorld() == target.getWorld()) {
				target.setHealth(Math.max(target.getHealth()-leechDamage, 0)); // prevent underflow
				caster.setHealth(Math.min(caster.getHealth()+leechDamage, caster.getMaxHealth())); // prevent overflow
				SavageUtility.displayMessage(ChatColor.RED+caster.getName()+" Leeches life from you!", target);
				SavageUtility.displayMessage(ChatColor.GOLD + "You Leech " + target.getName() + "'s life!", caster);
			}
			else {
				SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as they are too far.", caster);
				SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+"stops Leeching life from you, as they are too far.", target);
				cancel();
				return;
			}
		}
		public void casterDead() {
			SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as you have died.", caster);
			SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+ "stops Leeching life from you, as they have died.", target);
			cancel();
			return;
		}
		public void targetDead() {
			SavageUtility.displayMessage(ChatColor.RED + "You stop Leeching life from " + ChatColor.GOLD+target.getName()+ChatColor.RED+", as they have died.", caster);
			SavageUtility.displayMessage(ChatColor.GOLD+caster.getName()+ "stops Leeching life from you, as you have died.", target);
			cancel();
			return;
		}
	}
	private class Shield implements Runnable{
		Player caster;
		EntitySkeleton skele1;
		EntitySkeleton skele2;
		private int task;
		private int timer = 0;
		private EntityPlayer craftCaster;

		public Shield(Player player, SavageClasses SC) {
			caster = player;
			craftCaster = ((CraftPlayer)player).getHandle();
			skele1 = ((CraftSkeleton)(player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON))).getHandle();
			skele2 = ((CraftSkeleton)(player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON))).getHandle();
			skele1.setCustomNameVisible(true);
			skele2.setCustomNameVisible(true);
			skele1.setSkeletonType(1);
			skele2.setSkeletonType(1);
			skele1.setAbsorptionHearts(100);
			skele2.setAbsorptionHearts(100);
			((Skeleton)skele1.getBukkitEntity()).getEquipment().setItemInHand(new ItemStack(Material.STONE_AXE));
			((Skeleton)skele2.getBukkitEntity()).getEquipment().setItemInHand(new ItemStack(Material.STONE_SWORD));
			skele1.getBukkitEntity().setMetadata("boneShield", new FixedMetadataValue(SC, true));
			skele2.getBukkitEntity().setMetadata("boneShield", new FixedMetadataValue(SC, true));
			task = Bukkit.getScheduler().scheduleSyncRepeatingTask(SC, this, 0, 20);
			boneShieldCD.addCooldown(player);
			SavageUtility.broadcastMessage(ChatColor.GOLD + caster.getName()+" summons a Bone Shield around themself.", caster);
		}
		public void cancel() {
			if(skele1 != null) {
				skele1.getBukkitEntity().remove();
			}
			if(skele2 != null) {
				skele2.getBukkitEntity().remove();
			}
			craftCaster.setAbsorptionHearts(0);
			boneShieldList.remove(boneShieldList.indexOf(caster.getName()));
			Bukkit.getScheduler().cancelTask(task);
		}
		@Override
		public void run() {
			timer++;
			if(timer > boneShieldDur || craftCaster.getAbsorptionHearts() <= 0) {
				SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+"'s "+ChatColor.RED+"bone shield wears away", caster);
				cancel();
				return;
			}
			if(craftCaster.getAbsorptionHearts() >= boneShieldHearts*2*0.75) {
				skele1.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.GREEN+"100%");				
				skele2.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.GREEN+"100%");				
			}
			else if(craftCaster.getAbsorptionHearts() >= boneShieldHearts*2*0.5) {
				skele1.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.GOLD+"75%");				
				skele2.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.GOLD+"75%");				

			}
			else if(craftCaster.getAbsorptionHearts() >= boneShieldHearts*2*0.25) {
				skele1.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.RED+"50%");
				skele2.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.RED+"50%");				
			}
			else if(craftCaster.getAbsorptionHearts() >= boneShieldHearts*2*0.00){
				skele1.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.DARK_RED+"25%");				
				skele2.getBukkitEntity().setCustomName(ChatColor.GOLD+caster.getName()+"'s Bone Shield: "+ChatColor.DARK_RED+"25%");				
			}
			else {
				SavageUtility.broadcastMessage(ChatColor.GOLD+caster.getName()+"'s Bone Shield breaks.", caster);
				cancel();
				return;
			}
			if(skele1.getBukkitEntity().getLocation().distance(caster.getLocation())>= 20) {
				skele1.enderTeleportTo(craftCaster.locX, craftCaster.locY, craftCaster.locZ);
			}
			else {
				skele1.getNavigation().a(new BlockPosition(craftCaster.locX, craftCaster.locY, craftCaster.locZ));	
			}
			if(skele2.getBukkitEntity().getLocation().distance(caster.getLocation())>= 20) {
				skele2.enderTeleportTo(craftCaster.locX, craftCaster.locY, craftCaster.locZ);
			}
			else {
				skele2.getNavigation().a(new BlockPosition(craftCaster.locX, craftCaster.locY, craftCaster.locZ));	
			}
		}
	}
	public void imprison(Player player) {
		Player target = (Player)SavageUtility.getTarget(player, imprisonRange, false);
		if(target == null){
			return;
		}
		if(imprisonAffected.contains(target.getName())) {
			SavageUtility.displayMessage(ChatColor.RED+"Target is already affected by an Imprison.", player);
			return;
		}
		imprisonAffected.add(target.getName());
		SavageUtility.displayMessage(ChatColor.RED+"You have been Imprisoned by "+ChatColor.GOLD+player.getName(), target);
		SavageUtility.displayMessage(ChatColor.AQUA+"You have Imprisoned "+ChatColor.GOLD+target.getName(), player);
		SavageUtility.broadcastMessage(ChatColor.GOLD+player.getName()+ChatColor.LIGHT_PURPLE+" has Imprisoned "+ChatColor.GOLD+target.getName(), target);
		target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,imprisonDur*20,5));
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,imprisonDur*20,5));
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,imprisonDur*20,5));
		target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION,imprisonDur*20,5));
		Bukkit.getServer().getWorld(target.getWorld().getName()).playEffect(target.getLocation(), Effect.SMOKE, 4);
		Bukkit.getServer().getWorld(target.getWorld().getName()).playEffect(target.getLocation(), Effect.EXTINGUISH, 4);
		onImprison(target.getName());
		imprisonCD.addCooldown(player);
	}
	public void onImprison(String name){
		Player player = Bukkit.getPlayer(name);
		imprisonAffected.add(name);
		for (Player p : Bukkit.getOnlinePlayers()) {
			Log.info(p.getName());
			if (!(p.equals(player)) && p.canSee(player)){
				p.hidePlayer(player);
			}
		}
		new WarlockStealth(SC, player);
	}
	public void unImprison(String name)
	{
		Player player = Bukkit.getPlayer(name);
		if(player == null) return;
		if (imprisonAffected.contains(name)){
			imprisonAffected.remove(name);
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if ((!p.equals(player)) && (!p.canSee(player))) {
					p.showPlayer(player);
				}
			}
			if (player.isOnline()) {
				SavageUtility.displayMessage("&cYou are no longer imprisoned!", player);
			}
		}
	}
	public class WarlockStealth implements Runnable{
		Player player;
		int x = 0;
		int taskID;
		public WarlockStealth(SavageClasses SC, Player player) {
			this.player = player;
			this.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SC, this, 0L, 20L);
			Stun.stunPlayer(player, imprisonDur*1000);
		}

		public void run() {
			++this.x;
			if (this.x > imprisonDur || !(imprisonAffected.contains(this.player.getName()))) {
				if (player.isOnline()) {
					unImprison(this.player.getName());
				}
				Bukkit.getScheduler().cancelTask(this.taskID);
				imprisonAffected.remove(imprisonAffected.indexOf(player.getName()));
			}
		}
	}
	@EventHandler
	public void entityDamage(EntityDamageByEntityEvent event) {
		if(event.getDamager().hasMetadata("boneShield") || event.getEntity().hasMetadata("boneShield") || imprisonAffected.contains(event.getDamager().getName()) || imprisonAffected.contains(event.getEntity().getName())) {
			event.setCancelled(true);
			return;
		}
		if(event.getDamager() instanceof Player) {
			if(SC.isClass((Player)event.getDamager(), "warlock")) {
				Player player = (Player)event.getDamager();
				ItemStack item = player.getInventory().getItemInHand();
				if(item != null) {
					if(item.getType().equals(Material.GOLD_SWORD)) {
						return;
					}
				}
				SavageUtility.displayMessage(ChatColor.RED+"Warlocks can only use Golden Swords.", player);
				event.setCancelled(true);
			}
		}
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			Player player = (Player)event.getEntity();
			Player damager = (Player)event.getDamager();
			EntityPlayer craftPlayer = ((CraftPlayer)player).getHandle();
			if (boneShieldList.contains(player.getName())){
				if(craftPlayer.getAbsorptionHearts() > 0){
					SavageUtility.displayMessage(ChatColor.RED + player.getName() + "'s Bone Shield absorbs your attack!", damager);
				}
				else{
					SavageUtility.displayMessage(ChatColor.RED + "You have broken " + player.getName() + "'s Bone Shield!", damager);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {

		if(leechAffected.containsKey(event.getEntity().getName())) {
			leechAffected.get(event.getEntity().getName()).targetDead();
		}
		else if(leechCaster.containsKey(event.getEntity().getName())) {
			leechCaster.get(event.getEntity().getName()).casterDead();
		}
	}
}

