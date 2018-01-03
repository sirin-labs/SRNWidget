package widget.sirinlabs.com.crowdsale.network.cmc

/**
 * Created by yaron on 30/11/17.
 */
import retrofit2.Call
import retrofit2.http.GET

interface CmcApi {
    @GET("/v1/ticker/sirin-labs-token/")
    fun getSRNTicker()
            : Call<List<TickerResponse>>
    @GET("/v1/ticker/ethereum/")
    fun getETHTicker()
            : Call<List<TickerResponse>>
}