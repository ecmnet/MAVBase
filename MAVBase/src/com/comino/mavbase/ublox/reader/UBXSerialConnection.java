/*
 * Copyright (c) 2010 Eugenio Realini, Mirko Reguzzoni, Cryms sagl - Switzerland. All Rights Reserved.
 *
 * This file is part of goGPS Project (goGPS).
 *
 * goGPS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * goGPS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with goGPS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.comino.mavbase.ublox.reader;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import com.comino.mavbase.ublox.util.SerialInputStream;
import com.comino.mavbase.ublox.util.SerialOutputStream;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class UBXSerialConnection  {
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean connected = false;

	private SerialPort serialPort;

	private UBXSerialReader ubxReader;

	private String portName;
	private int speed;
	private int setMeasurementRate = 1;
	private int setEphemerisRate = 10;
	private int setIonosphereRate = 60;
	private boolean enableTimetag = true;
	private Boolean enableDebug = false;
	//private Boolean enableRnxObs = true;
	private List<String> enableNmeaList;
	private String outputDir = "/Users/ecmnet/test";

	public UBXSerialConnection(String portName, int speed) {
		this.portName = portName;
		this.speed = speed;
		enableNmeaList = new ArrayList<String>();
		enableNmeaList.add("GGA");
	}

	@SuppressWarnings("unchecked")
	public static Vector<String> getPortList(boolean showList) {

		String[] list = SerialPortList.getPortNames();
		Vector<String> portVect = new Vector<String>();
		if(list.length>0) {
			for(int i=0;i<list.length;i++)
				if(list[i].contains("usb"))
				    portVect.add(list[i]);
		}

		if (showList) {
			System.out.println("Found the following ports:");
			for (int i = 0; i < portVect.size(); i++) {
				System.out.println(portVect.elementAt(i));
			}
		}

		return portVect;
	}

	public boolean isConnected() {
		return connected;
	}


	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamResource#init()
	 */

	public void init() throws Exception {

		serialPort = new SerialPort(portName);

		if (serialPort.isOpened()) {
			System.out.println("Error: Port is currently in use");
		} else {
			serialPort.openPort();
			serialPort.setParams(speed, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);


			inputStream = new SerialInputStream(serialPort);
			outputStream = new SerialOutputStream(serialPort);

			ubxReader = new UBXSerialReader(inputStream,outputStream,portName,outputDir);
			ubxReader.setRate(this.setMeasurementRate);
			ubxReader.enableAidEphMsg(this.setEphemerisRate);
			ubxReader.enableAidHuiMsg(this.setIonosphereRate);
			ubxReader.enableSysTimeLog(this.enableTimetag);
			ubxReader.enableDebugMode(this.enableDebug);
			ubxReader.enableNmeaMsg(this.enableNmeaList);
			ubxReader.start();

			connected = true;
			System.out.println("Connection on " + portName + " established");
		}
	}


	/* (non-Javadoc)
	 * @see org.gogpsproject.StreamResource#release(boolean, long)
	 */

	public void release(boolean waitForThread, long timeoutMs)
			throws InterruptedException {

		if(ubxReader!=null){
			ubxReader.stop(waitForThread, timeoutMs);
		}

		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			outputStream.close();
			serialPort.closePort();
		} catch (IOException | SerialPortException e) {
			e.printStackTrace();
		}


		connected = false;
		System.out.println("Connection disconnected");

	}


	public void addStreamEventListener(StreamEventListener streamEventListener) {
		ubxReader.addStreamEventListener(streamEventListener);
	}

	public Vector<StreamEventListener> getStreamEventListeners() {
		return ubxReader.getStreamEventListeners();
	}


	public void removeStreamEventListener(
			StreamEventListener streamEventListener) {
		ubxReader.removeStreamEventListener(streamEventListener);
	}

	public void setMeasurementRate(int measRate) {
		if(ubxReader!=null){
			ubxReader.setRate(measRate);
		} else {
			this.setMeasurementRate = measRate;
		}
	}

	public void enableNmeaSentences(List<String> nmeaList) {
		if(ubxReader!=null){
			ubxReader.enableNmeaMsg(nmeaList);
		} else {
			this.enableNmeaList = nmeaList;
		}
	}



	public void enableDebug(Boolean enableDebug) {
		if(ubxReader!=null){
			ubxReader.enableDebugMode(enableDebug);
		} else {
			this.enableDebug = enableDebug;
		}
	}

	public void setOutputDir(String outDir) {
		if(ubxReader!=null){
			ubxReader.setOutputDir(outDir);
		} else {
			this.outputDir = null;
		}
	}
}
