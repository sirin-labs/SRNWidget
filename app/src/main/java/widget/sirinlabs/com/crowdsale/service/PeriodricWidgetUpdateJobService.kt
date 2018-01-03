package widget.sirinlabs.com.crowdsale.service

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import widget.sirinlabs.com.crowdsale.R


/**
 * Created by ttuo on 26/03/15.
 */
class PeriodricWidgetUpdateJobService : JobService() {

    //---------------------------------------- Members ---------------------------------------------

    private var mUpdateInterval: Int = 0
    private var mJobId: Int = 0

    //---------------------------------------- Overrides -------------------------------------------

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStartJob")
        firstInit()
        runUpdateService()
        reschedule()
        jobFinished(params, false)
        return true
    }

    //---------------------------------------- Overrides -------------------------------------------

    private fun firstInit() {
        if (mUpdateInterval == 0) {
            mUpdateInterval = (applicationContext!!.resources.getInteger(R.integer.update_interval) * 1000 * 1.1).toInt()
        }
        if (mJobId == 0) {
            mJobId = applicationContext!!.resources.getInteger(R.integer.periodric_job_id)
        }
    }

    override fun onStopJob(JobParameters: JobParameters?): Boolean {
        Log.i(TAG, "onStopJob")
        return true
    }

    //---------------------------------------- Private Methods -------------------------------------

    private fun reschedule() {
        val serviceComponent = ComponentName(applicationContext, PeriodricWidgetUpdateJobService::class.java)
        val jobInfo: JobInfo = JobInfo.Builder(mJobId, serviceComponent)
                .setMinimumLatency(mUpdateInterval.toLong()).build()

        val jobScheduler = applicationContext?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    private fun runUpdateService() {
        val serviceIntent = Intent(applicationContext, SingleWidgetUpdateIntentService::class.java)
        val pending = PendingIntent.getService(applicationContext, 0, serviceIntent, 0)
        pending.send()
    }

    //---------------------------------------- Companion -------------------------------------

    companion object {
        private val TAG = PeriodricWidgetUpdateJobService::class.java.simpleName
    }
}