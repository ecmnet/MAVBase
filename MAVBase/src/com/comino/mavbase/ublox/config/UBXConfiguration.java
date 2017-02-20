package com.comino.mavbase.ublox.config;

import java.nio.ByteBuffer;
import java.util.Vector;

public class UBXConfiguration {

	public final int uBloxPrefix1 = 0xB5;
	public final int uBloxPrefix2 = 0x62;

	private int CK_A;
	private int CK_B;
	private Vector<Integer> msg;

	public void UBXStartSurveyIn(int max_time, float accuracy) {

		msg = new Vector<Integer>();
		msg.addElement(new Integer(uBloxPrefix1));
		msg.addElement(new Integer(uBloxPrefix2));
		msg.addElement(new Integer(0x06)); // CFG
		msg.addElement(new Integer(0x71)); // TMODE3
		msg.addElement(new Integer(40)); // length low
		msg.addElement(new Integer(0)); // length hi
		msg.addElement(new Integer(0x00)); // version
		msg.addElement(new Integer(0x00)); // reserved
		msg.addElement(new Integer(0x01)); // Survey in
		msg.addElement(new Integer(0x00)); // version

		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));

		byte[] b_tim = ByteBuffer.allocate(4).putInt(max_time).array();
		msg.addElement(new Integer(b_tim[3]));
		msg.addElement(new Integer(b_tim[2]));
		msg.addElement(new Integer(b_tim[1]));
		msg.addElement(new Integer(b_tim[0]));

		byte[] b_acc = ByteBuffer.allocate(4).putInt((int)(accuracy * 10000f)).array();
		msg.addElement(new Integer(b_acc[3]));
		msg.addElement(new Integer(b_acc[2]));
		msg.addElement(new Integer(b_acc[1]));
		msg.addElement(new Integer(b_acc[0]));

		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));
		msg.addElement(new Integer(0));

		checkSum();
		msg.addElement(new Integer(CK_A));
		msg.addElement(new Integer(CK_B));

	}


	public byte[] getByte() {
		byte[] bytes = new byte[msg.size()];
		for (int i = 0; i < msg.size(); i++) {
			bytes[i] = (byte) ((byte)(msg.elementAt(i)).intValue());
		}
		return bytes;
	}

	private void checkSum() {
		CK_A = 0;
		CK_B = 0;
		for (int i = 2; i < msg.size(); i++) {
			CK_A = CK_A + ((Integer) msg.elementAt(i)).intValue();
			CK_B = CK_B + CK_A;

		}
		CK_A = CK_A & 0xFF;
		CK_B = CK_B & 0xFF;
	}

}
