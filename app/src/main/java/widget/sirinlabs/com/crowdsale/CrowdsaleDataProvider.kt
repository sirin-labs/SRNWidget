package widget.sirinlabs.com.crowdsale

import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import widget.sirinlabs.com.crowdsale.network.cmc.CmcRequestBuilder
import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse
import widget.sirinlabs.com.crowdsale.network.sirin.SirinRequestBuilder
import widget.sirinlabs.com.crowdsale.network.sirin.ValuesResponse

/**
 * Created by yaron on 01/12/17.
 */
fun fetchData(): Observable<Response<ValuesResponse>>? {

    var observable = Observable.just(SirinRequestBuilder()).map { builder ->

        val callResponse = builder.getNews()
        val response = callResponse.execute()

        return@map response
    }.subscribeOn(Schedulers.newThread()).doOnError { Log.e("fetchData", it.message) }

    return observable
}

fun fetchSRNticker(): Observable<Response<List<TickerResponse>>>? {

    var observable = Observable.just(CmcRequestBuilder()).map { builder ->

        val callResponse = builder.getSRNTicker()
        val response = callResponse.execute()

        return@map response
    }.subscribeOn(Schedulers.newThread()).doOnError { Log.e("fetchTicker", it.message) }

    return observable
}

//TODO code dup
fun fetchETHticker(): Observable<Response<List<TickerResponse>>>? {

    var observable = Observable.just(CmcRequestBuilder()).map { builder ->

        val callResponse = builder.getETHTicker()
        val response = callResponse.execute()

        return@map response
    }.subscribeOn(Schedulers.newThread()).doOnError { Log.e("fetchTicker", it.message) }

    return observable
}