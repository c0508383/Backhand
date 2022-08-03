# Backhand


## Setting up a Development Environment

Using IntelliJ, take the following steps: 
1. Run `setupDecompWorkspace` under the `forgegradle` tab.
2. Refresh Gradle.
3. Run `genIntellijRuns` under the `other` tab to set up your Minecraft runs.

or for Eclipse, run `gradlew setupDecompWorkspace eclipse` then import the project.

Finally, add the following VM option to all of your Minecraft configurations to enable the fixes and transformers for the mod to work: `-Dfml.coreMods.load=net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixLoaderO`

