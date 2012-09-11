package com.medo.blogirame;

import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.medo.blogirame.fragments.NewestFragment;
import com.medo.blogirame.fragments.PopularPortraitFragment;
import com.medo.blogirame.utils.Constants;
import com.medo.blogirame.utils.DataBaseCategories;
import com.medo.blogirame.utils.ListObjectBlogPost;
import com.medo.blogirame.utils.ListViewOverscrollBasic;
import com.medo.blogirame.utils.OnFragmentItemClick;


public class Main extends SherlockFragmentActivity implements OnFragmentItemClick {
	
	//variables 
	private String mEmail = null;
	
	//controls
	private PopupWindow mPopUpWindow = null;
	private Menu mMainMenu = null;
	private AlertDialog mOfflineAlertDialog = null;

	//fragment utilities
	FragmentManager fragmentManager = null;
	
	//shared preferences
	private SharedPreferences mPreferences = null;
	
	//listeners
	private ConnectionChangeReceiver mConnectionReceiver;
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			//get the text
			TextView clickedText = (TextView) view;
			String queryString = clickedText.getText().toString();
			//query the database for category slug
			DataBaseCategories database = new DataBaseCategories(view.getContext());
			String serverString = database.getSlugByName(queryString);
			database.closeDB();
			
			//update fragments
			PopularPortraitFragment popularPortraitFragment = (PopularPortraitFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_popular);
			popularPortraitFragment.executeNewServerTask(String.format(Constants.API_CATEGORIES_POPULAR, serverString, "0"));

			NewestFragment newestFragment = (NewestFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_newest);
			newestFragment.executeNewServerTask(String.format(Constants.API_CATEGORIES_NEWEST, serverString, "0"), true);
	        //show loading bar
			getSherlock().setProgressBarIndeterminateVisibility(true);
			//load fade out animation
			findViewById(R.id.linear_main).setAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out));
			//change title
			getSupportActionBar().setTitle(queryString);
			//add back icon
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			//dismiss the pop up window
			mPopUpWindow.dismiss();

		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //feature for displaying progress bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //set the content view with the fragments
        setContentView(R.layout.main);     
        //set progress bar
        setSupportProgressBarIndeterminateVisibility(true);
        //init fragment utilities and shared preferences
        fragmentManager = getSupportFragmentManager();
        mPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        
        //once a month check for categories update
        updateCategories();

    }
    
    @Override
    protected void onResume() {
		//register the connection change receiver
    	mConnectionReceiver = new ConnectionChangeReceiver(); 
		this.registerReceiver(mConnectionReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    	
		//icon should be changed on here since user can log in from the reader activity as well
    	if(mPreferences.getString(Constants.PREFERENCE_PASSWORD, null) == null) { 
    		
    	}
    	else {
    		mMainMenu.findItem(R.id.menu_profile).setIcon(R.drawable.ic_action_logged_in);
    	}
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	this.unregisterReceiver(mConnectionReceiver);
    	super.onPause();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   	//inflate the main menu
	   	MenuInflater inflater = getSupportMenuInflater();
	   	inflater.inflate(R.menu.menu_main, menu);
		
	   	mMainMenu = menu;
	   	
	   	//TODO: remove this when login will be available
	   	mMainMenu.findItem(R.id.menu_profile).setVisible(false);
	
	   	return super.onCreateOptionsMenu(menu);
	}

	//this is used for a workaround, no real impact on code
	@SuppressWarnings({ "deprecation" })
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_categories:
			
			if(mPopUpWindow == null) {
				//get entries from database and make adapter
				DataBaseCategories database = new DataBaseCategories(this);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.pop_up_categories_text, database.getCategories());
				database.closeDB();
				//inflate custom views and set adapter
				LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
				View view = inflater.inflate(R.layout.pop_up_categories, null, false);
				ListViewOverscrollBasic list = (ListViewOverscrollBasic) view.findViewById(R.id.list_categories);
				//add listener for item click
				list.setOnItemClickListener(mItemClickListener);
				list.setAdapter(adapter);
				//make pop up window and apply workaround to enable dialog dismissal 
				mPopUpWindow = new PopupWindow(view , (int) (findViewById(R.id.linear_main).getWidth()*0.8), (int) (findViewById(R.id.linear_main).getHeight()*0.9), true);
				mPopUpWindow.setBackgroundDrawable(new BitmapDrawable());
				mPopUpWindow.setOutsideTouchable(true);
				//position it where the category item is
				mPopUpWindow.showAsDropDown(findViewById(R.id.menu_categories));
			}
			//toggle pop up window state
			else if(mPopUpWindow.isShowing())
				mPopUpWindow.dismiss();
			else
				mPopUpWindow.showAsDropDown(findViewById(R.id.menu_categories));;
	
			break;
		case R.id.menu_popular:
			//scroll to top and show popular if menu item is clicked 
			item.setVisible(false);
			NewestFragment fragment = (NewestFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_newest);
			fragment.scrollToTop();
			break;
		case R.id.menu_refresh:
			//this is basically the same as calling the recreate() method on Android API 11 and greater
			finish();
			startActivity(getIntent());
			break;
		case R.id.menu_profile:
			//TODO: uncomment this when login will be available
			//if the user is never logged in show login dialog
			if(mPreferences.getString(Constants.PREFERENCE_PASSWORD, null) == null) {
				showLoginDialog(null);
			}
			//or show the logout dialog
			else {
				showLogoutDialog();
			}
			break;
		case R.id.menu_toggle_image_loading:
			//toggle menu item state for enabling/disabling images 
			if(mPreferences.getBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, true)) {
				mPreferences.edit().putBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, false).commit();
				mMainMenu.findItem(R.id.menu_toggle_image_loading).setTitle(R.string.menu_image_loading_enable);
			}
			else {
				mPreferences.edit().putBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, true).commit();
				mMainMenu.findItem(R.id.menu_toggle_image_loading).setTitle(R.string.menu_image_loading_disable);
			}
			break;
		case android.R.id.home:
			//update fragments
			PopularPortraitFragment popularPortraitFragment = (PopularPortraitFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_popular);
			popularPortraitFragment.executeNewServerTask(Constants.API_MAIN_POPULAR);
			
			NewestFragment newestFragment = (NewestFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_newest);
			newestFragment.executeNewServerTask(String.format(Constants.API_MAIN_NEWEST, "0"), true);
	        //show loading bar
			getSherlock().setProgressBarIndeterminateVisibility(true);
			//load fade out animation
			findViewById(R.id.linear_main).setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));
			//change title
			getSupportActionBar().setTitle(R.string.app_name);
			//add back icon
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);;
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateCategories() {
        int savedMonth = getPreferences(Context.MODE_PRIVATE).getInt(Constants.PREFERENCE_CATEGORIES, -1);
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        if(savedMonth != currentMonth) {
        	GetFromServer task = new GetFromServer();
        	task.execute();
        }
	}

	private void showLogoutDialog() {
		
   		//make alert dialog with users e-mail as title and prompt for logging out
		new AlertDialog.Builder(this)
		.setMessage(R.string.dialog_logout_message)
		.setTitle(mPreferences.getString(Constants.PREFERENCE_EMAIL, ""))
		.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mMainMenu.findItem(R.id.menu_profile).setIcon(R.drawable.ic_action_logged_out);
				
				// no need for server communication, just erase the credentials in shared preferences
				mPreferences.edit().putString(Constants.PREFERENCE_EMAIL, null).commit();
				mPreferences.edit().putString(Constants.PREFERENCE_PASSWORD, null).commit();
				
			}
		})
		.setNegativeButton(R.string.btn_cancel, null)
		.setCancelable(true)
		.show();

	}

	private void showLoginDialog(String emailText) {
   		//inflate he custom layout with edit fields for e-mail and password
		final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_login, null, false);
		
		if(emailText != null) {
			EditText emailField = (EditText) dialogLayout.findViewById(R.id.edit_dialog_login_email);
			emailField.setText(emailText);
		}
   		//make alert dialog
		new AlertDialog.Builder(this)
		.setView(dialogLayout)
		.setTitle(R.string.dialog_login_title)
		.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//send login credentials to server for verification
				EditText email = (EditText) dialogLayout.findViewById(R.id.edit_dialog_login_email);
				EditText password = (EditText) dialogLayout.findViewById(R.id.edit_dialog_login_password);
				//save the e-mail in case login failure
				mEmail = email.getText().toString();
				LoginToServer task = new LoginToServer();
				task.execute(new String[] {email.getText().toString(), password.getText().toString()});
			}
		})
		.setNegativeButton(R.string.btn_cancel, null)
		.setCancelable(true)
		.show();
	}
	
	private void showLoginFailedDialog() {
   		//make alert dialog informing user about login failure
		new AlertDialog.Builder(this)
		.setMessage(R.string.dialog_login_failed_message)
		.setTitle(R.string.dialog_login_failed_title)
		.setPositiveButton(R.string.btn_confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//show login dialog again
				showLoginDialog(mEmail);
				
			}
		})
		.setNegativeButton(R.string.btn_register, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//open external browser for registering
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_BLOGIRAME_REGISTER));
				startActivity(browserIntent);

			}
		})
		.setCancelable(true)
		.show();
	}
	
	@Override
	public void showOfflineAlert() {
		//this is the same offline alert as the one in Newest Fragment
		mOfflineAlertDialog = new AlertDialog.Builder(this)
		.setMessage(R.string.toast_no_internet)
		.setPositiveButton(R.string.btn_settings, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//hide the progress bar and rearrange menu items
				setSupportProgressBarIndeterminateVisibility(false);
				changeMenuItemsForOfflineMode();
				//make new intent for wifi settings
				Intent networkSettingIntent = null;
				//open appropriate screen according to Android OS version
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					networkSettingIntent = new Intent(android.provider.Settings.ACTION_SETTINGS);
				else
					networkSettingIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
				startActivity(networkSettingIntent);
			}
		})
		.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//hide the progress bar and rearrange menu items
				setSupportProgressBarIndeterminateVisibility(false);
				changeMenuItemsForOfflineMode();
			}
		})
		.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				//hide the progress bar and rearrange menu items
				setSupportProgressBarIndeterminateVisibility(false);
				changeMenuItemsForOfflineMode();
			}
		})
		.create();
		
		mOfflineAlertDialog.show();
	}

	@Override
	public void openReader(ListObjectBlogPost blogPost) {
		//standard connectivity check
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected()) {
			showOfflineAlert();
		} 
		else {
			//a fragment based approach is preferred, but decided to go with an activity
			//fragment replacement is only an option if the fragments are added dynamically 
			//but even then only one can be replaced, the other should replaced/hidden which is a pain in the ass
			//add parameters and start new activity
			Intent readerIntent = new Intent(this, Reader.class);
			readerIntent.putExtra(Constants.JSON_PARAM_URL_CANONICAL, blogPost.getmMobileURL());
			readerIntent.putExtra(Constants.JSON_PARAM_URL_BLOGIRAME, blogPost.getmURL());
			readerIntent.putExtra(Constants.JSON_PARAM_POST_TITLE, blogPost.getmTitle());
			readerIntent.putExtra(Constants.JSON_PARAM_BLOG_TITLE, blogPost.getmBlog());
			readerIntent.putExtra(Constants.JSON_PARAM_ECHOES, blogPost.getmEchos());
			//start the activity with all the relevant informations
		startActivity(readerIntent);
		}
	}

	@Override
	public void updateProgress(int current, int max) {
		//update the progress bar in the main layout when we get a call from the adapter
		ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_main);
		mProgressBar.setMax(max);
		mProgressBar.setProgress(current);
	}

	@Override
	public void shrinkPopularFragmentHeight(boolean shouldShrink) {
		//this is where the magic happens
		if(shouldShrink) {
			//if the fragment is visible we need to hide it with a fancy animation
			if(getSupportFragmentManager().findFragmentById(R.id.fragment_popular).isVisible()) {
				
				Animation shrinkPopular = AnimationUtils.loadAnimation(this, R.anim.shrink_popular);
				shrinkPopular.setAnimationListener(new Animation.AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						//hide the fragment when the animation ends
						getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentById(R.id.fragment_popular)).commit();	
						mMainMenu.findItem(R.id.menu_popular).setVisible(true);
					}
				});
				//start the animation
				getSupportFragmentManager().findFragmentById(R.id.fragment_popular).getView().startAnimation(shrinkPopular);
			}
		}
		else {
			//if the fragment is hidden we need to show it wit a fancy animation
			if(getSupportFragmentManager().findFragmentById(R.id.fragment_popular).isHidden())
				//just set the animation and show the fragment, listeners are not needed
				getSupportFragmentManager().findFragmentById(R.id.fragment_popular).getView().setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_popular));
				getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentById(R.id.fragment_popular)).commit();
				mMainMenu.findItem(R.id.menu_popular).setVisible(false);
		}
	}

	@Override
	public void changeMenuItemsForOfflineMode() {
		if(mMainMenu != null) {
				mMainMenu.findItem(R.id.menu_categories).setVisible(false);
				mMainMenu.findItem(R.id.menu_popular).setVisible(false);
				mMainMenu.findItem(R.id.menu_profile).setVisible(false);
				mMainMenu.findItem(R.id.menu_refresh).setVisible(true);
		}
	}

	private class LoginToServer extends AsyncTask<String, Void, String[]> {
		
		
		@Override
		protected void onPreExecute() {
			//show progress
			getSherlock().setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}
	
		@Override
		protected String[] doInBackground(String... params) {
			//make default returnig values in case of login failure
			String[] toReturn = new String[2];
			toReturn[0] = null;
			toReturn[1] = null;
			//send credentials for server verification
			
			
			return toReturn;
		}
		
		@Override
		protected void onPostExecute(String[] result) {
			//hide progress
			
			if(result[0] == null || result[1] == null) {
				//erase the credentials in shared preferences
				mPreferences.edit().putString(Constants.PREFERENCE_EMAIL, null).commit();
				mPreferences.edit().putString(Constants.PREFERENCE_PASSWORD, null).commit();
				
				showLoginFailedDialog();
			}
			else {
				mMainMenu.findItem(R.id.menu_profile).setIcon(R.drawable.ic_action_logged_in);
				
				//save credentials in shared preferences
				mPreferences.edit().putString(Constants.PREFERENCE_EMAIL, result[0]).commit();
				mPreferences.edit().putString(Constants.PREFERENCE_PASSWORD, result[1]).commit();
			}
			getSherlock().setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}
		
	}

	private class GetFromServer extends AsyncTask<Void, Integer, Boolean> {
	
		@Override
		protected Boolean doInBackground(Void... params) {
			//get the database and erase all records
			DataBaseCategories database = new DataBaseCategories(getBaseContext());
			database.deleteAll();
			
			try
			{
				//send GET to the server for autocomplete
				HttpGet request = new HttpGet(Constants.API_CATEGORIES_GET);
				//inform the server we want json object
				request.setHeader("Accept", "application/json");
		        request.setHeader("Content-type", "application/json");
		        
		        //execute GET method
		        DefaultHttpClient httpClient = new DefaultHttpClient();
		        HttpResponse response = httpClient.execute(request);
		        //get the response
		        HttpEntity responseEntity = response.getEntity();
		        //convert it to string
		        String jsonString = EntityUtils.toString(responseEntity);
		        //make json object
		        JSONObject jsonObject = new JSONObject(jsonString);
		        
		        //check if  there is error in responce 
		        if(jsonObject.getInt(Constants.JSON_PARAM_ERROR_CODE) != 0) {
		        	//inform the user
		        	new AlertDialog.Builder(getBaseContext())
		        	.setMessage(jsonObject.getInt(Constants.JSON_PARAM_ERROR))
		        	.setPositiveButton("OK", null)
		        	.show();
		        	
		        	return false;
		        }
		        else {
		        	
		        	JSONArray jsonArray = jsonObject.getJSONArray(Constants.JSON_PARAM_RESULTS);
	
		        	for(int i=0; i<jsonArray.length(); i++) {
		        		//get JSON responce and put values into custom object
		        		JSONObject jsonCategory = jsonArray.getJSONObject(i);
		        		//fill database with new values once a month
		        		database.insertInto(jsonCategory.getString(Constants.JSON_PARAM_CATEGORY_NAME), jsonCategory.getString(Constants.JSON_PARAM_CATEGORY_SLUG));
		        	}
		        	//close the database and return succsess
		        	database.closeDB();
		        	return true;
		        }
			}
			catch (Exception e) {
				//delete all entries in case of exception
				database.deleteAll();
				database.closeDB();
				e.printStackTrace();
				return false;
			}
		}	
		
		@Override
		protected void onPostExecute(Boolean result) {
			//get current month
	        Calendar calendar = Calendar.getInstance();
	        int currentMonth = calendar.get(Calendar.MONTH);
			
	        if(result == true) {
				//update the shared preference 
				getPreferences(Context.MODE_PRIVATE).edit().putInt(Constants.PREFERENCE_CATEGORIES, currentMonth).commit();
			}
			else {
				//update the shared preference with error code
				getPreferences(Context.MODE_PRIVATE).edit().putInt(Constants.PREFERENCE_CATEGORIES, -1).commit();
			}
			super.onPostExecute(result);
		}
	}
	
	private class ConnectionChangeReceiver extends BroadcastReceiver {
		
		
	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	
	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

	        if (activeNetInfo != null) {

	        	//check if we have a mobile data connection
	        	if(activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
//			        System.out.println(activeNetInfo.getTypeName());
	        		//check if the user hasn't made a choice about image loading
	        		if(!mPreferences.contains(Constants.PREFERENCE_SHOULD_LOAD_IMAGES)) {
	        			//prompt the user for image loading 
	        			showImageLoadingPromptDialog();
	        		}
	        	}

	        	//refresh the activity if internet connection is gained while the offline message is shown
	        	//useful when user enables wifi from the pull down notification instead of the settings 
	        	if(mOfflineAlertDialog != null && mOfflineAlertDialog.isShowing()) {
	    			//this is basically the same as calling the recreate() method on Android API 11 and greater
	    			finish();
	    			startActivity(getIntent());
	        	}
	        } 
	        else {
	        	
	        }
	     }

		private void showImageLoadingPromptDialog() {
			new AlertDialog.Builder(Main.this)
			.setMessage(R.string.dialog_block_images)
			.setPositiveButton(R.string.btn_images_block, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//disable future image loading and change menu item
					mPreferences.edit().putBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, false).commit();
					mMainMenu.findItem(R.id.menu_toggle_image_loading).setTitle(R.string.menu_image_loading_enable);
				}
			})
			.setNegativeButton(R.string.btn_images_dispay, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//enable future image loading and change menu item
					mPreferences.edit().putBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, true).commit();
					mMainMenu.findItem(R.id.menu_toggle_image_loading).setTitle(R.string.menu_image_loading_disable);
				}
			})
			.show();
		}
	}	
}