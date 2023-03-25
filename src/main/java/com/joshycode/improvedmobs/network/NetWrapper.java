package com.joshycode.improvedmobs.network;

import com.joshycode.improvedmobs.ImprovedVils;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class NetWrapper {
	
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(ImprovedVils.MODID);

}
