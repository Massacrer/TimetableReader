package me.massacrer.timetablereader;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
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
	
	public void update(ArrayList<VEvent> events)
	{
		this.events = events;
		Collections.sort(events);
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
	
	// relies heavily on events being sorted (by date, ascending)
	public ArrayList<Day> getWeek(Date week)
	{
		// get week into a decent format
		Calendar weekStart = Calendar.getInstance();
		weekStart.setTime(week);
		
		ArrayList<Day> result = new ArrayList<Day>();
		
		// isolate conveniently-named variable day
		{
			int day = weekStart.get(Calendar.DAY_OF_WEEK);
			int monday = Calendar.MONDAY;
			if (day != monday)
			{
				// get date at start of week
				weekStart.add(Calendar.DAY_OF_WEEK, monday - day);
			}
		}
		
		{
			Calendar c = (Calendar) weekStart.clone();
			// fill result array with new empty Days
			// days 1-5 (don't care about weekend)
			for (int i = 0; i < 5; i++)
			{
				result.add(new Day(c.getTime()));
				c.add(Calendar.DAY_OF_WEEK, 1);
			}
		}
		
		VEvent currentEvent;
		Calendar currentEventDate = Calendar.getInstance();
		
		// loop past events that are before this week
		int index = 0;
		for (; index < events.size(); index++)
		{
			currentEvent = events.get(index);
			currentEventDate.setTime(currentEvent.dateStart);
			if (currentEventDate.get(Calendar.WEEK_OF_YEAR) == weekStart
					.get(Calendar.WEEK_OF_YEAR))
				break;
			
		}
		
		// current is now the first event this week
		// loop over all events this week
		for (; index < events.size(); index++)
		{
			currentEvent = events.get(index);
			currentEventDate.setTime(currentEvent.dateStart);
			
			// stop if we've got through the week
			if (currentEventDate.get(Calendar.WEEK_OF_YEAR) != weekStart
					.get(Calendar.WEEK_OF_YEAR))
				break;
			
			int i =
					currentEventDate.get(Calendar.DAY_OF_WEEK)
							- weekStart.get(Calendar.DAY_OF_WEEK);
			
			Day day = result.get(i);
			day.put(currentEvent);
		}
		return result;
	}
	
	public ArrayList<Day> getAllEventsByDay()
	{
		if (this.events == null)
			return new ArrayList<Day>(0);
		
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