package widget.sirinlabs.com.crowdsale.network.cmc

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.Json

/**
 * Created by yaron on 30/11/17.
 */
class TickerResponse(val price_usd: String, val total_supply: String, val percent_change_1h: String, val percent_change_24h: String, @Json(name = "24h_volume_usd") val volume_usd: String, val last_updated: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(price_usd)
        parcel.writeString(total_supply)
        parcel.writeString(percent_change_1h)
        parcel.writeString(percent_change_24h)
        parcel.writeString(volume_usd)
        parcel.writeString(last_updated)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TickerResponse> {
        override fun createFromParcel(parcel: Parcel): TickerResponse {
            return TickerResponse(parcel)
        }

        override fun newArray(size: Int): Array<TickerResponse?> {
            return arrayOfNulls(size)
        }
    }
}