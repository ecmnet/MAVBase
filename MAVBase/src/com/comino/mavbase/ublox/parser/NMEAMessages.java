package com.comino.mavbase.ublox.parser;

public class NMEAMessages {

	public static final int HEAD1 = 0x24;
	public static final int HEAD2 = 0x47;

	public static final String GGA = "GGA";
	public static final String GSV = "GSV";

	public double  latitude;
	public double  longitude;
	public double  altitude;
	public double  geoidHeight;
	public String  time;
	public int     sats;
	public int     fix;


	public boolean doGGA(String[] words)
	{
		// words won't be null, but it could be the wrong length
		if (words.length < 7)
			return false;

		this.time = words[1];
		this.latitude = this.parseLatitude(words[2], words[3]);
		this.longitude = this.parseLongitude(words[4], words[5]);
		this.fix = Integer.parseInt(words[6]);
		this.sats = Integer.parseInt(words[7]);
		if (words.length >= 11)
			this.altitude = this.parseElevation(words[9], words[10]);
		if (words.length >= 13)
			this.geoidHeight = this.parseElevation(words[11], words[12]);
		return true;
	}

	private double parseLatitude(String angle, String direction)
	{
		if (angle.length() == 0)
			return 0;

		double minutes = angle.length() > 2 ? Double.parseDouble(angle.substring(2, angle.length())) : 0d;
		double degrees = Double.parseDouble(angle.substring(0, 2)) + minutes / 60d;

		return direction.equalsIgnoreCase("S") ? -degrees : degrees;
	}

	private double parseLongitude(String angle, String direction)
	{
		if (angle.length() == 0)
			return 0;

		double minutes = angle.length() > 3 ? Double.parseDouble(angle.substring(3, angle.length())) : 0d;
		double degrees = Double.parseDouble(angle.substring(0, 3)) + minutes / 60d;

		return direction.equalsIgnoreCase("W") ? -degrees : degrees;
	}

	private double parseElevation(String height, String units)
	{
		if (height.length() == 0)
			return 0;

		return Double.parseDouble(height) * unitsToMeters(units);
	}

	private double unitsToMeters(String units)
	{
		double f;

		if (units.equals("M")) // meters
			f = 1d;
		else if (units.equals("f")) // feet
			f = 3.2808399;
		else if (units.equals("F")) // fathoms
			f = 0.5468066528;
		else
			f = 1d;

		return f;
	}

	public String toString()
	{
		return String.format("(%10.8f\u00B0, %11.8f\u00B0, %10.4g m, %10.4g m, %s, %d, %d)", this.latitude, this.longitude,
				this.altitude, this.geoidHeight, this.time, this.sats, this.fix);
	}

}
