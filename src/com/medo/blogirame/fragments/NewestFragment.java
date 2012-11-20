package com.medo.blogirame.fragments;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.medo.blogirame.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.medo.blogirame.utils.Constants;
import com.medo.blogirame.utils.DataBaseCategories;
import com.medo.blogirame.utils.ListAdapterNewest;
import com.medo.blogirame.utils.ListObjectBlogPost;
import com.medo.blogirame.utils.OnFragmentItemClick;

public class NewestFragment extends SherlockFragment {

	// views
	private View mView = null;
	// private View mHeader = null;
	private PullToRefreshListView mList = null;
	// variables
	private Integer mCurrentPage = 0;
	private ArrayList<ListObjectBlogPost> mAllResults = null;
	// async tasks
	private GetFromServer mTask = null;
	// main interface and listeners
	private OnFragmentItemClick mListener = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view from xml
		mView = inflater.inflate(R.layout.fragment_newest, container, false);
		// get the gallery widget
		mList = (PullToRefreshListView) mView.findViewById(R.id.list_newest);
		// mList.setDisableScrollingWhileRefreshing(true);
		mList.setPullLabel(getString(R.string.refresh_push_down),
				Mode.PULL_DOWN_TO_REFRESH);
		mList.setPullLabel(getString(R.string.refresh_pull_up),
				Mode.PULL_UP_TO_REFRESH);
		mList.setRefreshingLabel(getString(R.string.refresh_loading));
		mList.setReleaseLabel(getString(R.string.refresh_release));
		mList.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh(PullToRefreshBase refreshView) {
				if (mList.getCurrentMode() == Mode.PULL_DOWN_TO_REFRESH) {
					executeNewServerTask(
							String.format(Constants.API_MAIN_NEWEST,
									mCurrentPage.toString()), true);
				} else {
					mCurrentPage++;
					String urlParam = null;
					// page or blogs/categoires
					String queryString = getSherlockActivity()
							.getSupportActionBar().getTitle().toString();
					// if home is displayed as up
					if (!queryString
							.equalsIgnoreCase(getString(R.string.app_name))) {
						// get the category slug by its name
						DataBaseCategories database = new DataBaseCategories(
								getSherlockActivity());
						String serverString = database
								.getSlugByName(queryString);
						database.closeDB();
						urlParam = String.format(
								Constants.API_CATEGORIES_NEWEST, serverString,
								mCurrentPage.toString());
					} else {
						// we are on main page
						urlParam = String.format(Constants.API_MAIN_NEWEST,
								mCurrentPage.toString());
					}
					// execute the task with the appropriate parameters
					executeNewServerTask(urlParam, false);
				}
			}
		});

		// get the list from the wrapper class
		ListView tempList = mList.getRefreshableView();
		// set item click listener
		tempList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				// the list has a header that counts as a view so we need to get
				// the items at positon - 1
				ListObjectBlogPost clickedObject = mAllResults.get(pos - 1);
				// call main activity method
				mListener.openReader(clickedObject);

			}
		});
		// set scroll listener
		tempList.setOnScrollListener(new AbsListView.OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// state is of no interest to us
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// lets do a little magic here
				// when the 4 item is visible hide the popular gallery
				if (firstVisibleItem == 4)
					mListener.shrinkPopularFragmentHeight(true);
				// but when the user reaches the top of the page than bring it
				// back on
				if (firstVisibleItem == 1)
					mListener.shrinkPopularFragmentHeight(false);
			}
		});

		// initialize the list
		mTask = new GetFromServer();
		mTask.execute(String.format(Constants.API_MAIN_NEWEST,
				mCurrentPage.toString()));
		// initialize the ad
		GetAdsFromServer task = new GetAdsFromServer();
		task.execute(Constants.API_ADS);
		// return the inflated view
		return mView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			// try to cast the main activity to see if has implemented the
			// interface
			mListener = (OnFragmentItemClick) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFeaturedProductSelectedListener");
		}
	}

	public void executeNewServerTask(String urlParam,
			boolean shouldResetPageCounter) {
		if (shouldResetPageCounter)
			mCurrentPage = 0;

		if (mTask != null)
			mTask.cancel(true);
		mTask = new GetFromServer();
		mTask.execute(urlParam);

	}

	public void scrollToTop() {
		mList.getRefreshableView().setSelection(1);
	}

	private class GetFromServer extends
			AsyncTask<String, Integer, ArrayList<ListObjectBlogPost>> {

		@Override
		protected void onPreExecute() {
			// standard connectivity check
			ConnectivityManager connectivityManager = (ConnectivityManager) getSherlockActivity()
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (networkInfo == null || !networkInfo.isConnected()) {
				mListener.showOfflineAlert();
			}
			super.onPreExecute();
		}

		@Override
		protected ArrayList<ListObjectBlogPost> doInBackground(String... params) {

			ArrayList<ListObjectBlogPost> listToReturn = new ArrayList<ListObjectBlogPost>();

			try {
				// send GET to the server for autocomplete
				HttpGet request = new HttpGet(params[0]);
				// inform the server we want json object
				request.setHeader("Accept", "application/json");
				request.setHeader("Content-type", "application/json");

				// execute GET method
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(request);
				// get the response
				HttpEntity responseEntity = response.getEntity();

				// convert it to string
				String jsonString = EntityUtils.toString(responseEntity);
				// make json object
				JSONObject jsonObject = new JSONObject(jsonString);

				// check if there is error in responce
				if (jsonObject.getInt(Constants.JSON_PARAM_ERROR_CODE) != 0) {
					// inform the user
					new AlertDialog.Builder(getSherlockActivity())
							.setMessage(
									jsonObject
											.getInt(Constants.JSON_PARAM_ERROR))
							.setPositiveButton("OK", null).show();

					return null;
				}
				// if not, proceed with making the adapter
				else {
					JSONArray jsonArray = jsonObject
							.getJSONArray(Constants.JSON_PARAM_RESULTS);

					for (int i = 0; i < jsonArray.length(); i++) {
						// get JSON responce and put values into custom object
						JSONObject jsonBlogPost = jsonArray.getJSONObject(i);

						ListObjectBlogPost tempObject = new ListObjectBlogPost();
						tempObject.setmID(jsonBlogPost
								.getString(Constants.JSON_PARAM_ID));
						tempObject.setmBlog(jsonBlogPost
								.getString(Constants.JSON_PARAM_BLOG_TITLE));
						tempObject.setmTitle(jsonBlogPost
								.getString(Constants.JSON_PARAM_POST_TITLE));
						tempObject.setmDescription(jsonBlogPost
								.getString(Constants.JSON_PARAM_DESCRIPTION));
						tempObject.setmType(jsonBlogPost
								.getInt(Constants.JSON_PARAM_TYPE));
						tempObject.setmMobileURL(jsonBlogPost
								.getString(Constants.JSON_PARAM_URL_CANONICAL));
						tempObject.setmEchos(jsonBlogPost
								.getInt(Constants.JSON_PARAM_ECHOES));
						tempObject.setmImageURL(jsonBlogPost
								.getString(Constants.JSON_PARAM_IMAGE));
						tempObject.setmCategory(jsonBlogPost
								.getString(Constants.JSON_PARAM_CATEGORY));
						tempObject.setmURL(jsonBlogPost
								.getString(Constants.JSON_PARAM_URL_BLOGIRAME));
						tempObject.setmDate(jsonBlogPost
								.getString(Constants.JSON_PARAM_DATE));

						// add to the result array
						listToReturn.add(tempObject);

					}
					return listToReturn;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(ArrayList<ListObjectBlogPost> result) {
			if (result == null) {
				// error handling code
				mList.getRefreshableView().setAdapter(null);
			} else if (result.size() < 2) {
				// no results handling code
				Toast.makeText(getSherlockActivity(),
						R.string.toast_no_entries, Toast.LENGTH_SHORT).show();
				mList.getRefreshableView().setAdapter(null);
			} else {
				if (mAllResults == null) {
					mAllResults = new ArrayList<ListObjectBlogPost>();
				} else {
					mAllResults.addAll(result);
				}
				// if we have more results they should be appended to the
				// originals
				if (mCurrentPage != 0) {
					// get the existing array
					ListView tempList = mList.getRefreshableView();
					ListAdapterNewest apendedAdapter = new ListAdapterNewest(
							getSherlockActivity(), mAllResults,
							getSherlockActivity());
					tempList.setAdapter(apendedAdapter);
					// set selection to the last 20 items so the user dosn't
					// scroll down
					tempList.setSelection(apendedAdapter.getCount() - 20);
				} else {
					// reinitialize the result list
					mAllResults = new ArrayList<ListObjectBlogPost>();
					mAllResults.addAll(result);
					// fill the list with the server response
					ListView tempList = mList.getRefreshableView();
					ListAdapterNewest adapter = new ListAdapterNewest(
							getSherlockActivity(), result,
							getSherlockActivity());
					tempList.setAdapter(adapter);
				}
				// hide the progress bar
				getSherlockActivity()
						.setSupportProgressBarIndeterminateVisibility(false);
				mList.onRefreshComplete();
				// hide the image splash
				getSherlockActivity().findViewById(R.id.img_splash)
						.setVisibility(View.GONE);
				// load fade in animation
				getSherlockActivity().findViewById(R.id.linear_main)
						.setAnimation(
								AnimationUtils.loadAnimation(
										getSherlockActivity(), R.anim.fade_in));
			}
			super.onPostExecute(result);
		}
	}

	private class GetAdsFromServer extends
			AsyncTask<String, Integer, ArrayList<String>> {

		ArrayList<String> toReturn = new ArrayList<String>();

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			try {
				// send GET to the server for autocomplete
				HttpGet request = new HttpGet(params[0]);
				// inform the server we want json object
				request.setHeader("Accept", "application/json");
				request.setHeader("Content-type", "application/json");

				// execute GET method
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse response = httpClient.execute(request);
				// get the response
				HttpEntity responseEntity = response.getEntity();

				// convert it to string
				String jsonString = EntityUtils.toString(responseEntity);
				// make json object
				JSONObject jsonObject = new JSONObject(jsonString);
				// check for ads and get info
				if (jsonObject.getBoolean(Constants.JSON_PARAM_ADS_HAS) == false)
					return toReturn;
				toReturn.add(jsonObject
						.getString(Constants.JSON_PARAM_ADS_TITLE));
				toReturn.add(jsonObject
						.getString(Constants.JSON_PARAM_ADS_DESCRIPTION));
				toReturn.add(jsonObject.getString(Constants.JSON_PARAM_ADS_URL));

			} catch (Exception e) {
				return toReturn;
			}

			return toReturn;
		}

		@Override
		protected void onPostExecute(final ArrayList<String> result) {
			// if no results or empty results hide ad
			if (result.size() == 0)
				return;
			else {
				// get the whole ad
				final RelativeLayout ad = (RelativeLayout) mView
						.findViewById(R.id.relative_newest_ads);

				// get the add title and description
				TextView adTitle = (TextView) mView
						.findViewById(R.id.txt_newest_ad_title);
				TextView adDescription = (TextView) mView
						.findViewById(R.id.txt_newest_ad_description);
				// set them accordingly
				adTitle.setText(result.get(0));
				adDescription.setText(result.get(1));

				// get the close button
				ImageButton adClose = (ImageButton) mView
						.findViewById(R.id.img_newest_ad_close);
				adClose.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						ad.setAnimation(AnimationUtils.loadAnimation(
								getActivity(), R.anim.slide_out_to_bottom));
						ad.setVisibility(View.INVISIBLE);
					}
				});

				// get the linear layout representing the ad
				LinearLayout adLayout = (LinearLayout) mView
						.findViewById(R.id.linear_newest_ad);
				// set click listener i.e. open ad in browser
				adLayout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// open external browser for ad
						Intent browserIntent = new Intent(Intent.ACTION_VIEW,
								Uri.parse(result.get(2)));
						startActivity(browserIntent);
					}
				});

				// show the ad
				ad.setAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.slide_in_from_bottom));
				ad.setVisibility(View.VISIBLE);
			}
			super.onPostExecute(result);
		}
	}

}
