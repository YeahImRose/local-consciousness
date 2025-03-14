package com.localconsciousness;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Slf4j
public class LocalConsciousnessPanel extends PluginPanel {
    protected final LocalConsciousnessPlugin plugin;

    JButton searchButton;

    JLabel scaleLabel;
    JSpinner scaleSpinner;
    SpinnerNumberModel scaleSpinnerModel;

    JLabel speedLabel;
    JSpinner speedSpinner;
    SpinnerNumberModel speedSpinnerModel;

    JLabel opacityLabel;
    JSpinner opacitySpinner;
    SpinnerNumberModel opacitySpinnerModel;

    JLabel currentItemLabel;
    JLabel currentItemName;


    public LocalConsciousnessPanel(Client client, LocalConsciousnessConfig config, final LocalConsciousnessPlugin plugin, ConfigManager configManager) {
        super();

        this.plugin = plugin;

        setBorder(new EmptyBorder(18, 10, 0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        this.scaleLabel = new JLabel("Icon Scale:");
        add(scaleLabel, c);

        c.gridx++;

        this.scaleSpinnerModel = new SpinnerNumberModel(config.size(), 1, 5000, 1);
        this.scaleSpinner = new JSpinner(scaleSpinnerModel);
        scaleSpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "size", (int) scaleSpinner.getValue()));
        add(scaleSpinner, c);

        c.gridy++;
        c.gridx = 0;

        this.speedLabel = new JLabel("Icon Speed:");
        add(speedLabel, c);

        c.gridx++;

        this.speedSpinnerModel = new SpinnerNumberModel(config.speed(), 1, 1000, 1);
        this.speedSpinner = new JSpinner(speedSpinnerModel);
        speedSpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "speed", (int) speedSpinner.getValue()));
        add(speedSpinner, c);

        c.gridy++;
        c.gridx = 0;

        this.opacityLabel = new JLabel("Icon Opacity:");
        add(opacityLabel, c);

        c.gridx++;

        this.opacitySpinnerModel = new SpinnerNumberModel(config.opacity(), 0, 100, 5);
        this.opacitySpinner = new JSpinner(opacitySpinnerModel);
        opacitySpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "opacity", (int) opacitySpinner.getValue()));
        add(opacitySpinner, c);

        c.gridy++;
        c.gridx = 0;

        this.currentItemLabel = new JLabel("Current Item:");
        add(currentItemLabel, c);

        c.gridx++;

        this.currentItemName = new JLabel("");
        add(currentItemName, c);

        c.gridy++;
        c.gridx = 0;

        this.searchButton = new JButton("Search For Item");
        searchButton.setFocusable(false);
        searchButton.addActionListener(e -> {
            searchButton.setFocusable(false);
            plugin.updateFromSearch();
            searchButton.setFocusable(true);
        });
        c.gridwidth = 2;
        add(searchButton, c);

    }

    public void updateItemName(String name) {
        if(name.length() > 10) {
            name = name.substring(0, 11);
            name += "...";
        }
        this.currentItemName.setText(name);
    }

}
