package widget.sirinlabs.com.crowdsale.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PersistableBundle
import android.widget.RemoteViews
import widget.sirinlabs.com.crowdsale.MainActivity
import widget.sirinlabs.com.crowdsale.R


/**
 * Created by yarons on 29/11/17.
 */
/**
 * Created by yarons on 27/11/17.
 */
class CrowdsaleAppWidgetProvider : AppWidgetProvider() {

    val TAG: String = "CrowdsaleAppWidget"

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        startPeriodricUpdates(context!!, appWidgetIds)
        setClick(context, appWidgetManager, appWidgetIds)
    }

    private fun setClick(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val remoteViews = RemoteViews(context?.packageName, R.layout.appwidget)
        remoteViews.setOnClickPendingIntent(R.id.bg, pendingIntent)
        appWidgetManager?.updateAppWidget(appWidgetIds!![0], remoteViews)
    }

    @SuppressLint("NewApi")
    private fun startPeriodricUpdates(context: Context, appWidgetIds: IntArray?) {
        val serviceComponent = ComponentName(context, SRNWidgetService::class.java)
        val builder = JobInfo.Builder(0, serviceComponent)

        builder.setPersisted(true)
        builder.setMinimumLatency(0) // wait at least
        builder.setOverrideDeadline(0) // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require unmetered network
        builder.setPeriodic(40000)
        val bundle = PersistableBundle()
        bundle.putInt("id",appWidgetIds!![0])
        builder.setExtras(bundle)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }
}