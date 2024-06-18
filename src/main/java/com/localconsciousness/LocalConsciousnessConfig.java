package com.localconsciousness;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("localconsciousness")
public interface LocalConsciousnessConfig extends Config
{
	@ConfigItem(
		keyName = "item",
		name = "Overlay item",
		description = "The item to be displayed",
		position = 1
	)
	default int item() {
		// What up it's me, Monkfish- from RuneScape!
		return 7946;
	}

	@ConfigItem(
		keyName = "size",
		name = "Size",
		description = "The size of the item",
		position = 2
	)
	default int size() {
		return 100;
	}

	@ConfigItem(
			keyName = "speed",
			name = "Speed",
			description = "The speed of the item",
			position = 3
	)
	default int speed() {
		return 50;
	}

	@Range(
			max = 100
	)
	@ConfigItem(
			keyName = "opacity",
			name = "Opacity",
			description = "The opacity of the item",
			position = 4
	)
	default int opacity() {
		return 100;
	}
}
