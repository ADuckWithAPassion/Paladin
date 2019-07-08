package apallo.savage.savageclasses;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static apallo.savage.savageclasses.SavageClasses.classMap;

public class CommandClass implements CommandExecutor, TabCompleter  {

    public SavageClasses SC;

    public CommandClass(SavageClasses SC){
        this.SC = SC;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player)sender;
                // /class
                if(args.length == 0){
                    if(classMap.containsKey(player.getName())){
                        String playerClass = classMap.get(player.getName());
                        SavageUtility.displayMessage("You are a member of the " + playerClass + " class.", player );
                        return true;
                    }
                    else {
                        SavageUtility.displayMessage("You don't have a class.", player);
                        return true;
                    }
                }
                else {
                    if(sender.hasPermission("savageclasses.class.change")) {
                        if (args[0].equalsIgnoreCase("scout") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a scout.", player);
                            SC.setPlayerClass(player, "scout");
                        } else if (args[0].equalsIgnoreCase("gladiator") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a gladiator.", player);
                            SC.setPlayerClass(player, "gladiator");
                        } else if (args[0].equalsIgnoreCase("marksman") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a marksman.", player);
                            SC.setPlayerClass(player, "marksman");
                        } else if (args[0].equalsIgnoreCase("assassin") && args.length == 1) {
                            SavageUtility.displayMessage("You are now an assassin.", player);
                            SC.setPlayerClass(player, "assassin");
                        } else if (args[0].equalsIgnoreCase("bard") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a bard.", player);
                            SC.setPlayerClass(player, "bard");
                        } else if (args[0].equalsIgnoreCase("paladin") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a paladin.", player);
                            SC.setPlayerClass(player, "paladin");
                        } else if (args[0].equalsIgnoreCase("berserker") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a berserker.", player);
                            SC.setPlayerClass(player, "berserker");
                        } else if (args[0].equalsIgnoreCase("triton") && args.length == 1) {
                            SavageUtility.displayMessage("You are now a Triton.", player);
                            SC.setPlayerClass(player, "triton");
                            
                        } else if (args[0].equalsIgnoreCase("info") && args.length == 1) {
                            SavageUtility.displayMessage("This is a Savage Realms unique class plugin featuring multiple classes with PVP altering abilities.", player);
                        } else {
                            SavageUtility.displayMessage("Strange args. Try typing /class info", player);
                        }
                    }
                    else{
                        SavageUtility.displayMessage("You do not have permission for this command.", player);
                    }
                }
                return true;

        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList();
        if(sender.hasPermission("savageclasses.class.change")) {
                list.add("scout");
                list.add("gladiator");
                list.add("marksman");
                list.add("assassin");
                list.add("bard");
                list.add("paladin");
                list.add("berserker");
                list.add("triton");

                Iterator<String> iter = list.iterator();
            while(iter.hasNext()){
                String s = iter.next();

                if(s.contains(args[0])){
                    continue;
                }
                else{
                    iter.remove();
                }
            }
        }
        return list;
    }

}
