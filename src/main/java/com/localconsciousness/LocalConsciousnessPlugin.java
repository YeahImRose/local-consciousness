package com.localconsciousness;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.CanvasSizeChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.api.events.ClientTick;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;



@Slf4j
@PluginDescriptor(
	name = "Local Consciousness"
)
public class LocalConsciousnessPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private LocalConsciousnessConfig config;

	@Inject
	private LocalConsciousnessOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	private BufferedImage currentItem;
	private int currentItemID;
	private int itemWidth;
	private int itemHeight;
	private int x;
	private int y;
	private double angle;
	private int canvasHeight;
	private int canvasWidth;
	private boolean active;
	private Random rand;

	public BufferedImage getCurrentItem() {
		return currentItem;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getItemWidth() {
		return itemWidth;
	}

	public int getItemHeight() {
		return itemHeight;
	}

	public boolean getActive() {
		return active;
	}

	private void resetMovement()
	{
		float wiggle = (rand.nextFloat() * 80.0f) + 5.0f;
		angle = (rand.nextInt(4) * 90 ) + wiggle;
		canvasWidth = client.getCanvasWidth();
		canvasHeight = client.getCanvasHeight();
		int sizeOffsetX = itemWidth / 2;
		int sizeOffsetY = itemHeight / 2;
		x = canvasWidth / 2;
		x -= sizeOffsetX;
		y = canvasHeight / 2;
		y -= sizeOffsetY;
	}

	@Override
	protected void startUp() throws Exception
	{
		active = true;
		rand = new Random();
		resetMovement();

		currentItem = itemManager.getImage(config.item());
		currentItemID = config.item();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		active = false;
		// This is here to make the sprite restart from the center on plugin restart

	}

	@Subscribe
	protected void onCanvasSizeChanged(CanvasSizeChanged canvasSizeChanged) {
		resetMovement();
	}

	@Subscribe
	protected void onClientTick(ClientTick tick) {
		double speed = config.speed() / 10.0d;

		if(x >= canvasWidth - itemWidth || x <= 0) {
			angle = 180 - angle;
		}
		if(y >= canvasHeight - itemHeight || y <= 0) {
			angle = 360 - angle;
		}

		double cosAngle = Math.cos(Math.toRadians(angle));
		double sinAngle = Math.sin(Math.toRadians(angle));

		int nextX = (int) (cosAngle * speed);
		int nextY = (int) (sinAngle * speed);

		// Fix for low speed values causing movement to stop
		if(nextX == 0) nextX = (int)(1 * Math.signum(cosAngle));
		if(nextY == 0) nextY = (int)(1 * Math.signum(sinAngle));

		x += nextX;
		y += nextY;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if(currentItemID != config.item()) currentItem = itemManager.getImage(config.item());

		float sizeMult = config.size() / 100.0f;
		itemWidth = (int)(currentItem.getWidth() * sizeMult);
		itemHeight = (int)(currentItem.getHeight() * sizeMult);
	}

	@Provides
	LocalConsciousnessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LocalConsciousnessConfig.class);
	}
}