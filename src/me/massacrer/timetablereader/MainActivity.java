package me.massacrer.timetablereader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import me.massacrer.ICalParser.*;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.ShapeDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	public static final int FIRST_HOUR = 9;
	public static final int LAST_HOUR = 17;
	
	private static final String URL_BASE =
			"https://studentrecord.aber.ac.uk/en/ical.php?user=";
	private static final String TEST_UID = "DB788938E421CDFC7A3E43F7277208C1";
	
	private static final String FILE_NAME = "saved_events";
	
	private Date lastSync;
	
	private EventManager eventManager;
	// private ArrayList<VEvent> events = new ArrayList<VEvent>();
	
	private ListView mainList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// setupActionBarTabs();
		// setupButton();
		// setupTimetableViews();
		// TODO: remove call - setup done through ListView / adapter
		// setupColouredBoxes();
		readSavedEvents();
		setupListView();
		// refreshEventsList();
		setupSyncButton();
		
		updateSyncText();
		updateAdapter();
	}
	
	private void getNewData()
	{
		SharedPreferences prefs =
				PreferenceManager
						.getDefaultSharedPreferences(MainActivity.this);
		String userUrl = prefs.getString("userUrl", "");
		refreshEventsList(this.makeURL(userUrl));
	}
	
	private void setupSyncButton()
	{
		Button button = ((Button) findViewById(R.id.button1));
		button.setText("Sync");
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getNewData();
			}
		});
	}
	
	private void setupListView()
	{
		mainList = (ListView) findViewById(R.id.mainListView);
		mainList.setAdapter(new TimetableArrayAdapter(this));
		updateAdapter();
	}
	
	private void readSavedEvents()
	{
		if (eventManager == null)
		{
			eventManager = new EventManager();
		}
		try
		{
			ObjectInputStream ois =
					new ObjectInputStream(this.openFileInput(FILE_NAME));
			Date lastSync = (Date) ois.readObject();
			ArrayList<VEvent> events = (ArrayList<VEvent>) ois.readObject();
			ois.close();
			// wait to assign until the read has completed successfully
			this.lastSync = lastSync;
			
			this.eventManager.update(events);
			
			Log.i("TR", "readSavedEvents: lastSync " + lastSync.toString());
		}
		catch (IOException e)
		{
			// DEBUG
			e.printStackTrace();
			this.deleteFile(FILE_NAME);
		}
		catch (ClassNotFoundException e)
		{
			String text =
					"Something went very wrong: ClassNotFoundException on "
							+ "serialized file access, cannot find VEvent";
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}
	}
	
	private boolean writeEvents()
	{
		try
		{
			ObjectOutputStream oos =
					new ObjectOutputStream(this.openFileOutput(FILE_NAME,
							MODE_PRIVATE));
			oos.writeObject(this.lastSync);
			this.eventManager.write(oos);
			oos.close();
			Toast.makeText(this, "events written", Toast.LENGTH_SHORT).show();
			return true;
		}
		catch (IOException e)
		{
			this.deleteFile(FILE_NAME);
			Log.i("TR", "Write failure: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	private void updateSyncText()
	{
		SimpleDateFormat sdf =
				(SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		((TextView) findViewById(R.id.lastSyncText)).setText("Last fetched: "
				+ (lastSync == null ? "(not found, resync)" : sdf
						.format(lastSync)));
	}
	
	/**
	 * called when a new set of events has been pulled from the network
	 */
	private void update(ArrayList<VEvent> events)
	{
		this.lastSync = new Date();
		this.eventManager.update(events);
		writeEvents();
		updateSyncText();
		updateAdapter();
		// DEBUG
		Toast.makeText(this,
				"Populating event list, " + events.size() + " events",
				Toast.LENGTH_LONG).show();
	}
	
	private void updateAdapter()
	{
		TimetableArrayAdapter adapter =
				((TimetableArrayAdapter) mainList.getAdapter());
		
		adapter.update(eventManager
		/*.getWeek(new Date()));*/.getAllEventsByDay());
	}
	
	private void refreshEventsList(URL url)
	{
		setSyncingUI(true);
		NetworkThread nt = new NetworkThread(url);
		nt.start();
	}
	
	private URL makeURL(String extension)
	{
		try
		{
			return new URL(this.URL_BASE + /*this.TEST_UID*/extension);
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	private void setupActionBarTabs()
	{
		ActionBar.TabListener tabListener = new ActionBar.TabListener()
		{
			TextView textview1 = (TextView) MainActivity.this
					.findViewById(R.id.dayText);
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft)
			{
				SharedPreferences prefs =
						PreferenceManager
								.getDefaultSharedPreferences(MainActivity.this);
				switch (tab.getPosition())
				{
					case 0:
					{
						textview1.setText("tab 1 selected");
						break;
					}
					case 1:
					{
						String temp = prefs.getString("cat1_edittext", "fail");
						textview1.setText(temp);
						break;
					}
				}
			}
			
			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft)
			{
				textview1.setText("");
			}
			
			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft)
			{
				// nothing
			}
		};
		
		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		bar.addTab(bar.newTab().setText("Tab 1").setTabListener(tabListener));
		bar.addTab(bar.newTab().setText("Tab 2").setTabListener(tabListener));
	}
	
	private void setupButton()
	{
		Button button = (Button) this.findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent =
						new Intent(MainActivity.this, DayViewActivity.class);
				intent.putExtra("date", new Date());
				MainActivity.this.startActivity(intent);
			}
		});
	}
	
	private void setSyncingUI(final boolean syncing)
	{
		
		class Animator implements Runnable
		{
			private final boolean animate;
			private final View view;
			
			public Animator(boolean animate, View view)
			{
				this.animate = animate;
				this.view = view;
			}
			
			public void run()
			{
				if (animate)
					view.setVisibility(View.GONE);
			}
		}
		
		final int delay = 500;
		final View progress = findViewById(R.id.syncProgress);
		final View button = findViewById(R.id.button1);
		
		if (syncing)
		{
			progress.setAlpha(0f);
			progress.setVisibility(View.VISIBLE);
		}
		else
		{
			button.setAlpha(0f);
			button.setVisibility(View.VISIBLE);
			mainList.setAlpha(0f);
			mainList.setVisibility(View.VISIBLE);
		}
		
		mainList.animate().alpha(syncing ? 0f : 1f).setDuration(delay)
				.withEndAction(new Animator(syncing, mainList));
		progress.animate().alpha(syncing ? 1f : 0f).setDuration(delay)
				.withEndAction(new Animator(!syncing, progress));
		button.animate().alpha(syncing ? 0f : 1f).setDuration(delay)
				.withEndAction(new Animator(syncing, mainList));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.add(Menu.NONE, 6, 0, "refresh");
		// TODO: comment out for release :(
		menu.add(Menu.NONE, 7, 0, "toggle :)");
		menu.add(0, 8, 0, "temp day");
		menu.add(0, 9, 0, "newmain");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id)
		{
			case R.id.action_settings:
			{
				Intent settingsIntent =
						new Intent(MainActivity.this, SettingsActivity.class);
				MainActivity.this.startActivity(settingsIntent);
				return true;
			}
			case 6:
			{
				View root = findViewById(R.id.root);
				root.invalidate();
				root.requestLayout();
				Toast.makeText(this, "refreshed", Toast.LENGTH_SHORT).show();
				return true;
			}
			case 7:
			{
				mainList.animate().alpha(1 - mainList.getAlpha())
						.setDuration(500);
				return true;
			}
			case 8:
			{
				Intent intent = new Intent(this, DayViewActivity.class);
				intent.putExtra("date", new Date());
				startActivity(intent);
				return true;
			}
			case 9:
			{
				Intent intent = new Intent(this, NewMain.class);
				startActivity(intent);
				return true;
			}
			case R.id.reloadButton:
			{
				getNewData();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	static class ColouredBoxView extends View
	{
		// private Random random = new Random();
		private static TextPaint paint = new TextPaint();
		private int width = 50, hour;
		// private String text;
		private int colour;
		static LinearLayout.LayoutParams layoutParams =
				new LinearLayout.LayoutParams(/*LayoutParams.WRAP_CONTENT*/0,
						LayoutParams.WRAP_CONTENT, 1);
		private static ShapeDrawable border;
		
		ColouredBoxView(Context context, int hour)
		{
			super(context);
			this.hour = hour;
			// this.colour = 0x0;
			
			paint.setTextSize(context.getResources().getDimensionPixelSize(
					R.dimen.text_size));
			paint.setTextAlign(Align.LEFT);
			paint.setStyle(Style.FILL);
			paint.setStrokeWidth(10f);
			
			// this.setBackgroundColor(colour);
			
			this.setLayoutParams(layoutParams);
		}
		
		public void setSize(int size)
		{
			this.width = size;
		}
		
		public void setColour(int colour)
		{
			this.colour = colour;
		}
		
		@Override
		protected void onMeasure(int w, int h)
		{
			// DEBUG:
			// Log.i("TR", "onMeasure: " + MeasureSpec.getMode(w) + ": "+
			// MeasureSpec.getSize(w) + ":" + MeasureSpec.getSize(h));
			
			switch (MeasureSpec.getMode(w))
			{
				case 0:// MeasureSpec.UNSPECIFIED: // case error...
				{
					setMeasuredDimension(width, width);
					break;
				}
				case MeasureSpec.AT_MOST:
				case MeasureSpec.EXACTLY:
				{
					int result = MeasureSpec.getSize(w);
					setMeasuredDimension(result, result);
					break;
				}
			}
			return;
			// Log.i("TR", "onMeasure(" + w + ", " + h);
			// this.setMeasuredDimension(width, width);
		}
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			int width = this.getWidth();
			int height = this.getHeight();
			
			// int colour = random.nextInt();
			// colour |= 0xff000000;
			paint.setColor(colour);
			canvas.drawPaint(paint);
			
			paint.setColor(Color.BLACK);
			
			// top
			canvas.drawLine(0, 0, width, 0, paint);
			// left
			canvas.drawLine(0, 0, 0, height, paint);
			// right
			if (hour == MainActivity.LAST_HOUR)
				canvas.drawLine(width, 0, width, height, paint);
			// bottom
			canvas.drawLine(0, height, width, height, paint);
			// canvas.drawText(text, 0, width / 2, paint);
		}
		
		static void setupBoxes(final Context context,
				final ViewGroup colouredBoxHolder)
		{
			
			// Log.i("TR", "setupBoxes: " + colouredBoxHolder.toString());
			
			for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++)
			{
				ColouredBoxView box = new ColouredBoxView(context, hour);
				// box.setBackgroundColor(Color.BLACK);
				box.setPadding(3, 3, 3, 3);
				
				colouredBoxHolder.addView(box);
			}
		}
		
		static void
				formatBoxes(final ViewGroup colouredBoxHolder, final Day day)
		{
			for (int i = 0; i < colouredBoxHolder.getChildCount(); i++)
			{
				ColouredBoxView box =
						(ColouredBoxView) colouredBoxHolder.getChildAt(i);
				VEvent event = day.get(i);
				int colour = EventManager.getColour(event);
				box.setBackgroundColor(colour);
			}
		}
	}
	
	private class NetworkThread extends Thread
	{
		private URL url;
		ArrayList<VEvent> events = null;
		String error = "";
		
		public NetworkThread(URL url)
		{
			this.url = url;
		}
		
		@Override
		public void run()
		{
			ConnectivityManager cm =
					(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (ni == null || !ni.isConnected())
			{
				error = "Not connected to network, can't download";
			}
			else
			{
				try
				{
					events = Parser.parseFromURL(url);
				}
				catch (IllegalArgumentException e)
				{
					error = "URL did not contain a valid VCalendar object";
				}
				catch (IOException e)
				{
					error = "IO exception while trying to access URL";
				}
				MainActivity.this.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						if (events == null)
						{
							Toast.makeText(MainActivity.this, error,
									Toast.LENGTH_LONG).show();
						}
						else
						{
							update(events);
						}
						MainActivity.this.setSyncingUI(false);
					}
				});
			}
		}
	}
}