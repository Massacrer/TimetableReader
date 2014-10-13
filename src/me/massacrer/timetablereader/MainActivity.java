package me.massacrer.timetablereader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import me.massacrer.ICalParser.*;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
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
	
	private ListView listView;
	
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
	
	private void setupSyncButton()
	{
		Button button = ((Button) findViewById(R.id.button1));
		button.setText("Sync");
		button.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//TODO: use an actual url from settings
				MainActivity.this.refreshEventsList(MainActivity.this
						.tempMakeURL());
			}
		});
	}
	
	private void setupListView()
	{
		listView = new ListView(this);
		RelativeLayout.LayoutParams rlp =
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
		rlp.addRule(RelativeLayout.BELOW, R.id.lastSync);
		listView.setLayoutParams(rlp);
		listView.setAdapter(new TimetableArrayAdapter(this, eventManager
				.getAllEventsByDay()));
		((ViewGroup) findViewById(R.id.root)).addView(listView);
	}
	
	private void readSavedEvents()
	{
		if (eventManager == null)
		{
			eventManager = new EventManager(new ArrayList<VEvent>());
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
		((TextView) findViewById(R.id.lastSyncText)).setText("Last fetched: "
				+ (lastSync == null ? "never" : lastSync.toString()));
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
				((TimetableArrayAdapter) listView.getAdapter());
		
		adapter.update(eventManager.getAllEventsByDay());
	}
	
	private void refreshEventsList(URL url)
	{
		setSyncingUI(true);
		NetworkThread nt = new NetworkThread(url);
		nt.start();
	}
	
	private URL tempMakeURL()
	{
		try
		{
			return new URL(this.URL_BASE + this.TEST_UID);
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
	
	/*private void setupTimetableViews()
	{
		// get holder layout
		TableLayout holder = (TableLayout) findViewById(R.id.timetableHolder);
		TableRow topRow = (TableRow) findViewById(R.id.timetableTopRow);
		// add blank view @ top left
		topRow.addView(new View(this));
		
		TableRow.LayoutParams layoutParams =
				new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT);
		
		// add cells in top row
		// hours in the day - first 0900, last 1700
		for (int i = 9; i <= 17; i++)
		{
			TextView text = new TextView(this);
			String timeStart =
					(i == 9 ? "0" : "") + i + ":" + ((i < 11) ? "00" : "10");
			String timeEnd =
					"" + (i == 9 ? "0" : "")
							+ (i < 11 ? i + ":50" : i + 1 + "00");
			text.setText(timeStart + "\t" + timeEnd );
			text.setHorizontallyScrolling(false);
			// text.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			// text.setSingleLine(false);
			text.setGravity(Gravity.CENTER_HORIZONTAL);
			text.setLayoutParams(layoutParams);
			
			topRow.addView(text);
		}
	}*/
	
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
	
	private void setSyncingUI(boolean syncing)
	{
		findViewById(R.id.syncProgress).setVisibility(
				syncing ? View.VISIBLE : View.GONE);
		findViewById(R.id.button1).setVisibility(
				syncing ? View.GONE : View.VISIBLE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
			Intent settingsIntent =
					new Intent(MainActivity.this, SettingsActivity.class);
			MainActivity.this.startActivity(settingsIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	static class ColouredBoxView extends View
	{
		private Random random = new Random();
		private static TextPaint paint = new TextPaint();
		private int width, hour;
		// private String text;
		private int colour;
		
		ColouredBoxView(Context context, int width, int hour, /* String text, */
				int colour)
		{
			super(context);
			this.width = width;
			this.hour = hour;
			// this.text = text;
			this.colour = colour;
			
			paint.setTextSize(context.getResources().getDimensionPixelSize(
					R.dimen.text_size));
			paint.setTextAlign(Align.LEFT);
			paint.setStyle(Style.FILL);
			paint.setStrokeWidth(10f);
		}
		
		@Override
		protected void onMeasure(int w, int h)
		{
			this.setMeasuredDimension(width, width);
		}
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			// int colour = random.nextInt();
			// colour |= 0xff000000;
			paint.setColor(colour);
			canvas.drawPaint(paint);
			
			paint.setColor(Color.BLACK);
			
			// top
			canvas.drawLine(0, 0, width, 0, paint);
			// left
			canvas.drawLine(0, 0, 0, width, paint);
			// right
			if (hour == MainActivity.LAST_HOUR)
				canvas.drawLine(width, 0, width, width, paint);
			// bottom
			canvas.drawLine(0, width, width, width, paint);
			// canvas.drawText(text, 0, width / 2, paint);
		}
		
		//TODO: add arguments and impl to allow specification of which boxes
		// to colour in, what colours to use etc
		static void setupBoxes(final Context context,
				final ViewGroup colouredBoxHolder, final Day day)
		{
			
			/*colouredBoxHolder.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener()
					{
						@Override
						public void onGlobalLayout()
						{
							final int HOURS_IN_DAY = LAST_HOUR - FIRST_HOUR;
							int width =
									colouredBoxHolder.getWidth()
											/ (HOURS_IN_DAY + 1);
							ColouredBoxView.layoutBoxes(context,
									colouredBoxHolder, day, width);
							colouredBoxHolder.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
						}
					});*/
			
			OnLayoutChangeListener lcl = new OnLayoutChangeListener()
			{
				@Override
				public void onLayoutChange(View v, int left, int top,
						int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom)
				{
					final int HOURS_IN_DAY = LAST_HOUR - FIRST_HOUR;
					final int width = (right - left) / (HOURS_IN_DAY + 1);
					ColouredBoxView.layoutBoxes(context, colouredBoxHolder,
							day, width);
					// colouredBoxHolder.removeOnLayoutChangeListener(this);
				}
			};
			colouredBoxHolder.addOnLayoutChangeListener(lcl);
		}
		
		private static void layoutBoxes(Context context,
				ViewGroup colouredBoxHolder, Day day, int width)
		{
			for (int hour = FIRST_HOUR; hour <= LAST_HOUR; hour++)
			{
				VEvent event = day.get(Day.index(hour));
				int colour = EventManager.getColour(event);
				
				// TODO: actual colour and text goes here
				View box = new ColouredBoxView(context, width, hour, colour);
				colouredBoxHolder.addView(box);
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
						MainActivity.this.setSyncingUI(false);
						if (events == null)
						{
							Toast.makeText(MainActivity.this, error,
									Toast.LENGTH_LONG).show();
							return;
						}
						update(events);
					}
				});
			}
		}
	}
}