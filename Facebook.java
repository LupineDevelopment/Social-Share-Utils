package cc.lupine.quicksocial.shareutils;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.acra.ACRA;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import cc.lupine.quicksocial.utils.StaticUtilities;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;

public class Facebook extends SharingAdapter {

	private Bundle params;
	private OnShare event;
	private Activity act;

	/**
	 * Class constructor for Facebook sharing
	 * 
	 * @param activity
	 *            instance of a current {@link Activity}
	 * @param postParams
	 *            {@link Bundle} containing all the post values
	 * @throws IllegalArgumentException
	 *             when the postParams doesn't contain all required values
	 */
	public Facebook(Activity activity, Bundle postParams)
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

			Session session = getUserSession(params.getString("user"));
			Bundle postParams = new Bundle();

			if (params.containsKey("image")) {
				InputStream imageStream = act.getContentResolver()
						.openInputStream(Uri.parse(params.getString("image")));
				Bitmap selectedImage = StaticUtilities.getOptimalBitmap(
						imageStream, 1);
				postParams.putString("caption", params.getString("message"));
				postParams
						.putString("description", params.getString("message"));
				postParams.putString("name", params.getString("message"));
				postParams.putParcelable("picture", selectedImage);
			} else if (params.containsKey("link"))
				postParams.putString("link", params.getString("link"));

			String param = "";
			if (params.containsKey("image"))
				param = "photos";
			else
				param = "feed";
			postParams.putString("message", params.getString("message"));
			Request request = new Request(session, params.getString("user")
					+ "/" + param, postParams, HttpMethod.POST,
					getFacebookCallback());
			RequestAsyncTask task = new RequestAsyncTask(request);
			event.onSharingStarted();
			task.execute();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			event.onError(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			event.onError(e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			event.onError("Unexpected exception: " + e.getLocalizedMessage());
			ACRA.getErrorReporter().handleException(e);
		}
	}

	/**
	 * Handle asynchronous response from Facebook and call
	 * {@link OnShare#onShared(String)} if the post has been shared or
	 * {@link OnShare#onError(String)} when an error occurred.
	 * 
	 * @return instance of a {@link com.facebook.Request.Callback} class
	 */
	private Request.Callback getFacebookCallback() {
		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				try {
					FacebookRequestError error = response.getError();
					if (error != null && error.shouldNotifyUser()) {
						error.getException().printStackTrace();
						event.onError(error.getErrorMessage());
						return;
					}
					JSONObject graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					String postID = graphResponse.getString("id");
					event.onShared(postID);
				} catch (JSONException e) {
					e.printStackTrace();
					event.onError(e.getLocalizedMessage());
				} catch (Exception e) {
					e.printStackTrace();
					event.onError("Unexpected exception: "
							+ e.getLocalizedMessage());
					ACRA.getErrorReporter().handleException(e);
				}
			}
		};
		return callback;
	}

	/**
	 * Get an opened Facebook user session. The method checks whether the uid
	 * param is an ID of a user or page and always returns the session of user.
	 * 
	 * @param uid
	 *            ID of a user or page who shares the post
	 * @return user's session
	 */
	private Session getUserSession(String uid) {
		Session session;
		if (!StaticUtilities.isFBPage(uid))
			session = StaticUtilities.getFBSessionForUser(uid);
		else
			session = StaticUtilities.getFBSessionForUser(StaticUtilities
					.getPageOwnerID(uid));
		if (session == null || !session.isOpened())
			session.openForPublish(new OpenRequest(act));

		if (!session.isOpened())
			throw new IllegalStateException("Couldn't open Facebook session");

		return session;
	}
}