package com.localconsciousness;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;


import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

@Slf4j
public class LocalConsciousnessPanel extends PluginPanel {
    @Inject
    ConfigManager configManager;

    protected final LocalConsciousnessPlugin plugin;
    protected final LocalConsciousnessConfig config;

    private final JPanel fullPanel = new JPanel();
    private final JPanel titlePanel = new JPanel();
    private final JPanel currentItemPanel = new JPanel();
    private final JPanel buttonsPanel = new JPanel();
    private final JPanel optionsPanel = new JPanel();

    JLabel currentItemLabel;
    JLabel currentItemName;

    JButton searchButton;
    JButton randomButton;

    JLabel scaleLabel;
    JSpinner scaleSpinner;
    SpinnerNumberModel scaleSpinnerModel;
    JLabel speedLabel;
    JSpinner speedSpinner;
    SpinnerNumberModel speedSpinnerModel;
    JLabel opacityLabel;
    JSpinner opacitySpinner;
    SpinnerNumberModel opacitySpinnerModel;


    public LocalConsciousnessPanel(Client client, LocalConsciousnessConfig config, final LocalConsciousnessPlugin plugin, ConfigManager configManager) {
        super();
        this.plugin = plugin;
        this.config = config;

        setPanelStaticLabels();
        this.fullPanel.add(buildTitlePanel());
        //this.fullPanel.add(buildCurrentItemPanel());
        this.fullPanel.add(buildButtonsPanel());
        this.fullPanel.add(buildOptionsPanel());

        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.fullPanel.setLayout(new GridLayout(10, 0, 0, 10));
        this.add(fullPanel);
    }

    private JPanel buildTitlePanel() {
        titlePanel.setBorder(new CompoundBorder(new EmptyBorder(5, 0, 0, 0), new MatteBorder(0, 0, 1, 0, new Color(37, 125, 141))));
        titlePanel.setLayout(new BorderLayout());
        PluginErrorPanel errorPanel = new PluginErrorPanel();
        errorPanel.setBorder(new EmptyBorder(2, 0, 3, 0));
        errorPanel.setContent("Local Consciousness", "");
        titlePanel.add(errorPanel, "Center");
        return titlePanel;
    }

    private JPanel buildCurrentItemPanel() {
        //currentItemPanel.setBorder();
        //currentItemPanel.setLayout(new BorderLayout());

        return currentItemPanel;
    }

    private JPanel buildButtonsPanel() {
        this.buttonsPanel.setLayout(new GridLayout(2, 1, 2, 5));

        this.searchButton = new JButton("Search For Item");
        searchButton.setFocusable(false);
        searchButton.addActionListener(e -> {
            searchButton.setFocusable(false);
            plugin.updateFromSearch();
            searchButton.setFocusable(true);
        });

        this.randomButton = new JButton("Randomize Item");
        randomButton.setFocusable(false);
        randomButton.addActionListener(e -> {
            randomButton.setFocusable(false);
            plugin.randomizeItem();
            randomButton.setFocusable(true);
        });
        this.buttonsPanel.add(this.searchButton);
        this.buttonsPanel.add(this.randomButton);

        return buttonsPanel;
    }

    private JPanel buildOptionsPanel() {
        this.optionsPanel.setLayout(new GridLayout(3, 2, 2, 10));

        this.scaleSpinnerModel = new SpinnerNumberModel(config.size(), 1, 5000, 1);
        this.scaleSpinner = new JSpinner(scaleSpinnerModel);
        scaleSpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "size", (int) scaleSpinner.getValue()));

        this.speedSpinnerModel = new SpinnerNumberModel(config.speed(), 1, 1000, 1);
        this.speedSpinner = new JSpinner(speedSpinnerModel);
        speedSpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "speed", (int) speedSpinner.getValue()));

        this.opacitySpinnerModel = new SpinnerNumberModel(config.opacity(), 0, 100, 5);
        this.opacitySpinner = new JSpinner(opacitySpinnerModel);
        opacitySpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "opacity", (int) opacitySpinner.getValue()));

        this.optionsPanel.add(this.scaleLabel);
        this.optionsPanel.add(this.scaleSpinner);

        this.optionsPanel.add(this.speedLabel);
        this.optionsPanel.add(this.speedSpinner);

        this.optionsPanel.add(this.opacityLabel);
        this.optionsPanel.add(this.opacitySpinner);

        return optionsPanel;
    }

    private void setPanelStaticLabels() {
        this.scaleLabel = new JLabel("Icon Scale: ");
        this.speedLabel = new JLabel("Icon Speed:");
        this.opacityLabel = new JLabel("Icon Opacity:");
        this.currentItemLabel = new JLabel("Current Item:");

    }

    public void updateItemName(String name) {
        if(name.length() > 10) {
            name = name.substring(0, 11);
            name += "...";
        }
        this.currentItemName.setText(name);
    }

}
