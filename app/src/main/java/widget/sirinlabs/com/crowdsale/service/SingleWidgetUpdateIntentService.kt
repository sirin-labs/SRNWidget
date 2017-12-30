package widget.sirinlabs.com.crowdsale.service

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import retrofit2.Response
import widget.sirinlabs.com.crowdsale.CrowdsaleAppWidgetProvider
import widget.sirinlabs.com.crowdsale.R
import widget.sirinlabs.com.crowdsale.fetchETHticker
import widget.sirinlabs.com.crowdsale.fetchSRNticker
import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse
import java.text.DecimalFormat
import java.util.*


/**
 * Created by gilad or on 18/12/17.
 */
class SingleWidgetUpdateIntentService : IntentService(SingleWidgetUpdateIntentService::class.java.simpleName) {
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent")
        updateWidget()
    }

    private lateinit var mDisposable: Disposable

    private fun updateWidget() {
        Log.d(TAG, "updateWidget")
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val remoteViews = RemoteViews(application.packageName, R.layout.appwidget)
        remoteViews.setViewVisibility(R.id.animating_bar, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.place_holder_bar, View.INVISIBLE)
        val thisWidget = ComponentName(applicationContext, CrowdsaleAppWidgetProvider::class.java)
        updateData(remoteViews, appWidgetManager, thisWidget)
        appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)
    }

    private fun updateData(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, widgetId: ComponentName?) {
        Observable.zip(fetchSRNticker(), fetchETHticker(), BiFunction<Response<List<TickerResponse>>, Response<List<TickerResponse>>, Pair<Response<List<TickerResponse>>, Response<List<TickerResponse>>>> { SRNtickerResponse, ETHtickerResponse ->
            Pair(SRNtickerResponse, ETHtickerResponse)
        })
                .doOnSubscribe { disposable -> mDisposable = disposable }
                .subscribeBy(onNext = { pair ->
                    val res = resources
                    val srnTickerResponse = pair.first
                    val ethTickerResponse = pair.second

                    remoteViews.setViewVisibility(R.id.place_holder_bar, View.VISIBLE)
                    remoteViews.setViewVisibility(R.id.animating_bar, View.INVISIBLE)

                    if (srnTickerResponse.isSuccessful) {
                        Log.d(TAG, "ticker response is successful, srn: " + ethTickerResponse.body().get(0).price_usd)
                        setSRNdata(srnTickerResponse, remoteViews, res)
                    } else {
                        Log.d(TAG, "srn  ticker response is not successful")
                    }
                    if (ethTickerResponse.isSuccessful) {
                        Log.d(TAG, "ticker response is successful, eth: " + ethTickerResponse.body().get(0).price_usd)
                        setETHdata(ethTickerResponse, remoteViews, res)

                    } else {
                        Log.d(TAG, "eth ticker response is not successful")
                    }
                    appWidgetManager!!.updateAppWidget(widgetId, remoteViews)

                }, onError = { Throwable ->
                    Log.e(TAG, Throwable.message)
                }, onComplete = {
                    Log.d(TAG, "onComplete -> disposing")
                    mDisposable.dispose()
                })

    }

    private fun setETHdata(etherTickerResponse: Response<List<TickerResponse>>, remoteViews: RemoteViews, res: Resources?) {
        val ticker = etherTickerResponse.body().get(0)
        Log.d(TAG, "srn:$:" + ticker.price_usd)
        remoteViews.setTextViewText(R.id.eth_in_usd, kotlin.String.format(res!!.getString(R.string.in_usd_format), ticker.price_usd.toDouble()))
        remoteViews.setTextViewText(R.id.eth_change, kotlin.String.format(res!!.getString(R.string.precent), ticker.percent_change_24h.toFloat()))
        if (ticker.percent_change_24h.toDouble() > 0) {
            remoteViews.setTextColor(R.id.eth_change, Color.GREEN)
        } else {
            remoteViews.setTextColor(R.id.eth_change, Color.RED)
        }
        remoteViews.setViewVisibility(R.id.eth_text, View.VISIBLE)
        remoteViews.setTextViewText(R.id.update_time, DateFormat.format(res!!.getString(R.string.time_short), Date()))
    }

    private fun setSRNdata(tickerResponse: Response<List<TickerResponse>>, remoteViews: RemoteViews, res: Resources?) {
        val ticker = tickerResponse.body().get(0)
        Log.d(TAG, "srn:$:" + ticker.price_usd)
        remoteViews.setTextViewText(R.id.srn_in_usd, kotlin.String.format(res!!.getString(R.string.in_usd_long_format), ticker.price_usd.toDouble()))
        remoteViews.setTextViewText(R.id.srn_change, kotlin.String.format(res!!.getString(R.string.precent), ticker.percent_change_24h.toFloat()))
        if (ticker.percent_change_24h.toDouble() > 0) {
            remoteViews.setTextColor(R.id.srn_change, Color.GREEN)
        } else {
            remoteViews.setTextColor(R.id.srn_change, Color.RED)
        }
        val amountFormatter = DecimalFormat(res.getString(R.string.readable_number))
        remoteViews.setViewVisibility(R.id.srn_text, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.cir_text, View.VISIBLE)
        remoteViews.setTextViewText(R.id.circulation, amountFormatter.format(ticker.volume_usd.toDouble()))


        remoteViews.setTextViewText(R.id.update_time, DateFormat.format(res!!.getString(R.string.time_short), Date()))
    }

    companion object {
        private val TAG = SingleWidgetUpdateIntentService::class.java.simpleName
    }
}