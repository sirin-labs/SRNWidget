package widget.sirinlabs.com.crowdsale.widget

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
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
 * Created by ttuo on 26/03/15.
 */
class SRNWidgetService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        val id = params.extras.getInt("id", 0)
        updateWidget(id)
        return false
    }

    override fun onStopJob(JobParameters: JobParameters?): Boolean {
        return true
    }


    private lateinit var mDisposable: Disposable

    private fun updateWidget(widgetId: Int?) {
        val appWidgetManager = AppWidgetManager
                .getInstance(applicationContext)

        val remoteViews = RemoteViews(application.packageName, R.layout.appwidget)
        remoteViews.setViewVisibility(R.id.progressBar, View.VISIBLE)
        appWidgetManager.updateAppWidget(widgetId!!, remoteViews)

        updateData(remoteViews, appWidgetManager, widgetId!!)
        remoteViews.setViewVisibility(R.id.progressBar, View.GONE)
    }

    private fun updateData(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, widgetId: Int) {
        mDisposable = fetchData()!!.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { SirinValueResponse ->
                    if (SirinValueResponse.isSuccessful) {
                        Log.d(TAG, "value:" + SirinValueResponse.body().value)
                        val dollar = SirinValueResponse.body().value
                        val amountFormatter = DecimalFormat("#,###,###")

                        val ether:Int= SirinValueResponse.body().multisig_eth.toDouble().toInt() + SirinValueResponse.body().vault_eth.toDouble().toInt();
                        remoteViews.setTextViewText(R.id.dollar_raised, dollar + "$")
                        val str = amountFormatter.format(ether) + " ETH"
                        remoteViews.setTextViewText(R.id.ether_raised, str)
                        remoteViews.setTextViewText(R.id.update_time, android.text.format.DateFormat.format("hh:mm a", java.util.Date()))
                        Log.d(TAG, "vault_eth:" + SirinValueResponse.body().vault_eth + "multisig_eth:" + SirinValueResponse.body().multisig_eth)
                        appWidgetManager!!.updateAppWidget(widgetId, remoteViews)
                        mDisposable.dispose()
                    } else {
                        Log.d(TAG, "not successful")
                    }
                }, onError = { Throwable ->
                    Log.e(TAG, Throwable.message)
                })
    }

    companion object {
        private val TAG = SRNWidgetService::class.java.simpleName
    }
}