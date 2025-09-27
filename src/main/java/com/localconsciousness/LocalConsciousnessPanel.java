package com.localconsciousness;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class LocalConsciousnessPanel extends PluginPanel {
    protected final LocalConsciousnessPlugin plugin;
    protected final LocalConsciousnessConfig config;
    protected final ConfigManager configManager;

    @Getter
    private boolean opened = false;

    private JPanel fullPanel;
    private JPanel titlePanel;
    private JPanel currentItemPanel;
    private JPanel buttonsPanel;
    private JPanel optionsPanel;

    JLabel pluginTitleLabel;

    JLabel currentItemLabel;
    JLabel currentItemIcon;
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

    Color borderColor = new Color(220, 138, 0);


    public LocalConsciousnessPanel(Client client, LocalConsciousnessConfig config, final LocalConsciousnessPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.config = config;
        this.configManager = configManager;

        this.fullPanel = new JPanel();
        this.titlePanel = new JPanel();
        this.currentItemPanel = new JPanel();
        this.buttonsPanel = new JPanel();
        this.optionsPanel = new JPanel();

        this.fullPanel = new JPanel();
        this.titlePanel = new JPanel();
        this.currentItemPanel = new JPanel();
        this.buttonsPanel = new JPanel();
        this.optionsPanel = new JPanel();
        initJLabels();
        /*JButton reloadButton = new JButton("reload");
        reloadButton.setFocusable(false);
        reloadButton.addActionListener(e -> {
            reloadButton.setFocusable(false);
            render();
            reloadButton.setFocusable(true);
        });*/

        this.fullPanel.add(buildTitlePanel());
        this.fullPanel.add(buildCurrentItemPanel());
        this.fullPanel.add(buildButtonsPanel());
        this.fullPanel.add(buildOptionsPanel());

        //this.fullPanel.add(reloadButton);

        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.fullPanel.setLayout(new BoxLayout(this.fullPanel, BoxLayout.Y_AXIS));
        this.add(fullPanel, "North");
    }

    @Override
    public void onActivate() {
        opened = true;
    }

    @Override
    public void onDeactivate() {
        opened = false;
    }

    void render() {
        removeAll();
    }

    private JPanel buildTitlePanel() {
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, borderColor),
                                                new EmptyBorder(5, 0, 10, 0)));
        pluginTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(pluginTitleLabel);
        return titlePanel;
    }

    private JPanel buildCurrentItemPanel() {
        currentItemPanel.setLayout(new BoxLayout(this.currentItemPanel, BoxLayout.Y_AXIS));
        currentItemPanel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, borderColor),
                                                      new EmptyBorder(5, 0, 10, 0)));

        currentItemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentItemLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentItemPanel.add(currentItemLabel);

        currentItemIcon.setMinimumSize(new Dimension(64, 64));
        currentItemIcon.setPreferredSize(new Dimension(64, 64));
        currentItemIcon.setMaximumSize(new Dimension(64, 64));
        currentItemIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentItemIcon.setHorizontalAlignment(SwingConstants.CENTER);
        currentItemIcon.setBorder(new MatteBorder(1, 1, 1, 1, Color.gray));
        currentItemPanel.add(currentItemIcon);

        currentItemName.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentItemName.setHorizontalAlignment(SwingConstants.CENTER);
        currentItemName.setMinimumSize(new Dimension(192, 1));
        currentItemName.setBorder(new EmptyBorder(5, 0, 0, 0));
        currentItemPanel.add(currentItemName);

        return currentItemPanel;
    }

    private JPanel buildButtonsPanel() {
        buttonsPanel.setLayout(new GridLayout(2, 1, 0, 5));
        buttonsPanel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, borderColor),
                                                  new EmptyBorder(10, 0, 10, 0)));

        searchButton.setFocusable(false);
        searchButton.addActionListener(e -> {
            searchButton.setFocusable(false);
            plugin.updateFromSearch();
            //searchButton.setFocusable(true);
        });

        randomButton.setFocusable(false);
        randomButton.addActionListener(e -> {
            randomButton.setFocusable(false);
            plugin.randomizeItem();
            //randomButton.setFocusable(true);
        });

        buttonsPanel.add(searchButton);
        buttonsPanel.add(randomButton);

        return buttonsPanel;
    }

    private JPanel buildOptionsPanel() {
        this.optionsPanel.setLayout(new GridLayout(3, 2, 2, 10));
        optionsPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        this.scaleSpinnerModel = new SpinnerNumberModel(config.size(), 1, 50000, 1);
        this.scaleSpinner = new JSpinner(scaleSpinnerModel);
        scaleSpinner.addChangeListener(e -> configManager.setConfiguration("localconsciousness", "size", (int) scaleSpinner.getValue()));

        this.speedSpinnerModel = new SpinnerNumberModel(config.speed(), 1, 5000, 1);
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

    private void initJLabels() {
        this.pluginTitleLabel = new JLabel("<html><b><h1 align='center'>Local Consciousness</h1></b></html>");
        this.scaleLabel = new JLabel("Icon Scale");
        this.speedLabel = new JLabel("Icon Speed");
        this.opacityLabel = new JLabel("Icon Opacity");
        this.currentItemLabel = new JLabel("<html><b><h2>Current Item</h2></b></html>");
        this.currentItemIcon = new JLabel();
        this.currentItemName = new JLabel("Not Yet Loaded");

        this.searchButton = new JButton("Search For Item");
        this.randomButton = new JButton("Randomize Item");
    }

    public void updateItemIcon(BufferedImage image) {
        currentItemIcon.setIcon(new ImageIcon(image));
    }

    public void updateItemName(String name) {
        if(name.length() > 30) {
            name = name.substring(0, 31);
            name += "...";
        }
        this.currentItemName.setText(name);
    }

}
