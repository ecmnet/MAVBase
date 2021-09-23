package com.comino.mavbase.main;

import com.comino.mavbase.ublox.reader.StreamEventListener;
import com.comino.mavbase.ublox.reader.UBXSerialConnection;
import com.comino.mavcom.control.IMAVController;
import com.comino.mavcom.control.impl.MAVUdpController;
import com.comino.mavcom.model.DataModel;

public class StartUp {


	private IMAVController control = null;


	public StartUp() {
		System.out.println("MAVBase initializing");

		control = new MAVUdpController("172.168.178.1",14555,14550, false);
//		control.connect();

//		while(!control.isConnected()) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//
//			}
//		}


		UBXSerialConnection ubx = new UBXSerialConnection(9600);
		try {
			ubx.init(60,10f);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		ubx.addStreamEventListener( new StreamEventListener() {

			@Override
			public void streamClosed() {
				System.out.println("RTCM3 connection lost");
			}

			@Override
			public void getPosition(double lat, double lon, double altitude, int fix, int sats) {
				System.out.println("Base position: Lat: "+lat+" Lon: "+lon+ " Alt: "+altitude+" Sat: "+sats);

			}

			@Override
			public void getRTCM3(byte[] buffer, int len) {

	//			System.out.println(len+" "+UBXSerialReader.byteToHex(buffer, len));

//				msg_gps_rtcm_data msg = new msg_gps_rtcm_data(2,1);
//				if(len < msg.data.length) {
//					msg.flags = 0;
//					msg.len   = len;
//					for(int i = 0;i<len;i++)
//						msg.data[i] = buffer[i];
//					control.sendMAVLinkMessage(msg);
//				} else {
//					int start = 0;
//					while (start < len) {
//						int length = Math.min(len - start, msg.data.length);
//						msg.flags = 1;
//						msg.len   = length;
//						for(int i = start;i<length;i++)
//							msg.data[i] = buffer[i];
//						control.sendMAVLinkMessage(msg);
//						start += length;
//					}
//				}

			}

			@Override
			public void getSurveyIn(float time_svin, boolean is_svin, boolean is_valid, float mean_acc) {
				System.out.println("Time:"+time_svin+" SVIN:"+is_svin+" Active:"+is_valid+" Accuracy: "+mean_acc);

			}

		});

		DataModel model = control.getCurrentModel();

		while(true) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new StartUp();
	}

}
