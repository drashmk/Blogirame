package com.medo.blogirame.utils;

public interface OnFragmentItemClick {
	
	public void openReader(ListObjectBlogPost blogPost);
	public void updateProgress(int current, int max);
	public void shrinkPopularFragmentHeight(boolean shouldShrink);
	public void changeMenuItemsForOfflineMode();
	public void showOfflineAlert();
}
