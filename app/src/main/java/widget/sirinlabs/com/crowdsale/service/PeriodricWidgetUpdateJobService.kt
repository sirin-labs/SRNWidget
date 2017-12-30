package widget.sirinlabs.com.crowdsale.service

import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log


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
        Log.i(TAG, "onStopJob")

        return true
    }

    companion object {
        private val TAG = PeriodricWidgetUpdateJobService::class.java.simpleName
    }
}