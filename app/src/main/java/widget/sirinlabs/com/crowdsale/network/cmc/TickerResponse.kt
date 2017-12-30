package widget.sirinlabs.com.crowdsale.network.cmc

import com.squareup.moshi.Json

/**
 * Created by yaron on 30/11/17.
 */
class TickerResponse(val price_usd: String, val total_supply: String, val percent_change_1h: String, val percent_change_24h: String, @Json(name = "24h_volume_usd") val volume_usd : String, val last_updated: String)
