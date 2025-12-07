package cn.popcraft.verify.managers;

import cn.popcraft.verify.VerifyPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * GUI管理器
 * 作者: Popcraft
 * 
 * 负责:
 * - 规则书界面管理
 * - 验证界面管理
 * - 其他GUI功能
 */
public class GUIManager {
    
    private final VerifyPlugin plugin;
    
    public GUIManager(VerifyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 打开规则书界面
     */
    public void openRuleBookGUI(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        
        // 创建界面 (54格 = 6行)
        Inventory inventory = Bukkit.createInventory(null, 54, 
            ChatColor.translateAlternateColorCodes('&', configManager.getBookTitle()));
        
        // 获取配置的书页内容
        String[] pages = configManager.getBookPages().toArray(new String[0]);
        
        // 创建书页按钮
        for (int i = 0; i < Math.min(pages.length, 45); i++) {
            ItemStack pageItem = createPageItem(i + 1, pages[i]);
            inventory.setItem(i, pageItem);
        }
        
        // 创建关闭按钮
        ItemStack closeButton = createCloseButton();
        inventory.setItem(53, closeButton);
        
        // 打开界面
        player.openInventory(inventory);
        
        // 记录日志
        if (configManager.logVerifications()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 打开了规则书界面");
        }
    }
    
    /**
     * 创建书页物品
     */
    private ItemStack createPageItem(int pageNumber, String content) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        
        // 设置物品名称
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "§6第 " + pageNumber + " 页"));
        
        // 设置物品描述
        String[] lines = content.split("\n");
        if (lines.length > 0) {
            String firstLine = ChatColor.translateAlternateColorCodes('&', lines[0]);
            meta.setLore(Arrays.asList(
                "§7" + firstLine,
                "",
                "§e左键点击查看完整内容"
            ));
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 创建关闭按钮
     */
    private ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "§c关闭"));
        meta.setLore(Arrays.asList(
            "§7点击关闭此界面"
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 创建验证界面
     */
    public void openVerifyGUI(Player player, String verificationCode) {
        // 创建界面
        Inventory inventory = Bukkit.createInventory(null, 27, 
            ChatColor.translateAlternateColorCodes('&', "§l验证码验证"));
        
        // 创建验证码显示物品
        ItemStack codeDisplay = createCodeDisplay(verificationCode);
        inventory.setItem(13, codeDisplay);
        
        // 创建验证按钮
        ItemStack verifyButton = createVerifyButton();
        inventory.setItem(11, verifyButton);
        
        // 创建复制按钮
        ItemStack copyButton = createCopyButton(verificationCode);
        inventory.setItem(15, copyButton);
        
        // 打开界面
        player.openInventory(inventory);
        
        // 记录日志
        if (plugin.getConfigManager().logVerifications()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 打开了验证界面");
        }
    }
    
    /**
     * 创建验证码显示物品
     */
    private ItemStack createCodeDisplay(String verificationCode) {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
            "§b您的验证码"));
        meta.setLore(Arrays.asList(
            "§a" + verificationCode,
            "",
            "§7请在聊天框输入此验证码",
            "§7格式: /verify " + verificationCode
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 创建验证按钮
     */
    private ItemStack createVerifyButton() {
        ItemStack item = new ItemStack(Material.GREEN_TERRACOTTA);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "§a开始验证"));
        meta.setLore(Arrays.asList(
            "§7点击后请在聊天框",
            "§7输入验证码进行验证",
            "",
            "§e例如: /verify ABC123"
        ));
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 创建复制按钮
     */
    private ItemStack createCopyButton(String verificationCode) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "§7复制验证码"));
        meta.setLore(Arrays.asList(
            "§7点击将验证码复制到剪贴板",
            "",
            "§7验证码: §b" + verificationCode
        ));
        
        item.setItemMeta(meta);
        return item;
    }
}