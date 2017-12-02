package widget.sirinlabs.com.crowdsale

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import io.reactivex.rxkotlin.subscribeBy

/**
 * Created by yarons on 29/11/17.
 */
/**
 * Created by yarons on 27/11/17.
 */
class CrowdsaleAppWidgetProvider : AppWidgetProvider() {

    val TAG:String = "CrowdsaleAppWidget"

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val remoteViews = RemoteViews(context!!.packageName,
                R.layout.appwidget)

        val subscribeBy = fetchData()!!.subscribeBy(onNext = {RedditNewsResponse->
            if(RedditNewsResponse.isSuccessful)
            {
                remoteViews.setTextViewText(R.id.text_view,RedditNewsResponse.body().data.children.get(0).data.author )
                appWidgetManager!!.updateAppWidget(appWidgetIds!![0],remoteViews)
            }
        },onError = {Throwable->
            Log.e(TAG,Throwable.message)
        });

    }




}