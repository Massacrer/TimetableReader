package me.massacrer.timetablereader;

import java.util.Date;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DayViewActivity extends Activity
{
	private Date date;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day_view);
		date = (Date) getIntent().getExtra("date");
		this.temp_toast();
	}
	
	private void temp_toast()
	{
		if (date == null)
		{
			Toast.makeText(
					this,
					"Oops. Failed to retrieve date from intent."
							+ " This shouldn't happen", Toast.LENGTH_LONG)
					.show();
		}
		else
		{
			Toast.makeText(this, date.toString(), Toast.LENGTH_LONG).show();
		}
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
