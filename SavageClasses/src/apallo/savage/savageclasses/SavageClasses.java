package apallo.savage.savageclasses;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SavageClasses extends JavaPlugin implements Listener {
    private File customConfigFile;
    private FileConfiguration customConfig;

    private File classStatsConfigFile;
    private FileConfiguration classStatsConfig;
    
    public static HashMap<String, String> classMap = new HashMap();
    ArrayList<String> classes = new ArrayList<String>();

    public static WorldGuardPlugin wg;

    public SavageClasses() {}

    @Override
    public void onEnable() {
        createCustomConfig();
        createClassStatsConfig();
        wg = (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        for(Player online: Bukkit.getOnlinePlayers()){
            String playerClass = getConfig().getString(online.getName());
            if(playerClass != null) { // added null check. Otherwise you get null pointer spam on reload if someone has no class.
                classMap.put(online.getName(), playerClass);             	
            }
        }

        registerCommands();
        getServer().getPluginManager().registerEvents(new Scout(this), this);
        getServer().getPluginManager().registerEvents(new Gladiator(this), this);
        getServer().getPluginManager().registerEvents(new Marksman(this), this);
        getServer().getPluginManager().registerEvents(new Assassin(this), this);
        getServer().getPluginManager().registerEvents(new Bard(this), this);
        getServer().getPluginManager().registerEvents(new Paladin(this), this);
        getServer().getPluginManager().registerEvents(new Berserker(this), this);
        getServer().getPluginManager().registerEvents(new Triton(this), this);
        getServer().getPluginManager().registerEvents(new Bandage(this), this);
        getServer().getPluginManager().registerEvents(new ArmorAdjustment(this), this);
        getServer().getPluginManager().registerEvents(new EnforceEnchants(this), this);
        getServer().getPluginManager().registerEvents(new Restriction(this), this);
        getServer().getPluginManager().registerEvents(new Consumable(this), this);
        getServer().getPluginManager().registerEvents(new Stun(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("enabled!");
    }

    void setPlayerClass(Player p, String className){
        classMap.put(p.getName(), className);
        getConfig().set(p.getName(), className);
        saveConfig();
        SavageUtility.removePotionEffects(p);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent evt)
    {
        if (getConfig().get(evt.getPlayer().getName()) != null)
        {
            String className = getConfig().getString(evt.getPlayer().getName());
            classMap.put(evt.getPlayer().getName(), className);
        }
    }

    boolean isClass(String player, String className)
    {
        try
        {
                if (classMap.containsKey(player))
                {
                    return (classMap.get(player)).equalsIgnoreCase(className);
                }
                return false;
        }
        catch (Exception e) {System.out.println(e);}

        return false;
    }

    boolean isClass(Player player, String className)
    {
        try
        {
            if (classMap.containsKey(player.getName()))
            {
                return (classMap.get(player.getName())).equalsIgnoreCase(className);
            }
            return false;
        }
        catch (Exception e) {}

        return false;
    }

    String getPlayerClass(Player player) {
    	if(classMap.containsKey(player.getName())) {
    		return(classMap.get(player.getName()));
    	}
    	return("None");
    }
    
    private void registerCommands(){
        Objects.requireNonNull(this.getCommand("class")).setExecutor(new CommandClass(this));
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }
    
    public FileConfiguration getClassStatsConfig() {
        return classStatsConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "custom.yml");
        if (!customConfigFile.exists()) {
            boolean fileMade = customConfigFile.getParentFile().mkdirs();
            saveResource("custom.yml", false);
        }

        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    private void createClassStatsConfig() {
        classStatsConfigFile = new File(getDataFolder(), "classStats.yml");
        if (!classStatsConfigFile.exists()) {
            boolean fileMade = classStatsConfigFile.getParentFile().mkdirs();
            saveResource("classStats.yml", false);
        }

        classStatsConfig = new YamlConfiguration();
        try {
            classStatsConfig.load(classStatsConfigFile);

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }
        return (WorldGuardPlugin) plugin;
    }

    public void onDisable(){
        getLogger().info("disabled!");
    }
}
