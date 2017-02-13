package com.comino.mavbase.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import com.comino.mavbase.ublox.reader.UBXSerialConnection;

public class StartUp {

	public static void main(String[] args) {
	     System.out.println("MAVBase initializing");

	     Vector<String> ports = UBXSerialConnection.getPortList(true);

	     UBXSerialConnection ubx = new UBXSerialConnection(ports.firstElement(), 9600);
	     ubx.setOutputDir("/Users/ecmnet/test");

	     try {
			ubx.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
