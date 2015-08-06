package org.magnum.mobilecloud.integration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.magnum.mobilecloud.video.TestData;
import org.magnum.mobilecloud.video.client.SecuredRestBuilder;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;

/**
 * A test for the Asgn2 video service
 * 
 * @author mitchell
 */
public class VideoSvcApiTest {

	private class ErrorRecorder implements ErrorHandler {

		private RetrofitError error;

		@Override
		public Throwable handleError(RetrofitError cause) {
			error = cause;
			return error.getCause();
		}

		public RetrofitError getError() {
			return error;
		}
	}

	private final String TEST_URL = "https://localhost:8443";

	private final String USERNAME1 = "admin";
	private final String USERNAME2 = "user0";
	private final String PASSWORD = "pass";
	private final String CLIENT_ID = "mobile";

	private VideoSvcApi readWriteVideoSvcUser1 = new SecuredRestBuilder()
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL)
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			// .setLogLevel(LogLevel.FULL)
			.setUsername(USERNAME1).setPassword(PASSWORD).setClientId(CLIENT_ID)
			.build().create(VideoSvcApi.class);

	private VideoSvcApi readWriteVideoSvcUser2 = new SecuredRestBuilder()
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL)
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			// .setLogLevel(LogLevel.FULL)
			.setUsername(USERNAME2).setPassword(PASSWORD).setClientId(CLIENT_ID)
			.build().create(VideoSvcApi.class);

	private Video video = TestData.randomVideo();


	@Test
	public void testAddVideoMetadata() throws Exception {
		Video received = readWriteVideoSvcUser1.addVideo(video);
		assertEquals(video.getName(), received.getName());
		assertEquals(video.getDuration(), received.getDuration());
		assertTrue(received.getLikes() == 0);
		assertTrue(received.getId() > 0);
	}

	@Test
	public void testAddGetVideo() throws Exception {
		readWriteVideoSvcUser1.addVideo(video);
		Collection<Video> stored = readWriteVideoSvcUser1.getVideoList();
		assertTrue(stored.contains(video));
	}

	@Test
	public void testDenyVideoAddWithoutOAuth() throws Exception {
		ErrorRecorder error = new ErrorRecorder();

		// Create an insecure version of our Rest Adapter that doesn't know how
		// to use OAuth.
		VideoSvcApi insecurevideoService = new RestAdapter.Builder()
				.setClient(
						new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
				.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL)
				.setErrorHandler(error).build().create(VideoSvcApi.class);
		try {
			// This should fail because we haven't logged in!
			insecurevideoService.addVideo(video);

			fail("Yikes, the security setup is horribly broken and didn't require the user to authenticate!!");

		} catch (Exception e) {
			// Ok, our security may have worked, ensure that
			// we got a 401
			assertEquals(HttpStatus.SC_UNAUTHORIZED, error.getError()
					.getResponse().getStatus());
		}

		// We should NOT get back the video that we added above!
		Collection<Video> videos = readWriteVideoSvcUser1.getVideoList();
		assertFalse(videos.contains(video));
	}

	@Test
	public void testLikeCount() throws Exception {

		// Add the video
		Video v = readWriteVideoSvcUser1.addVideo(video);

		// Like the video
		readWriteVideoSvcUser1.likeVideo(v.getId());

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is 1
		assertTrue(v.getLikes() == 1);

		// Unlike the video
		readWriteVideoSvcUser1.unlikeVideo(v.getId());

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is 0
		assertTrue(v.getLikes() == 0);
	}

	@Test
	public void testLikedBy() throws Exception {

		// Add the video
		Video v = readWriteVideoSvcUser1.addVideo(video);

		// Like the video
		readWriteVideoSvcUser1.likeVideo(v.getId());

		Collection<String> likedby = readWriteVideoSvcUser1.getUsersWhoLikedVideo(v.getId());

		// Make sure we're on the list of people that like this video
		assertTrue(likedby.contains(USERNAME1));
		
		// Have the second user like the video
		readWriteVideoSvcUser2.likeVideo(v.getId());
		
		// Make sure both users show up in the like list
		likedby = readWriteVideoSvcUser1.getUsersWhoLikedVideo(v.getId());
		assertTrue(likedby.contains(USERNAME1));
		assertTrue(likedby.contains(USERNAME2));

		// Unlike the video
		readWriteVideoSvcUser1.unlikeVideo(v.getId());

		// Get the video again
		likedby = readWriteVideoSvcUser1.getUsersWhoLikedVideo(v.getId());

		// Make sure user1 is not on the list of people that liked this video
		assertTrue(!likedby.contains(USERNAME1));
		
		// Make sure that user 2 is still there
		assertTrue(likedby.contains(USERNAME2));
	}

	@Test
	public void testLikingTwice() throws Exception {

		// Add the video
		Video v = readWriteVideoSvcUser1.addVideo(video);

		// Like the video
		readWriteVideoSvcUser1.likeVideo(v.getId());

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is 1
		assertTrue(v.getLikes() == 1);

		try {
			// Like the video again.
			readWriteVideoSvcUser1.likeVideo(v.getId());

			fail("The server let us like a video twice without returning a 400");
		} catch (RetrofitError e) {
			// Make sure we got a 400 Bad Request
			assertEquals(400, e.getResponse().getStatus());
		}

		// Get the video again
		v = readWriteVideoSvcUser1.getVideoById(v.getId());

		// Make sure the like count is still 1
		assertTrue(v.getLikes() == 1);
	}

	@Test
	public void testLikingNonExistantVideo() throws Exception {

		try {
			// Like the video again.
			readWriteVideoSvcUser1.likeVideo(getInvalidVideoId());

			fail("The server let us like a video that doesn't exist without returning a 404.");
		} catch (RetrofitError e) {
			// Make sure we got a 400 Bad Request
			assertEquals(404, e.getResponse().getStatus());
		}
	}

	private long getInvalidVideoId() {
		Set<Long> ids = new HashSet<Long>();
		Collection<Video> stored = readWriteVideoSvcUser1.getVideoList();
		for (Video v : stored) {
			ids.add(v.getId());
		}

		long nonExistantId = Long.MIN_VALUE;
		while (ids.contains(nonExistantId)) {
			nonExistantId++;
		}
		return nonExistantId;
	}

}
