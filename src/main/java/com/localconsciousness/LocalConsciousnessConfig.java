package com.localconsciousness;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("localconsciousness")
public interface LocalConsciousnessConfig extends Config
{
	@ConfigItem(
		keyName = "showPanelButton",
		name = "Show Panel Button",
		description = "Whether the panel button is shown or not",
		position = 1
	)
	default boolean showPanelButton () { return true; }
	@ConfigItem(
		keyName = "item",
		name = "Overlay item",
		description = "The item to be displayed",
		hidden = true
	)
	default int item() {
		// What up it's me, Monkfish- from RuneScape!
		return 7946;
	}

	@ConfigItem(
		keyName = "size",
		name = "Size",
		description = "The size of the item",
		hidden = true
	)
	default int size() {
		return 60;
	}

	@ConfigItem(
		keyName = "speed",
		name = "Speed",
		description = "The speed of the item",
		hidden = true
	)
	default int speed() {
		return 50;
	}

	@Range(
			max = 60
	)
	@ConfigItem(
		keyName = "opacity",
		name = "Opacity",
		description = "The opacity of the item",
		hidden = true
	)
	default int opacity() {
		return 70;
	}
}
