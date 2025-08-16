package com.verchasel.specsound;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.Function;

import static com.verchasel.specsound.SpecSoundPlugin.*;

public class SpecSoundPanel extends PluginPanel
{
    private final ConfigManager configManager;

    private final JCheckBox useBundledCheck;
    private JButton chooseBtn = null;
    private JLabel pathLabel = null;
    private final JSlider volumeSlider;
    private final JLabel volumeValue;

    public SpecSoundPanel(ConfigManager configManager, Runnable testAction)
    {
        this.configManager = configManager;

        setLayout(new BorderLayout());
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBorder(new EmptyBorder(12, 12, 16, 12));
        add(col, BorderLayout.CENTER);

        Function<JComponent, JPanel> row = comp -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            p.setBorder(new EmptyBorder(0, 0, 10, 0));
            p.add(comp);
            return p;
        };
        Function<JComponent[], JPanel> rowMany = comps -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            p.setBorder(new EmptyBorder(0, 0, 10, 0));
            for (JComponent c : comps) p.add(c);
            return p;
        };

        useBundledCheck = new JCheckBox("Use bundled sound");
        useBundledCheck.addActionListener(e -> {
            boolean val = useBundledCheck.isSelected();
            setCfg(K_USE_BUNDLED, val);
            if (val) {
                setCfg(K_CUSTOM_PATH, "");
                pathLabel.setText("(no file selected)");
                chooseBtn.setEnabled(false);
            } else {
                chooseBtn.setEnabled(true);
            }
        });
        col.add(row.apply(useBundledCheck));

        chooseBtn = new JButton("Choose custom WAV…");
        chooseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select a WAV file");
            fc.setFileFilter(new FileNameExtensionFilter("WAV files", "wav", "wave"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f != null) {
                    setCfg(K_CUSTOM_PATH, f.getAbsolutePath());
                    setCfg(K_USE_BUNDLED, false);
                    pathLabel.setText(shorten(f.getAbsolutePath()));
                    useBundledCheck.setSelected(false);
                    chooseBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Custom sound set.");
                }
            }
        });
        col.add(row.apply(chooseBtn));

        pathLabel = new JLabel("(no file selected)", SwingConstants.CENTER);
        JPanel pathRow = row.apply(pathLabel);
        pathRow.setBorder(new EmptyBorder(0, 0, 14, 0));
        col.add(pathRow);

        JLabel volLabel = new JLabel("Volume");
        volumeSlider = new JSlider(0, 100, 100);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeValue = new JLabel("100%");

        volumeSlider.addChangeListener((ChangeEvent e) -> {
            int val = volumeSlider.getValue();
            volumeValue.setText(val + "%");
            if (!volumeSlider.getValueIsAdjusting()) {
                setCfg(K_VOLUME, val);
            }
        });

        JPanel volRowPanel = rowMany.apply(new JComponent[]{ volLabel, volumeSlider, volumeValue });
        volRowPanel.setBorder(new EmptyBorder(6, 0, 14, 0));
        col.add(volRowPanel);

        JButton testBtn = new JButton("Test sound");
        testBtn.addActionListener(e -> { if (testAction != null) testAction.run(); });
        col.add(row.apply(testBtn));

        refreshFromConfig();
    }

    public void refreshFromConfig()
    {
        boolean useBundled = getBool();
        String path        = getString();
        int vol            = getInt();

        useBundledCheck.setSelected(useBundled);
        chooseBtn.setEnabled(!useBundled);
        pathLabel.setText(shorten(path));
        volumeSlider.setValue(Math.max(0, Math.min(100, vol)));
        volumeValue.setText(volumeSlider.getValue() + "%");
    }

    private void setCfg(String key, Object value)
    {
        configManager.setConfiguration(GROUP, key, value);
    }
    private boolean getBool()
    {
        String s = configManager.getConfiguration(GROUP, K_USE_BUNDLED);
        return (s == null) ? D_USE_BUNDLED : Boolean.parseBoolean(s);
    }
    private int getInt()
    {
        String s = configManager.getConfiguration(GROUP, K_VOLUME);
        if (s == null) return D_VOLUME;
        try { return Integer.parseInt(s); } catch (Exception ignored) { return D_VOLUME; }
    }
    private String getString()
    {
        String s = configManager.getConfiguration(GROUP, K_CUSTOM_PATH);
        return (s == null) ? D_CUSTOM_PATH : s;
    }

    private static String shorten(String s)
    {
        if (s == null || s.isBlank()) return "(no file selected)";
        return s.length() > 60 ? "…" + s.substring(s.length() - 60) : s;
    }
}
