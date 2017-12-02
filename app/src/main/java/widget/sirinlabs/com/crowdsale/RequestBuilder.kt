package widget.sirinlabs.com.crowdsale

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import widget.sirinlabs.com.crowdsale.model.RedditNewsResponse

/**
 * Created by yaron on 30/11/17.
 */
class RequestBuilder {

    private var requestService: RequestService? = null

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.reddit.com")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        requestService = retrofit.create(RequestService::class.java)
    }

    fun getNews(after: String, limit: String): Call<RedditNewsResponse> {
        return requestService!!.getTop(after, limit)
    }

}