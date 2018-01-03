package widget.sirinlabs.com.crowdsale

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import widget.sirinlabs.com.crowdsale.service.PeriodricWidgetUpdateJobService
import widget.sirinlabs.com.crowdsale.service.SingleWidgetUpdateIntentService


/**
 * Created by yarons on 27/11/17.
 */
class CrowdsaleAppWidgetProvider : AppWidgetProvider() {

    //---------------------------------------- Members ---------------------------------------------

    private val TAG: String = "CrowdsaleAppWidget"
    private val JOB_ID: Int = 1001
    private var mUpdateInterval: Int = 0

    //---------------------------------------- Overrides -------------------------------------------

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        mUpdateInterval = context!!.resources.getInteger(R.integer.update_interval) * 1000
        fetchData(context)
        startPeriodicUpdates(context)
        setClick(context, appWidgetManager, appWidgetIds)
    }

    //---------------------------------------- Private Methods -------------------------------------

    private fun setClick(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
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
}