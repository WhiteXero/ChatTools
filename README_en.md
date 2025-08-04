[>>Simplified Chinese<<](README.md)

**[>>FAQ<<](https://70centsapple.top/blogs/#/chat-tools-faq)**

# 📋 Overview
Chat Tools is a highly customizable Minecraft client mod that supports various features, allowing them to interoperate through commands and more.

# 📖 Glossary and Technical Details
- **Session Identifier** _(Chat Tools-specific term)_ is a string that, in single-player mode, corresponds to the current save name, and in multiplayer mode, corresponds to the server address. Related Regular Expressions use the `matches()` method for matching.
- **Pattern** _(General term)_ refers to the Regular Expression pattern. Chat Tools uses the MULTILINE mode for Regular Expressions and matches them using the `find()` method.
- Chat Tools maintains multi versions by using macro replacement. The same version of Chat Tools may exhibit slight differences between Minecraft versions.
- When processing player messages, Chat Tools removes color codes (§.).
- By enabling Advanced Tooltips (F3+H), you can hover over configuration items on the configuration page to view their keys, default values, and other details.

# 🛠️ Commands
- `/chattools on` - Enable the mod.
- `/chattools off` - Disable the mod.
- `/chattools opengui` - Open the configuration page.
- `/chattools download` - Download addons.
- `/chattools regex_checker <pattern> [<test_content>]` - Without test content: check if the pattern is a valid regex. With test content: validate the pattern and test if it matches the content.
- `/chattools send_to_client text <text_JSON_component>` - Send specific text to the client chat bar.
- `/chattools send_to_client actionbar <text_JSON_component>` - Display specific text on the client actionbar.
- `/chattools config openfile` - Open the configuration file (editing requires a reload to apply changes).
- `/chattools config reload` - Reload the configurations.
- `/chattools config get <key>` - Retrieve the value of a specific configuration key for the current session.
- `/chattools config set <key> <value> [<save>]` - Set the value of a specific configuration key. If save is set to true, the changes will also be saved to the file.
- `/chattools config toggle <key> [<save>]` - Toggle the state of a boolean-type configuration key. If save is set to true, the changes will also be saved to the file.

# ✨ Function introduction
For elaborate descriptions and examples, please see **[>>FAQ<<](https://70centsapple.top/blogs/#/chat-tools-faq)**.

<details>
<summary>General Section</summary>

## General Section
Contains the basic settings of Chat Tools
- Show Timestamp  
Inserts a timestamp in front of the message.  
![Timestamp](<images/Timestamp.png>)
- Restore Messages  
Restore messages from previous sessions.
- Nickname Hider  
Hides your real nickname in your own view.  
![Nickname Hider](<images/Nickname Hider.png>)
- Enable Chat History Navigator  
Use Ctrl + F to start navigating in chat!  
![Chat History Navigator](<images/Chat History Navigator.png>)
- Translator  
Press Shift + Tab in your chat bar to start translation.
- Max History Length  
Adjusts the maximum number of chat history kept in the game.  
![Max History Length](<images/Max History Length.png>)
</details>

<details>
<summary>Notifier Section</summary>

## Notifier Section
Various chat notifier features
- Toast Notification  
![Toast](<images/Toast.gif>)
- Sound  
Support custom sound effects.
- Actionbar  
Remind attention to messages in the action bar.
- Highlight  
Support custom highlighting prefix.  
![Highlight Function](<images/Highlight Function.png>)
- Allow List  
The contents of the list will be matched.
- Ban List  
The contents of the list will not be matched. (Its priority is greater than Allow List)
</details>

<details>
<summary>Formatter Section</summary>

## Formatter Section
Format your own messages using the specified pattern. You can apply different rules to different servers.
- Pattern  
Automatically format the replacement style.  
For example:  
`&e{text}` will make your message gold on servers that support a custom color prefix of &.  
`&e{text} ~(ovo)~` will additionally personalize your message with a suffix.  
`My coordinates are: {pos}` will automatically replace `{pos}` with the current coordinates for you.
- Auto-Disable when matches...  
In some cases, we **do not want** our text to be formatted.  
These situations include (but are not limited to):  
The number of items (or `all`) sent in chat when selling items to the Chest Shop;  
Commands that begin with various special characters.  
Chat Tools' default RegEx string `^\d+$|^[.#%$/].*|\ball\b` is all that is needed.  
Of course, you can change it or add more yourself.
</details>

<details>
<summary>Chat Keybindings Section</summary>

## Chat Keybindings Section
Use hotkeys to replace frequently used commands
- Trigger Last Command Hotkey  
Press the set hotkey to repeat your previous command.  
For example:  
While playing a Parkour map, do F3+C and activate it once. Then you can quickly go back to the recorded point location every time you press the hotkey.
- Command Keybindings  
Set hotkeys for frequently used commands.  
![Command Keybindings](<images/Command Keybindings.png>)
</details>

<details>
<summary>Bubble Section</summary>

## Bubble Section
- Enable Chat Bubbles  
Renders a chat bubble over one's head.  
![Chat Bubbles](<images/Chat Bubbles.png>)
- Bubble Rules  
Apply different bubble rules on different servers.  
![Bubble Rules](<images/Bubble Rules.png>)
</details>

<details>
<summary>Responder Section</summary>

## Responder Section
> **Warning: If you are going to use this feature in a server, please inquire with other players and admins first!**
- Enable Responder  
Response to other's messages automatically.
- Responder Rules  
Apply different responder rules on different servers.
</details>

# 🔗 Credits
- [ChatToolsAddon](https://github.com/litwak913/ChatToolsAddon): The Toast notification external program implementation of Chat Tools.