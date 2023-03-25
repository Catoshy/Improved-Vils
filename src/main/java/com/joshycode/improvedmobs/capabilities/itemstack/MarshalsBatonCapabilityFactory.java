package com.joshycode.improvedmobs.capabilities.itemstack;

import java.util.concurrent.Callable;

public class MarshalsBatonCapabilityFactory implements Callable<MarshalsBatonCapability> {

	@Override
	public MarshalsBatonCapability call() throws Exception {
		return new MarshalsBatonCapability();
	}
}