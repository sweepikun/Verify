package cn.popcraft.verify.commands;

import cn.popcraft.verify.VerifyPlugin;
import cn.popcraft.verify.managers.VerificationManager;
import cn.popcraft.verify.managers.BookManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 验证命令处理器
 * 作者: Popcraft
 */
public class VerifyCommand implements CommandExecutor {
    
    private final VerifyPlugin plugin;
    
    public VerifyCommand(VerifyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c此命令只能由玩家执行！");
            return true;
        }
        
        Player player = (Player) sender;
        
        // 检查参数
        if (args.length == 0) {
            // 显示帮助信息
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(player);
                break;
                
            case "help":
                showHelp(player);
                break;
                
            case "status":
                handleStatus(player);
                break;
                
            case "book":
                handleBook(player);
                break;
                
            default:
                // 尝试作为验证码处理
                handleVerification(player, args[0]);
                break;
        }
        
        return true;
    }
    
    /**
     * 处理验证
     */
    private void handleVerification(Player player, String verificationCode) {
        // 检查玩家是否已经有验证记录
        VerificationManager.PlayerVerification verification = plugin.getVerificationManager().getVerification(player);
        
        if (verification == null) {
            player.sendMessage("§c您没有待验证的记录！");
            return;
        }
        
        // 检查验证状态
        if (verification.getStatus() != VerificationManager.VerificationStatus.PENDING) {
            switch (verification.getStatus()) {
                case SUCCESS:
                    player.sendMessage("§a您已经验证成功！");
                    break;
                case FAILED:
                    player.sendMessage("§c验证已失败，请重新加入服务器！");
                    break;
                case TIMEOUT:
                    player.sendMessage("§c验证已超时，请重新加入服务器！");
                    break;
                case KICKED:
                    player.sendMessage("§c您已被踢出，请重新加入服务器！");
                    break;
            }
            return;
        }
        
        // 验证验证码
        boolean success = plugin.getVerificationManager().verifyCode(player, verificationCode);
        
        if (success) {
            // 验证成功
            handleVerificationSuccess(player, verification);
        } else {
            // 验证失败
            handleVerificationFailed(player, verification);
        }
    }
    
    /**
     * 处理验证成功
     */
    private void handleVerificationSuccess(Player player, VerificationManager.PlayerVerification verification) {
        // 发送成功消息
        plugin.getBookManager().sendVerificationSuccessMessage(player);
        
        // 清理验证记录
        plugin.getVerificationManager().removeVerification(player);
        
        // 显示规则书（如果启用）
        if (plugin.getConfigManager().isBookEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getBookManager().showRuleBook(player);
            }, 20L);
        }
        
        // 给予奖励（如果配置）
        if (plugin.getConfigManager().giveRewards()) {
            giveRewards(player);
        }
        
        player.sendMessage("§a✅ 验证成功！欢迎来到服务器！");
    }
    
    /**
     * 处理验证失败
     */
    private void handleVerificationFailed(Player player, VerificationManager.PlayerVerification verification) {
        int remainingAttempts = verification.getMaxAttempts() - verification.getAttempts();
        
        if (remainingAttempts <= 0) {
            // 没有剩余次数，玩家将被踢出
            player.sendMessage("§c❌ 验证失败次数过多，您将被踢出服务器！");
            player.sendMessage("§7请重新加入服务器重试。");
        } else {
            // 还有剩余次数
            plugin.getBookManager().sendVerificationFailedMessage(player, remainingAttempts);
            player.sendMessage("§7正确验证码: §e" + verification.getVerificationCode());
        }
    }
    
    /**
     * 处理重载命令
     */
    private void handleReload(Player player) {
        if (!player.hasPermission("verify.reload")) {
            player.sendMessage("§c您没有权限使用此命令！");
            return;
        }
        
        try {
            plugin.reloadPlugin();
            player.sendMessage("§a✅ 配置文件已重载！");
            
            if (plugin.getConfigManager().logVerifications()) {
                plugin.getLogger().info("玩家 " + player.getName() + " 重载了插件配置");
            }
        } catch (Exception e) {
            player.sendMessage("§c❌ 重载失败: " + e.getMessage());
            plugin.getLogger().severe("重载配置时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 处理状态命令
     */
    private void handleStatus(Player player) {
        if (!player.hasPermission("verify.admin")) {
            player.sendMessage("§c您没有权限使用此命令！");
            return;
        }
        
        int pendingCount = plugin.getVerificationManager().getPendingVerificationsCount();
        int verifiedCount = plugin.getVerificationManager().getVerifiedPlayers();
        
        player.sendMessage("§6=== 验证状态 ===");
        player.sendMessage("§7待验证玩家: §e" + pendingCount);
        player.sendMessage("§7已验证玩家: §a" + verifiedCount);
        player.sendMessage("§7验证功能: " + (plugin.getConfigManager().isVerificationEnabled() ? "§a启用" : "§c禁用"));
        player.sendMessage("§7规则书功能: " + (plugin.getConfigManager().isBookEnabled() ? "§a启用" : "§c禁用"));
    }
    
    /**
     * 处理规则书命令
     */
    private void handleBook(Player player) {
        if (!player.hasPermission("verify.admin")) {
            player.sendMessage("§c您没有权限使用此命令！");
            return;
        }
        
        plugin.getBookManager().showRuleBook(player);
        player.sendMessage("§a✅ 规则书已发送给您！");
    }
    
    /**
     * 显示帮助信息
     */
    private void showHelp(Player player) {
        player.sendMessage("§6=== Verify 插件帮助 ===");
        player.sendMessage("§7/verify <验证码> §e- 验证您的账户");
        player.sendMessage("§7/verify status §e- 查看验证状态 (管理员)");
        player.sendMessage("§7/verify reload §e- 重载配置 (管理员)");
        player.sendMessage("§7/verify book §e- 查看规则书 (管理员)");
        player.sendMessage("§7/verify help §e- 显示此帮助");
    }
    
    /**
     * 给予奖励
     */
    private void giveRewards(Player player) {
        List<String> rewardCommands = plugin.getConfigManager().getRewardCommands();
        
        for (String command : rewardCommands) {
            String processedCommand = command.replace("{player}", player.getName());
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            } catch (Exception e) {
                plugin.getLogger().warning("执行奖励命令失败: " + processedCommand + " - " + e.getMessage());
            }
        }
        
        if (plugin.getConfigManager().logVerifications()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 发放了验证奖励");
        }
    }
}