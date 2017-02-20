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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.comino.mavbase.ublox.parser.NMEAMessage;
import com.comino.mavbase.ublox.parser.RTCMMessage;
import com.comino.mavbase.ublox.parser.UBXException;
import com.comino.mavbase.ublox.parser.UBXMessage;
import com.comino.mavbase.ublox.parser.UBXMessageType;
import com.comino.mavbase.ublox.parser.UBXMsgConfiguration;
import com.comino.mavbase.ublox.util.InputStreamCounter;
import com.comino.mavbase.ublox.util.UnsignedOperation;



public class UBXSerialReader implements Runnable {



	private InputStreamCounter in;
	private OutputStream out;
	private Thread t = null;
	private boolean stop = false;
	private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();

	private String COMPort;
	private int measRate = 1;
	private List<String> requestedNmeaMsgs = null;
	private String dateFile;
	private String outputDir = null;

	private boolean debugModeEnabled = true;

	public UBXSerialReader(InputStream in,OutputStream out, String COMPort, String outputDir) {
		this(in,out,COMPort,outputDir,null);
		this.COMPort = padCOMSpaces(COMPort);
	}

	public UBXSerialReader(InputStream in,OutputStream out,String COMPort,String outputDir,StreamEventListener streamEventListener) {

		FileOutputStream fos_ubx= null;
		COMPort = padCOMSpaces(COMPort);
		String COMPortStr = prepareCOMStringForFilename(COMPort);


		setOutputDir(outputDir);

		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String date1 = sdf1.format(date);
		SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		dateFile = sdfFile.format(date);
		if(outputDir!=null) {
			try {
				System.out.println(date1+" - "+COMPort+" - Logging UBX stream in "+outputDir+"/"+ COMPortStr+ "_" + dateFile + ".ubx");
				fos_ubx = new FileOutputStream(outputDir+"/"+COMPortStr+ "_" + dateFile + ".ubx");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		this.in = new InputStreamCounter(in,fos_ubx);
		this.out = out;

	}

	public void start()  throws IOException{
		t = new Thread(this);
		t.setName("UBXSerialReader");
		t.start();

		Date date = new Date();
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String date1 = sdf1.format(date);

		System.out.println(date1+" - "+COMPort+" - Measurement rate set at "+measRate+" Hz");
		UBXRateConfiguration ratecfg = new UBXRateConfiguration(1000/measRate, 1, 1);
		out.write(ratecfg.getByte());
		out.flush();

		int nmeaAll[] = { UBXMessageType.NMEA_GGA };
		for (int i = 0; i < nmeaAll.length; i++) {
			UBXMsgConfiguration msgcfg = new UBXMsgConfiguration(UBXMessageType.CLASS_NMEA, nmeaAll[i], false);
			out.write(msgcfg.getByte());
			out.flush();
		}

		int nmeaRequested[];
		try {
			if (requestedNmeaMsgs.isEmpty()) {
				System.out.println(date1+" - "+COMPort+" - NMEA messages disabled");
			} else {
				nmeaRequested = new int[requestedNmeaMsgs.size()];
				for (int n = 0; n < requestedNmeaMsgs.size(); n++) {
					UBXMessageType msgtyp = new UBXMessageType("NMEA", requestedNmeaMsgs.get(n));
					nmeaRequested[n] = msgtyp.getIdOut();
				}
				for (int i = 0; i < nmeaRequested.length; i++) {
					System.out.println(date1+" - "+COMPort+" - NMEA "+requestedNmeaMsgs.get(i)+" messages enabled");
					UBXMsgConfiguration msgcfg = new UBXMsgConfiguration(UBXMessageType.CLASS_NMEA, nmeaRequested[i], true);
					out.write(msgcfg.getByte());
					out.flush();
				}
			}
		} catch (NullPointerException e) {
		}

		if (this.debugModeEnabled) {
			System.out.println(date1+" - "+COMPort+" - !!! DEBUG MODE !!!");
		}
	}
	public void stop(boolean waitForThread, long timeoutMs){
		stop = true;
		if(waitForThread && t!=null && t.isAlive()){
			try {
				t.join(timeoutMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public void run() {

		int data = 0;

		NMEAMessage nmea = new NMEAMessage();

		byte[] buffer = new byte[1024];

		in.start();


		while (!stop) {
			try {
				if(in.available()>0){
					data = in.read() & 0x00FF;
					if(data == UBXMessage.HEAD1){
						data = in.read() & 0x00FF;
						if(data == UBXMessage.HEAD2){
							in.read(buffer, 0, 4);
							int len = (int)(buffer[2] & 0x00FF) | ((buffer[3] & 0x00FF) << 8);
							switch(buffer[0]) {
							case UBXMessage.CLASS_NAV:
								switch(buffer[1]) {
								case UBXMessage.UBX_SVIN:
									in.read(buffer, 0, len+2);
						//			System.out.println(byteToHex(buffer,len));
									int svin_time = readInt(buffer,8);
									boolean valid =  (buffer[36] & 0x01) == 0x01;
									boolean active = (buffer[37] & 0x01) == 0x01;
									float   meanacc  = readInt(buffer, 28) / 10000f;
									for(StreamEventListener sel:streamEventListeners)
										sel.getSurveyIn(svin_time, active, valid, meanacc);
									break;
								default:
									in.read(buffer, 0, len+2);
								}
								break;
								default:
									in.read(buffer, 0, len+2);
							}
						}
					}  else if(data == RTCMMessage.RTCM3_PREAMBLE) {
						buffer[0] = (byte)(data &0x00FF);
						buffer[1] = (byte)(in.read() & 0x00FF);
						buffer[2] = (byte)(in.read() & 0x00FF);
						int len = ((int)(buffer[1] & 0x0003) << 8) | (buffer[2] & 0x00FF);
						in.read(buffer, 3, len+3);

						for(StreamEventListener sel:streamEventListeners)
							sel.getRTCM3(buffer, len+6);

					} else if(data == NMEAMessage.HEAD1){
						if (!requestedNmeaMsgs.isEmpty()) {
							String sentence = "" + (char) data;
							data = in.read();
							if(data == NMEAMessage.HEAD2) {
								sentence = sentence + (char) data;
								data = in.read();
								sentence = sentence + (char) data;
								data = in.read();
								while (data != 0x0A && data != 0xB5) {
									sentence = sentence + (char) data;
									data = in.read();
								}
								sentence = sentence + (char) data;
								String[] words = sentence.split("[,*]");

								if(words[0].contains(NMEAMessage.GGA)) {
									if(nmea.doGGA(words)) {
										for(StreamEventListener sel:streamEventListeners)
											sel.getPosition(nmea.latitude, nmea.longitude, nmea.altitude, nmea.fix, nmea.sats);
									}
								}
							}
						}
					} else {
						if (this.debugModeEnabled) {
							System.out.println("Warning: wrong sync char 1 "+data+" "+Integer.toHexString(data)+" ["+((char)data)+"]");
						}
					}

				} else{
					// no bytes to read, wait 1 msec
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {}
				}
			} catch (Exception e) {
				// error counter
				e.printStackTrace();
				stop=true;
			}

		}

		for(StreamEventListener sel:streamEventListeners){
			sel.streamClosed();
		}

	}


	@SuppressWarnings("unchecked")
	public Vector<StreamEventListener> getStreamEventListeners() {
		return (Vector<StreamEventListener>)streamEventListeners.clone();
	}

	public void addStreamEventListener(StreamEventListener streamEventListener) {
		if(streamEventListener==null) return;
		if(!streamEventListeners.contains(streamEventListener))
			this.streamEventListeners.add(streamEventListener);
	}

	public void removeStreamEventListener(
			StreamEventListener streamEventListener) {
		if(streamEventListener==null) return;
		if(streamEventListeners.contains(streamEventListener))
			this.streamEventListeners.remove(streamEventListener);
	}

	public void setRate(int measRate) {
		this.measRate = measRate;
	}

	public void enableNmeaMsg(List<String> nmeaList) {
		this.requestedNmeaMsgs = nmeaList;
	}

	public void enableDebugMode(Boolean enableDebug) {
		this.debugModeEnabled = enableDebug;
	}

	private String padCOMSpaces(String COMPortIn) {
		if (COMPortIn.substring(0, 3).equals("COM") && COMPortIn.length() == 4) {
			COMPortIn = COMPortIn + " ";
		}
		return COMPortIn;
	}

	private String prepareCOMStringForFilename(String COMPort) {
		String [] tokens = COMPort.split("/");
		if (tokens.length > 0) {
			COMPort = tokens[tokens.length-1].trim();          //for UNIX /dev/tty* ports
		}
		return COMPort;
	}

	public void setOutputDir(String outDir) {
		this.outputDir = outDir;
		if(outDir!=null) {
			File file = new File(outputDir);
			if(!file.exists() || !file.isDirectory()){
				boolean wasDirectoryMade = file.mkdirs();
				if(wasDirectoryMade)System.out.println("Directory "+outputDir+" created");
				else System.out.println("Could not create directory "+outputDir);
			}
		}
	}

	 public final int readInt(byte[] buffer, int offset) throws IOException {
	        return (buffer[3+offset]) << 24 | (buffer[2+offset] & 0xff) << 16 | (buffer[1+offset] & 0xff) << 8 | (buffer[0+offset] & 0xff);
	    }


	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String byteToHex(byte[] bytes, int len) {
		char[] hexChars = new char[len * 2];
		for ( int j = 0; j <len; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
