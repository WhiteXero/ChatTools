## Chinese (Simplified)
### v2.3.8
- 修复 `{pos}` 在自动响应中被错误地解析成组名称的问题
- 优化 在聊天记录搜索屏幕默认快速滚动，按住 Shift 滚动一行
- 新增 聊天提醒“高亮前缀插入到时间戳前”选项
- 新增 聊天提醒“从相机位置播放音效”选项
- 修复 从复制菜单返回聊天记录搜索菜单时，若消息已过时则抛出空指针错误的问题
- 新增 `/chatools send_to_client actionbar` 命令现在可以接受持续时间（以毫秒为单位）参数（仅在独占动作栏启用时生效）

## English
### v2.3.8
- Fixed: `{pos}` was incorrectly parsed as a group name in Responder.
- Optimization: Scroll faster on Chat History Navigator screen by default, while you can hold Shift to scroll by one line.
- Added: Option `Notifier` - `Highlight` - `Insert Before Timestamps`.
- Added: Option `Notifier` - `Sound` - `Play Sound From Camera Position`.
- Fixed: NullPointerException was thrown when returning to Chat History Navigator from Copy Screen if the message is outdated.
- Added: `/chatools send_to_client actionbar` command now accepts a duration (in milliseconds) parameter (will only work if exclusive actionbar is enabled).