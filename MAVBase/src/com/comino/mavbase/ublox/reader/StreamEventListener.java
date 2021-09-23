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



/**
 * @author Cryms.com
 *
 */
public interface StreamEventListener {

	public void streamClosed();
	public void getPosition(double lat, double lon, double altitude, int fix, int sats, float hdop, float vdop);
	public void getRTCM3(byte[] buffer, int len);
	public void getSurveyIn(float time_svin, boolean is_svin, boolean is_valid, float mean_acc);

}
