package steam.data;

import java.util.Calendar;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import steam.data.market.MarketItemKey;

@Entity
@NamedQuery(name = WatchedMarketItem.QUERY_NOT_WATCHED_WITH_PRICE_RANGE, query = "SELECT i FROM MarketItem i LEFT JOIN i.listings l WHERE l.price.priceWithFee >= :minPrice AND l.price.priceWithFee <= :maxPrice AND l.price.currency = :currency AND i.key.itemName NOT IN :ignoredItemNames AND NOT EXISTS (SELECT ii FROM MarketItem ii, WatchedMarketItem w WHERE i.key.gameId = ii.key.gameId AND ii.key.itemName = i.key.itemName AND w.key.gameId = ii.key.gameId AND ii.key.itemName = w.key.itemName)")
public class WatchedMarketItem {

	public static final String QUERY_NOT_WATCHED_WITH_PRICE_RANGE = "WatchedMarketItem.QUERY_NOT_WATCHED";

	@EmbeddedId
	public MarketItemKey key;

	public String jobId;

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar createdTimestamp;

	public WatchedMarketItem() {
	}

	public WatchedMarketItem(MarketItemKey key, String jobId) {
		this.key = key;
		this.jobId = jobId;
		this.createdTimestamp = Calendar.getInstance();
	}

	public MarketItemKey getKey() {
		return key;
	}

	public void setKey(MarketItemKey key) {
		this.key = key;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
}