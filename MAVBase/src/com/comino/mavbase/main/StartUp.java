package com.comino.mavbase.main;

import com.comino.mavbase.ublox.reader.StreamEventListener;
import com.comino.mavbase.ublox.reader.UBXSerialConnection;
import com.comino.mavcom.control.IMAVController;
import com.comino.mavcom.control.impl.MAVUdpController;
import com.comino.mavcom.model.DataModel;
import com.comino.mavutils.hw.upboard.UpLEDControl;
import com.comino.mavutils.workqueue.WorkQueue;

public class StartUp {

	private final WorkQueue wq = WorkQueue.getInstance();
	private IMAVController control = null;


	public StartUp() {
		
		System.out.println("MAVBase initializing");
		
		wq.start();

				control = new MAVUdpController("172.168.178.1",13555,13550, false);
				control.connect();

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

			ubx.addStreamEventListener( new StreamEventListener() {

				@Override
				public void streamClosed() {
					System.out.println("RTCM3 connection lost");
				}

				@Override
				public void getPosition(double lat, double lon, double altitude, int fix, int sats, float hdop, float vdop) {
					System.out.println("Base position: Lat: "+lat+" Lon: "+lon+ " Alt: "+altitude+" Sat: "+sats+" HDOP: "+hdop+" VDOP: "+vdop);

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

		} catch (Exception e1) {

		}

		//		DataModel model = control.getCurrentModel();

		while(true) {
			try {
				UpLEDControl.flash(UpLEDControl.GREEN, 10);
				Thread.sleep(5000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new StartUp();
	}

}
