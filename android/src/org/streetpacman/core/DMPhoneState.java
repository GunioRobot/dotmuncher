package org.streetpacman.core;

public class DMPhoneState {
	public int phone = -1;
	public double lat = -1;
	public double lng = -1;
	public int idle = -1;

	public volatile int status = DMConstants.PHONE_INIT;
	public volatile boolean alive = true;

	public volatile boolean visible = true;

	public DMStatus dmStatus = new DMStatus(0x0);

}
