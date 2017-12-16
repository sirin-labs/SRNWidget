package widget.sirinlabs.com.crowdsale.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
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
    private lateinit var service:PendingIntent
    private var alarmManager: AlarmManager? = null

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        startPeriodricUpdates(context, appWidgetIds)
        setClick(context, appWidgetManager, appWidgetIds)
    }

    private fun setClick(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val remoteViews = RemoteViews(context?.packageName, R.layout.appwidget)
        remoteViews.setOnClickPendingIntent(R.id.bg, pendingIntent)
        appWidgetManager?.updateAppWidget(appWidgetIds!![0], remoteViews)
    }

    private fun startPeriodricUpdates(context: Context?, appWidgetIds: IntArray?) {
        alarmManager = context?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, SRNWidgetService::class.java).putExtra("id", appWidgetIds!![0])
        service = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager?.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60000, service);
    }
}