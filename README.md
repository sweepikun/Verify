# Verify 插件

**作者**: Popcraft  
**版本**: 1.0.0  
**支持版本**: Spigot 1.19.3  
**Java版本**: Java 17  

## 功能介绍

Verify 是一个 Minecraft Spigot 插件，提供玩家验证码验证和规则书展示功能，帮助服务器管理者维护服务器安全和向玩家展示规则。

### 主要功能

1. **验证码验证系统**
   - 支持随机验证码和自定义验证码
   - 可配置验证超时时间
   - 验证失败次数限制
   - 验证失败自动踢出

2. **规则书展示系统**
   - 自动向验证成功玩家展示规则书
   - 可配置书内容和样式
   - 支持个性化欢迎信息

3. **灵活配置系统**
   - 完全可自定义的消息
   - 可配置的验证规则
   - 支持奖励系统

## 安装方法

1. 将 `Verify-1.0.0.jar` 文件放入服务器的 `plugins` 文件夹
2. 重启服务器或使用 `/reload` 命令
3. 插件会自动创建配置文件

## 配置说明

### 验证设置 (verification)

```yaml
verification:
  enabled: true                    # 是否启用验证功能
  type: RANDOM                     # 验证类型: RANDOM(随机) 或 CUSTOM(自定义)
  custom-code: "WELCOME2024"       # 自定义验证码(当type为CUSTOM时使用)
  random-length: 6                 # 随机验证码长度
  characters: "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"  # 验证码字符集
  timeout: 300                     # 验证超时时间(秒)
  max-attempts: 3                  # 最大重试次数
  kick-message: "§c验证码验证失败！" # 验证失败踢出消息
```

### 规则书设置 (book)

```yaml
book:
  enabled: true                    # 是否启用规则书功能
  auto-open: true                  # 是否在验证成功后自动打开规则书
  title: "§l服务器规则"             # 书标题
  author: "§6管理员"                # 作者
  pages:                           # 书页内容
    - |
      §l§a欢迎来到我们的服务器！
      ...
```

### 消息设置 (messages)

```yaml
messages:
  join-message:                    # 玩家加入时的验证消息
    - "§a=== 欢迎加入服务器 ==="
    - "§7您的验证码是: §b{code}"
  success-message:                 # 验证成功消息
    - "§a✅ 验证成功！欢迎来到服务器！"
  failed-message:                  # 验证失败消息
    - "§c❌ 验证失败！"
    - "§7剩余尝试次数: §e{attempts}"
  timeout-message:                 # 验证超时消息
    - "§c⏰ 验证超时！"
```

## 使用方法

### 玩家命令

- `/verify <验证码>` - 输入验证码进行验证
- `/verify help` - 查看帮助信息

### 管理员命令

- `/verify status` - 查看验证状态统计
- `/verify reload` - 重载插件配置
- `/verify book` - 手动发送规则书给自己

### 权限节点

- `verify.admin` - 管理员权限
- `verify.reload` - 重载插件配置权限
- `verify.bypass` - 跳过验证权限

## 玩家流程

1. **玩家加入服务器**
   - 系统自动生成验证码
   - 发送验证消息给玩家

2. **玩家验证**
   - 在聊天框输入 `/verify <验证码>`
   - 或点击聊天栏中的验证链接

3. **验证成功**
   - 显示成功消息
   - 自动展示规则书
   - 发放奖励（如果配置）

4. **验证失败**
   - 显示失败消息
   - 扣除一次尝试次数
   - 尝试次数用完后被踢出

## 自定义规则书

在配置文件的 `book.pages` 部分可以自定义书的内容：

```yaml
book:
  pages:
    - |
      §l§a欢迎页面内容
      §r§7这里是第一页的内容
      §r§7支持多行文本和颜色代码
      
    - |
      §l§6规则页面内容
      §r§7这里是第二页的内容
      §r§7可以添加服务器规则等信息
      
    - |
      §l§d指南页面内容
      §r§7这里是第三页的内容
      §r§7可以添加游戏指南等
```

### 颜色代码说明

- `§a` - 绿色
- `§b` - 青色
- `§c` - 红色
- `§d` - 粉色
- `§e` - 黄色
- `§f` - 白色
- `§7` - 灰色
- `§8` - 深灰色
- `§9` - 蓝色
- `§l` - 粗体
- `§o` - 斜体
- `§n` - 下划线

## 高级配置

### 奖励系统

```yaml
settings:
  give-rewards: true
  reward-commands:
    - "give {player} minecraft:stone 64"
    - "eco give {player} 100"
    - "give {player} minecraft:wooden_sword 1"
```

### 日志设置

```yaml
settings:
  log-verifications: true      # 记录验证日志
  debug: false                # 调试模式
```

## 故障排除

### 常见问题

1. **插件无法加载**
   - 检查 Java 版本是否为 17 或更高
   - 检查 Spigot 版本是否为 1.19.3
   - 查看服务器控制台错误信息

2. **验证码不显示**
   - 检查 `verification.enabled` 是否为 `true`
   - 查看配置文件是否正确

3. **规则书无法打开**
   - 检查 `book.enabled` 是否为 `true`
   - 确认玩家背包有空间

### 调试模式

启用调试模式查看详细日志：

```yaml
settings:
  debug: true
```

## 更新日志

### v1.0.0 (2025-12-07)
- 初始版本发布
- 实现验证码验证功能
- 实现规则书展示功能
- 支持自定义配置
- 添加管理员命令
- 支持奖励系统

## 技术支持

如有问题请联系插件作者 Popcraft。

## 开源协议

本插件遵循 MIT 开源协议。