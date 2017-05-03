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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.comino.mavbase.ublox.config.UBXConfiguration;
import com.fazecast.jSerialComm.SerialPort;


public class UBXSerialConnection  {
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean connected = false;

	private SerialPort serialPort;

	private UBXSerialReader ubxReader;

	private int speed;
	private int setMeasurementRate = 5;
	private boolean enableTimetag = true;
	private Boolean enableDebug = false;
	private List<String> enableNmeaList;
	private String outputDir = null;

	private UBXConfiguration conf = new UBXConfiguration();


	public UBXSerialConnection(int speed) {
		this.speed = speed;
		enableNmeaList = new ArrayList<String>();
		enableNmeaList.add("GGA");
	}

	@SuppressWarnings("unchecked")
	private static Vector<SerialPort> getPortList(boolean showList) {

		SerialPort[] list = SerialPort.getCommPorts();
		Vector<SerialPort> portVect = new Vector<SerialPort>();
		if(list.length>0) {
			for(int i=0;i<list.length;i++)
				if(list[i].getSystemPortName().contains("usb"))
					portVect.add(list[i]);
		}

		if (showList) {
			if(portVect.size()>0) {
				System.out.println("Found the following ports:");
				for (int i = 0; i < portVect.size(); i++) {
					System.out.println(portVect.elementAt(i));
				}
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

	public void init(int time, float accuracy) throws Exception {

		this.serialPort = getPortList(false).firstElement();

		if (serialPort.isOpen()) {
			System.out.println("Error: Port is currently in use");
		} else {
			serialPort.openPort();
			serialPort.setComPortParameters(speed, 8,SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
			serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 5000, 5000);


			inputStream = serialPort.getInputStream();
			outputStream = serialPort.getOutputStream();


			conf.UBXStartSurveyIn(time, accuracy);
			outputStream.write(conf.getByte());
			outputStream.flush();

			ubxReader = new UBXSerialReader(inputStream,outputStream,serialPort.getDescriptivePortName(),outputDir);
			ubxReader.setRate(this.setMeasurementRate);
			ubxReader.enableDebugMode(this.enableDebug);
			ubxReader.enableNmeaMsg(this.enableNmeaList);
			ubxReader.start();

			connected = true;
			System.out.println("Connection to " + serialPort.getDescriptivePortName() + " established");
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
		} catch (IOException  e) {
			e.printStackTrace();
		}


		connected = false;
		System.out.println(serialPort.getDescriptivePortName()+" disconnected");

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
