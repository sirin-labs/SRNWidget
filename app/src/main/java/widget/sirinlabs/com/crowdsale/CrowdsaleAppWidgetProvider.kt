package widget.sirinlabs.com.crowdsale

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.PersistableBundle
import android.util.Log
import android.widget.RemoteViews
import widget.sirinlabs.com.crowdsale.service.PeriodricWidgetUpdateJobService
import widget.sirinlabs.com.crowdsale.service.SingleWidgetUpdateIntentService


/**
 * Created by yarons on 29/11/17.
 */
/**
 * Created by yarons on 27/11/17.
 */
class CrowdsaleAppWidgetProvider : AppWidgetProvider() {

    private val TAG: String = "CrowdsaleAppWidget"
    private val INTERVAL: Long = 40000

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        startPeriodicUpdates(context, appWidgetIds)
        setClick(context, appWidgetManager, appWidgetIds)
    }

//    override fun onReceive(context: Context?, intent: Intent?) {
//        super.onReceive(context, intent)
//        Log.d(TAG,"onrecieve")
//        val activityIntent = Intent(context, MainActivity::class.java)
//        activityIntent.flags = FLAG_ACTIVITY_NEW_TASK
//        context?.startActivity(activityIntent, null)
//    }

    private fun setClick(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        clickOnUpd(context, appWidgetManager)
        clickExpand(context, appWidgetManager)
    }

    private fun clickExpand(context: Context?,appWidgetManager: AppWidgetManager?) {
        val remoteViews = RemoteViews(context?.packageName, R.layout.appwidget)
        val activityIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.frame, pendingIntent)

        val thisWidget = ComponentName(context, CrowdsaleAppWidgetProvider::class.java)

        appWidgetManager?.updateAppWidget(thisWidget, remoteViews)
    }

    private fun clickOnUpd(context: Context?, appWidgetManager: AppWidgetManager?) {
        val remoteViews = RemoteViews(context?.packageName, R.layout.appwidget)
        val serviceIntent = Intent(context, SingleWidgetUpdateIntentService::class.java)
        val pendingIntent = PendingIntent.getService(context, 0, serviceIntent, 0)
        remoteViews.setOnClickPendingIntent(R.id.progress_bar, pendingIntent)
        val thisWidget = ComponentName(context, CrowdsaleAppWidgetProvider::class.java)

        appWidgetManager?.updateAppWidget(thisWidget, remoteViews)
    }

    @SuppressLint("NewApi")
    private fun startPeriodicUpdates(context: Context?, appWidgetIds: IntArray?) {
        val serviceComponent = ComponentName(context, PeriodricWidgetUpdateJobService::class.java)
        val builder = JobInfo.Builder(0, serviceComponent)

        builder.setPersisted(true)
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        builder.setPeriodic(INTERVAL)
        val bundle = PersistableBundle()
        bundle.putInt("id",appWidgetIds!![0])
        builder.setExtras(bundle)

        val jobScheduler = context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }
}