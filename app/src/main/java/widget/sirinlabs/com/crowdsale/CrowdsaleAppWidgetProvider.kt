package widget.sirinlabs.com.crowdsale

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/**
 * Created by yarons on 29/11/17.
 */
/**
 * Created by yarons on 27/11/17.
 */
class CrowdsaleAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val remoteViews = RemoteViews(context!!.packageName,
                R.layout.appwidget)
        remoteViews.setTextViewText(R.id.text_view, "new update")

        appWidgetManager!!.updateAppWidget(appWidgetIds!![0],remoteViews)

    }




}