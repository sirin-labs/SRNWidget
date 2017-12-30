package widget.sirinlabs.com.crowdsale.network.sirin

/**
 * Created by yaron on 30/11/17.
 */
import retrofit2.Call
import retrofit2.http.GET

interface SirinAPI {
    @GET("/value.json")
    fun getValue()
            : Call<ValuesResponse>
}