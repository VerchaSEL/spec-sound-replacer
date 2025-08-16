package com.verchasel.specsound;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.events.GameTick;

import net.runelite.client.audio.AudioPlayer;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
@PluginDescriptor(
        name = "Spec Sound Replacer",
        description = "Plays a custom sound when special attack energy is consumed",
        tags = {"sound","audio","special"}
)
public class SpecSoundPlugin extends Plugin
{
    private static final int VARP_SPEC_PERCENT = 300;

    static final String GROUP         = "specsound";
    static final String K_USE_BUNDLED = "useBundled";
    static final String K_CUSTOM_PATH = "customPath";
    static final String K_VOLUME      = "volume";

    static final boolean D_USE_BUNDLED = true;
    static final String  D_CUSTOM_PATH = "";
    static final int     D_VOLUME      = 100;

    private int  lastSpecPct = -1;
    private long lastPlayMs  = 0;

    @Inject private Client client;
    @Inject private ClientToolbar clientToolbar;
    @Inject private ConfigManager configManager;
    @Inject private AudioPlayer audioPlayer;
    @Inject private ClientThread clientThread;

    private NavigationButton navButton;
    private SpecSoundPanel panel;

    @Override
    protected void startUp()
    {
        if (configManager.getConfiguration(GROUP, K_USE_BUNDLED) == null)
            configManager.setConfiguration(GROUP, K_USE_BUNDLED, D_USE_BUNDLED);
        if (configManager.getConfiguration(GROUP, K_CUSTOM_PATH) == null)
            configManager.setConfiguration(GROUP, K_CUSTOM_PATH, D_CUSTOM_PATH);
        if (configManager.getConfiguration(GROUP, K_VOLUME) == null)
            configManager.setConfiguration(GROUP, K_VOLUME, D_VOLUME);

        panel = new SpecSoundPanel(configManager, this::playCustomSound);

        BufferedImage icon = ImageUtil.loadImageResource(SpecSoundPlugin.class, "/icons/icon.png");
        navButton = NavigationButton.builder()
                .tooltip("Spec Sound")
                .icon(icon)
                .panel(panel)
                .priority(5)
                .build();
        clientToolbar.addNavigation(navButton);

        clientThread.invoke(() -> {
            lastSpecPct = client.getVarpValue(VARP_SPEC_PERCENT);
            lastPlayMs  = 0;
        });

        SwingUtilities.invokeLater(panel::refreshFromConfig);
    }

    @Override
    protected void shutDown()
    {
        if (navButton != null) clientToolbar.removeNavigation(navButton);
        navButton = null;
        panel = null;

        lastSpecPct = -1;
        lastPlayMs  = 0;
    }

    @Subscribe
    public void onGameTick(GameTick t)
    {
        final int now = client.getVarpValue(VARP_SPEC_PERCENT);

        if (lastSpecPct >= 0 && now < lastSpecPct && System.currentTimeMillis() - lastPlayMs > 250)
        {
            log.debug("Spec drop detected: {} -> {}", lastSpecPct, now);
            playCustomSound();
            lastPlayMs = System.currentTimeMillis();
        }

        lastSpecPct = now;
    }

    void playCustomSound()
    {
        try
        {
            final boolean useBundled = getBool();
            final String path        = getString();
            final float gainDb       = volumeToDb(getInt());

            if (useBundled)
            {
                if (SpecSoundPlugin.class.getResource("/specsound/bundled.wav") == null) {
                    log.warn("Bundled sound missing at /specsound/bundled.wav");
                    return;
                }
                audioPlayer.play(SpecSoundPlugin.class, "/specsound/bundled.wav", gainDb);
            }
            else
            {
                if (path != null && !path.isBlank())
                {
                    final File f = new File(path);
                    if (f.exists() && f.isFile())
                    {
                        audioPlayer.play(f, gainDb);
                        return;
                    }
                    log.info("Custom file missing ({}), falling back to bundled", path);
                }
                if (SpecSoundPlugin.class.getResource("/specsound/bundled.wav") != null) {
                    audioPlayer.play(SpecSoundPlugin.class, "/specsound/bundled.wav", gainDb);
                } else {
                    log.warn("Fallback bundled sound missing at /specsound/bundled.wav");
                }
            }
        }
        catch (Exception ex)
        {
            log.warn("Failed to play custom spec sound", ex);
        }
    }

    boolean getBool() {
        final String s = configManager.getConfiguration(GROUP, SpecSoundPlugin.K_USE_BUNDLED);
        return (s == null) ? SpecSoundPlugin.D_USE_BUNDLED : Boolean.parseBoolean(s);
    }
    int getInt() {
        final String s = configManager.getConfiguration(GROUP, SpecSoundPlugin.K_VOLUME);
        if (s == null) return SpecSoundPlugin.D_VOLUME;
        try { return Integer.parseInt(s); } catch (Exception ignored) { return SpecSoundPlugin.D_VOLUME; }
    }
    String getString() {
        final String s = configManager.getConfiguration(GROUP, SpecSoundPlugin.K_CUSTOM_PATH);
        return (s == null) ? SpecSoundPlugin.D_CUSTOM_PATH : s;
    }

    private static float volumeToDb(int vol0to100)
    {
        int v = Math.max(0, Math.min(100, vol0to100));
        return (v == 0) ? -80f : (float)(20.0 * Math.log10(v / 100.0));
    }
}
