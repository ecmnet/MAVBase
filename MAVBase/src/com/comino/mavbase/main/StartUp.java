package com.comino.mavbase.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.mavlink.messages.lquac.msg_gps_rtcm_data;

import com.comino.mav.control.IMAVController;
import com.comino.mav.control.impl.MAVUdpController;
import com.comino.mavbase.ublox.reader.StreamEventListener;
import com.comino.mavbase.ublox.reader.UBXSerialConnection;

public class StartUp {


	private IMAVController control = null;


	public StartUp() {
		System.out.println("MAVBase initializing");

		control = new MAVUdpController("172.168.1.1",14555,14550, false);

		if(!control.isConnected())
			control.connect();

		Vector<String> ports = UBXSerialConnection.getPortList(true);

		if(ports.size()==0) {
			control.close();
			System.out.println("No port found");
			return;
		}

		UBXSerialConnection ubx = new UBXSerialConnection(ports.firstElement(), 9600);

		ubx.addStreamEventListener( new StreamEventListener() {

			@Override
			public void streamClosed() {
				control.close();

			}

			@Override
			public void getPosition(double lat, double lon, double altitude, int fix, int sats) {
				System.out.println("Lat: "+lat+" Lon: "+lon+ " Alt: "+altitude+" Sat: "+sats);

			}

			@Override
			public void getRTCM3(byte[] buffer, int len) {

				System.out.println("Sending RTCM3: "+len);

				msg_gps_rtcm_data msg = new msg_gps_rtcm_data(2,1);
				if(len < msg.data.length) {
					msg.flags = 0;
					msg.len   = len;
					for(int i = 0;i<len;i++)
						msg.data[i] = buffer[i];
					control.sendMAVLinkMessage(msg);
				} else {
					int start = 0;
					while (start < len) {
						int length = Math.min(len - start, msg.data.length);
						msg.flags = 1;
						msg.len   = length;
						for(int i = start;i<length;i++)
							msg.data[i] = buffer[i];
						control.sendMAVLinkMessage(msg);
						start += length;
					}
				}

			}

		});

		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new StartUp();
	}

}
