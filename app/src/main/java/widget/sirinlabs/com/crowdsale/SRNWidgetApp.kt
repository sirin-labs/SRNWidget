package widget.sirinlabs.com.crowdsale

import android.app.Application
import io.reactivex.disposables.Disposable

/**
 * Created by gilado on 12/31/2017.
 */
class SRNWidgetApp : Application() {
    var mTimerAnimationDisposable: Disposable? = null
}
