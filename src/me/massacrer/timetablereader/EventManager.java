package me.massacrer.timetablereader;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import android.graphics.Color;
import android.util.SparseArray;
import me.massacrer.ICalParser.VEvent;

public class EventManager
{
	// private MainActivity mainActivity;
	private ArrayList<VEvent> events;
	
	EventManager()
	{
	}
	
	EventManager(ArrayList<VEvent> events)
	{
		this.events = events;
	}
	
	public void update(ArrayList<VEvent> events)
	{
		this.events = events;
	}
	
	public Day getByDay(Date date)
	{
		if (this.events == null)
			return null;
		
		Day d = new Day(date);
		Calendar day = Calendar.getInstance();
		day.setTime(date);
		Calendar eventDate = Calendar.getInstance();
		
		for (VEvent event : this.events)
		{
			eventDate.setTime(event.dateStart);
			if ((day.get(Calendar.YEAR) == eventDate.get(Calendar.YEAR))
					&& (day.get(Calendar.DAY_OF_YEAR) == eventDate
							.get(Calendar.DAY_OF_YEAR)))
			{
				d.put(event);
			}
		}
		return d;
	}
	
	public ArrayList<Day> getAllEventsByDay()
	{
		if (this.events == null)
			return null;
		
		SparseArray<Day> sa = new SparseArray<Day>();
		
		Calendar cal = Calendar.getInstance();
		
		for (VEvent event : events)
		{
			cal.setTime(event.dateStart);
			
			int dayNum = cal.get(Calendar.DAY_OF_YEAR);
			// if key not mapped 
			if (sa.indexOfKey(dayNum) < 0)
			{
				sa.put(dayNum, new Day(cal.getTime()));
			}
			Day day = sa.get(dayNum);
			day.put(event);
		}
		ArrayList<Day> result = new ArrayList<Day>();
		for (int i = 0; i < sa.size(); i++)
		{
			result.add(sa.valueAt(i));
		}
		Collections.sort(result);
		return result;
	}
	
	public void write(ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(events);
	}
	
	public static int getColour(VEvent event)
	{
		//TODO: preferences lookup etc
		//return 0xff000000;
		return event == null ? Color.TRANSPARENT : Color.RED;
	}
}