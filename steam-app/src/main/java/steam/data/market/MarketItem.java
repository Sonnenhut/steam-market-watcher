package steam.data.market;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import steam.util.Constants;

@Entity
@Table(schema = Constants.DERBY_SCHEMA)
@NamedQueries({ @NamedQuery(name = MarketItem.QUERY_ALL, query = "SELECT i FROM MarketItem i") })
public class MarketItem {

	public static final String QUERY_ALL = "MarketItem.QUERY_ALL_NOT_IGNORED";

	@EmbeddedId
	private MarketItemKey key;

	@Column
	private String url;

	@OneToMany(cascade=CascadeType.ALL)
	@JoinColumns({ @JoinColumn(name = "GAMEID", referencedColumnName = "GAMEID"),
			@JoinColumn(name = "ITEMNAME", referencedColumnName = "ITEMNAME") })
	private Set<MarketListing> listings = new HashSet<>();


	public MarketItem() {
	}

	public MarketItem(int gameId, String itemName, String url) {
		this.key = new MarketItemKey(gameId, itemName);
		this.url = url;
	}

	public int getGameId() {
		return key.getGameId();
	}

	public String getItemName() {
		return key.getItemName();
	}

	public MarketItemKey getKey() {
		return key;
	}

	public String getUrl() {
		return url;
	}
	
	public Set<MarketListing> getListings() {
		return listings;
	}

	public void setListings(Set<MarketListing> listings) {
		this.listings = listings;
	}

	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		toStringBuilder.append("gameId", getGameId());
		toStringBuilder.append("itemName", getItemName());
		return toStringBuilder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MarketItem)) {
			return false;
		}
		MarketItem other = (MarketItem) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(getKey(), other.getKey());
		return equalsBuilder.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(getKey().hashCode());
		return hashCodeBuilder.toHashCode();
	}
}
