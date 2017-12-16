package widget.sirinlabs.com.crowdsale.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import widget.sirinlabs.com.crowdsale.R
import widget.sirinlabs.com.crowdsale.fetchData
import java.util.*


/**
 * Created by ttuo on 26/03/15.
 */
class SRNWidgetService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWidget(intent?.getIntExtra("id", 0))
        return super.onStartCommand(intent, flags, startId)
    }

    private lateinit var mDisposable: Disposable

    private fun updateWidget(widgetId: Int?) {
        val appWidgetManager = AppWidgetManager
                .getInstance(applicationContext)

        val remoteViews = RemoteViews(application.packageName, R.layout.appwidget)
        remoteViews.setViewVisibility(R.id.dollar_raised, View.GONE)
        remoteViews.setViewVisibility(R.id.progressBar, View.VISIBLE)
        appWidgetManager.updateAppWidget(widgetId!!, remoteViews)

        updateData(remoteViews, appWidgetManager, widgetId!!)
        remoteViews.setViewVisibility(R.id.dollar_raised, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.progressBar, View.GONE)
    }

    private fun updateData(remoteViews: RemoteViews, appWidgetManager: AppWidgetManager?, widgetId: Int) {
        mDisposable = fetchData()!!.observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribeBy(onNext = { SirinValueResponse ->
                    if (SirinValueResponse.isSuccessful) {
                        Log.d(TAG, "value:" + SirinValueResponse.body().value)
                        remoteViews.setTextViewText(R.id.dollar_raised, SirinValueResponse.body().value)
                        remoteViews.setTextViewText(R.id.update_time, android.text.format.DateFormat.format("hh:mm a", java.util.Date()))
                        appWidgetManager!!.updateAppWidget(widgetId, remoteViews)
                    } else {
                        Log.d(TAG, "not successful")
                    }
                }, onError = { Throwable ->
                    Log.e(TAG, Throwable.message)
                })
    }

    private fun clearSubscriptions() {
        mDisposable?.dispose()
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        clearSubscriptions()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private val TAG = SRNWidgetService::class.java.simpleName
    }
}