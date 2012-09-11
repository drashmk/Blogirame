package com.medo.blogirame.utils;

public class Constants {
	
	//server API
	/** 
	 * @param  pageNumber
	 */
	public static String API_MAIN_NEWEST = "http://www.blogirame.mk/jsonapi/main/page/%s/order/newest";
	public static String API_MAIN_POPULAR = "http://www.blogirame.mk/jsonapi/main/page/0/order/popular";
	/** 
	 * @param  categorySlug
	 */
	public static String API_CATEGORIES_NEWEST = "http://www.blogirame.mk/jsonapi/category/in/%s/page/%s/order/newest";
	public static String API_CATEGORIES_POPULAR = "http://www.blogirame.mk/jsonapi/category/in/%s/page/%s/order/popular";
	public static String API_CATEGORIES_GET = "http://blogirame.mk/jsonapi/categories/";
	/** 
	 * @param  blogId
	 */
	public static String API_BLOGS = "http://www.blogirame.mk/jsonapi/blog/in/%s";
	public static String API_ADS = "http://blogirame.mk/jsonapi/banner/";
	
	//json fields
	public static String JSON_PARAM_ERROR = "error";
	public static String JSON_PARAM_ERROR_CODE = "errorCode";
	public static String JSON_PARAM_PAGE_NUMBER = "page";
	public static String JSON_PARAM_PAGES_TOTAL = "pages";
	public static String JSON_PARAM_LIST = "list";
	public static String JSON_PARAM_RESULTS = "results";
	
	//blog post
	public static String JSON_PARAM_ID = "id";
//	public static String JSON_PARAM_FEED_ID = "feedId";
	public static String JSON_PARAM_BLOG_TITLE = "feedTitle";
	public static String JSON_PARAM_POST_TITLE = "title";
	public static String JSON_PARAM_DESCRIPTION = "description";
	public static String JSON_PARAM_TYPE = "type";
	public static String JSON_PARAM_DATE = "date";
	public static String JSON_PARAM_URL_CANONICAL = "mobile_url";
	public static String JSON_PARAM_URL_BLOGIRAME = "url";
	public static String JSON_PARAM_ECHOES = "echoes";
	public static String JSON_PARAM_IMAGE = "image";
	public static String JSON_PARAM_THUMBNAIL = "thumb";
	public static String JSON_PARAM_CATEGORY = "category";	
	public static String JSON_PARAM_CATEGORY_NAME = "name";
	public static String JSON_PARAM_CATEGORY_SLUG = "slug";	
	
	//ads
	public static String JSON_PARAM_ADS_TITLE = "title";
	public static String JSON_PARAM_ADS_DESCRIPTION = "description";	
	public static String JSON_PARAM_ADS_URL = "url";
	public static String JSON_PARAM_ADS_HAS = "has";	
	
	//shared preferences
	public static String PREFERENCE_NAME = "prefs";
	public static String PREFERENCE_EMAIL = "email";
	public static String PREFERENCE_PASSWORD = "password";
	public static String PREFERENCE_SHOULD_LOAD_IMAGES = "images";
	
	//other
	public static String PREFERENCE_CATEGORIES = "categories";
	public static String URL_BLOGIRAME_REGISTER = "http://blogirame.mk/user/login/";

}
