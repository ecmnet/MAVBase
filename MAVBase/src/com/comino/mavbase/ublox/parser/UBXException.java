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
package com.comino.mavbase.ublox.parser;
/**
 * <p>
 *
 * </p>
 *
 * @author Lorenzo Patocchi cryms.com
 */

/**
 * @author Lorenzo
 *
 */
public class UBXException extends Exception {


	/**
	 * @param message
	 */
	public UBXException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UBXException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UBXException(String message, Throwable cause) {
		super(message, cause);
	}

}
