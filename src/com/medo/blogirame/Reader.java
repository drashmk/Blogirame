package com.medo.blogirame;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.medo.blogirame.utils.Constants;

public class Reader extends SherlockFragmentActivity {
	
	//variables
	private String mEmail = null;
	
	private WebView mWebView = null;
	private ProgressBar mProgressBar = null;
	//shared preferences
	private SharedPreferences mPreferences = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //feature for displaying progress bar
        requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.reader);
		//set title - blog name and display home for navigation
		getSupportActionBar().setTitle(getIntent().getExtras().getString(Constants.JSON_PARAM_BLOG_TITLE));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        mPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
		
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_reader);		
		
		//initialize web view and load the cannonical url
		mWebView = (WebView) findViewById(R.id.web_view_reader);
		setWebViewSettings();
		mWebView.loadUrl(getIntent().getExtras().getString(Constants.JSON_PARAM_URL_CANONICAL));
		
		
	}
	
	@Override
	public void onBackPressed() {
		//navigate trough history of the web view if possible
		if(mWebView.canGoBack())
			mWebView.goBack();
		else 
			super.onBackPressed();
	}

	@Override
		public boolean onPrepareOptionsMenu(Menu menu) {
			//another stupid workaround since ICS cannot display title on the action bar
			//menu items in portrait orientation even though there is enough room.
			//using custom view/layout for displaying echo icon + count
		   	MenuItem echo = menu.findItem(R.id.menu_echo);
		   	TextView echoCount = (TextView) echo.getActionView().findViewById(R.id.txt_menu_echo);
		   	echoCount.setText(""+getIntent().getExtras().getInt(Constants.JSON_PARAM_ECHOES));
		   	//TODO: change this when echo is available
		   	
//		   	echo.getActionView().setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					//disable new click until server responce is received
//	//				v.setEnabled(false);
//			    	if(mPreferences.getString(Constants.PREFERENCE_PASSWORD, null) == null) { 
//			    		showLoginDialog(null);
//			    		Toast.makeText(v.getContext(), R.string.toast_echo_not_logged_in, Toast.LENGTH_SHORT).show();
//			    	}
//			    	else {
//			    		//TODO: send echo request to server (maybe pass the view so it can be enabled on post execute?)
//			    	}
//				}
//			});
			return super.onPrepareOptionsMenu(menu);
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   	//inflate the reader menu
	   	MenuInflater inflater = getSupportMenuInflater();
	   	inflater.inflate(R.menu.menu_reader, menu);
		
	   	return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		switch(item.getItemId()) {
		//close the activity when home is pressed
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_echo:
			//this is handled in the custom layout onClick method
			break;
		case R.id.menu_share:
			//this should be done by share action provider buy is not fully functional on Sherlock action bar
			//using chooser for the time being
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			//the subject is the name of the app, the body is the title + the link + @blogirame
			intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
			String textToShare = String.format(getString(R.string.share_format),
					getIntent().getExtras().getString(Constants.JSON_PARAM_POST_TITLE), 
					getIntent().getExtras().getString(Constants.JSON_PARAM_URL_BLOGIRAME));
			intent.putExtra(Intent.EXTRA_TEXT, textToShare);
			//start the chooser
			startActivity(Intent.createChooser(intent, getString(R.string.menu_share)));
			
		    break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setWebViewSettings() {
		//standard web view setting with no JS, wide view and no zoom controls
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(false);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setGeolocationEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setDatabaseEnabled(true);
		webSettings.setUseWideViewPort(true);
		
		//check if image loading is enabled on mobile network 
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetInfo != null 
        		&& activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE
        		&& !getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, true)) {
        	//return null and never execute the task for obtaining images
        	webSettings.setLoadsImagesAutomatically(false);
        }
        else {
        	webSettings.setLoadsImagesAutomatically(true);
        }

		//set the view view client
		mWebView.setWebViewClient(new CustomWebClient());
		mWebView.setWebChromeClient(new CustomWebChromeClient());
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

	private class CustomWebClient extends WebViewClient {
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
		        //show loading bar
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
		        //hide loading bar
	//			getSherlock().setProgressBarVisibility(false);
				super.onPageFinished(view, url);
			}
			
		}

	private class CustomWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			mProgressBar.setProgress(newProgress);
			super.onProgressChanged(view, newProgress);
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
			//TODO: send credentials for server verification
					
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
				//save credentials in shared preferences
				mPreferences.edit().putString(Constants.PREFERENCE_EMAIL, result[0]).commit();
				mPreferences.edit().putString(Constants.PREFERENCE_PASSWORD, result[1]).commit();
				
				//TODO: send echo request so server
			}
			getSherlock().setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}
		
	}
}
