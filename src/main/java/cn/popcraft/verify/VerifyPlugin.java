package cn.popcraft.verify;

import cn.popcraft.verify.commands.VerifyCommand;
import cn.popcraft.verify.commands.VerifyTabCompleter;
import cn.popcraft.verify.events.PlayerEventListener;
import cn.popcraft.verify.managers.ConfigManager;
import cn.popcraft.verify.managers.VerificationManager;
import cn.popcraft.verify.managers.BookManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Verify 插件主类
 * 作者: Popcraft
 * 
 * 功能:
 * - 验证码验证系统
 * - 规则书展示系统
 * - 配置管理
 */
public class VerifyPlugin extends JavaPlugin {
    
    private static VerifyPlugin instance;
    private ConfigManager configManager;
    private VerificationManager verificationManager;
    private BookManager bookManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        
        // 初始化验证管理器
        verificationManager = new VerificationManager(this);
        
        // 初始化书管理器
        bookManager = new BookManager(this);
        
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        
        // 注册命令
        if (getCommand("verify") != null) {
            getCommand("verify").setExecutor(new VerifyCommand(this));
            getCommand("verify").setTabCompleter(new VerifyTabCompleter());
        }
        
        // 加载配置
        reloadPlugin();
        
        getLogger().info("Verify 插件已成功启用！");
        getLogger().info("版本: " + getDescription().getVersion());
        getLogger().info("作者: " + getDescription().getAuthors());
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Verify 插件已禁用！");
        
        // 清理资源
        if (verificationManager != null) {
            verificationManager.cleanup();
        }
        
        instance = null;
    }
    
    /**
     * 重载插件配置
     */
    public void reloadPlugin() {
        // 重载配置文件
        saveDefaultConfig();
        reloadConfig();
        
        // 重新加载配置管理器
        if (configManager != null) {
            configManager.reloadConfig();
        }
        
        getLogger().log(Level.INFO, "配置文件已重载！");
    }
    
    /**
     * 获取插件实例
     */
    public static VerifyPlugin getInstance() {
        return instance;
    }
    
    /**
     * 获取配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取验证管理器
     */
    public VerificationManager getVerificationManager() {
        return verificationManager;
    }
    
    /**
     * 获取书管理器
     */
    public BookManager getBookManager() {
        return bookManager;
    }
}