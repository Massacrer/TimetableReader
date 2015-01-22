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
		class ViewHolder
		{
			public TextView dayText, dateText, summaryText;
			public ViewGroup colouredBoxHolder;
		}
		
		Day day = days.get(position);
		
		View row = convertView;
		
		if (row == null)
		{
			// ignore lint warning about null root - we're not putting the new
			// view into the heirarchy yet
			row =
					mainActivity.getLayoutInflater().inflate(
							R.layout.day_layout, null);
			
			ViewHolder vh = new ViewHolder();
			vh.colouredBoxHolder =
					(ViewGroup) row.findViewById(R.id.colouredBoxHolder);
			vh.dateText = (TextView) row.findViewById(R.id.dateText);
			vh.dayText = (TextView) row.findViewById(R.id.dayText);
			vh.summaryText = (TextView) row.findViewById(R.id.summaryText);
			row.setTag(vh);
			
			MainActivity.ColouredBoxView.setupBoxes(mainActivity,
					vh.colouredBoxHolder);
		}
		
		ViewHolder vh = (ViewHolder) row.getTag();
		
		calendar.setTime(day.getDate());
		
		String dayText;
		if (Build.VERSION.SDK_INT < 9)
		{
			dayText = Util.getDisplayName(calendar);
		}
		else
		{
			dayText =
					calendar.getDisplayName(Calendar.DAY_OF_WEEK,
							Calendar.SHORT, Locale.ENGLISH);
		}
		vh.dayText.setText(dayText);
		vh.dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ (calendar.get(Calendar.MONTH) + 1));
		vh.summaryText.setText(day.getSummary());
		
		MainActivity.ColouredBoxView.formatBoxes(vh.colouredBoxHolder, day);
		
		// row.invalidate();
		// row.requestLayout();
		// row.getViewTreeObserver().dispatchOnGlobalLayout();
		
		return row;
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
		/**
		 * for use on pre-api9 devices
		 * 
		 * @param calendar
		 * @return
		 */
		public static String getDisplayName(Calendar calendar)
		{
			switch (calendar.get(Calendar.DAY_OF_WEEK))
			{
				case 1:
					return "Sun";
				case 2:
					return "Mon";
				case 3:
					return "Tue";
				case 4:
					return "Wed";
				case 5:
					return "Thu";
				case 6:
					return "Fri";
				case 7:
					return "Sat";
				default:
					return "";
			}
		}
	}
}
