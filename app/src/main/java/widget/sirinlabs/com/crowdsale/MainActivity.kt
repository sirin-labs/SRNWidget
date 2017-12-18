package widget.sirinlabs.com.crowdsale

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        fetchData()!!.observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = { SirinValueResponse ->
                    val gson = Gson()
                    val j = JSONObject(gson.toJson(SirinValueResponse.body()))
                    json.text = formatString(j.toString())

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
