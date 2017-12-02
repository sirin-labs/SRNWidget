package widget.sirinlabs.com.crowdsale

/**
 * Created by yaron on 30/11/17.
 */
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import widget.sirinlabs.com.crowdsale.model.RedditNewsResponse

interface RequestService {
    @GET("/top.json")
    fun getTop(@Query("after") after: String,
               @Query("limit") limit: String)
            : Call<RedditNewsResponse>;
}