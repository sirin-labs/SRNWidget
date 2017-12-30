package widget.sirinlabs.com.crowdsale.network.sirin

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by yaron on 30/11/17.
 */
class SirinRequestBuilder() {

    private val URL = "https://value.sirinlabs.com"

    private var requestService: SirinAPI? = null

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        requestService = retrofit.create(SirinAPI::class.java)
    }

    fun getNews(): Call<ValuesResponse> {
        return requestService!!.getValue()
    }

}