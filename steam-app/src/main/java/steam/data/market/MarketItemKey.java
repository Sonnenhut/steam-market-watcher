package steam.data.market;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Embeddable
public class MarketItemKey implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column
	private int gameId;

	@Column
	private String itemName;

	public MarketItemKey() {
	}

	public MarketItemKey(int gameId, String itemName) {
		this.gameId = gameId;
		this.itemName = itemName;
	}

	public int getGameId() {
		return gameId;
	}

	public String getItemName() {
		return itemName;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(gameId);
		hashCodeBuilder.append(itemName);
		return hashCodeBuilder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MarketItemKey)) {
			return false;
		}
		MarketItemKey other = (MarketItemKey) obj;
		org.apache.commons.lang3.builder.EqualsBuilder equalsBuilder = new org.apache.commons.lang3.builder.EqualsBuilder();
		equalsBuilder.append(gameId, other.gameId);
		equalsBuilder.append(itemName, other.itemName);
		return equalsBuilder.isEquals();
	}

	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		toStringBuilder.append("gameId", gameId);
		toStringBuilder.append("itemName", itemName);
		return toStringBuilder.toString();
	}
}
