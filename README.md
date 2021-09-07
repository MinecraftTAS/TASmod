# TASmod  
Lets you record and play back tickperfect inputs.  
  
This mod is currently only for Forge 1.12.2 and will update/downgrade once 1.12.2 works properly  

## Credits  
Author of the original mod: tr7zw  
Author: ScribbleLP  
  
Contributions by: Famous1622, Pancake (MCPfannkuchenYT)  
  
Tickratechanger: [Guichaguri](https://github.com/Guichaguri/TickrateChanger)  
Tickrate 0 idea: [Cubitect](https://github.com/Cubitect/Cubitick)  
Savestate idea: [bspkrs, MightyPork](https://github.com/bspkrs-mods/WorldStateCheckpoints), although implementation is totally different now  
  
RNG Math: [Admiral_Stapler](https://www.youtube.com/channel/UCB4XuRBJZBOpnoJSWekMohw)  
  
Special thanks: Darkmoon
## Commands  
`/record` Starts to record your input, hit the same command again to stop recording  
`/play` Starts to  play back a the stored inputs  
`/save <filename>` Saves stored inputs to a file  
`/load <filename>` Load inputs from file  
`/clearinputs` Delete all stored inputs, use this before starting a brand new recording. `/record` will resume the recording and not clear the inputs first.  
  
## Development
To load mixins in eclipse use `--mixin mixins.tasmod.json --tweakClass org.spongepowered.asm.launch.MixinTweaker` in program arguments  
  
You will also find a KillTheRNG.jar (not with -deobf at the end!) in the libs folder. Copy that into `run/mods` to trick Minecraft into loading a second mod.