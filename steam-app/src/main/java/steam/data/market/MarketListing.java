package steam.data.market;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import steam.util.Constants;

@Entity
@Table(schema=Constants.DERBY_SCHEMA)
@NamedQueries({ @NamedQuery(name = MarketListing.QUERY_ALL, query = "SELECT l FROM MarketListing l"),
		@NamedQuery(name = MarketListing.QUERY_HIGHEST_PRICE_IN_X_DAYS, query = "SELECT max(l.price.priceWithFee) FROM MarketListing l WHERE l.itemKey.gameId = ?1 AND l.itemKey.itemName = ?2 AND l.timestamp <= ?3 ") })
public class MarketListing {

	public static final String QUERY_ALL = "MarketListing.QUERY_ALL";
	public static final String QUERY_HIGHEST_PRICE_IN_X_DAYS = "MarketListing.QUERY_HIGHEST_PRICE_IN_X_DAYS";

	@Id
	private String id;
	
	@Embedded
	private MarketItemKey itemKey;

	@Embedded
	private MarketPrice price;

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Calendar timestamp;

	public MarketListing() {
	}

	public MarketListing(String id, MarketItemKey itemKey, MarketPrice price) {
		this.id = id;
		this.price = price;
		this.timestamp = Calendar.getInstance();
		this.itemKey = itemKey;
	}

	public MarketPrice getPrice() {
		return price;
	}

	public String getId() {
		return id;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public MarketItemKey getMarketItemKey() {
		return itemKey;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", getId())
				.append("timestamp", Constants.DATE_FORMAT.format(getTimestamp().getTime())) //
				.append("price", getPrice())
				.build();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MarketListing)) {
			return false;
		}
		MarketListing other = (MarketListing) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(getId(), other.getId());
		return equalsBuilder.isEquals();
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(getId());
		return hashCodeBuilder.toHashCode();
	}
}
