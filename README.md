# Special Attack Sound (RuneLite)

Play a custom sound whenever **special attack energy is consumed**.  
Bundle a default WAV in the plugin, or pick your own file from the sidebar panel.

---

## Features
- 🔊 Plays a sound on **any** spec energy drop (weapon-agnostic).
- 📁 **File picker** in the sidebar to choose a custom `.wav`.
- 📦 Optional **bundled** default sound (ships inside the plugin).
- 🔉 **Volume** slider (0–100%) with instant apply.
- ▶️ **Test sound** button.

---

## Requirements
- Java 17+ (the RuneLite dev workflow is happiest on 17).
- Gradle (or IntelliJ with Gradle integration).

---

## Project structure
- src/
- main/
- java/com/verchasel/specsound/
- SpecSoundPlugin.java
- SpecSoundPanel.java
- resources/
- icons/icon.png # toolbar icon
- specsound/bundled.wav (default sound)

---

## Build & run (local dev)

### IntelliJ (recommended)
1. Import the Gradle project.
2. Create a small launcher (already included in earlier instructions), or add one:
   ```java
   // src/main/java/com/verchasel/specsound/SpecSoundPluginTest.java
   package com.verchasel.specsound;

   import net.runelite.client.RuneLite;
   import net.runelite.client.externalplugins.ExternalPluginManager;

   public class SpecSoundPluginTest {
       public static void main(String[] args) throws Exception {
           ExternalPluginManager.loadBuiltin(SpecSoundPlugin.class);
           RuneLite.main(args);
       }
   }
