package com.joshycode.improvedmobs.capabilities.entity;

import java.util.concurrent.Callable;

public class VilPlayerCapabilityFactory implements Callable<IImprovedVilCapability> {

	@Override
	public IImprovedVilCapability call() throws Exception {
		return new ImprovedVilCapability();
	}
}
