package widget.sirinlabs.com.crowdsale

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webview) as WebView
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(getString(R.string.sirin_labs))

    }
}
