package widget.sirinlabs.com.crowdsale.model

/**
 * Created by yarons on 29/11/17.
 */
class RedditDataResponse(
        val children: List<RedditChildrenResponse>,
        val after: String?,
        val before: String?
)