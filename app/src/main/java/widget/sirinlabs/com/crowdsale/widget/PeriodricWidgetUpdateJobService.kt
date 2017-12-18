package widget.sirinlabs.com.crowdsale.widget

import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
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
 * Created by ttuo on 26/03/15.
 */
class PeriodricWidgetUpdateJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG,"onStartJob")
        val serviceIntent = Intent(applicationContext, SingleWidgetUpdateIntentService::class.java)
        val pending = PendingIntent.getService(applicationContext, 0, serviceIntent, 0)
        pending.send()
        return false
    }

    override fun onStopJob(JobParameters: JobParameters?): Boolean {
        return true
    }

    companion object {
        private val TAG = PeriodricWidgetUpdateJobService::class.java.simpleName
    }
}