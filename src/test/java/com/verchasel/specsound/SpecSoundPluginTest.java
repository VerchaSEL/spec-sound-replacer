package com.verchasel.specsound;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SpecSoundPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(SpecSoundPlugin.class);
        RuneLite.main(args);
    }
}