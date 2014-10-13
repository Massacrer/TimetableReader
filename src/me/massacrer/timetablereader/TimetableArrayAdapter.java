package me.massacrer.timetablereader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import me.massacrer.ICalParser.VEvent;
import me.massacrer.timetablereader.MainActivity.ColouredBoxView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TimetableArrayAdapter extends BaseAdapter
{
	private final MainActivity mainActivity;
	private List<Day> days;
	
	Calendar calendar = Calendar.getInstance();
	
	public TimetableArrayAdapter(MainActivity context, List<Day> days)
	{
		this.mainActivity = context;
		this.days = days;
	}
	
	/**
	 * Note: commented lines at the top of this method enable view recycling,
	 * which does not yet work properly. Disabling as a test
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View// row = convertView;
		
		//if (row == null)
		//{
		row =
				mainActivity.getLayoutInflater().inflate(R.layout.day_layout,
						null);
		ViewHolder vh = new ViewHolder();
		vh.colouredBoxHolder =
				(ViewGroup) row.findViewById(R.id.colouredBoxHolder);
		vh.dateText = (TextView) row.findViewById(R.id.dateText);
		vh.dayText = (TextView) row.findViewById(R.id.dayText);
		vh.summaryText = (TextView) row.findViewById(R.id.summaryText);
		//row.setTag(vh);
		//}
		
		//ViewHolder vh = (ViewHolder) row.getTag();
		Day day = days.get(position);
		
		calendar.setTime(day.getDate());
		
		vh.dayText.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK,
				Calendar.SHORT, Locale.ENGLISH));
		
		vh.dateText.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ (calendar.get(Calendar.MONTH) + 1));
		
		vh.summaryText.setText(getSummary(day));
		
		vh.colouredBoxHolder.setTag(Boolean.valueOf(false));
		
		// row.invalidate();
		// row.requestLayout();
		// row.getViewTreeObserver().dispatchOnGlobalLayout();
		
		vh.colouredBoxHolder.removeAllViews();
		MainActivity.ColouredBoxView.setupBoxes(mainActivity,
				vh.colouredBoxHolder, day);
		
		/*row.invalidate();
		row.requestLayout();
		row.getViewTreeObserver().dispatchOnGlobalLayout();*/
		
		return row;
	}
	
	private String getSummary(Day day)
	{
		//TODO
		return "Summary goes here :)";
	}
	
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
		return days.size();
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
}
