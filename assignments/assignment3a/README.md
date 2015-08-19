
#Assignment 3, Option A

## Overview
This assignment is for students who have a working version of Option A of Assignment 2's
Video Service.

The purpose of this "mini-project" assignment is to give you
experience using Android and Spring to develop a complete
client/server video upload/download app.  The client-side of this app
runs on an Android device and uses ContentProviders to persist the
videos and associated meta-data on the device.  It also uses Retrofit
to upload and download videos via an extension of the 
server you implemented in 
https://github.com/juleswhite/mobilecloudsecurity-15/tree/master/assignments/assignment2a


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

3) The client should implement its own ContentProvider using a
   SQlite database to persist the video meta-data on the device, such as 
   the id, title, duration, contentType, data URL, and star rating.
   In addition to these fields, you are free to store any other data you
   like in your client's ContentProvider.

4) The client should make the appropriate Retrofit calls to
   the video service to get the list of available videos
   (along with the average star ratings) and display them
   on the device. 

5) The client should allow uploading a video to the video
   service by making the appropriate Retrofit calls to the
   server.  It should either record video and/or get videos from Gallery
   app to get a video to upload to
   the video service (you must implement one of these
   mechanisms, it optional to implement both). 
   If you decide to get videos
   from the Gallery, make sure the minimum SDK of your client
   is API 19, as discussed here
    http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework

6) The client should limit the size of an uploaded video to
   50MB or less.  The client should display an error toast
   if the video size is greater than 50MB.

7) The client should download a video from the video service
   by making the appropriate Retrofit calls.  Downloaded
   videos can be stored in either
    Internal Storage
   or 
    External Storage
             

8) The client should play the downloaded video or have an
   option to download it if it is not present on device. You
   can use the
    VideoView widget 
   or
     ACTION_VIEW
   implicit Intent to play the video using another video app on the Android device.

9) The client should allow the user to give a star rating
   (i.e., a 1 through 5 star rating) to a video and upload
   that information to the video service via a Retrofit
   call. 

10) The client should make the appropriate Retrofit calls to
    the Video Service to get the list of available videos
    (along with the average star ratings) and display them
    on the device. 
    


##Implementation Hints

1) These requirements are same as the ones given in Assignment-3 of the previous MOOC
   with the addition of Secure HTTPS communication with the video service and OAuth to
   authenticate with the server. You can take your earlier implementation and add security to it.
   You can use the provided SecuredRestBuilder, SecuredRestException, and UnsafeHttpsClient for this purpose. 
   


##Concluding Remarks
  
  This assignment is designed to deepen your understanding on how to
  create and integrate an Android client containing Content Providers
  with an extended version of your secure Video Service implementation from
  Assignment 2a. If you have questions please come to
  Virtual Office Hours and/or post them on the discussion forums.

      