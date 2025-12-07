package cn.popcraft.verify.managers;

import cn.popcraft.verify.VerifyPlugin;
import org.bukkit.entity.Player;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证管理器
 * 作者: Popcraft
 * 
 * 负责:
 * - 验证码生成
 * - 验证状态管理
 * - 验证超时处理
 */
public class VerificationManager {
    
    private final VerifyPlugin plugin;
    private final Map<UUID, PlayerVerification> playerVerifications;
    private final SecureRandom random;
    
    // 验证状态枚举
    public enum VerificationStatus {
        PENDING,    // 等待验证
        SUCCESS,    // 验证成功
        FAILED,     // 验证失败
        TIMEOUT,    // 验证超时
        KICKED      // 已被踢出
    }
    
    /**
     * 玩家验证信息类
     */
    public static class PlayerVerification {
        private final UUID playerId;
        private final String verificationCode;
        private final long startTime;
        private final int maxAttempts;
        private int attempts;
        private VerificationStatus status;
        private final List<String> pendingCommands;
        
        public PlayerVerification(UUID playerId, String verificationCode, int maxAttempts) {
            this.playerId = playerId;
            this.verificationCode = verificationCode;
            this.startTime = System.currentTimeMillis();
            this.maxAttempts = maxAttempts;
            this.attempts = 0;
            this.status = VerificationStatus.PENDING;
            this.pendingCommands = new ArrayList<>();
        }
        
        // Getters
        public UUID getPlayerId() { return playerId; }
        public String getVerificationCode() { return verificationCode; }
        public long getStartTime() { return startTime; }
        public int getAttempts() { return attempts; }
        public int getMaxAttempts() { return maxAttempts; }
        public VerificationStatus getStatus() { return status; }
        public List<String> getPendingCommands() { return pendingCommands; }
        
        // Methods
        public void incrementAttempts() { this.attempts++; }
        public void setStatus(VerificationStatus status) { this.status = status; }
        public void addPendingCommand(String command) { this.pendingCommands.add(command); }
        
        /**
         * 检查是否超时
         */
        public boolean isTimeout(ConfigManager configManager) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long timeoutMs = configManager.getVerificationTimeout() * 1000L;
            return elapsedTime > timeoutMs;
        }
        
        /**
         * 检查是否可以继续尝试
         */
        public boolean canRetry(ConfigManager configManager) {
            if (isTimeout(configManager)) {
                setStatus(VerificationStatus.TIMEOUT);
                return false;
            }
            return attempts < maxAttempts;
        }
        
        /**
         * 检查验证是否通过
         */
        public boolean verifyCode(String inputCode) {
            if (status != VerificationStatus.PENDING) {
                return false;
            }
            
            if (verificationCode.equalsIgnoreCase(inputCode.trim())) {
                setStatus(VerificationStatus.SUCCESS);
                return true;
            } else {
                incrementAttempts();
                if (attempts >= maxAttempts) {
                    setStatus(VerificationStatus.FAILED);
                }
                return false;
            }
        }
        
        /**
         * 获取剩余时间(秒)
         */
        public long getRemainingTime(ConfigManager configManager) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long timeoutMs = configManager.getVerificationTimeout() * 1000L;
            long remainingMs = timeoutMs - elapsedTime;
            return Math.max(0, remainingMs / 1000);
        }
    }
    
    public VerificationManager(VerifyPlugin plugin) {
        this.plugin = plugin;
        this.playerVerifications = new ConcurrentHashMap<>();
        this.random = new SecureRandom();
        
        // 启动超时检查任务
        startTimeoutCheckTask();
    }
    
    /**
     * 为玩家创建验证
     */
    public PlayerVerification createVerification(Player player) {
        String verificationCode = generateVerificationCode();
        PlayerVerification verification = new PlayerVerification(
            player.getUniqueId(),
            verificationCode,
            plugin.getConfigManager().getMaxAttempts()
        );
        
        playerVerifications.put(player.getUniqueId(), verification);
        
        // 记录日志
        if (plugin.getConfigManager().logVerifications()) {
            plugin.getLogger().info("为玩家 " + player.getName() + " 创建验证，验证码: " + verificationCode);
        }
        
        return verification;
    }
    
    /**
     * 获取玩家的验证信息
     */
    public PlayerVerification getVerification(Player player) {
        return playerVerifications.get(player.getUniqueId());
    }
    
    /**
     * 获取玩家的验证信息(通过UUID)
     */
    public PlayerVerification getVerification(UUID playerId) {
        return playerVerifications.get(playerId);
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(Player player, String inputCode) {
        PlayerVerification verification = getVerification(player);
        if (verification == null) {
            return false;
        }
        
        // 检查是否超时或已失败
        if (!verification.canRetry(plugin.getConfigManager())) {
            return false;
        }
        
        boolean result = verification.verifyCode(inputCode);
        
        // 记录验证结果
        if (plugin.getConfigManager().logVerifications()) {
            plugin.getLogger().info("玩家 " + player.getName() + 
                (result ? " 验证成功" : " 验证失败 (尝试 " + verification.getAttempts() + "/" + verification.getMaxAttempts() + ")"));
        }
        
        // 如果验证成功，执行待执行命令
        if (result) {
            executePendingCommands(player, verification);
        }
        
        // 如果验证失败且次数已满，踢出玩家
        if (!result && verification.getStatus() == VerificationStatus.FAILED) {
            kickPlayerForFailedVerification(player);
        }
        
        return result;
    }
    
    /**
     * 移除玩家的验证信息
     */
    public void removeVerification(Player player) {
        playerVerifications.remove(player.getUniqueId());
    }
    
    /**
     * 移除玩家的验证信息(通过UUID)
     */
    public void removeVerification(UUID playerId) {
        playerVerifications.remove(playerId);
    }
    
    /**
     * 检查玩家是否已验证
     */
    public boolean isVerified(Player player) {
        PlayerVerification verification = getVerification(player);
        return verification != null && verification.getStatus() == VerificationStatus.SUCCESS;
    }
    
    /**
     * 生成验证码
     */
    private String generateVerificationCode() {
        ConfigManager configManager = plugin.getConfigManager();
        String type = configManager.getVerificationType();
        
        if ("CUSTOM".equalsIgnoreCase(type)) {
            return configManager.getCustomVerificationCode();
        } else {
            // 随机生成验证码
            StringBuilder code = new StringBuilder();
            String characters = configManager.getVerificationCharacters();
            int length = configManager.getRandomCodeLength();
            
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characters.length());
                code.append(characters.charAt(index));
            }
            
            return code.toString();
        }
    }
    
    /**
     * 执行待执行命令
     */
    private void executePendingCommands(Player player, PlayerVerification verification) {
        if (!verification.getPendingCommands().isEmpty()) {
            for (String command : verification.getPendingCommands()) {
                String processedCommand = command.replace("{player}", player.getName());
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), processedCommand);
            }
        }
    }
    
    /**
     * 踢出验证失败的玩家
     */
    private void kickPlayerForFailedVerification(Player player) {
        ConfigManager configManager = plugin.getConfigManager();
        String kickMessage = configManager.getKickMessage();
        
        // 设置验证状态为已踢出
        PlayerVerification verification = getVerification(player);
        if (verification != null) {
            verification.setStatus(VerificationStatus.KICKED);
        }
        
        player.kickPlayer(kickMessage);
    }
    
    /**
     * 启动超时检查任务
     */
    private void startTimeoutCheckTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, PlayerVerification>> iterator = playerVerifications.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<UUID, PlayerVerification> entry = iterator.next();
                PlayerVerification verification = entry.getValue();
                
                if (verification.isTimeout(plugin.getConfigManager())) {
                    Player player = plugin.getServer().getPlayer(verification.getPlayerId());
                    if (player != null && player.isOnline()) {
                        verification.setStatus(VerificationStatus.TIMEOUT);
                        
                        // 显示超时消息
                        List<String> timeoutMessages = plugin.getConfigManager().getTimeoutMessages();
                        for (String message : timeoutMessages) {
                            player.sendMessage(message);
                        }
                        
                        // 踢出玩家
                        kickPlayerForFailedVerification(player);
                    }
                    
                    iterator.remove();
                }
            }
        }, 20L, 20L); // 每秒检查一次
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        playerVerifications.clear();
    }
    
    /**
     * 获取所有验证中的玩家数量
     */
    public int getPendingVerificationsCount() {
        return (int) playerVerifications.values().stream()
                .filter(v -> v.getStatus() == VerificationStatus.PENDING)
                .count();
    }
    
    /**
     * 获取已验证玩家数量
     */
    public int getVerifiedPlayers() {
        return (int) playerVerifications.values().stream()
            .filter(v -> v.getStatus() == VerificationStatus.SUCCESS)
            .count();
    }
}