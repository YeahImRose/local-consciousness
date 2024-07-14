package com.localconsciousness;

import com.google.inject.Provides;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Random;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
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

	@Getter
	private BufferedImage currentItem;
	private int size;
	@Getter
	private int width;
	@Getter
	private int height;
	@Getter
	private double x;
	@Getter
	private double y;
	private int newItemID;
	private int currentItemID;
	private double angle;
	private int canvasHeight;
	private int canvasWidth;
	private Random rand;
	private boolean checkedForOversize = false;

	private void resetMovement()
	{
		float wiggle = (rand.nextFloat() * 80.0f) + 5.0f;
		angle = (rand.nextInt(4) * 90 ) + wiggle;
		canvasWidth = client.getCanvasWidth();
		canvasHeight = client.getCanvasHeight();
		int sizeOffsetX = width / 2;
		int sizeOffsetY = height / 2;
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
			width = (int)(currentItem.getWidth() * sizeMult);
			height = (int)(currentItem.getHeight() * sizeMult);

			currentItemID = newItemID;
		}

		double speed = config.speed() / 10.0d;

		if(x > canvasWidth) x = canvasWidth;
		if(x < 0) x = 0;
		if(x >= canvasWidth - width || x <= 0) {
			angle = 180 - angle;
		}

		if(y > canvasHeight) y = canvasHeight;
		if(y < 0) y = 0;
		if(y >= canvasHeight - height || y <= 0) {
			angle = 360 - angle;
		}

		angle %= 360;

		double cosComponent = Math.cos(Math.toRadians(angle));
		double sinComponent = Math.sin(Math.toRadians(angle));

		double nextX = cosComponent * speed;
		double nextY = sinComponent * speed;

		// Fix for low speed values causing movement to stop
		//if(nextX == 0) nextX = (int)(1 * Math.signum(cosComponent));
		//if(nextY == 0) nextY = (int)(1 * Math.signum(sinComponent));

		x += nextX;
		y += nextY;

		if(!checkedForOversize) {
			if(width >= canvasWidth
				|| height >= canvasHeight) {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
						"Your local consciousness sprite may be too big! Consider reducing its size.", "");
			}
			checkedForOversize = true;
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		newItemID = config.item();

		int newSize = config.size();
		if(newSize != size) {
			size = newSize;
			float sizeMult = size / 100.0f;
			width = (int)(currentItem.getWidth() * sizeMult);
			height = (int)(currentItem.getHeight() * sizeMult);

			resetMovement();
		}

		checkedForOversize = false;
	}

	@Provides
	LocalConsciousnessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LocalConsciousnessConfig.class);
	}
}