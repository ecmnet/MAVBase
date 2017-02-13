package com.comino.mavbase.ublox.parser;

public class RTCMMessage {

	public static final int RTCM3_PREAMBLE = 0xD3;
	public static final int RTCM_INITIAL_BUFFER_LENGTH	= 300;


	private byte[] rtcm_buffer = new byte[RTCM_INITIAL_BUFFER_LENGTH];
	private int pos = 0;


	public RTCMMessage() {

	}


}
