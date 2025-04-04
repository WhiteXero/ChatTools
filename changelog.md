## Chinese (Simplified)
### v2.3.9
- 更新 1.21.5
- 修复 独占聊天栏现在使用线程安全的列表
- 优化 `/chattools config` 相关命令提示
- 新增 聊天提醒“高亮前缀插入到时间戳前”选项
- 修复 昵称隐藏处理失败时会被踢出世界的问题
- 新增 聊天注入渐变语法
- 新增 覆盖聊天颜色样式

### v2.3.9.1
- 优化 聊天注入渐变语法处理空格和 emoji 的情况
- 优化 去除聊天栏最大字符数限制选项现在也会去除发包的大小限制

## English
### v2.3.9
- Update: Bumped to 1.21.5
- Fixed: Exclusive Actionbar is now more thread-safe.
- Optimization: `/chattools config` command now suggested config keys.
- Added: Highlight Prefixes `Insert Before Timestamps` option.
- Fixed: Players would disconnect if the nick-hider failed to work.
- Added: Formatter Gradient syntax
- Added: Override Chat Color option

### v2.3.9.1
- Optimization: Formatter Gradient syntax now deals with whitespaces and emojis better.
- Optimization: IncreaseChatFieldMaxLength now increases packet size as well.