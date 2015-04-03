package com.team01.disasterrecovery.snapshot;

public interface SnapshotInterface {
	public boolean takeSnapshot();
	public boolean useSnapshot();
	public boolean purgeSnapshot();
}
