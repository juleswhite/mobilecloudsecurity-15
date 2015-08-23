package org.magnum.mobilecloud.video.client;

import java.util.Collection;

import org.magnum.mobilecloud.video.model.AverageVideoRating;
import org.magnum.mobilecloud.video.model.UserVideoRating;
import org.magnum.mobilecloud.video.model.Video;
import org.magnum.mobilecloud.video.model.VideoStatus;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

/**
 * This interface defines an API for a VideoSvc. The
 * interface is used to provide a contract for client/server
 * interactions. The interface is annotated with Retrofit
 * annotations so that clients can automatically convert the
 * 
 * 
 * @author jules
 *
 */
public interface VideoSvcApi {

	public static final String DATA_PARAMETER = "data";

	public static final String ID_PARAMETER = "id";

	public static final String RATING_PARAMETER = "rating";

	public static final String TOKEN_PATH = "/oauth/token";

	// The path where we expect the VideoSvc to live
	public static final String VIDEO_SVC_PATH = "/video";

	public static final String VIDEO_ITEM_PATH
		= VIDEO_SVC_PATH + "/{"+VideoSvcApi.ID_PARAMETER+"}";

	// The path where we expect the VideoSvc to live
	public static final String VIDEO_DATA_PATH = VIDEO_ITEM_PATH + "/data";

	public static final String VIDEO_RATING_PATH = VIDEO_ITEM_PATH + "/rating";

	public static final String VIDEO_RATE_VIDEO_PATH
		= VIDEO_RATING_PATH + "/{"+VideoSvcApi.RATING_PARAMETER+"}";
	
	@GET(VIDEO_SVC_PATH)
	public Collection<Video> getVideoList();
	
	@GET(VIDEO_ITEM_PATH)
	public Video getVideoById(@Path(ID_PARAMETER) long id);
	
	@POST(VIDEO_SVC_PATH)
	public Video addVideo(@Body Video v);
	
	@POST(VIDEO_RATE_VIDEO_PATH)
	public AverageVideoRating rateVideo(@Path(ID_PARAMETER) long id,
		@Path(RATING_PARAMETER) int rating);
	
	@GET(VIDEO_RATING_PATH)
	public AverageVideoRating getVideoRating(@Path(ID_PARAMETER) long id);
	
	@Multipart
	@POST(VIDEO_DATA_PATH)
	public VideoStatus setVideoData(@Path(ID_PARAMETER) long id,
		@Part(DATA_PARAMETER) TypedFile videoData);
	
	/**
	 * This method uses Retrofit's @Streaming annotation to indicate that the
	 * method is going to access a large stream of data (e.g., the mpeg video 
	 * data on the server). The client can access this stream of data by obtaining
	 * an InputStream from the Response as shown below:
	 * 
	 * VideoSvcApi client = ... // use retrofit to create the client
	 * Response response = client.getData(someVideoId);
	 * InputStream videoDataStream = response.getBody().in();
	 * 
	 * @param id
	 * @return
	 */
	@Streaming
	@GET(VIDEO_DATA_PATH)
	Response getVideoData(@Path(ID_PARAMETER) long id);
	
}
