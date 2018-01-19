package widget.sirinlabs.com.crowdsale

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import android.content.pm.PackageInfo




class MainActivity : AppCompatActivity() {

    //---------------------------------------- Members ---------------------------------------------

    private lateinit var mDisposable: Disposable

    //---------------------------------------- Overrides -------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        mDisposable = fetchData()!!.observeOn(AndroidSchedulers.mainThread())
                .retry()
                .repeatWhen { completed -> completed.delay(10, TimeUnit.SECONDS) }
                .subscribeBy(onNext = { SirinValueResponse ->
                    val res = resources
                    val amountFormatter = DecimalFormat(res.getString(R.string.readable_number))
                    val totalEther = SirinValueResponse.body().multisig_eth.toDouble() + SirinValueResponse.body().vault_eth.toDouble()

                    val info = getPackageManager().getPackageInfo(this.packageName, 0)

                    version_modifier.text = info.versionName
                    eth_in_usd.text = String.format(res.getString(R.string.dollar_amount), amountFormatter.format(SirinValueResponse.body().ethusd.toDouble().toInt()).toString())
                    total_in_usd.text = String.format(res.getString(R.string.dollar_amount), SirinValueResponse.body().value)
                    ether_raised.text = String.format(res.getString(R.string.eth_amount), amountFormatter.format(totalEther.toInt()).toString())
                    multisig.text = String.format(res.getString(R.string.eth_amount), amountFormatter.format(SirinValueResponse.body().multisig_eth.toDouble().toInt()).toString())
                    vault_total_ether.text = String.format(res.getString(R.string.eth_amount), amountFormatter.format(SirinValueResponse.body().vault_eth.toDouble().toInt()).toString())
                    fiat_raised.text = String.format(res.getString(R.string.dollar_amount), SirinValueResponse.body().fiat_usd)
                    sirin_total_supply.text = String.format(res.getString(R.string.srn_amount), amountFormatter.format(SirinValueResponse.body().srn_total_supply_wei.toDouble().toInt()))
                }, onError = { Throwable ->
                    Log.e(MainActivity.TAG, Throwable.message)
                })
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG,"onPause")

        mDisposable.dispose()
    }

    //---------------------------------------- Companion -------------------------------------

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
