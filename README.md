[>>English<<](README_en.md)

**[>>常见问题解答<<](https://70centsapple.top/blogs/#/chat-tools-faq-zh-cn)**

# 📋 概述
Chat Tools 是一个可以高度自定义各种功能的 Minecraft 客户端 mod，并且各功能间可以通过命令等方式联动。

# 📖 专有名词解释以及一些技术上的细节
- **会话标识符**_（本 mod 术语）_ 是这样一个字符串：在单人模式下为当前存档名称，玩家在多人模式下为所连接服务器地址。相关正则表达式调用 `matches()` 方法匹配。
- 正则表达式**模式（串）**_（通用术语）_ 指的是 pattern。相关正则表达式采用 MULTILINE 模式，调用 `find()` 方法匹配。
- Chat Tools 使用宏替换的方式维护多版本，在不同的 Minecraft 版本间，同一个 Chat Tools 版本号的 mod 表现也或许有细微差异。
- 此 mod 处理玩家信息时会将颜色代码（§.）去除。
- 打开高级提示框（F3+H）后，在配置页面将光标悬停在各配置项上可以看见其配置键名称、默认值等信息。

# 🛠️ 指令
- `/chattools on` - 启用 mod
- `/chattools off` - 禁用 mod
- `/chattools opengui` - 打开配置页面
- `/chattools download` - 下载 addons 附件
- `/chattools regex_checker <模式串> [<测试内容>]` - 若无测试内容：测试模式串是否符合正则表达式规则；若有测试内容：测试模式串是否符合正则表达式规则，并测试测试内容能否被模式串匹配。
- `/chattools send_to_client text <文本JSON组件>` - 将特定文本发送至客户端聊天栏
- `/chattools send_to_client actionbar <文本JSON组件>` - 将特定文本显示在客户端动作栏
- `/chattools config openfile` - 打开配置文件（编辑后需要重载配置）
- `/chattools config reload` - 重载配置
- `/chattools config get <键名>` - 获取当前实例某配置键的值
- `/chattools config set <键名> <值> [<保存>]` - 设置当前实例某配置键的值，如果保存为 true，则一并保存到文件
- `/chattools config toggle <键名> [<保存>]` - 切换当前实例某布尔类型配置键的启用状态，如果保存为 true，则一并保存到文件
- 
# ✨ 功能介绍
详细的介绍及示例请参阅 **[>>常见问题解答<<](https://70centsapple.top/blogs/#/chat-tools-faq-zh-cn)**

<details>
<summary>基本（General Section）</summary>

## 基本（General Section）
包含模组基本设定
- 显示聊天时间（Show Timestamp）  
在信息前面插入一个时间戳  
![Timestamp](<images/Timestamp.png>)
- 储存聊天记录（Restore Messages）  
在切换会话之后从上一个会话储存聊天记录
- 隐藏自己昵称（Nickname Hider）  
在自己视角里隐藏自己真实昵称  
![Nickname Hider](<images/Nickname Hider.png>)
- 启用聊天搜索（Enable Chat History Navigator）  
在聊天栏中按下 Ctrl + F 搜索聊天历史记录  
![Chat History Navigator](<images/Chat History Navigator.png>)
- 翻译器（Translator）  
在聊天栏里按 Shift + Tab 开始翻译
- 最大聊天记录数量（Max History Length）  
调整游戏保留聊天记录上限  
![Max History Length](<images/Max History Length.png>)
</details>

<details>
<summary>聊天提醒（Notifier Section）</summary>

## 聊天提醒（Notifier Section）
各种聊天提醒功能
- 后台弹窗提醒（Toast）  
![Toast](<images/Toast.gif>)
- 声音选项（Sound）  
支持自定义音效
- 动作栏选项（Actionbar）  
在动作栏提醒关注的消息
- 高亮选项（Highlight）  
支持自定义高亮前缀（匹配到的消息前面加前缀）  
![Highlight Function](<images/Highlight Function.png>)
- 匹配白名单（Allow List）  
列表中的内容将会被匹配
- 匹配黑名单（Ban List）  
列表中的内容将不会被匹配（优先级大于白名单列表）
</details>

<details>
<summary>注入聊天（Formatter Section）</summary>

## 注入聊天（Formatter Section）
使用指定样式格式化自己的消息，可以根据不同服务器应用不同的规则
- 注入文本（Pattern）  
即自动格式化替换的样式  
例如：  
`&e{text}` 在支持以 & 作为自定义颜色前缀的服务器中将会让您的消息变成金色  
`&e{text} ~(ovo)~` 将额外为您加上个性化后缀（小尾巴）  
`我的坐标是：{pos}` 将为您自动替换 `{pos}` 为当前坐标
- 匹配黑名单（Auto-Disable when matches...）  
在有些情况下，我们**不希望**自己的文本被格式化。  
这些情况包括（但不限于）：  
向箱子商店插件售卖物品时在聊天栏输入的物品数量（或all）；  
以各种特殊字符开头的指令。  
Chat Tools的默认正则表达式字串 `^\d+$|^[.#%$/].*|\ball\b` 即可满足需求，  
当然，您也可以更改或自行添加更多。
</details>

<details>
<summary>快捷发言（Chat Keybindings Section）</summary>

## 快捷发言（Chat Keybindings Section）
用快捷键来代替常用的指令
- 一键复读（Trigger Last Command Hotkey）  
按下设置的热键即可将您上一条指令重复一遍  
例如：  
在跑酷地图中 F3+C 记录坐标并发送一次后，此后每按下一次快捷键即可快速回溯到记录点位置。
- 指令宏（Command Keybindings）  
为常用指令设置热键  
![Command Keybindings](<images/Command Keybindings.png>)
</details>

<details>
<summary>聊天气泡（Bubble Section）</summary>

## 聊天气泡（Bubble Section）
- 启用聊天气泡（Enable Chat Bubbles）  
在玩家头上渲染聊天气泡  
![Chat Bubbles](<images/Chat Bubbles.png>)
- 聊天气泡规则（Bubble Rules）  
为不同的服务器应用不同的聊天气泡规则  
![Bubble Rules](<images/Bubble Rules.png>)
</details>

<details>
<summary>聊天回应（Responder Section）</summary>

## 聊天回应（Responder Section）
> **注意：在服务器中使用此功能前，请先咨询他人及管理员的意见！**
- 启用聊天回应（Enable Responder）  
聊天回应功能允许对于特定的聊天内容自动回复特定的信息
- 聊天回应规则列表（Responder Rules）  
在不同的服务器应用不同的聊天回应规则
</details>

# 🔗 致谢
- [ChatToolsAddon](https://github.com/litwak913/ChatToolsAddon)：该项目的 Toast 通知外部程序实现