# Crafter

A blocky game written in java with LWJGL 3

Based off of what I've learned from Minetest's engine and lua api

Discord: https://discord.gg/fEjssvEYMH

You can try these flags for a performance boost, if you want.

`
-Xms6G -Xmx6G -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:MaxGCPauseMillis=100  -XX:+DisableExplicitGC -XX:TargetSurvivorRatio=90 -XX:G1NewSizePercent=50 -XX:G1MaxNewSizePercent=80  -XX:G1MixedGCLiveThresholdPercent=35 -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled
`


# Todo List:

1. Re-implement basic lighting

2. Implement torches

3. Implement crafting with basic craft recipe handling

4. Implement day/night

5. Implement mobs

6. Save map to disk better

