Social Share Utils
==================

Set of utilities used in [Quick Social](https://play.google.com/store/apps/details?id=cc.lupine.quicksocial) (1.3 and newer) for sharing on Facebook and Twitter.

## Note

This isn't a standalone Android library. You cannot add it to your project as a dependency. It won't even work without some changes to its code (keep in mind that Social-Share-Utils is provided under the GPLv2 license). I advise you to fork this project, and add to your app as a git submodule.

## Required libraries

* Facebook SDK for Android
* twitter4j library (3.0.4+)

## Required code changes

Firstly, you can remove all `ACRA` calls from both imports and code. Unless you need them, of course.

Secondly, find `getUserSession` method in Facebook.java and adapt it to your needs.

Thirdly, open Twitter.java file and find these lines:

`Configuration configuration = ConfigurationBuilders.getTwitterConfig();`

`twitter.setOAuthAccessToken(StaticUtilities.getTwtTokensForUser(params.getString("user")));`

And also adapt it according to your needs. If you're willing to use this library, you know how to use it.

## Usage example

```
	Bundle shareBundle = new Bundle();
	
	shareBundle.putString("user", TWITTER_USER_ID); // TWITTER_USER_ID is an ID of an account you want to send a status from
	shareBundle.putString("message", edt.getText().toString()); // get the value of an edt EditText 

	if (imagePath) // if there's an imagePath variable with a path to the image
		shareBundle.putString("image", imagePath);
	else if (link) // if there's a link variable with a URL to attach to a status
		shareBundle.putString("link", link);

	Twitter shareTwitter = new Twitter(act, shareBundle);
	
	shareTwitter.setOnShareListener(new OnShare() {

		@Override
		public void onShared(String postID) {
			Status.out.println("The status (ID: " + postID + ") has been sent!")
		}

		@Override
		public void onError(String error) {
			throw new Exception(error);
		}

		@Override
		public void onSharingStarted() {
			Status.out.println("sharing to Twitter started");
		}

	});
	shareTwitter.shareAsync();
```

## Pull requests

Pull requests are welcome! If you want to make an improvement to this code, feel free to do this!

## LICENSE

Social-Share-Utils library is provided under the GPLv2 license. See its contents in the `LICENSE` file.
