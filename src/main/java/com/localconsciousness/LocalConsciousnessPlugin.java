package com.localconsciousness;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
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
	name = "Local Consciousness", description = "Make an item bounce around your screen!", enabledByDefault = true
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
	private int newItemID;
	private int currentItemID;
	private int size;
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


	private BufferedImage cropSpriteByTransparency(BufferedImage sprite)
	{
		// Method from https://stackoverflow.com/a/36938923
		WritableRaster raster = sprite.getAlphaRaster();
		int width = raster.getWidth();
		int height = raster.getHeight();
		int left = 0;
		int top = 0;
		int right = width - 1;
		int bottom = height - 1;
		int minRight = width - 1;
		int minBottom = height - 1;

		top:
		for (;top <= bottom; top++){
			for (int x = 0; x < width; x++){
				if (raster.getSample(x, top, 0) != 0){
					minRight = x;
					minBottom = top;
					break top;
				}
			}
		}

		left:
		for (;left < minRight; left++){
			for (int y = height - 1; y > top; y--){
				if (raster.getSample(left, y, 0) != 0){
					minBottom = y;
					break left;
				}
			}
		}

		bottom:
		for (;bottom > minBottom; bottom--){
			for (int x = width - 1; x >= left; x--){
				if (raster.getSample(x, bottom, 0) != 0){
					minRight = x;
					break bottom;
				}
			}
		}

		right:
		for (;right > minRight; right--){
			for (int y = bottom; y >= top; y--){
				if (raster.getSample(right, y, 0) != 0){
					break right;
				}
			}
		}

		return sprite.getSubimage(left, top, right - left + 1, bottom - top + 1);
	}

	@Override
	protected void startUp() throws Exception
	{
		rand = new Random();
		resetMovement();

		currentItem = itemManager.getImage(config.item());
		currentItemID = config.item();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	protected void onCanvasSizeChanged(CanvasSizeChanged canvasSizeChanged) {
		resetMovement();
	}

	@Subscribe
	protected void onClientTick(ClientTick tick) {
		// This needs to be in onClientTick due to getImage not guaranteeing
		// A proper image to be returned 
		if (newItemID != currentItemID) {
			BufferedImage item = itemManager.getImage(newItemID);
			try {
				currentItem = cropSpriteByTransparency(item);
			} catch (Exception e) {
				// This is just here to catch weird empty items, such as 798, 12897, 12898, etc.
			}

			float sizeMult = size / 100.0f;
			itemWidth = (int)(currentItem.getWidth() * sizeMult);
			itemHeight = (int)(currentItem.getHeight() * sizeMult);

			currentItemID = newItemID;
		}

		double speed = config.speed() / 10.0d;

		if(x >= canvasWidth - itemWidth || x <= 0) {
			angle = 180 - angle;
		}
		if(y >= canvasHeight - itemHeight || y <= 0) {
			angle = 360 - angle;
		}

		double cosComponent = Math.cos(Math.toRadians(angle));
		double sinComponent = Math.sin(Math.toRadians(angle));

		int nextX = (int) (cosComponent * speed);
		int nextY = (int) (sinComponent * speed);

		// Fix for low speed values causing movement to stop
		if(nextX == 0) nextX = (int)(1 * Math.signum(cosComponent));
		if(nextY == 0) nextY = (int)(1 * Math.signum(sinComponent));

		x += nextX;
		y += nextY;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		newItemID = config.item();
		size = config.size();
	}

	@Provides
	LocalConsciousnessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LocalConsciousnessConfig.class);
	}
}