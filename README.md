# Backhand

A minimalist fork of [The Offhand Mod](https://github.com/TCLProject/theoffhandmod), which itself is forked from [Mine&Blade Battlegear 2](https://github.com/Mine-and-blade-admin/Battlegear2), that backports a sole offhand slot with functionality close to vanilla Minecraft's later versions' offhand. No extra items are added from Battlegear2, but extra functionality has been added for the offhand such as a blacklist and allowing attacking with the offhand. These functions are disabled by default and can be toggled through the config file.

Big thanks to TCLProject, the creator of the offhand mod. Another huge thanks to nerd-boy & GotoLink as well, the authors of Mine&Blade Battlegear 2.

## Setting up a Development Environment

Using IntelliJ, take the following steps: 
1. Run `setupDecompWorkspace` under the `forgegradle` tab.
2. Refresh Gradle.
3. Run `genIntellijRuns` under the `other` tab to set up your Minecraft runs.

or for Eclipse, run `gradlew setupDecompWorkspace eclipse` then import the project.

Finally, add the following VM option to all of your Minecraft configurations to enable the fixes and transformers for the mod to work: `-Dfml.coreMods.load=net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixLoaderO`

