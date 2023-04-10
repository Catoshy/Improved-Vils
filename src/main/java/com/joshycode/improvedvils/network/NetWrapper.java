package com.joshycode.improvedvils.network;

import com.joshycode.improvedvils.ImprovedVils;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NetWrapper {
	
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(ImprovedVils.MODID);

}
