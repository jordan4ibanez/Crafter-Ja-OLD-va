# Crafter 0.07a

A blocky game written in java with LWJGL 3

Based off of what I've learned from Minetest's engine and lua api

Discord: https://discord.gg/dRPyvubfyg

IRC: #crafter on Libera (https://libera.chat/)

Required Java version (JRE): 17

You can get this version at: https://jdk.java.net/17/

Or you can install openJDK on most linux distros.

Ubuntu: ``sudo apt install openjdk-17-jdk``

Fedora: ``sudo dnf install java-latest-openjdk-devel.x86_64``

To update default JRE: `sudo update-alternatives --config java`

You can try these flags for a performance boost, if you want. These are experimental.

`
-Xmx1G -XX:-UseAdaptiveSizePolicy -XX:-UseParallelGC -Xmn128M
`
 
# Building with ANT:

### If you know how to improve the ant build script, feel free to.

You must install ANT (Another Neat Tool) for the build to work.

Once you have ANT installed, you can simply CD to the Crafter directory.

Check build.properties to make sure that ``jdk.home.17=`` is pointing to your openJDK 17 install.

You can find this with: ``readlink -f $(which java)`` (Don't copy the bin/java part)

Run ``ant -keep-going``

Once it says build successful, you must create a folder for the game, preferably on your desktop.

Drop the jar from the build directory (WHEREVER/Crafter/out/artifacts/Crafter_jar/Crafter.jar) to your new folder directory.

Copy the /textures/ and /sounds/ folders into the new directory.

You should now be able to run the game using java -jar Crafter.jar in the folder you created, or double clicking the jar in Windows.

# Todo List: (This is probably outdated)

1. Modularize things - less hardcoded implementations


## Stargazers over time

[![Stargazers over time](https://starchart.cc/jordan4ibanez/Crafter.svg)](https://starchart.cc/jordan4ibanez/Crafter)

If you want to see the first ever commit done to this project:

https://github.com/oilboi/Crafter/commit/282313ab6d3f3041e385161907d0b551240f3cbd

This was done on my secondary account.