# TASmod  
Lets you record and play back tickperfect inputs.  
  
This mod is currently only for Forge 1.12.2 and will update/downgrade once 1.12.2 works properly  

## Credits  
Author of the original mod: tr7zw  
Main Author: Scribble  
  
Contributions by: famous1622, Pancake  
  
Tickratechanger: [Guichaguri](https://github.com/Guichaguri/TickrateChanger)  
Tickrate 0 idea: [Cubitect](https://github.com/Cubitect/Cubitick)  
Savestate idea: [bspkrs, MightyPork](https://github.com/bspkrs-mods/WorldStateCheckpoints), although implementation is totally different now
  
Special thanks: Darkmoon, The Minecraft TAS Community  
## Commands  
`/record` Starts to record your input, hit the same command again to stop recording  
`/play` Starts to  play back a the stored inputs  
`/save <filename>` Saves stored inputs to a file  
`/load <filename>` Load inputs from file  
`/clearinputs` Delete all stored inputs, use this before starting a brand new recording. `/record` will resume the recording and not clear the inputs first.  
  
## Development
This Project uses ForgeNoGradle. Please download and run [this file](https://mgnet.work/ForgeNoGradle-1.0.1.jar) in your repository root directory
To export a jar, clean the project first. Project -> Clean, then run the -export.launch file
You will also find a KillTheRNG.jar, copy that into `run/mods` to tell ForgeNoGradle to load a second mod.