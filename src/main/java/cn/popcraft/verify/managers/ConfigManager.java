package cn.popcraft.verify.managers;

import cn.popcraft.verify.VerifyPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 配置管理器
 * 作者: Popcraft
 */
public class ConfigManager {
    
    private final VerifyPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    public ConfigManager(VerifyPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfig();
    }
    
    /**
     * 保存配置
     */
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存配置文件: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置
     */
    public FileConfiguration getConfig() {
        return config;
    }
    
    // ========== 验证配置相关方法 ==========
    
    /**
     * 是否启用验证功能
     */
    public boolean isVerificationEnabled() {
        return config.getBoolean("verification.enabled", true);
    }
    
    /**
     * 获取验证类型
     */
    public String getVerificationType() {
        return config.getString("verification.type", "RANDOM");
    }
    
    /**
     * 获取自定义验证码
     */
    public String getCustomVerificationCode() {
        return config.getString("verification.custom-code", "WELCOME2024");
    }
    
    /**
     * 获取随机验证码长度
     */
    public int getRandomCodeLength() {
        return config.getInt("verification.random-length", 6);
    }
    
    /**
     * 获取验证码字符集
     */
    public String getVerificationCharacters() {
        return config.getString("verification.characters", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
    }
    
    /**
     * 获取验证超时时间(秒)
     */
    public int getVerificationTimeout() {
        return config.getInt("verification.timeout", 300);
    }
    
    /**
     * 获取最大重试次数
     */
    public int getMaxAttempts() {
        return config.getInt("verification.max-attempts", 3);
    }
    
    /**
     * 获取验证失败踢出消息
     */
    public String getKickMessage() {
        return config.getString("verification.kick-message", "§c验证码验证失败！");
    }
    
    // ========== 规则书配置相关方法 ==========
    
    /**
     * 是否启用规则书功能
     */
    public boolean isBookEnabled() {
        return config.getBoolean("book.enabled", true);
    }
    
    /**
     * 是否在验证成功后自动打开规则书
     */
    public boolean isBookAutoOpen() {
        return config.getBoolean("book.auto-open", true);
    }
    
    /**
     * 是否使用GUI界面展示规则书
     */
    public boolean useGUIBook() {
        return config.getBoolean("book.use-gui", true);
    }
    
    /**
     * 获取书标题
     */
    public String getBookTitle() {
        return config.getString("book.title", "§l服务器规则");
    }
    
    /**
     * 获取书作者
     */
    public String getBookAuthor() {
        return config.getString("book.author", "§6管理员");
    }
    
    /**
     * 获取书页内容
     */
    public List<String> getBookPages() {
        return config.getStringList("book.pages");
    }
    
    // ========== 消息配置相关方法 ==========
    
    /**
     * 获取玩家加入时的验证消息
     */
    public List<String> getJoinMessages() {
        return config.getStringList("messages.join-message");
    }
    
    /**
     * 获取验证成功消息
     */
    public List<String> getSuccessMessages() {
        return config.getStringList("messages.success-message");
    }
    
    /**
     * 获取验证失败消息
     */
    public List<String> getFailedMessages() {
        return config.getStringList("messages.failed-message");
    }
    
    /**
     * 获取验证超时消息
     */
    public List<String> getTimeoutMessages() {
        return config.getStringList("messages.timeout-message");
    }
    
    // ========== 其他设置相关方法 ==========
    
    /**
     * 是否给予奖励
     */
    public boolean giveRewards() {
        return config.getBoolean("settings.give-rewards", false);
    }
    
    /**
     * 获取奖励命令列表
     */
    public List<String> getRewardCommands() {
        return config.getStringList("settings.reward-commands");
    }
    
    /**
     * 是否记录验证日志
     */
    public boolean logVerifications() {
        return config.getBoolean("settings.log-verifications", true);
    }
    
    /**
     * 是否启用调试模式
     */
    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }
}