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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.medo.blogirame.R;
import com.medo.blogirame.utils.Constants;
import com.medo.blogirame.utils.ListAdapterPopular;
import com.medo.blogirame.utils.ListObjectBlogPost;
import com.medo.blogirame.utils.OnFragmentItemClick;

public class PopularPortraitFragment extends SherlockFragment {
	
	private View mView = null;
	private Gallery mGallery = null;
	private GetFromServer mTask = null;
	private OnFragmentItemClick mListener = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//inflate the view from xml
		mView = inflater.inflate(R.layout.fragment_popular, container, false);
		//get the gallery widget
		mGallery = (Gallery) mView.findViewById(R.id.gallery_popular);
		//set galleri item click listener
		mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				//get the item at the current position from the adapter
				ListAdapterPopular adapter = (ListAdapterPopular) parent.getAdapter();
				ListObjectBlogPost clickedObject = adapter.getItem(pos);
//				ListObjectBlogPost clickedObject = mAllResults.get(pos);
				mListener.openReader(clickedObject);
				
			}
		});
		//initialize the gallery
		mTask = new GetFromServer();
		mTask.execute(Constants.API_MAIN_POPULAR);
		//return the inflated view
		return mView;
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	//try to cast the main activity to see if has implemented the interface
            mListener = (OnFragmentItemClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFeaturedProductSelectedListener");
        }
    }
    
    

	public void executeNewServerTask(String urlParam) {
		//initialize the list
		if(mTask != null)
			mTask.cancel(true);
		mTask = new GetFromServer();
		mTask.execute(urlParam);
	}
	
	private class GetFromServer extends AsyncTask<String, Integer, ArrayList<ListObjectBlogPost>> {
		
		@Override
		protected void onPreExecute() {
			//disable view and cancel image lazy loading
//			mGallery.setEnabled(false);
			ListAdapterPopular adapter = (ListAdapterPopular) mGallery.getAdapter();
			if(adapter != null)
				adapter.cancelServerTask();
			super.onPreExecute();
		}

	@Override
	protected ArrayList<ListObjectBlogPost> doInBackground(String... params) {
		
		ArrayList<ListObjectBlogPost> listToReturn = new ArrayList<ListObjectBlogPost>();
		
		try {
			//send GET to the server for autocomplete
			HttpGet request = new HttpGet(params[0]);
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
	        	new AlertDialog.Builder(getSherlockActivity())
	        	.setMessage(jsonObject.getInt(Constants.JSON_PARAM_ERROR))
	        	.setPositiveButton("OK", null)
	        	.show();
	        	
	        	return null;
	        }
	        //if not, proceed with making the adapter
	        else {
	        	JSONArray jsonArray = jsonObject.getJSONArray(Constants.JSON_PARAM_RESULTS);
	        	
	        	for(int i=0; i<jsonArray.length(); i++) {
	        		//get JSON responce and put values into custom object
	        		JSONObject jsonBlogPost = jsonArray.getJSONObject(i);
	        		
	        		ListObjectBlogPost tempObject = new ListObjectBlogPost();
	        		tempObject.setmID(jsonBlogPost.getString(Constants.JSON_PARAM_ID));
	        		tempObject.setmBlog(jsonBlogPost.getString(Constants.JSON_PARAM_BLOG_TITLE));
	        		tempObject.setmTitle(jsonBlogPost.getString(Constants.JSON_PARAM_POST_TITLE));
	        		tempObject.setmDescription(jsonBlogPost.getString(Constants.JSON_PARAM_DESCRIPTION));
	        		tempObject.setmType(jsonBlogPost.getInt(Constants.JSON_PARAM_TYPE));
	        		tempObject.setmMobileURL(jsonBlogPost.getString(Constants.JSON_PARAM_URL_CANONICAL));
	        		tempObject.setmEchos(jsonBlogPost.getInt(Constants.JSON_PARAM_ECHOES));
	        		tempObject.setmImageURL(jsonBlogPost.getString(Constants.JSON_PARAM_IMAGE));
	        		tempObject.setmCategory(jsonBlogPost.getString(Constants.JSON_PARAM_CATEGORY));
					tempObject.setmURL(jsonBlogPost.getString(Constants.JSON_PARAM_URL_BLOGIRAME));
	        		
	        		//add to the result array
	        		listToReturn.add(tempObject);

	        	}	
	        	return listToReturn;
	        }
	        
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	
		@Override
		protected void onPostExecute(ArrayList<ListObjectBlogPost> result) {
			if(result == null) {
				//error handling code
				mGallery.setAdapter(null);
			}
			else if(result.size() < 2) {
				//no results handling code
				Toast.makeText(getSherlockActivity(), R.string.toast_no_entries, Toast.LENGTH_SHORT).show();
				mGallery.setAdapter(null);
			}
			else {
//				mGallery.setEnabled(true);
				//fill the gallery with server responce
				ListAdapterPopular adapter = new ListAdapterPopular(getSherlockActivity(), result);
				mGallery.setAdapter(adapter);
					mGallery.setSelection(1);
			}
			super.onPostExecute(result);
		}
	}
}
