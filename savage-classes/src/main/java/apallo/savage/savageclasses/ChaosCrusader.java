package apallo.savage.savageclasses;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class ChaosCrusader implements Listener
{
	private static Cooldown blinkCD;
	private static Cooldown stunCD;
	private static Cooldown switchCD;
	private static Cooldown critCD;

	private static int stunDuration;
	
	private static int effectStrengthAmp;
	private static int effectStrengthDur;

	private static int effectSlowAmp;
	private static int effectSlowDur;

	private static double critMult;

	private SavageClasses SC;

	ChaosCrusader(SavageClasses SC)
	{
		this.SC = SC;
		reload(SC);
	}

	public static void reload(SavageClasses SC) {
		blinkCD = new Cooldown("chaoscrusader.blinkCD", SC);
		stunCD = new Cooldown("chaoscrusader.stunCD", SC);
		switchCD = new Cooldown("chaoscrusader.switchCD", SC);
		critCD = new Cooldown("chaoscrusader.critCD", SC);

		stunDuration = SC.getClassStatsConfig().getInt("chaoscrusader.stunDuration")*1000;
	
		effectStrengthAmp = SC.getClassStatsConfig().getInt("chaoscrusader.effectStrengthAmp")-1;
		effectStrengthDur = SC.getClassStatsConfig().getInt("chaoscrusader.effectStrengthDur")*20;
		
		effectSlowAmp = SC.getClassStatsConfig().getInt("chaoscrusader.effectSlowAmp")-1;
		effectSlowDur = SC.getClassStatsConfig().getInt("chaoscrusader.effectSlowDur")*20;

		critMult = SC.getClassStatsConfig().getDouble("chaoscrusader.critMult");
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent evt)
	{
		Player p = evt.getPlayer();
		if (SC.isClass(p, "chaoscrusader"))
		{
			Material mainHand = p.getInventory().getItemInHand().getType();
			Action action = evt.getAction();
			if (mainHand == Material.DIAMOND_SWORD)
			{
				if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
				{
					if(!(stunCD.isOnCooldown(p))) {
						chaosStun(p);	
					}
					else {
						SavageUtility.displayMessage("Chaos Stun is on cooldown for "+stunCD.getRemainingTime(p)+" seconds.", p);
					}
				}
			}
			else if(mainHand == Material.GOLD_SWORD) {
				if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
					if(!(switchCD.isOnCooldown(p))) {
						switchPlayer(p);
					}
					else {
						SavageUtility.displayMessage("Chaos Switch is on cooldown for "+switchCD.getRemainingTime(p)+" seconds.", p);
					}
				}
				else if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					if(!(blinkCD.isOnCooldown(p))) {
						blink(p);
					}
					else {
						SavageUtility.displayMessage("Chaos Blink is on cooldown for "+blinkCD.getRemainingTime(p)+" seconds.", p);
					}
				}
			}
		}
	}

	private void blink(Player player) {
		Player target = (Player)SavageUtility.getTarget(player,20, true);

		if(target == null) {
			return;
		}
		
		if(SavageUtility.isInOwnTerritory(target)){
			SavageUtility.displayMessage(ChatColor.RED + "You may not blink to a player in their own territory.", player);
			return;
		}

		Location playerLocation = player.getLocation();
		player.teleport(target);
		player.getLocation().setDirection(playerLocation.getDirection());
		
		player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, effectStrengthDur, effectStrengthAmp));
		
		blinkCD.addCooldown(player);
		SavageUtility.broadcastMessage(player.getName() + " &6used &bChaos Blink&6 on "+target.getName()+".", player);
	}

	private void switchPlayer(Player player) {
		if(!SavageUtility.isPVPAllowed(player)){
			SavageUtility.displayMessage(ChatColor.RED + "PVP is not allowed in this area!", player);
			return;
		}

		Player target = (Player) SavageUtility.getTarget(player, 20, false);

		if(SavageUtility.isInOwnTerritory(target)){
			SavageUtility.displayMessage(ChatColor.RED + "You may not switch with a player in their own territory.", player);
			return;
		}
		
		if(target == null) {
            player.sendMessage(ChatColor.DARK_GRAY + "[SR] " + ChatColor.RED + "You must have a player target not in your party!");
            return;
		}
		
		Location playerLocation = player.getLocation().setDirection((target.getLocation().subtract(player.getLocation())).toVector());
		Location targetLocation = target.getLocation().setDirection((player.getLocation().subtract(target.getLocation())).toVector());
		
		player.teleport(targetLocation);
		target.teleport(playerLocation);
		
		target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, effectSlowDur, effectSlowAmp));
		
		switchCD.addCooldown(player);
		SavageUtility.broadcastMessage(player.getName() + " &6used &bChaos Switch&6 on "+target.getName()+".", player);
	}

	void chaosStun(Player player) {
		if(!SavageUtility.isPVPAllowed(player)){
			SavageUtility.displayMessage(ChatColor.RED + "PVP is not allowed in this area!", player);
			return;
		}

		Player target = (Player)SavageUtility.getTarget(player,20, false);
		
		if(target == null) {
            return;
		}

		Stun.stunPlayer(target, stunDuration);
		stunCD.addCooldown(player);
		SavageUtility.broadcastMessage(player.getName() + " &6used &bChaos Stun&6 on "+target.getName()+".", player);
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent evt)
	{
		if ((evt.getDamager() instanceof Player))
		{
			Player p = (Player)evt.getDamager();
			if (SC.isClass(p, "chaoscrusader"))
			{
				if ((evt.getEntity() instanceof LivingEntity))
				{
					double random = Math.random();

					LivingEntity enemy = (LivingEntity)evt.getEntity();
					if (!critCD.isOnCooldown(p))
					{
						if (random <= 0.10D)
						{
							double dmg = evt.getDamage();
							evt.setDamage(dmg * critMult);
							if ((evt.getEntity() instanceof Player))
							{
								Player player = (Player)evt.getEntity();
								SavageUtility.displayMessage("You've been &7Crit!", player);
							}
							SavageUtility.displayMessage("You &7Crit!", p);

							critCD.addCooldown(p);
						}
					}
				}
			}
		}
	}
}
