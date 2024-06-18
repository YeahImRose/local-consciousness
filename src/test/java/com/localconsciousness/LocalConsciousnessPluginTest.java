package com.localconsciousness;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LocalConsciousnessPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LocalConsciousnessPlugin.class);
		RuneLite.main(args);
	}
}