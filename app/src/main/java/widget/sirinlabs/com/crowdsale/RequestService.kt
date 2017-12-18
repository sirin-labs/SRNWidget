package widget.sirinlabs.com.crowdsale

/**
 * Created by yaron on 30/11/17.
 */
import retrofit2.Call
import retrofit2.http.GET
import widget.sirinlabs.com.crowdsale.model.SRNResponse

interface RequestService {
    @GET("/value.json")
    fun getMoney()
            : Call<SRNResponse>
}