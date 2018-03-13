# facebook-ad-video-issue
demonstrates facebook audience-network sdk bug in playing rewarded video

ISSUE DETAILS:

We are using a custom URLStreamHandlerFactory in our project, setting it via URL#setURLStreamHandlerFactory.
The RewardedVideoAd video playing stucks for us. The player doesn't send a GET request to the video URL.
But the everything works fine, if we don't use a custom handler.

For isolating the bug, we created a dummy handler which just uses the platform default protocol handlers and passes on to them.
The same issue persists with the dummy handler too.

If we insert a time delay of 1-2 seconds in URLStreamHandler#openConnection method of the handlers returned by our factory, it makes the video play. This hints towards a race condition or similar concurrency bug in the way RewardedVideoAd class interacts with ExoPlayer (possibly when it tries to initialize the player).


STEPS TO REPRODUCE:

1. Clone the repository
2. Open the project in android studio, build and deploy to a device
3. From the main menu of the app, go to "Rewarded Videos" section, click on "Load Rewarded Video", once loaded, click "Show"
4. The video player will stuck in the loading stage. (this is the bug)
5. Now, from MainActivity.java, comment out line 53 (URL.setURLStreamHandlerFactory call). Build the app again, video playing will start working.
6. To confirm the concurrency bug, uncomment the same line in previous step, and uncomment lines 86 and 95 (sleep calls) to introduce a delay, build the app again, video will start playing.

ADDITIONAL INFO:

1. SDK Version: 4.28.0 (compile 'com.facebook.android:audience-network-sdk:4.+')
2. Occurs independent of any particular device model, android version.. etc
