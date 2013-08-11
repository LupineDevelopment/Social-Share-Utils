package cc.lupine.quicksocial.shareutils;

import java.io.File;

import org.acra.ACRA;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.conf.Configuration;
import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import cc.lupine.quicksocial.utils.ConfigurationBuilders;
import cc.lupine.quicksocial.utils.StaticUtilities;

public class Twitter extends SharingAdapter {

	private Bundle params;
	private OnShare event;
	private Activity act;

	/**
	 * Class constructor for Twitter sharing
	 * 
	 * @param activity
	 *            instance of a current {@link Activity}
	 * @param postParams
	 *            {@link Bundle} containing all the post values
	 * @throws IllegalArgumentException
	 *             when the postParams doesn't contain all required values
	 */
	public Twitter(Activity activity, Bundle postParams)
			throws IllegalArgumentException {
		if (postParams == null
				|| (!postParams.containsKey("message")
						&& !postParams.containsKey("image") && !postParams
							.containsKey("link"))
				|| !postParams.containsKey("user"))
			throw new IllegalArgumentException(
					"Bundle does not contain required keys");
		params = postParams;
		act = activity;
	}

	/**
	 * OnShare class instance containing listeners for common sharing events
	 * 
	 * @param listener
	 *            instance of the {@link OnShare} class with overriden abstract
	 *            methods
	 * @see OnShare
	 */
	@Override
	public void setOnShareListener(OnShare listener) {
		event = listener;
	}

	/**
	 * Start asynchronous sharing in a new thread. No Exception can be thrown
	 * there, as the errors are now passed to the
	 * {@link OnShare#onError(String)} method.
	 * 
	 * @see OnShare
	 */
	@Override
	public void shareAsync() {
		try {
			if (params == null)
				throw new IllegalStateException("No post bundle provided");

			Configuration configuration = ConfigurationBuilders
					.getTwitterConfig();
			AsyncTwitterFactory factory = new AsyncTwitterFactory(configuration);
			AsyncTwitter twitter = factory.getInstance();
			twitter.setOAuthAccessToken(StaticUtilities
					.getTwtTokensForUser(params.getString("user")));
			String statusText = params.getString("message");

			if (params.containsKey("link"))
				statusText += " " + params.getString(params.getString("link"));

			StatusUpdate status = new StatusUpdate(statusText);
			if (params.containsKey("image"))
				status.setMedia(new File(params.getString("image")));

			twitter.addListener(getTwitterCallback());
			event.onSharingStarted();
			twitter.updateStatus(status);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			event.onError(e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			event.onError("Unexpected exception: " + e.getLocalizedMessage());
			ACRA.getErrorReporter().handleException(e);
		}
	}

	/**
	 * Handle asynchronous response from twitter4j and call (in the UI thread)
	 * {@link OnShare#onShared(String)} if the post has been shared or
	 * {@link OnShare#onError(String)} when an error occurred.
	 * 
	 * @return instance of a {@link com.facebook.Request.Callback} class
	 */
	private TwitterListener getTwitterCallback() {
		TwitterListener listener = new TwitterAdapter() {
			@Override
			public void updatedStatus(final Status status) {
				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						event.onShared(String.valueOf(status.getId()));
					}

				});
			}

			@Override
			public void onException(final TwitterException e,
					final twitter4j.TwitterMethod method) {

				act.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Log.d("sn", "looper main: "
								+ (Looper.myLooper() == Looper.getMainLooper()));
						if (method == twitter4j.TwitterMethod.UPDATE_STATUS) {
							e.printStackTrace();
							event.onError(e.getLocalizedMessage());
						} else {
							e.printStackTrace();
							event.onError("Unknown Twitter method, this should not happen: "
									+ e.getLocalizedMessage());
							ACRA.getErrorReporter().handleException(e);
						}
					}

				});
			}
		};
		return listener;
	}
}
