package me.massacrer.timetablereader;

import java.util.Calendar;
import java.util.Date;
import me.massacrer.ICalParser.VEvent;

public class Day implements Comparable<Day>
{
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
		Calendar c = Calendar.getInstance();
		c.setTime(event.dateStart);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		if (hour < MainActivity.FIRST_HOUR || hour > MainActivity.LAST_HOUR)
			return;
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
}
