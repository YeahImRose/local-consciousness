package com.localconsciousness;

import net.runelite.api.*;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class LocalConsciousnessOverlay extends Overlay
{
	private final Client client;
	private final LocalConsciousnessPlugin plugin;
	private final LocalConsciousnessConfig config;



	@Inject
	private LocalConsciousnessOverlay(Client client, LocalConsciousnessPlugin plugin, LocalConsciousnessConfig config)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.opacity() / 100.0f));
		graphics.drawImage(plugin.getCurrentItem(),
						plugin.getX(), plugin.getY(),
						plugin.getItemWidth(), plugin.getItemHeight(), null);
		return null;
	}
}
