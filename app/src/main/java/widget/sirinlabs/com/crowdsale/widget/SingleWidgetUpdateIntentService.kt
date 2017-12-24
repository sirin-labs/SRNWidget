package widget.sirinlabs.com.crowdsale.widget

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import widget.sirinlabs.com.crowdsale.R
import widget.sirinlabs.com.crowdsale.fetchData
import java.text.DecimalFormat


/**
 * Created by gilad or on 18/12/17.
 */
class SingleWidgetUpdateIntentService : IntentService(SingleWidgetUpdateIntentService::class.java.simpleName) {
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG,"onHandleIntent")
        updateWidget()
    }

    private lateinit var mDisposable: Disposable

    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager
                .getInstance(applicationContext)

        val remoteViews = RemoteViews(application.packageName, R.layout.appwidget)
        remoteViews.setViewVisibility(R.id.progressBar, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.progressBarShadow, View.INVISIBLE)
        val thisWidget = ComponentName(applicationContext, CrowdsaleAppWidgetProvider::class.java)

        appWidgetManager.updateAppWidget(thisWidget, remoteViews)

        updateData(remoteViews, appWidgetManager, thisWidget)
    }

    private fun updateData(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, widgetId: ComponentName?) {
        fetchData()!!.observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { t: Disposable ->
                    mDisposable = t
                }
                .subscribeBy(onNext = { SirinValueResponse ->
                    if (SirinValueResponse.isSuccessful) {
                        Log.d(SingleWidgetUpdateIntentService.TAG, "value:" + SirinValueResponse.body().value)

                        val res = resources
                        val totalEtherRaised:Int= SirinValueResponse.body().multisig_eth.toDouble().toInt() + SirinValueResponse.body().vault_eth.toDouble().toInt()
                        val amountFormatter = DecimalFormat(res.getString(R.string.readable_number))

                        remoteViews.setTextViewText(R.id.dollar_raised, SirinValueResponse.body().value + " $")
                        remoteViews.setTextViewText(R.id.ether_raised, amountFormatter.format(totalEtherRaised) + " eth")
                        remoteViews.setTextViewText(R.id.eth_in_usd, String.format(res.getString(R.string.eth_in_usd_format), SirinValueResponse.body().ethusd))
                        remoteViews.setTextViewText(R.id.update_time, android.text.format.DateFormat.format(res.getString(R.string.time_short), java.util.Date()))
                        remoteViews.setViewVisibility(R.id.progressBarShadow, View.VISIBLE)
                        remoteViews.setViewVisibility(R.id.progressBar, View.INVISIBLE)

                        Log.d(SingleWidgetUpdateIntentService.TAG, "vault_eth:" + SirinValueResponse.body().vault_eth + "multisig_eth:" + SirinValueResponse.body().multisig_eth)
                        appWidgetManager!!.updateAppWidget(widgetId, remoteViews)
                    } else {
                        Log.d(SingleWidgetUpdateIntentService.TAG, "not successful")
                    }
                }, onError = { Throwable ->
                    Log.e(SingleWidgetUpdateIntentService.TAG, Throwable.message)
                }, onComplete = {
                    mDisposable.dispose()
                })
    }

    companion object {
        private val TAG = SingleWidgetUpdateIntentService::class.java.simpleName
    }
}