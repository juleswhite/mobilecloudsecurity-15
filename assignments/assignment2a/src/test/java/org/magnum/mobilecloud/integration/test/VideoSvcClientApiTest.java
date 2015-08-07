package org.magnum.mobilecloud.integration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.magnum.mobilecloud.video.TestData;
import org.magnum.mobilecloud.video.client.SecuredRestBuilder;
import org.magnum.mobilecloud.video.client.SecuredRestException;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.model.AverageVideoRating;
import org.magnum.mobilecloud.video.model.Video;
import org.magnum.mobilecloud.video.model.VideoStatus;
import org.magnum.mobilecloud.video.model.VideoStatus.VideoState;

import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.ApacheClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import com.google.gson.JsonObject;

/**
 * 
 * The test requires that the VideoSvc be running first (see the directions in
 * the README.md file for how to launch the Application).
 * 
 * To run this test, right-click on it in Eclipse and select
 * "Run As"->"JUnit Test"
 * 
 * 
 * @author jules
 *
 */
public class VideoSvcClientApiTest {

	private final String USERNAME = "admin";
	private final String PASSWORD = "pass";
	private final String USERNAME2 = "user0";
	private final String PASSWORD2 = "pass";
	private final String CLIENT_ID = "mobile";
	private final String READ_ONLY_CLIENT_ID = "mobileReader";

	private final String TEST_URL = "https://localhost:8443";

	private File testVideoData = new File("src/test/resources/test.mp4");

	private VideoSvcApi videoSvc = new SecuredRestBuilder()
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			.setUsername(USERNAME)
			.setPassword(PASSWORD)
			.setClientId(CLIENT_ID)
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(VideoSvcApi.class);

	private VideoSvcApi videoSvc2 = new SecuredRestBuilder()
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			.setUsername(USERNAME2)
			.setPassword(PASSWORD2)
			.setClientId(CLIENT_ID)
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(VideoSvcApi.class);

	private VideoSvcApi readOnlyVideoService = new SecuredRestBuilder()
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			.setUsername(USERNAME)
			.setPassword(PASSWORD)
			.setClientId(READ_ONLY_CLIENT_ID)
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(VideoSvcApi.class);

	private VideoSvcApi invalidClientVideoService = new SecuredRestBuilder()
			.setLoginEndpoint(TEST_URL + VideoSvcApi.TOKEN_PATH)
			.setUsername(UUID.randomUUID().toString())
			.setPassword(UUID.randomUUID().toString())
			.setClientId(UUID.randomUUID().toString())
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient()))
			.setEndpoint(TEST_URL).setLogLevel(LogLevel.FULL).build()
			.create(VideoSvcApi.class);

	private Video video = TestData.randomVideo();

	/**
	 * This test creates a Video, adds the Video to the VideoSvc, and then
	 * checks that the Video is included in the list when getVideoList() is
	 * called.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testVideoAddAndList() throws Exception {
		// Add the video
		videoSvc.addVideo(video);

		// We should get back the video that we added above
		Collection<Video> videos = videoSvc.getVideoList();

		// The server isn't going to send back owner information
		// with the videos, so we shouldn't expect to find it
		video.setOwner(null);

		assertTrue(videos.contains(video));
	}

	/**
	 * This test ensures that clients with invalid credentials cannot get access
	 * to videos.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAccessDeniedWithIncorrectCredentials() throws Exception {

		try {
			// Add the video
			invalidClientVideoService.addVideo(video);

			fail("The server should have prevented the client from adding a video"
					+ " because it presented invalid client/user credentials");
		} catch (RetrofitError e) {
			assert (e.getCause() instanceof SecuredRestException);
		}
	}

	/**
	 * This test ensures that read-only clients can access the video list but
	 * not add new videos.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testReadOnlyClientAccess() throws Exception {

		Collection<Video> videos = readOnlyVideoService.getVideoList();
		assertNotNull(videos);

		try {
			// Add the video
			readOnlyVideoService.addVideo(video);

			fail("The server should have prevented the client from adding a video"
					+ " because it is using a read-only client ID");
		} catch (RetrofitError e) {
			JsonObject body = (JsonObject) e.getBodyAs(JsonObject.class);
			assertEquals("insufficient_scope", body.get("error").getAsString());
		}
	}

	@Test
	public void testAddVideoMetadata() throws Exception {
		Video received = videoSvc.addVideo(video);
		assertEquals(video.getTitle(), received.getTitle());
		assertEquals(video.getDuration(), received.getDuration());
		assertTrue(received.getId() > 0);
	}

	@Test
	public void testAddGetVideo() throws Exception {
		videoSvc.addVideo(video);
		Collection<Video> stored = videoSvc.getVideoList();
		assertTrue(stored.contains(video));
	}

	@Test
	public void testAddVideoData() throws Exception {
		Video received = videoSvc.addVideo(video);
		VideoStatus status = videoSvc.setVideoData(received.getId(),
				new TypedFile("video/mpeg", testVideoData));
		assertEquals(VideoState.READY, status.getState());

		Response response = videoSvc.getVideoData(received.getId());
		assertEquals(200, response.getStatus());

		InputStream videoData = response.getBody().in();
		byte[] originalFile = IOUtils.toByteArray(new FileInputStream(
				testVideoData));
		byte[] retrievedFile = IOUtils.toByteArray(videoData);
		assertTrue(Arrays.equals(originalFile, retrievedFile));
	}

	@Test
	public void testAddVideoDataForOtherUsersVideo() throws Exception {
		Video received = videoSvc.addVideo(video);

		try {
			videoSvc2.setVideoData(received.getId(),
					new TypedFile("video/mpeg", testVideoData));
			
			fail("A user should not be able to set the video data for another user's video");
		} catch (Exception e) {
			// This is what we expect because we shouldn't have access
			// to the other user's video. Ideally, we should also be
			// checking for an appropriate response code here too.
		}
	}
	
	@Test
	public void testUsersCanOnlyHaveASingleRatingForAVideo() throws Exception {
		Video received = videoSvc.addVideo(video);
		videoSvc.rateVideo(received.getId(), 1);
		videoSvc.rateVideo(received.getId(), 2);
		AverageVideoRating rating = videoSvc.getVideoRating(received.getId());
		
		assertEquals(2, rating.getRating(), 0);
		assertEquals(1, rating.getTotalRatings());
		
		rating = videoSvc2.getVideoRating(received.getId());
		assertEquals(2, rating.getRating(), 0);
		assertEquals(1, rating.getTotalRatings());
		
		videoSvc2.rateVideo(received.getId(), 4);
		rating = videoSvc2.getVideoRating(received.getId());
		assertEquals(3, rating.getRating(), 0);
		assertEquals(2, rating.getTotalRatings());
	}

	@Test
	public void testGetNonExistantVideosData() throws Exception {

		long nonExistantId = getInvalidVideoId();

		try {
			Response r = videoSvc.getVideoData(nonExistantId);
			assertEquals(404, r.getStatus());
		} catch (RetrofitError e) {
			assertEquals(404, e.getResponse().getStatus());
		}
	}

	@Test
	public void testAddNonExistantVideosData() throws Exception {
		long nonExistantId = getInvalidVideoId();
		try {
			videoSvc.setVideoData(nonExistantId, new TypedFile("video/mpeg",
					testVideoData));
			fail("The client should receive a 404 error code and throw an exception if an invalid"
					+ " video ID is provided in setVideoData()");
		} catch (RetrofitError e) {
			assertEquals(404, e.getResponse().getStatus());
		}
	}

	private long getInvalidVideoId() {
		Set<Long> ids = new HashSet<Long>();
		Collection<Video> stored = videoSvc.getVideoList();
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
