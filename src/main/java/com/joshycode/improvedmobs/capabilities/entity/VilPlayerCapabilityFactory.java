package com.joshycode.improvedmobs.capabilities.entity;

import java.util.concurrent.Callable;

public class VilPlayerCapabilityFactory implements Callable<IVilPlayerIdCapability> {

	@Override
	public IVilPlayerIdCapability call() throws Exception {
		return new VilPlayerIdCapability();
	}
}
