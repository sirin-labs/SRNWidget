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
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import widget.sirinlabs.com.crowdsale.*
import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit


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
                    val res = resources
                    val srnTickerResponse = pair.first
                    val ethTickerResponse = pair.second

                    if (srnTickerResponse.isSuccessful) {
                        Log.d(TAG, "ticker response is successful, srn: " + ethTickerResponse.body().get(0).price_usd)
                        updateSRNUi(srnTickerResponse, remoteViews, res)
                    } else {
                        Log.d(TAG, "srn  ticker response is not successful")
                        return@subscribeBy
                    }
                    if (ethTickerResponse.isSuccessful) {
                        Log.d(TAG, "ticker response is successful, eth: " + ethTickerResponse.body().get(0).price_usd)
                        updateETHUi(ethTickerResponse, remoteViews, res)

                    } else {
                        Log.d(TAG, "eth ticker response is not successful")
                        return@subscribeBy
                    }

                    setSrnToEther(srnTickerResponse.body().get(0).price_usd.toDouble(), ethTickerResponse.body().get(0).price_usd.toDouble(), remoteViews, res)
                    fixColorBug(remoteViews)
                    appWidgetManager!!.updateAppWidget(widgetId, remoteViews)

                }, onError = { Throwable ->
                    Log.e(TAG, Throwable.message)
                }, onComplete = {
                    Log.d(TAG, "onComplete -> disposing")
                    mDisposable.dispose()
                })
    }

    private fun fixColorBug(remoteViews: RemoteViews) {
        remoteViews.setTextColor(R.id.eth_text, Color.WHITE)
        remoteViews.setTextColor(R.id.eth_in_usd, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_text, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_in_usd, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_in_ether, Color.WHITE)
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

    private fun setSrnToEther(srnPriceUsd: Double, etherPriceUsd: Double, remoteViews: RemoteViews, res: Resources?) {
        val srnInEther = srnPriceUsd / etherPriceUsd
        val amountFormatter = DecimalFormat(res?.getString(R.string.readable_fraction))
        remoteViews.setTextViewText(R.id.srn_in_ether, kotlin.String.format(res!!.getString(R.string.eth_amount), amountFormatter.format(srnInEther)).toString())
        remoteViews.setViewVisibility(R.id.srn_to_ether, View.VISIBLE)

    }

    private fun updateETHUi(etherTickerResponse: Response<List<TickerResponse>>, remoteViews: RemoteViews, res: Resources?) {
        val ticker = etherTickerResponse.body().get(0)
        Log.d(TAG, "srn:$:" + ticker.price_usd)
        remoteViews.setTextViewText(R.id.eth_in_usd, kotlin.String.format(res!!.getString(R.string.in_usd_format), ticker.price_usd.toDouble()))
        remoteViews.setTextViewText(R.id.eth_change, kotlin.String.format(res!!.getString(R.string.precent), ticker.percent_change_24h.toFloat()))
        if (ticker.percent_change_24h.toDouble() > 0) {
            remoteViews.setTextColor(R.id.eth_change, Color.GREEN)
        } else {
            remoteViews.setTextColor(R.id.eth_change, Color.RED)
        }
        remoteViews.setViewVisibility(R.id.ether, View.VISIBLE)
        remoteViews.setTextViewText(R.id.update_time, DateFormat.format(res!!.getString(R.string.time_short), Date()))
    }

    private fun updateSRNUi(tickerResponse: Response<List<TickerResponse>>, remoteViews: RemoteViews, res: Resources?) {
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
        remoteViews.setTextViewText(R.id.circulation, kotlin.String.format(res!!.getString(R.string.dollar_amount), amountFormatter.format(ticker.volume_usd.toDouble()).toString()))
        remoteViews.setViewVisibility(R.id.srn, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.total_supply, View.VISIBLE)
        remoteViews.setTextViewText(R.id.update_time, DateFormat.format(res!!.getString(R.string.time_short), Date()))
    }

    //---------------------------------------- Companion -------------------------------------

    companion object {
        private val TAG = SingleWidgetUpdateIntentService::class.java.simpleName
    }
}