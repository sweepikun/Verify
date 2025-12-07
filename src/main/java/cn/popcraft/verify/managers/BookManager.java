package cn.popcraft.verify.managers;

import cn.popcraft.verify.VerifyPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

/**
 * 书管理器
 * 作者: Popcraft
 * 
 * 负责:
 * - 规则书创建
 * - 书页管理
 * - 书籍展示
 */
public class BookManager {
    
    private final VerifyPlugin plugin;
    
    public BookManager(VerifyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 为玩家创建规则书
     */
    public ItemStack createRuleBook() {
        ConfigManager configManager = plugin.getConfigManager();
        
        // 创建书物品
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        // 设置书的基本信息
        bookMeta.setTitle(configManager.getBookTitle());
        bookMeta.setAuthor(configManager.getBookAuthor());
        
        // 获取书页内容
        List<String> pages = configManager.getBookPages();
        
        // 将字符串列表转换为书页格式
        for (String pageContent : pages) {
            bookMeta.addPage(processBookPage(pageContent));
        }
        
        book.setItemMeta(bookMeta);
        return book;
    }
    
    /**
     * 处理书页内容
     */
    private String processBookPage(String content) {
        // 处理换行符
        content = content.replace("\\n", "\n");
        
        // 处理颜色代码
        content = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', content);
        
        return content;
    }
    
    /**
     * 给玩家展示规则书
     */
    public void showRuleBook(Player player) {
        if (!plugin.getConfigManager().isBookEnabled()) {
            return;
        }
        
        try {
            // 根据配置决定是否使用GUI界面
            if (plugin.getConfigManager().useGUIBook()) {
                // 使用GUI界面展示规则书
                plugin.getGUIManager().openRuleBookGUI(player);
                
                if (plugin.getConfigManager().logVerifications()) {
                    plugin.getLogger().info("为玩家 " + player.getName() + " 展示了规则书界面");
                }
            } else {
                // 使用原有的实体书方式
                showPhysicalBook(player);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("展示规则书时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 展示实体书（原有的逻辑）
     */
    private void showPhysicalBook(Player player) {
        // 创建规则书
        ItemStack ruleBook = createRuleBook();
        
        // 将书给玩家并自动打开
        player.getInventory().addItem(ruleBook);
        
        // 自动打开书（如果配置启用）
        if (plugin.getConfigManager().isBookAutoOpen()) {
            // 延迟一tick再打开书，确保物品已经添加到背包
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                try {
                    player.openBook(ruleBook);
                } catch (Exception e) {
                    plugin.getLogger().warning("无法为玩家 " + player.getName() + " 打开规则书: " + e.getMessage());
                }
            }, 1L);
        }
        
        if (plugin.getConfigManager().logVerifications()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 发送了实体规则书");
        }
    }
    
    /**
     * 创建自定义书
     */
    public ItemStack createCustomBook(String title, String author, List<String> pages) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        bookMeta.setTitle(title);
        bookMeta.setAuthor(author);
        
        for (String pageContent : pages) {
            bookMeta.addPage(processBookPage(pageContent));
        }
        
        book.setItemMeta(bookMeta);
        return book;
    }
    
    /**
     * 创建欢迎书
     */
    public ItemStack createWelcomeBook(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        
        // 个性化欢迎书
        String playerName = player.getName();
        String welcomeTitle = "§a欢迎 " + playerName + "！";
        
        bookMeta.setTitle(welcomeTitle);
        bookMeta.setAuthor(configManager.getBookAuthor());
        
        // 创建欢迎页面
        List<String> welcomePages = new java.util.ArrayList<>();
        welcomePages.add(
            "§l§a欢迎来到我们的服务器！\n\n" +
            "§r§7亲爱的 §b" + playerName + "§7,\n\n" +
            "§r§7感谢您通过验证并加入我们！\n" +
            "§r§7希望您在这里度过愉快的时光。\n\n" +
            "§r§2祝您游戏愉快！\n" +
            "§r§6服务器管理团队"
        );
        
        welcomePages.addAll(configManager.getBookPages());
        
        for (String pageContent : welcomePages) {
            bookMeta.addPage(processBookPage(pageContent));
        }
        
        book.setItemMeta(bookMeta);
        return book;
    }
    
    /**
     * 发送验证链接到聊天栏
     */
    public void sendVerificationLink(Player player, String verificationCode) {
        ConfigManager configManager = plugin.getConfigManager();
        
        // 创建验证链接组件
        TextComponent verifyLink = new TextComponent("§a[点击验证]");
        verifyLink.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/verify " + verificationCode));
        verifyLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("点击验证您的账户")));
        
        // 创建复制验证码组件
        TextComponent copyCode = new TextComponent("§7[复制验证码]");
        copyCode.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, verificationCode));
        copyCode.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("复制验证码: " + verificationCode)));
        
        // 发送消息
        player.spigot().sendMessage(verifyLink);
        player.spigot().sendMessage(copyCode);
    }
    
    /**
     * 发送快速验证消息
     */
    public void sendQuickVerifyMessage(Player player, String verificationCode) {
        ConfigManager configManager = plugin.getConfigManager();
        List<String> joinMessages = configManager.getJoinMessages();
        
        // 发送基础消息
        for (String message : joinMessages) {
            message = message.replace("{code}", verificationCode);
            message = message.replace("{timeout}", String.valueOf(configManager.getVerificationTimeout()));
            player.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message));
        }
        
        // 发送验证链接
        sendVerificationLink(player, verificationCode);
    }
    
    /**
     * 发送验证成功消息
     */
    public void sendVerificationSuccessMessage(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        List<String> successMessages = configManager.getSuccessMessages();
        
        for (String message : successMessages) {
            player.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    /**
     * 发送验证失败消息
     */
    public void sendVerificationFailedMessage(Player player, int remainingAttempts) {
        ConfigManager configManager = plugin.getConfigManager();
        List<String> failedMessages = configManager.getFailedMessages();
        
        for (String message : failedMessages) {
            message = message.replace("{attempts}", String.valueOf(remainingAttempts));
            player.sendMessage(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    /**
     * 检查版本兼容性
     */
    private boolean isSpigotVersion() {
        try {
            Class.forName("net.md_5.bungee.api.chat.TextComponent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}