package me.massacrer.timetablereader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.util.Log;
import me.massacrer.ICalParser.VEvent;

public class Day implements Comparable<Day>
{
	private final Calendar calendar = Calendar.getInstance();
	//DEBUG - remove
	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final Date day;
	// private ArrayList<VEvent> events = new ArrayList<VEvent>(
	//		MainActivity.LAST_HOUR - MainActivity.FIRST_HOUR);
	private VEvent[] events = new VEvent[MainActivity.LAST_HOUR
			- MainActivity.FIRST_HOUR + 1];
	
	public Day(Date day)
	{
		this.day = day;
	}
	
	public Date getDate()
	{
		// nobody messes with my date
		return (Date) day.clone();
	}
	
	public void put(VEvent event)
	{
		calendar.setTime(event.dateStart);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < MainActivity.FIRST_HOUR || hour > MainActivity.LAST_HOUR)
			return;
		//DEBUG
		Log.i("TR", "Day.put(), i = " + index(hour) + ": " + event.toString());
		// yay saving approx. 16 bytes of space / day
		events[index(hour)] = event;
	}
	
	public VEvent get(int index)
	{
		return events[index];
	}
	
	// saving typing ftw
	public static int index(int hour)
	{
		return hour - MainActivity.FIRST_HOUR;
	}
	
	@Override
	public int compareTo(Day other)
	{
		return Long.signum(this.day.getTime() - other.day.getTime());
	}
	
	public String getSummary()
	{
		
		return null;
	}
}
