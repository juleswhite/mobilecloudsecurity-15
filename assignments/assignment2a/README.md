# Assignment 2, Option A

## Overview

This version of the assignment is for students that completed Assignments 2 & 3 in the
prior MOOC. You are welcome to complete this assignment from scratch if you did not do
the prior assignments, but it will be a LOT of work. You are expected to reuse your implementation
of video uploading/downloading, metadata storage, ratings, etc. from the prior MOOC. The
main difference is that you will be adding account logic to your existing code to prevent
things like one user overwriting another user's video or rating a video multiple times.

This assignment will build on the ideas in the previous video service to add OAuth 2.0
authentication of clients and the ability to control editing and rating of videos. To complete
this assignment, you must allow users to authenticate using the OAuth 2.0 Password Grant flow.
Once authenticated, users must be able to perform all of the functions in the previous video
service assignment (https://github.com/juleswhite/mobilecloud-15/blob/master/assignments/assignment3). 
In addition, the original APIs must be updated to include user accounts and security as
described in this specification.

Finally, all communication must take place over https, port 8443. The code to setup HTTPS
on the embedded Tomcat instance is included in the Application class.

You should note that this assignment also requires concepts used in the first assignment, such
as request mapping and JSON marshalling in request/response bodies. 

## Adapting Your Prior Application

This application shell is provided solely as a reference and to give you sample OAuth configuration
code. You can start with your prior project and integrate this code into your other project or
vice-versa. You may have different package naming schemes in your project and / or additional 
variables, tests, etc. You are free to rename and modify anything that you wish. However, you
should ensure that you cover all of the requirements in the specification.

If you used a different HTTP interface (e.g., URL scheme, ec.) for the rating system in your prior 
implementation, you should adapt it to the current requirements.

## Warning

UNDER NO CIRCUMSTANCES SHOULD YOU USE THE INCLUDED KEYSTORE IN A PRODUCTION APP!!!
UNDER NO CIRCUMSTANCES SHOULD YOU USE THIS APP "AS IS" IN PRODUCTION!!!

## Running the Application

Please read the instructions carefully.

To run the application:

1. Right-click on the Application class in the assignment project->Run As->Java Application (the 
   application may try to start and fail with an error message - this is OK). If the application
   successfully starts, stop the application before proceeding to the next step.
2. (Menu Bar) Run->Run Configurations
3. Under Java Applications, select your run configuration for this app's Application class that
   was just created in step 1 (if you select the run configuration, it should list the assignment
   as the project name)
4. Open the Arguments tab
5. In VM Arguments, provide the following information to use the
   default keystore provided with the sample code:

   -Dkeystore.file=src/main/resources/private/keystore -Dkeystore.pass=changeit

6. Note, this keystore is highly insecure! If you want more security, you 
   should obtain a real SSL certificate:

   http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html
   
7. This keystore is not secured and should be in a more secure directory -- preferably
   completely outside of the app for non-test applications -- and with strict permissions
   on which user accounts can access it

## Instructions

First, clone this Git repository and import it into Eclipse as described
in the development environment setup guide 
[https://class.coursera.org/mobilecloudsecurity-001/wiki/Installing_Eclipse%2C_Git%2C_and_Gradle].

This assignment tests your ability to create a web application that
allows clients to authenticate using the OAuth 2.0 Password Grant Flow.
Clients can upload video metadata (name, duration, etc.) once logged in, 
as well as rate videos.

The test that you should use to check your implementation is VideoSvcClientApiTest
in the org.magnum.mobilecloud.integration.test package in src/test/java. 
**_You should use the
source code in the this test as the ground truth for what the expected
behavior of your solution is_.** Your app should pass this test without 
any errors. 

The HTTP API that you must implement so that this test will pass is as
follows (some key differences with prior specs are in __bold__):

__GET to any URL other than oauth token issuance__
   - Requires authentication and that the authenticated user have the "read" scope
   
__POST to any URL other than oauth token issuance__
   - Requires authentication and that the authenticated user have the "write" scope

__POST /oauth/token__
   - The access point for the OAuth 2.0 Password Grant flow.
   - Clients should be able to submit a request with their username, password,
      client ID, and client secret, encoded as described in the OAuth lecture
      videos.
   - The client ID for the Retrofit adapter is "mobile" with an empty password.
   - There must be 2 users, whose usernames are "user0" and "admin". All passwords 
     should simply be "pass".
   - Rather than implementing this from scratch, we suggest reusing the example
     configuration for OAuth 2.0 provided in this skeleton. The configuration
     is imported into the Application configuration with @Import. You may need to customize the users
     in the OAuth2Config constructor or the security applied by the ResourceServer.configure(...) 
     method. You should determine what (if any) adaptations are needed by comparing this 
     and the test specification against the code in that class.
        
GET /video
   - Returns the list of videos that have been added to the
     server as JSON. The list of videos should be persisted
     using Spring Data. The list of Video objects should be able 
     to be unmarshalled by the client into a Collection<Video>.
   - The return content-type should be application/json, which
     will be the default if you use @ResponseBody
     
POST /video
   - __The "owner" member variable of the Video must be set to the
     name of the currently authenticated Principal__
   - __If a Video already exists, it should not be overwritten unless
     the name of the authenticated Principal matches the name of the
     owner member variable of the Video__
   - The video metadata is provided as an application/json request
     body. The JSON should generate a valid instance of the 
     Video class when deserialized by Spring's default 
     Jackson library.
   - Returns the JSON representation of the Video object that
     was stored along with any updates to that object made by the server. 
   - **_The server should store the Video in a Spring Data JPA repository.
   	 If done properly, the repository should handle generating ID's._** 
   - A video should not have any ratings when it is initially created.
   - You will need to add one or more annotations to the Video object
     in order for it to be persisted with JPA.

POST /video/{id}/data
   - __Before saving the binary data received, the service should check to
     ensure that the name of the currently authenticated Principal matches
     the owner of the Video. If not, a HTTP 403 response should be sent
     to the client.__
   - The binary mpeg data for the video should be provided in a multipart
     request as a part with the key "data". The id in the path should be
     replaced with the unique identifier generated by the server for the
     Video. A client MUST *create* a Video first by sending a POST to /video
     and getting the identifier for the newly created Video object before
     sending a POST to /video/{id}/data. 
   - The endpoint should return a VideoStatus object with state=VideoState.READY
     if the request succeeds and the appropriate HTTP error status otherwise.
     VideoState.PROCESSING is not used in this assignment but is present in VideoState.
   - Rather than a PUT request, a POST is used because, by default, Spring 
     does not support a PUT with multipart data due to design decisions in the
     Commons File Upload library: https://issues.apache.org/jira/browse/FILEUPLOAD-197
     
GET /video/{id}/data
   - Returns the binary mpeg data (if any) for the video with the given
     identifier. If no mpeg data has been uploaded for the specified video,
     then the server should return a 404 status code.
     
GET /video/{id}
   - Returns the video with the given id or 404 if the video is not found.
     
__POST /video/{id}/rating/{rating}__
   - Only allows each user (e.g., authenticated Principal) to have a single
     rating for a video. If a user has an existing rating for a Video, the
     existing rating should be overwritten
   - Allows a user to rate a video. Returns 200 Ok on success or 404 if the
     video is not found.
     
__GET /video/{id}/rating__
   - Returns the AverageVideoRating for a Video, which contains the average
     star count for the video across all users and the total number of users
     that have rated the video
      

 This assignment also requires that you store your data using a Spring Data Jpa Repository.
      
 The VideoSvcClientApiTest should be used as the ultimate ground truth for what should be 
 implemented in the assignment. If there are any details in the description above 
 that conflict with the VideoSvcClientApiTest, use the details in the VideoSvcClientApiTest 
 as the correct behavior and report the discrepancy on the course forums. Further, 
 you should look at the VideoSvcClientApiTest to ensure that
 you understand all of the requirements. It is perfectly OK to post on the forums and
 ask what a specific section of the VideoSvcClientApiTest does. Do not, however, post any
 code from your solution or potential solution.
 
 There is a VideoSvcApi interface that is annotated with Retrofit annotations in order
 to communicate with the video service that you will be creating. Your solution controller(s)
 should not directly implement this interface in a "Java sense" (e.g., you should not have
 YourSolution implements VideoSvcApi). Your solution should support the HTTP API that
 is described by this interface, in the text above, and in the VideoSvcClientApiTest. In some
 cases it may be possible to have the Controller and the client implement the interface.
 
 Again -- the ultimate ground truth of how the assignment will be graded, is contained
 in VideoSvcClientApiTest, which shows the specific tests that will be run to grade your
 solution. You must implement everything that is required to make all of the tests in
 this class pass. If a test case is not mentioned in this README file, you are still
 responsible for it and will be graded on whether or not it passes. __Make sure and read
 the VideoSvcClientApiTest code and look at each test__!
 
## Testing Your Implementation

To test your solution, first run the application as described above. Once your application
is running, you can right-click on the VideoSvcClientApiTest->Run As->JUnit Test to launch the
test. Eclipse will report which tests pass or fail.

## Submitting Your Assignment

Submit your assignment for peer review per the instructions in the assignment description on Coursera.

 
## Provided Code

- __org.magnum.mobilecloud.video.repository.Video__: This is a simple class to represent the metadata for a video.

  You must annotate this object properly in order for it to be stored in the JPA repository. The annotations
  that you may want to include are @Entity, @Id, @GeneratedValue, and @ElementCollection. However, you do not
  have to use all of these annotations (in particular @ElementCollection) to complete this assignment successfully.
  A correct implementation can just use @Entity, @Id, and @GeneratedValue. 

- __OAuth 2.0 Configuration Code: 

	You should ensure that you create the proper users and set the proper security on the various endpoints
	to match the specification. If you want to use it, please do so. If not, feel free to implement your
	own approach that meets the assignment specification (but not from scratch!!).
	
- __SecuredRestBuilder__: This wrapper around the Retrofit library is used by the tests to construct a client
    that will automatically perform OAuth 2.0 authentication with a password grant before API methods are 
    invoked.

## Hints

- If you want to test your application without security (e.g., to add a simple request mapping
  and try it without OAuth), you will need to comment-out the following lines in the build.gradle
  file and then right-click on build.gradle->Refresh All:

```    
    compile("org.springframework.boot:spring-boot-starter-security:${springBootVersion}")
    compile("org.springframework.security.oauth:spring-security-oauth2:2.0.0.RC2")
    compile("org.springframework.security.oauth:spring-security-oauth2-javaconfig:1.0.0.M1")
```  

- The examples in GitHub will be helpful on this assignment
- A valid solution is going to have at least one class annotated with @Controller
- There will probably need to be several different methods annotated with @RequestMapping to
  implement the HTTP API described
- It is unlikely that you will be able to use Spring Data Rest to complete the assignment due to
  differences in the responses provided by Spring Data Rest when adding new videos, etc.
- Any Controller method can take a Principal as a parameter to gain access/control over the 
  user who is currently authenticated. Spring will automatically fill in this parameter when your 
  Controller's method is invoked:
```java
        ...
        @RequestMapping("/some/path/{id}")
        public MyObject doSomething(
                   @PathVariable("id") String id, 
                   Principal p) {
         
         String username = p.getName(); 
         // Maybe you want to store this in your video...
            ....       
        }
        
```
- The IDs must be of type long. The tests send long values to the server and will generate
  400 response codes if you use an int.
- If you get an error 400, you have incorrectly specified the parameter values that the method
  should accept and their mapping to HTTP parameters.
- There are multiple ways to implement most pieces of the application. Any solution that passes
  the tests will be given full credit.
- None of your Controllers or other classes should "implement VideoSvcApi" -- which is an interface
  that is only used to create a Retrofit client. None of your classes should look like this:
```java
        public class SomeClass implements VideoSvcApi // Don't implement this interface! 
        {
          ...
        }
```        
`
