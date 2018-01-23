package widget.sirinlabs.com.crowdsale.service

import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import widget.sirinlabs.com.crowdsale.*
import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * Created by gilad or on 18/12/17.
 */
class SingleWidgetUpdateIntentService : IntentService(SingleWidgetUpdateIntentService::class.java.simpleName) {

    //---------------------------------------- Members ---------------------------------------------

    private lateinit var mDisposable: Disposable
    private var progress_max: Int = 0

    //---------------------------------------- Overrides -------------------------------------------

    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent")
        progress_max = applicationContext.resources.getInteger(R.integer.update_interval)
        updateWidget()
    }

    //---------------------------------------- Private Methods -------------------------------------

    private fun updateWidget() {
        Log.d(TAG, "updateWidget")
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val remoteViews = RemoteViews(application.packageName, R.layout.app_widget)
        val thisWidget = ComponentName(applicationContext, CrowdsaleAppWidgetProvider::class.java)
        updateData(remoteViews, appWidgetManager, thisWidget)
        appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)
    }

    private fun updateData(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, widgetId: ComponentName?) {
        val thisWidget = ComponentName(applicationContext, CrowdsaleAppWidgetProvider::class.java)

        (applicationContext as SRNWidgetApp).mTimerAnimationDisposable?.dispose()
        (applicationContext as SRNWidgetApp).mTimerAnimationDisposable = getTimerAnimationObservable(remoteViews, appWidgetManager, thisWidget)

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
                    broadcastIntent.putExtra("srnTickerResponse",srnTickerResponse.body()[0])
                    broadcastIntent.putExtra("ethTickerResponse",ethTickerResponse.body()[0])
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

    private fun getTimerAnimationObservable(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, thisWidget: ComponentName): Disposable {
        return Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribeBy { l ->
                    Log.d(TAG, "timer: " + (progress_max - l.toInt()).toString())
                    remoteViews.setProgressBar(R.id.animating_bar, progress_max, progress_max - l.toInt(), false)
                    appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)


                    if (l >= progress_max) {
                        Log.d(TAG, "stop itter")

                        (applicationContext as SRNWidgetApp).mTimerAnimationDisposable?.dispose()
                    }
                }
    }

    //---------------------------------------- Companion -------------------------------------

    companion object {
        private val TAG = SingleWidgetUpdateIntentService::class.java.simpleName
    }
}