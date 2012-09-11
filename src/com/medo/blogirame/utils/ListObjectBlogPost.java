package com.medo.blogirame.utils;

import android.graphics.drawable.Drawable;

public class ListObjectBlogPost {
	
	private String mTitle;
	private String mDescription;
	private int mType;
	private String mImageURL;
	private String mBlog;
	private String mID;
	private int mEchos; 
	private String mDate;
	private String mURL;
	private String mMobileURL;
	private String mCategory;
	private Drawable mImage;
	
	public ListObjectBlogPost() {
		mTitle = "";
		mDescription = "";
		mType = 0;
		mImageURL = "";
		mBlog = "";
		mID = "";
		mEchos = 0;
		mDate = "";
		mURL = "";
		mMobileURL = "";
		mCategory = "";
		mImage = null;
	}
	
	public Drawable getmImage() {
		return mImage;
	}
	
	public String getmCategory() {
		return mCategory;
	}
	
	public String getmBlog() {
		return mBlog;
	}
	
	public String getmMobileURL() {
		return mMobileURL;
	}
	
	public String getmDate() {
		return mDate;
	}
	
	public String getmDescription() {
		return mDescription;
	}
	
	public int getmEchos() {
		return mEchos;
	}
	
	public String getmID() {
		return mID;
	}
	
	public String getmImageURL() {
		return mImageURL;
	}
	
	public String getmTitle() {
		return mTitle;
	}
	
	public int getmType() {
		return mType;
	}
	
	public String getmURL() {
		return mURL;
	}
	
	public void setmBlog(String mBlog) {
		this.mBlog = mBlog;
	}
	
	public void setmMobileURL(String mCannonicalURL) {
		this.mMobileURL = mCannonicalURL;
	}
	
	public void setmDate(String mDate) {
		this.mDate = mDate;
	}
	
	public void setmDescription(String mDescription) {
		this.mDescription = mDescription;
	}
	
	public void setmEchos(int mEchos) {
		this.mEchos = mEchos;
	}
	
	public void setmID(String mID) {
		this.mID = mID;
	}
	
	public void setmImageURL(String mImageURL) {
		this.mImageURL = mImageURL;
	}
	
	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	
	public void setmType(int mType) {
		this.mType = mType;
	}
	
	public void setmURL(String mURL) {
		this.mURL = mURL;
	}
	
	public void setmCategory(String mCategory) {
		this.mCategory = mCategory;
	}
	
	public void setmImage(Drawable mImage) {
		this.mImage = mImage;
	}
	
}
