package me.massacrer.timetablereader;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class NewMain extends Activity
{
	private final List<TableRow> tableRows = new ArrayList<TableRow>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_main);
		
		setupTable();
	}
	
	private void setupTable()
	{
		final int DAYS_IN_WEEK = 7;
		TableLayout table = (TableLayout) findViewById(R.id.main_table);
		for (int day = 0; day < DAYS_IN_WEEK; day++)
		{
			TableRow tr = new TableRow(this);
			// tr.setLayoutParams(new TableRow.LayoutParams(0, 0, 1));
			table.addView(tr);
			tableRows.add(tr);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.new_main, menu);
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
