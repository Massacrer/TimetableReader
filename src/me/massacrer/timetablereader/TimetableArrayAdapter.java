package me.massacrer.timetablereader;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TimetableArrayAdapter extends BaseAdapter
{
	private final MainActivity mainActivity;
	private List<Day> days;
	
	Calendar calendar = Calendar.getInstance();
	
	public TimetableArrayAdapter(MainActivity context)
	{
		this.mainActivity = context;
	}
	
	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Day day = days.get(position);
		
		View row = convertView;
		
		if (row == null)
		{
			row =
					mainActivity.getLayoutInflater().inflate(
							R.layout.day_layout, null);
			ViewHolder vh = new ViewHolder();
			vh.colouredBoxHolder =
					(ViewGroup) row.findViewById(R.id.colouredBoxHolder);
			vh.dateText = (TextView) row.findViewById(R.id.dateText);
			vh.dayText = (TextView) row.findViewById(R.id.dayText);
			vh.summaryText = (TextView) row.findViewById(R.id.summaryText);
			
			MainActivity.ColouredBoxView.setupBoxes(mainActivity,
					vh.colouredBoxHolder);
			
			row.setTag(vh);
		}
		
		ViewHolder vh = (ViewHolder) row.getTag();
		
		calendar.setTime(day.getDate());
		
		if (Build.VERSION.SDK_INT < 9)
		{
			String dayText = Util.getDisplayName(calendar);
			vh.dayText.setText(dayText);
		}
		else
		{
			vh.dayText.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK,
					Calendar.SHORT, Locale.ENGLISH));
		}
		
		vh.dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ (calendar.get(Calendar.MONTH) + 1));
		
		vh.summaryText.setText(day.getSummary());
		
		// vh.colouredBoxHolder.setTag(Boolean.valueOf(false));
		
		// row.invalidate();
		// row.requestLayout();
		// row.getViewTreeObserver().dispatchOnGlobalLayout();
		
		MainActivity.ColouredBoxView.formatBoxes(vh.colouredBoxHolder, day);
		
		row.invalidate();
		row.requestLayout();
		// row.getViewTreeObserver().dispatchOnGlobalLayout();
		
		return row;
	}
	
	/**
	 * for use on pre-api9 devices
	 * @param calendar
	 * @return
	 */
	
	private class ViewHolder
	{
		public TextView dayText, dateText, summaryText;
		public ViewGroup colouredBoxHolder;
	}
	
	public void update(List<Day> days)
	{
		this.days = days;
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount()
	{
		return days == null ? 0 : days.size();
	}
	
	@Override
	public Day getItem(int position)
	{
		return days.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	private static class Util
	{
		public static String getDisplayName(Calendar calendar)
		{
			String name;
			switch (calendar.get(Calendar.DAY_OF_WEEK))
			{
				case 1:
				{
					name = "Sun";
					break;
				}
				case 2:
				{
					name = "Mon";
					break;
				}
				case 3:
				{
					name = "Tue";
					break;
				}
				case 4:
				{
					name = "Wed";
					break;
				}
				case 5:
				{
					name = "Thu";
					break;
				}
				case 6:
				{
					name = "Fri";
					break;
				}
				case 7:
				{
					name = "Sat";
					break;
				}
				default:
					name = "";
			}
			return name;
		}
	}
}
