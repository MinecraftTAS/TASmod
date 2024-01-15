> [!WARNING]
> This mod is in **early-development** and is not ready for use yet. If you wish to download the early testing releases, be prepared to encounter various bugs and desyncs!  
> For a more stable mod, without input recording and playback, check [LoTAS](https://github.com/MinecraftTAS/LoTAS)
# TASmod  
A Tool-Assisted Speedrun-Mod for Minecraft 1.12.2.  
Is able to capture inputs and play them back, by pretending to be a keyboard.

To reduce complexity, all aspects of RNG-Manipulation have been externalized to a repository called [KillTheRNG](https://github.com/MinecraftTAS/KillTheRNG). It is now available as an optional dependency and a [standalone mod](https://maven.mgnet.work/#/main/com/minecrafttas/killtherng-full)

> [!IMPORTANT]
> We are currently shifting over to **[Legacy-Fabric 1.12.2](https://legacyfabric.net/)**. The development branch currently works with Legacy-Fabric while the release branch is on **Forge** 1.12.2
>
> This means all **Alpha** releases only work for Forge.
>
> Future **Beta** releases will be on Legacy-Fabric.

# Credits  
Author of the original mod: [tr7zw](https://github.com/tr7zw/MC-TASmod)  
Main Mod Author: Scribble  
  
Contributions by: famous1622, Pancake  
  
Tickratechanger: [Guichaguri](https://github.com/Guichaguri/TickrateChanger)  
Tickrate 0 idea: [Cubitect](https://github.com/Cubitect/Cubitick)  
Savestate idea: [bspkrs, MightyPork](https://github.com/bspkrs-mods/WorldStateCheckpoints), although implementation is totally different now
  
Special thanks: Darkmoon, The Minecraft TAS Community  
# Features  
## Playback
This mod can record and play back
- The entire keyboard, minus TASmod keybinds (see below).
- Gui-Screens like crafting, the pause menu and the main menu! (Except the keybinding screen)
- Any screen size and window size (No warranties here...)

### Commands
`/record` - Start a recording. This will record inputs to RAM. Closing the game will empty these inputs.  
`/play` - Start to play back the stored inputs.  
`/save <filename>` - Save stored inputs to a file.  
`/load <filename>` - Load inputs from file.  
`/clearinputs` - Delete all stored inputs, use this before starting a brand new recording.  
`/record` - will resume the recording and not clear the inputs first.  
`/playuntil <tickCount>` - Stops the next playback at the specified tick number, then switches to a recording. Run this command then start a playback via `/play`.

`/fullrecord, /fullplay` - Same as record/play however it will quit to the main menu first.  
`/restartandplay <filename>` - Quits Minecraft completely. When restarting, the specified file will be loaded and played back, when the menu appears.
### Keybinds
<kbd>F10</kbd> - Stops either a playback or a recording.  

## Savestate
### Commands
`/savestate` - Prints a full guide to the savestate command in chat.
### Keybinds
<kbd>J</kbd> - Make a new savestate.  
<kbd>K</kbd> - Load the most recent savestate.

## Tickratechanger (Slowdown)
### Commands
`/tickrate <ticks/second>` - Changes the game speed. Default is 20, anthing below will slow the game down, anything above will speed it up.
### Keybinds
<kbd>F8</kbd> - Enter "Tickrate 0". The game is paused but you can still look around.  
<kbd>F9</kbd> - While in tickrate 0, advance the tick by 1. By holding keyboardkeys, you can make inputs while tickadvancing.

## Multiplayer support
**Important:** This is **NOT** a clientside mod, a server side installation is required a.k.a This doesn't work on Hypixel, 2b2t etc. These servers will **NEVER** be supported.

Record TASes with friends! Needs operator permissions to run tasmod related commands.  
/savestate can be used to manage savestates.

> **Note:** /fullrecord, /fullplay and /restartandplay are not guaranteed to work in multiplayer at this time.

## HUD
When ingame, hitting <kbd>F6</kbd> will show you options for customising your HUD, with monitoring options and more. Even more options are available when KillTheRNG is installed. 

# Development
## Setup
1. Clone this repository and put it in your workspace directory (where you open eclipse or idea)
2. Import gradle project
	- Use gradle version 4.10.3
	- Use [JDK 8](https://adoptium.net/en/temurin/releases/?version=8)
3. Run gradle tasks `setupDecompWorkspace` then `eclipse`.
4. *Optional but recommended:* Run gradle task `downloadKTRNG` (in the "tasmod" category) to download KillTheRNG to the run/mods folder.

## Running
The task `eclipse` should've generated to launch configs: `TASmod_Client.launch` and `TASmod_Server.launch`. Select it, then click the run or debug button in your IDE.

> **Note:** Additional setup is required for the server to actually start, like changing the eula.txt and setting `online-mode` to false in server.properties.

## Building
Build the mod using the gradle task `build` (or alternatively `shadowJar`).
