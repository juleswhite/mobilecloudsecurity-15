
#Assignment 3, Option B

## Overview
This assignment is for students who have a working version of Option B of Assignment 2's
Video Service.

The purpose of this "mini-project" assignment is to give you
experience using Android and Spring to develop a complete
client/server video app.  The client-side of this app
runs on an Android device.  It uses Retrofit
to like/unlike videos via an extension of the 
server you implemented in 
https://github.com/juleswhite/mobilecloudsecurity-15/tree/master/assignments/assignment2b


You have a great degree of flexibility in how you design and implement this app,
as long as it meets the following client-side requirements: 



##Client-side requirements

1) The client must handle runtime configuration changes
   robustly, i.e., it should be able to handle screen
   orientation changes.Information on how to handle these types
   of changes is available at :-

   http://developer.android.com/guide/topics/resources/runtime-changes.html
    
   A video outlining several approaches to runtime configuration changes appears
   https://class.coursera.org/posacommunication-001/lecture/235


2) The client should use Retrofit to communicate with the
   video service, though none of these communications should
   block the UI thread. All the communication with server should
   be secure using HTTPS and use OAuth tokens to authenticate with 
   the server. You should provide a login screen to collect the
   user's username and password. You do not have to persist
   the OAuth tokens on the client and can prompt the user for
   their password each time the app is launched.

3) The client is not required to persist the data. So there is
  no need for you to implement a ContentProvider or cache to persist
  data.

4) The client should make the appropriate Retrofit calls to
   the Video Service to get the list of available videos
   (along with the likes/unlikes) and display them
   on the device. 

5) The client should allow uploading only video metadata (not the actual video binary data)
   to the video service by making the appropriate Retrofit calls to the
   Service.  It should provide a user interface for entering video metadata
   and/or get video metadata from the Gallery
   app (you must implement one of these
   mechanisms, its optional to implement both). 
   If you decide to get video metadata
   from the Gallery make sure the minimum SDK of your client
   is API 19, as discussed here
    http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework


6) The client should allow the user to like and unlike
   a video and upload that information to the Video Service
   via a Retrofit call. 

7) The client should make the appropriate Retrofit calls to
    the Video Service to get the list of users who liked a video
    and display them on the device. 

##Implementation Hints

0) The UnsafeHttpsClient is provided to you only to make connecting and working with
   your server, which has a self-signed certificate, easier. Without the UnsafeHttpsClient,
   your self-signed certificate will cause errors on the client side. Under no circumstances
   should you ever use the UnsafeHttpsClient in a production app. This code is strictly
   for test purposes.

1) You might note that these requirements are same as the one given in Assignment-3 of previous MOOC
   with the addition of Secure HTTPS communication with Video Service and using OAuth tokens to
   validate the user. So, you can easily take your earlier implementation and add security to it.
   You can use the provided SecuredRestBuilder, SecuredRestException, and UnsafeHttpsClient for this purpose. 

2) You can query the Android Mediastore ContentProvider to get the metadata of recorded videos or
    videos stored in Gallery. You can take a look at VideoUploadClient example in :-

    https://github.com/juleswhite/mobilecloud-15/tree/master/ex/VideoUploadClient(Unsecured)

  to see how it: records video and gets video data from the gallery, 
  queries the metadata of videos from the Android Mediastore ContentProvider, and finally
  uploads video data to the video service. For your implementation, you only need to upload the
  video metadata. You are free to use any file from the example App in your client implementation.
   


##Concluding Remarks
  
  This assignment is designed to deepen your understanding of how to
  create and integrate an Android client
  with an extended version of your secure video service implementation from
  Assignment 2b. If you have questions please come to
  Virtual Office Hours and/or post questions on the discussion forums.

      