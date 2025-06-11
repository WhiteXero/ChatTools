## Chinese (Simplified)
### v2.3.10
- 新增 西班牙语（es_es）翻译 @pilahito
- 优化 将去除聊天栏最大字符数限制的限制改为32767（防止意外情况）
- 优化 后台通知使用线程池
- 新增 部分功能过载熔断器
- 优化 自动响应现在会模拟从聊天栏发出，而不是直接发包
- 新增 指令 `/chattools config reload` 来重载配置

### v2.3.10.1
- 修复 模拟聊天窗口失败
- 新增 通过发送数据包来执行命令或发送文字

## English
### v2.3.10
- Added: Spanish(es_es) translations. @pilahito
- Optimization: Increase Chat Field Max Length will increase max value to 32767.
- Optimization: Use thread pools for toast notifiers.
- Added: Circuit Breaker for several features.
- Optimization: Responder now simulates sending text from Chat Screen, instead of just sending packets.
- Added: Command `/chattools config reload` to reload configurations.
## v2.3.10.1
- Fixed: Failed to simulate Chat Screen
- Added: Option "Use Data Packets For Sending Messages"
