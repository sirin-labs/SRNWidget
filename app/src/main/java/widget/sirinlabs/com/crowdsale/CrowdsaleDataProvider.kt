package widget.sirinlabs.com.crowdsale

import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import widget.sirinlabs.com.crowdsale.model.SRNResponse

/**
 * Created by yaron on 01/12/17.
 */
fun fetchData() : Observable<Response<SRNResponse>>? {

    var observable = Observable.just(RequestBuilder()).map { builder ->

        val callResponse = builder.getNews()
        val response = callResponse.execute()

        return@map response
    }.subscribeOn(Schedulers.io()).doOnError { Log.e("fetchData", it.message) }

    return observable

}