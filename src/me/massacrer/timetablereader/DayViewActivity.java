package me.massacrer.timetablereader;

import java.util.Calendar;
import java.util.Date;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class DayViewActivity extends Activity
{
	private Date date;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day_view);
		date = /*(Date) getIntent().getExtra("date");*/
		(Date) getIntent().getExtras().get("date");
		if (date == null)
		{
			Toast.makeText(
					this,
					"Oops. Failed to retrieve date from intent."
							+ " This shouldn't happen", Toast.LENGTH_LONG)
					.show();
			this.finish();
		}
		// testing - disabled
		if (Build.VERSION.SDK_INT > 8)
		{
			LinearLayout weekPicker =
					(LinearLayout) this.findViewById(R.id.tempLayout);
			CalendarView cv = new CalendarView(this);
			
			
			
			Calendar cal = Calendar.getInstance();
			
			cal.add(Calendar.WEEK_OF_MONTH, 3);
			cv.setMaxDate(cal.getTimeInMillis());
			
			cal.add(Calendar.WEEK_OF_MONTH, -12);
			cv.setMinDate(cal.getTimeInMillis());
			
			cal.add(Calendar.WEEK_OF_MONTH, 9);
			cv.setDate(cal.getTimeInMillis());
			
			LinearLayout.LayoutParams layoutParams =
					new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.MATCH_PARENT);
			weekPicker.addView(cv, layoutParams);
		}
		this.temp_toast();
	}
	
	private void temp_toast()
	{
		Toast.makeText(this, date.toString(), Toast.LENGTH_LONG).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.day_view, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings)
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
