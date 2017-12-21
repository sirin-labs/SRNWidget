package widget.sirinlabs.com.crowdsale

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        fetchData()!!.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { SirinValueResponse ->
                    val totalEther = SirinValueResponse.body().multisig_eth.toDouble() + SirinValueResponse.body().vault_eth.toDouble()
                    ether_raised.text = totalEther.toInt().toString()
                    eth_in_usd.text = SirinValueResponse.body().ethusd.toDouble().toInt().toString()
                    fiat_raised.text = SirinValueResponse.body().fiat_usd
                    multisig.text = SirinValueResponse.body().multisig_eth.toDouble().toInt().toString()
                    sirin_total_supply.text = SirinValueResponse.body().srn_total_supply_wei.toDouble().toInt().toString()
                    total_ether.text = SirinValueResponse.body().value
                    vault_total_ether.text = SirinValueResponse.body().vault_eth.toDouble().toInt().toString()
                })
    }

    fun formatString(text: String): String {

        val json = StringBuilder()
        var indentString = ""

        var inQuotes = false
        var isEscaped = false

        for (i in 0 until text.length) {
            val letter = text[i]

            when (letter) {
                '\\' -> isEscaped = !isEscaped
                '"' -> if (!isEscaped) {
                    inQuotes = !inQuotes
                }
                else -> isEscaped = false
            }

            if (!inQuotes && !isEscaped) {
                when (letter) {
                    '{', '[' -> {
                        json.append("\n" + indentString + letter + "\n")
                        indentString = indentString + "\t"
                        json.append(indentString)
                    }
                    '}', ']' -> {
                        indentString = indentString.replaceFirst("\t".toRegex(), "")
                        json.append("\n" + indentString + letter)
                    }
                    ',' -> json.append(letter + "\n" + indentString)
                    else -> json.append(letter)
                }
            } else {
                json.append(letter)
            }
        }

        return json.toString()
    }
}
