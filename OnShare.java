package cc.lupine.quicksocial.shareutils;

public abstract class OnShare {

	/**
	 * Executed when a post is successfully shared
	 * 
	 * @param postID
	 *            ID of a post returned from the remote service
	 */
	public abstract void onShared(String postID);

	/**
	 * Executed if an error occurs during sharing
	 * 
	 * @param error
	 *            error message
	 */
	public abstract void onError(String error);

	/**
	 * Executed when the sharing proccess begins
	 */
	public abstract void onSharingStarted();

}
