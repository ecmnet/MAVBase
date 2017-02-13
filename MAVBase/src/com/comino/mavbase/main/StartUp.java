package com.comino.mavbase.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import com.comino.mavbase.ublox.reader.StreamEventListener;
import com.comino.mavbase.ublox.reader.UBXSerialConnection;

public class StartUp {

	public static void main(String[] args) {
		System.out.println("MAVBase initializing");

		Vector<String> ports = UBXSerialConnection.getPortList(true);

		UBXSerialConnection ubx = new UBXSerialConnection(ports.firstElement(), 9600);

		ubx.addStreamEventListener( new StreamEventListener() {

			@Override
			public void streamClosed() {
				// TODO Auto-generated method stub

			}

			@Override
			public void getPosition(double lat, double lon, double altitude, int fix, int sats) {
				System.out.println("Lat: "+lat+" Lon: "+lon+ " Alt: "+altitude+" Sat: "+sats);

			}

			@Override
			public void getRTCM3(byte[] buffer, int len) {


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

}
