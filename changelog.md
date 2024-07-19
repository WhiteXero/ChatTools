## Chinese (Simplified)
- 优化 移除了聊天记录搜索框字符限制
- 新增 调试用指令`/chattools send_to_client`，用途是往当前客户端发送一条客户端消息
- 优化 注入聊天自动禁用聊天注入列表、自动响应规则正则表达式字段现在使用多行匹配模式
- 新增 响应被过滤的消息选项
- 新增 移除聊天长度限制选项
- 更改 在描述里新增了模组贡献者昵称
- 修复 预览点击事件功能（现在不再是实验性功能了）
- 优化 部分基本聊天提醒功能单独调用线程
- 修复 聊天气泡在 Minecraft 1.21 没有正确渲染的问题

## English
- Optimization: Raised Chat History Navigator search box character limit
- Added: New `/chattools send_to_client` command is used to send a client-side message to the current instance.
- Optimization: `formatter.DisableOnMatchList` and `responder.List.Pattern` RegEx patterns now use Multiline mode to match.
- Added: Option `responder.RespondToFilteredMessages`.
- Added: Option `general.IncreaseChatFieldMaxLength`.
- Changed: Add new contributor nicknames in the description. Thanks to you all!
- Fixed: Preview Click Events is now functioning well and no longer an experimental feature.
- Optimization: Start new threads for several basic notifiers.
- Fixed: Bubbles didn't render correctly in Minecraft 1.21
