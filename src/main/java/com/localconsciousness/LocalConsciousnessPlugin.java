package com.localconsciousness;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.CanvasSizeChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemClient;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.api.events.ClientTick;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
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
	private ChatboxItemSearch itemSearch;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	@Getter
	private ClientThread clientThread;
	@Inject
	private ItemClient itemClient;
	private NavigationButton navButton;

	@Getter
	private BufferedImage currentItem;
	private LocalConsciousnessPanel panel;
	private int size;
	@Getter
	private int width;
	@Getter
	private int height;
	@Getter
	private double x;
	@Getter
	private double y;
	@Getter
	private int currentItemID;
	private double angle;
	private int canvasHeight;
	private int canvasWidth;
	private Random rand;
	private boolean checkedForOversize = false;
    private boolean queuedToolbarUpdate = false;
	private ArrayList<Integer> validIdList = new ArrayList<Integer>();

	@Override
	protected void startUp() throws Exception
	{
		rand = new Random();
		resetMovement();

		currentItem = itemManager.getImage(config.item());
		currentItemID = config.item();
		clientThread.invokeLater(this::updateItem);
		overlayManager.add(overlay);
		// Used in place of a check here for needing to add panel button or not
        updateShowPanelButton();
		if(validIdList.isEmpty()) {
			clientThread.invoke(this::computeValidItemIdList);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);
	}

	private void computeValidItemIdList() {
		for (int i = 0; i < client.getItemCount(); i++) {
			ItemComposition itemComposition = itemManager.getItemComposition(itemManager.canonicalize(i));
			//if(itemComposition.getName() == null) continue;
			if(itemComposition.getName().isEmpty()) continue;
			if(itemComposition.getName().equalsIgnoreCase("null")) continue;
			validIdList.add(i);
		}
	}
	private void saveValidItemIdList() {

	}
	private void loadValidItemIdList() {

	}
	private void resetMovement()
	{
		float wiggle = (rand.nextFloat() * 80.0f) + 5.0f;
		angle = (rand.nextInt(4) * 90 ) + wiggle;
		canvasWidth = client.getCanvasWidth();
		canvasHeight = client.getCanvasHeight();
		int sizeOffsetX = width / 2;
		int sizeOffsetY = height / 2;
		x = (double) canvasWidth / 2;
		x -= sizeOffsetX;
		y = (double) canvasHeight / 2;
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

	@Subscribe
	protected void onCanvasSizeChanged(CanvasSizeChanged canvasSizeChanged) {
        resetMovement();
    }

	@Subscribe
	protected void onClientTick(ClientTick tick) {

        if(queuedToolbarUpdate) {
            if(!panel.isOpened()) {
                updateShowPanelButton();
            }
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
		if(!Objects.equals(event.getGroup(), "localconsciousness")) return;
		switch(event.getKey()) {
			case "item":
				clientThread.invokeLater(this::updateItem);
				break;
			case "size":
				updateSize();
				resetMovement();
				break;
			case "speed":
				break;
			case "opacity":
				break;
			case "showPanelButton":
                queuedToolbarUpdate = true;
				break;
			default: break;
		}
	}

	private boolean updateItem() {
		if(client.getGameState() != GameState.LOGGED_IN) {
            return false;
		}
		currentItemID = config.item();

		clientThread.invokeLater(() -> {
			BufferedImage item = itemManager.getImage(currentItemID);
			try {
				currentItem = cropSpriteByTransparency(item);
			} catch (Exception e) {
				// This is just here to catch weird empty items, such as 798, 12897, 12898, etc.
			}
            queuedToolbarUpdate = true;
			if(config.showPanelButton()) updatePanelItem();
			// Must be run after updating item image
			updateSize();
		});
		resetMovement();

		checkedForOversize = false;
		return true;
	}
	private void updateSize() {
		size = config.size();
		float sizeMult = size / 100.0f;
		width = (int)(currentItem.getWidth() * sizeMult);
		height = (int)(currentItem.getHeight() * sizeMult);
	}
	private void updateSpeed() {
	}
	private void updateOpacity() {
	}
	private void updateShowPanelButton() {
		clientThread.invoke(() -> {
			try {
				clientToolbar.removeNavigation(navButton);
			} catch(Exception ignored) {}

			if(config.showPanelButton()) {
				navButton = buildNavigationButton();
				clientToolbar.addNavigation(navButton);

				updatePanelItem();
			}
            queuedToolbarUpdate = false;
		});
	}
	// Must only be run on clientThread
	private void updatePanelItem()
	{
		String name = itemManager.getItemComposition(currentItemID).getName();
		panel.updateItemName(name);
        panel.updateItemIcon(currentItem);
	}
	public void updateFromSearch()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(panel,
					"You must be logged in to search.",
					"Cannot Search for Item",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		itemSearch
				.tooltipText("Set item to")
				.onItemSelected((itemId) -> {
					clientThread.invokeLater(() ->
					{
						int finalId = itemManager.canonicalize(itemId);
						configManager.setConfiguration("localconsciousness", "item", finalId);
					});
				})
				.build();
	}

	public void randomizeItem() {
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			JOptionPane.showMessageDialog(panel,
					"You must be logged in to randomize.",
					"Cannot Randomize Item",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		int randomId = validIdList.get(rand.nextInt(validIdList.size()));
        configManager.setConfiguration("localconsciousness", "item", randomId);
	}

	@Provides
	LocalConsciousnessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LocalConsciousnessConfig.class);
	}

	private NavigationButton buildNavigationButton() {
		panel = new LocalConsciousnessPanel(client, config, this, configManager);
		navButton = NavigationButton.builder()
				.tooltip("Local Consciousness")
				.priority(6)
				.panel(panel)
				.icon(currentItem)
				.build();
		return navButton;
	}
}