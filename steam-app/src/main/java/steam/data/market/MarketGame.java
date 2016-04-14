package steam.data.market;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import steam.util.Constants;

@Entity
@Table(schema = Constants.DERBY_SCHEMA)
public class MarketGame {

	@Id
	private int id;

	@Column
	private String gameName;

	public int getId() {
		return id;
	}

	public String getGameName() {
		return gameName;
	}

	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE);
		toStringBuilder.append("gameId", id);
		toStringBuilder.append("gameName", gameName);
		return toStringBuilder.toString();
	}
}