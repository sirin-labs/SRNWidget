package widget.sirinlabs.com.crowdsale.network.cmc

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by yaron on 30/11/17.
 */
class CmcRequestBuilder{

    private val URL = "https://api.coinmarketcap.com"

    private var requestService: CmcApi? = null

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        requestService = retrofit.create(CmcApi::class.java)
    }

    fun getSRNTicker(): Call<List<TickerResponse>> {
        return requestService!!.getSRNTicker()
    }

    fun getETHTicker(): Call<List<TickerResponse>> {
        return requestService!!.getETHTicker()
    }

}