package cn.popcraft.verify.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证命令自动补全器
 * 作者: Popcraft
 */
public class VerifyTabCompleter implements TabCompleter {
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            // 第一个参数的自动补全
            String input = args[0].toLowerCase();
            
            // 管理员命令
            if (player.hasPermission("verify.admin")) {
                completions.add("status");
                completions.add("reload");
                completions.add("book");
            }
            
            // 通用命令
            completions.add("help");
            
            // 过滤匹配的命令
            completions.removeIf(completion -> !completion.toLowerCase().startsWith(input));
        }
        
        return completions;
    }
}