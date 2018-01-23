package widget.sirinlabs.com.crowdsale

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse
import widget.sirinlabs.com.crowdsale.service.PeriodricWidgetUpdateJobService
import widget.sirinlabs.com.crowdsale.service.SingleWidgetUpdateIntentService
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by yarons on 27/11/17.
 */
class CrowdsaleAppWidgetProvider : AppWidgetProvider() {

    //---------------------------------------- Members ---------------------------------------------

    private val JOB_ID: Int = 1001
    private var mUpdateInterval: Long = 0
    private var mProgressMax: Int = 0


    //---------------------------------------- Overrides -------------------------------------------

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate")
        mUpdateInterval = context!!.resources.getInteger(R.integer.update_interval) * 1000L
        fetchData(context)
        startPeriodicUpdates(context)
        setClick(context, appWidgetManager)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive, action :" + intent?.action)

        mProgressMax = context!!.resources.getInteger(R.integer.update_interval)

        if (MY_WIDGET_UPDATE == intent.action) {
            val remoteViews = RemoteViews(context.packageName, R.layout.app_widget)
            val srnTickerResponse = intent.extras.get("srnTickerResponse") as TickerResponse
            val ethTickerResponse = intent.extras.get("ethTickerResponse") as TickerResponse

            remoteViews.setViewVisibility(R.id.animating_bar, View.VISIBLE)

            val res = context.resources

            if (srnTickerResponse != null) {
                updateSRNUi(srnTickerResponse, remoteViews, res)
            }
            if (ethTickerResponse != null) {
                updateETHUi(ethTickerResponse, remoteViews, res)
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, CrowdsaleAppWidgetProvider::class.java)

            if (srnTickerResponse != null && ethTickerResponse != null) {
                    setSrnToEther(srnTickerResponse, ethTickerResponse, remoteViews, res)
            }

            fixColorBug(remoteViews)

            setClick(context, appWidgetManager)

            appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)

            (context.applicationContext as SRNWidgetApp).mTimerAnimationDisposable?.dispose()
            (context.applicationContext as SRNWidgetApp).mTimerAnimationDisposable = getTimerAnimationObservable(remoteViews, appWidgetManager, thisWidget, context.applicationContext as SRNWidgetApp)
        }

    }
    //---------------------------------------- Private Methods -------------------------------------

    private fun setClick(context: Context?, appWidgetManager: AppWidgetManager?) {
        clickOnUpd(context, appWidgetManager)
        clickExpand(context, appWidgetManager)
    }

    private fun clickExpand(context: Context?, appWidgetManager: AppWidgetManager?) {
        val remoteViews = RemoteViews(context?.packageName, R.layout.app_widget)
        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.frame, pendingIntent)
        val thisWidget = ComponentName(context, CrowdsaleAppWidgetProvider::class.java)
        appWidgetManager?.updateAppWidget(thisWidget, remoteViews)
    }

    private fun clickOnUpd(context: Context?, appWidgetManager: AppWidgetManager?) {
        val remoteViews = RemoteViews(context?.packageName, R.layout.app_widget)
        val serviceIntent = Intent(context, SingleWidgetUpdateIntentService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.animating_bar, pendingIntent)
        val thisWidget = ComponentName(context, CrowdsaleAppWidgetProvider::class.java)
        appWidgetManager?.updateAppWidget(thisWidget, remoteViews)
    }

    private fun startPeriodicUpdates(context: Context?) {
        val serviceComponent = ComponentName(context, PeriodricWidgetUpdateJobService::class.java)
        val jobInfo: JobInfo = JobInfo.Builder(JOB_ID, serviceComponent)
                .setMinimumLatency(mUpdateInterval.toLong()).build()
        val jobScheduler = context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    private fun fetchData(context: Context?) {
        val serviceIntent = Intent(context, SingleWidgetUpdateIntentService::class.java)
        val pending = PendingIntent.getService(context, 0, serviceIntent, 0)
        pending.send()
    }

    private fun setSrnToEther(srnPriceUsd: TickerResponse, etherPriceUsd: TickerResponse, remoteViews: RemoteViews, res: Resources?) {
        val srnInEther = srnPriceUsd.price_usd.toDouble() / etherPriceUsd.price_usd.toDouble()
        val srnToEtherChange = srnPriceUsd.percent_change_24h.toDouble() - etherPriceUsd.percent_change_24h.toDouble()
        val amountFormatter = DecimalFormat(res?.getString(R.string.readable_fraction))
        remoteViews.setTextViewText(R.id.srn_in_ether, amountFormatter.format(srnInEther))
        remoteViews.setTextViewText(R.id.srn_in_ether_change, kotlin.String.format(res!!.getString(R.string.precent), srnToEtherChange.toFloat()))
        if (srnToEtherChange > 0) {
            remoteViews.setTextColor(R.id.srn_in_ether_change, Color.GREEN)
        } else {
            remoteViews.setTextColor(R.id.srn_in_ether_change, Color.RED)
        }
        remoteViews.setViewVisibility(R.id.srn_to_ether, View.VISIBLE)

    }

    private fun updateETHUi(etherTickerResponse: TickerResponse, remoteViews: RemoteViews, res: Resources?) {
        Log.d(TAG, "srn:$:" + etherTickerResponse.price_usd)
        remoteViews.setTextViewText(R.id.eth_in_usd, kotlin.String.format(res!!.getString(R.string.in_usd_format), etherTickerResponse.price_usd.toDouble()))
        remoteViews.setTextViewText(R.id.eth_change, kotlin.String.format(res!!.getString(R.string.precent), etherTickerResponse.percent_change_24h.toFloat()))
        if (etherTickerResponse.percent_change_24h.toDouble() > 0) {
            remoteViews.setTextColor(R.id.eth_change, Color.GREEN)
        } else {
            remoteViews.setTextColor(R.id.eth_change, Color.RED)
        }
        remoteViews.setViewVisibility(R.id.ether, View.VISIBLE)
    }

    private fun updateSRNUi(tickerResponse: TickerResponse, remoteViews: RemoteViews, res: Resources) {
        remoteViews.setTextViewText(R.id.srn_in_usd, kotlin.String.format(res!!.getString(R.string.in_usd_long_format), tickerResponse.price_usd.toDouble()))
        remoteViews.setTextViewText(R.id.srn_change, kotlin.String.format(res!!.getString(R.string.precent), tickerResponse.percent_change_24h.toFloat()))
        if (tickerResponse.percent_change_24h.toDouble() > 0) {
            remoteViews.setTextColor(R.id.srn_change, Color.GREEN)
        } else {
            remoteViews.setTextColor(R.id.srn_change, Color.RED)
        }
        val amountFormatter = DecimalFormat(res.getString(R.string.readable_number))
        remoteViews.setTextViewText(R.id.circulation, kotlin.String.format(res!!.getString(R.string.dollar_amount), amountFormatter.format(tickerResponse.volume_usd.toDouble()).toString()))
        remoteViews.setViewVisibility(R.id.srn, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.total_supply, View.VISIBLE)
    }

    private fun fixColorBug(remoteViews: RemoteViews) {
        remoteViews.setTextColor(R.id.eth_text, Color.WHITE)
        remoteViews.setTextColor(R.id.eth_in_usd, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_text, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_in_usd, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_in_ether, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_in_ether_srn, Color.WHITE)
        remoteViews.setTextColor(R.id.srn_in_ether_ether, Color.WHITE)
    }

    private fun getTimerAnimationObservable(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, thisWidget: ComponentName, applicationContext: SRNWidgetApp): Disposable {
        return Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { l ->
                    Log.d(TAG, "mProgressMax: " + mProgressMax + ", l: " + l + ", timer: " + (mProgressMax - l.toInt()).toString())
                    remoteViews.setProgressBar(R.id.animating_bar, mProgressMax, mProgressMax - l.toInt(), false)
                    appWidgetManager!!.updateAppWidget(thisWidget, remoteViews)

                    if (l >= mProgressMax) {
                        Log.d(TAG, "stop itter")
                        applicationContext.mTimerAnimationDisposable?.dispose()
                    }
                }
    }
    //---------------------------------------- Companion -------------------------------------

    companion object {
        private val TAG = CrowdsaleAppWidgetProvider::class.java.simpleName
        var MY_WIDGET_UPDATE = "MY_OWN_WIDGET_UPDATE"

    }
}