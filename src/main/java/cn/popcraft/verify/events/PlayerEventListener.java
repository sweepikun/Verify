package cn.popcraft.verify.events;

import cn.popcraft.verify.VerifyPlugin;
import cn.popcraft.verify.managers.VerificationManager;
import cn.popcraft.verify.managers.BookManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 作者: Popcraft
 * 
 * 负责:
 * - 处理玩家加入事件
 * - 处理玩家退出事件
 */
public class PlayerEventListener implements Listener {
    
    private final VerifyPlugin plugin;
    
    public PlayerEventListener(VerifyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理玩家加入事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 移除跳过验证权限的逻辑，确保每次都强制验证
        // 注释掉以下代码：
        
        if (player.hasPermission("verify.bypass")) {
            if (plugin.getConfigManager().logVerifications()) {
                plugin.getLogger().info("玩家 " + player.getName() + " 跳过验证 (拥有 bypass 权限)");
            }
            
            // 显示规则书（如果启用）
            if (plugin.getConfigManager().isBookEnabled()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getBookManager().showRuleBook(player);
                }, 20L); // 延迟1秒
            }
            
            return;
        }
        
        
        // 检查验证功能是否启用
        if (!plugin.getConfigManager().isVerificationEnabled()) {
            if (plugin.getConfigManager().logVerifications()) {
                plugin.getLogger().info("验证功能未启用，玩家 " + player.getName() + " 直接进入");
            }
            return;
        }
        
        // 创建验证
        VerificationManager.PlayerVerification verification = plugin.getVerificationManager().createVerification(player);
        
        if (verification != null) {
            // 修改加入消息
            String joinMessage = "§7[§a验证§7] §b" + player.getName() + " §7加入了服务器";
            event.setJoinMessage(joinMessage);
            
            // 延迟打开验证GUI界面
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getGUIManager().openVerifyGUI(player, verification.getVerificationCode());
            }, 20L); // 延迟1秒
            
            if (plugin.getConfigManager().logVerifications()) {
                plugin.getLogger().info("玩家 " + player.getName() + " 加入，需要验证 (验证码: " + verification.getVerificationCode() + ")");
            }
        } else {
            plugin.getLogger().severe("无法为玩家 " + player.getName() + " 创建验证！");
        }
    }
    
    /**
     * 处理玩家退出事件
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 清理玩家的验证信息
        plugin.getVerificationManager().removeVerification(player);
        
        if (plugin.getConfigManager().logVerifications()) {
            plugin.getLogger().info("玩家 " + player.getName() + " 退出，清理验证信息");
        }
        
        // 可以在这里添加其他清理逻辑
        cleanupPlayerData(player);
    }
    
    /**
     * 清理玩家数据
     */
    private void cleanupPlayerData(Player player) {
        // 移除临时文件（如果有）
        // 清理数据库记录（如果使用数据库）
        // 其他清理逻辑
    }
}