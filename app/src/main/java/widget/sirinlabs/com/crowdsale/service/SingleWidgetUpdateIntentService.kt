package widget.sirinlabs.com.crowdsale.service

import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import retrofit2.Response
import widget.sirinlabs.com.crowdsale.CrowdsaleAppWidgetProvider
import widget.sirinlabs.com.crowdsale.fetchETHticker
import widget.sirinlabs.com.crowdsale.fetchSRNticker
import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse


/**
 * Created by gilad or on 18/12/17.
 */
class SingleWidgetUpdateIntentService : IntentService(SingleWidgetUpdateIntentService::class.java.simpleName) {

    //---------------------------------------- Members ---------------------------------------------

    private lateinit var mDisposable: Disposable

    //---------------------------------------- Overrides -------------------------------------------

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent")
        updateWidget()
    }

    //---------------------------------------- Private Methods -------------------------------------

    private fun updateWidget() {
        Log.d(TAG, "updateWidget")
        Observable.zip(fetchSRNticker(), fetchETHticker(), BiFunction<Response<List<TickerResponse>>, Response<List<TickerResponse>>, Pair<Response<List<TickerResponse>>, Response<List<TickerResponse>>>> { SRNtickerResponse, ETHtickerResponse ->
            Pair(SRNtickerResponse, ETHtickerResponse)
        }).doOnSubscribe { disposable -> mDisposable = disposable }
                .subscribeBy(onNext = { pair ->
                    val srnTickerResponse = pair.first
                    val ethTickerResponse = pair.second
                    if (!srnTickerResponse.isSuccessful || !ethTickerResponse.isSuccessful) {
                        return@subscribeBy
                    }

                    val broadcastIntent = Intent(applicationContext, CrowdsaleAppWidgetProvider::class.java)
                    broadcastIntent.putExtra("srnTickerResponse", srnTickerResponse.body()[0])
                    broadcastIntent.putExtra("ethTickerResponse", ethTickerResponse.body()[0])
                    broadcastIntent.action = CrowdsaleAppWidgetProvider.MY_WIDGET_UPDATE
                    val pending = PendingIntent.getBroadcast(applicationContext, 0, broadcastIntent, 0)
                    pending.send()

                }, onError = { Throwable ->
                    Log.e(TAG, Throwable.message)
                }, onComplete = {
                    Log.d(TAG, "onComplete -> disposing")
                    mDisposable.dispose()
                })
    }

    //---------------------------------------- Companion -------------------------------------

    companion object {
        private val TAG = SingleWidgetUpdateIntentService::class.java.simpleName
    }
}