<p align="center"><img src="https://minecrafttas.com/images/TASmodLogo.svg" alt="Logo" width="500"></p>

A [Tool-Assisted Speedrun](https://tasvideos.org/WelcomeToTASVideos) mod for Minecraft.  
Adds tools such as input playback, slowdown and savestates for creating TASes,  
which aim to beat the game as fast as possible.

> [!Warning]
This port project is AI slop!!

# Installation
This mod uses the modloader [LegacyFabric](https://legacyfabric.net/), a fork of [Fabric](https://fabricmc.net/) for versions below 1.14.4.  
**Does NOT need LegacyFabric-API**
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

`/filecommand <filecommandname>` - Enables/Disables certain FileCommands in the TASfile, which are special lines in the TASfile that will trigger actions, when the playback reaches over that point.  
`/folder <tasfiles|savestates>` - Opens the folder for tasfiles or savestates in the file explorer
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
<kbd>.</kbd> - Increases the tickrate in steps  
<kbd>,</kbd> - Decreases the tickrate in steps  
<kbd>F8</kbd> - Enter "Tickrate 0". The game is paused but you can still look around.  
<kbd>F9</kbd> - While in tickrate 0, advance the tick by 1. By holding keyboardkeys, you can make inputs while tickadvancing.

## Multiplayer support
**Important:** This is **NOT** a clientside mod, a server side installation is required a.k.a This doesn't work on Hypixel, 2b2t etc. These servers will **NEVER** be supported.

Record TASes with friends! Needs operator permissions to run tasmod related commands.  
/savestate can be used to manage savestates.

> [!Note]
> /fullrecord, /fullplay and /restartandplay are not guaranteed to work in multiplayer at this time.

## HUD
When ingame, hitting <kbd>F6</kbd> will show you options for customising your HUD, with monitoring options and more. Even more options are available when KillTheRNG is installed. 

# Credits  
Author of the original prototype: [tr7zw](https://github.com/tr7zw/MC-TASmod)  
Main Mod Author: Scribble  

Contributions by: cittyinthecloud, forester302, Pancake
  
Initial Tickratechanger: [Guichaguri](https://github.com/Guichaguri/TickrateChanger)  
Tickrate 0 idea: [Cubitect](https://github.com/Cubitect/Cubitick)  
Savestate idea: [bspkrs, MightyPork](https://github.com/bspkrs-mods/WorldStateCheckpoints)

Special thanks: Darkmoon, notz23, The Minecraft TAS Community  

# Development
For setting up and building the mod, check [CONTRIBUTING.md](https://github.com/MinecraftTAS/TASmod/blob/develop/CONTRIBUTING.md)