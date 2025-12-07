package cn.popcraft.verify.events;

import cn.popcraft.verify.VerifyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI事件监听器
 * 作者: Popcraft
 * 
 * 负责:
 * - 处理GUI点击事件
 * - 处理界面关闭事件
 */
public class GUIClickListener implements Listener {
    
    private final VerifyPlugin plugin;
    
    public GUIClickListener(VerifyPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 处理界面点击事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Inventory clickedInventory = event.getClickedInventory();
            
            if (clickedInventory == null) {
                return;
            }
            
            // 检查界面是否有标题（Bukkit API兼容）
            String title = event.getView().getTitle();
            
            // 处理规则书界面点击
            if (title.contains("服务器规则")) {
                handleRuleBookClick(player, event);
            }
            
            // 处理验证界面点击
            else if (title.contains("验证码验证")) {
                handleVerifyClick(player, event);
            }
        }
    }
    
    /**
     * 处理规则书界面点击
     */
    private void handleRuleBookClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true); // 取消默认行为
        
        if (event.getCurrentItem() == null) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        String itemName = clickedItem.getItemMeta().getDisplayName();
        
        // 处理关闭按钮
        if (itemName.contains("关闭") || itemName.contains("§c关闭")) {
            player.closeInventory();
        }
        
        // 处理书页点击
        else if (itemName.contains("第") && itemName.contains("页")) {
            // 可以在这里添加显示具体书页内容的逻辑
            player.sendMessage("§7点击了书页: " + itemName);
        }
    }
    
    /**
     * 处理验证界面点击
     */
    private void handleVerifyClick(Player player, InventoryClickEvent event) {
        event.setCancelled(true); // 取消默认行为
        
        if (event.getCurrentItem() == null) {
            return;
        }
        
        ItemStack clickedItem = event.getCurrentItem();
        String itemName = clickedItem.getItemMeta().getDisplayName();
        
        // 处理验证按钮
        if (itemName.contains("开始验证") || itemName.contains("§a开始验证")) {
            player.sendMessage("§7请在聊天框输入验证码进行验证");
            player.sendMessage("§7例如: /verify ABC123");
        }
        
        // 处理复制按钮
        else if (itemName.contains("复制验证码") || itemName.contains("§7复制验证码")) {
            // 获取验证码（这里简化处理，实际中需要存储）
            player.sendMessage("§7请从界面中查看您的验证码");
        }
        
        // 处理关闭（点击界面外的区域）
        else if (event.getRawSlot() == -999) {
            player.closeInventory();
        }
    }
    
    /**
     * 处理界面关闭事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            String title = event.getView().getTitle();
            
            // 记录关闭事件
            if (title.contains("服务器规则")) {
                if (plugin.getConfigManager().logVerifications()) {
                    plugin.getLogger().info("玩家 " + player.getName() + " 关闭了规则书界面");
                }
            } else if (title.contains("验证码验证")) {
                if (plugin.getConfigManager().logVerifications()) {
                    plugin.getLogger().info("玩家 " + player.getName() + " 关闭了验证界面");
                }
            }
        }
    }
}