package widget.sirinlabs.com.crowdsale.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import io.reactivex.rxkotlin.subscribeBy
import widget.sirinlabs.com.crowdsale.R
import widget.sirinlabs.com.crowdsale.fetchData
import java.util.concurrent.TimeUnit

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

        val subscribeBy = fetchData()!!.repeatWhen { completed -> completed.delay(40, TimeUnit.SECONDS) }.subscribeBy(onNext = { SirinValueResponse->
            if(SirinValueResponse.isSuccessful)
            {
                Log.d("gilad","value:" + SirinValueResponse.body().value)
                remoteViews.setTextViewText(R.id.text_view, SirinValueResponse.body().value)
                appWidgetManager!!.updateAppWidget(appWidgetIds!![0],remoteViews)
            }
        },onError = {Throwable->
            Log.e(TAG,Throwable.message)
        });

    }




}