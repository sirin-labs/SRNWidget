package widget.sirinlabs.com.crowdsale

import widget.sirinlabs.com.crowdsale.network.cmc.TickerResponse
import widget.sirinlabs.com.crowdsale.network.sirin.ValuesResponse

/**
 * Created by yaron on 30/11/17.
 */
class WidgetData(val eth_usd: String, val price_usd: String, val total_supply: String, val percent_change_1h: String, val percent_change_24h: String, val last_updated: String) {
    constructor(ticker: TickerResponse, value: ValuesResponse?) : this(
            value!!.ethusd, ticker.price_usd, ticker.total_supply, ticker.percent_change_1h, ticker.percent_change_24h, ticker.last_updated)
}
