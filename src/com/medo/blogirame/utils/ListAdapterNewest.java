package com.medo.blogirame.utils;


import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.medo.blogirame.R;

public class ListAdapterNewest extends BaseAdapter {
	
	//async task
	GetFromServer mTask;
	//context in which we want to use the adapter
	private Context context;
	//the custom made object for this adapter
	private ArrayList<ListObjectBlogPost> items;
	//handler for updating the data set from the async task (worker thread)
	private Handler mHandler = new Handler() {
		 @Override
		 public void handleMessage (Message msg) {
			 //refresh the list
			 notifyDataSetChanged();
			 //first argument is the current image that is loaded into the list
			 //second argument is the total number of entries in the list
			 mListener.updateProgress(msg.arg1+1, msg.arg2);
		 }
	};
	
	private OnFragmentItemClick mListener;

	//constructor
    public ListAdapterNewest(Context context, ArrayList<ListObjectBlogPost> items, Activity activity) {
    	this.context = context;
    	this.items = items;
    	this.mListener = (OnFragmentItemClick) activity;
    	//make string array with the image URL for downloading
    	String[] params = new String[items.size()];
    	int i = 0;
    	for(ListObjectBlogPost tempObj : items)
    		params[i++] = tempObj.getmImageURL();
    	
    	if(mTask != null)
    		mTask.cancel(true);
    	//make new server task
    	mTask = new GetFromServer();
    	mTask.execute(params);
    }

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public ListObjectBlogPost getItem(int position) {
		return items.get(position);
	}
	
	public ArrayList<ListObjectBlogPost> getArray() {
		return items;
	}

	@Override
	//no need to use item ID in our app
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		//get the specific list entry
		final ListObjectBlogPost object = items.get(position);
		//make custom list entries specified by row_featured layout 
		if(view == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        view = inflater.inflate(R.layout.item_newest, null);
		}
		
		//find the elements of the list item
		TextView postTitle = (TextView) view.findViewById(R.id.txt_newest_title);
		TextView blogAndDescription = (TextView) view.findViewById(R.id.txt_newest_description);
		TextView echoes = (TextView) view.findViewById(R.id.txt_newest_echo_count);
		TextView postDate = (TextView) view.findViewById(R.id.txt_newest_date);
		ImageView snipetImage = (ImageView) view.findViewById(R.id.img_newest);
		
		//fill them with values
		postTitle.setText(object.getmTitle());
		blogAndDescription.setText(Html.fromHtml(String.format(view.getContext().getString(R.string.item_description_format), object.getmBlog() , object.getmDescription())));
		echoes.setText(""+object.getmEchos());
		//make new data format for easier reading
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat newFormat = new SimpleDateFormat("MMM d, yyyy"); 
		try {
			Date date = dateFormat.parse(object.getmDate());
			postDate.setText(newFormat.format(date));
		} catch (ParseException e) {
			postDate.setText(object.getmDate());
			e.printStackTrace();
		}
		//add image from server
		if(object.getmImage() != null)
			snipetImage.setImageDrawable(object.getmImage());
		
		return view;
	}
	
	public void cancelServerTask() {
		mTask.cancel(true);
	}
	
	private class GetFromServer extends AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... params) {
			
			//check if image loading is enabled on mobile network 
	        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

	        if (activeNetInfo != null 
	        		&& activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE
	        		&& !context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getBoolean(Constants.PREFERENCE_SHOULD_LOAD_IMAGES, true)) {
	        	//return null and never execute the task for obtaining images
	        	return null;
	        }
			
			try
			{
				for(int i=0; i< params.length; i++) {
					//do this if there is there is an image URL
					//but don't do this when there is already an image present
					//this is useful when we are going to append new page elements to the adapter
					if(params[i] != "null" && items.get(i).getmImage() == null) {
						//get the drawable from stream
						InputStream is = (InputStream) new URL(params[i]).getContent();
						Drawable d = Drawable.createFromStream(is, "src name");
						//add it to the list
						items.get(i).setmImage(d);
					}
					//notify UI tread so it updates the list view
					Message newMessage = new Message();
					newMessage.arg1 = i;
					newMessage.arg2 = params.length;
					mHandler.sendMessage(newMessage);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}	
	}

}
