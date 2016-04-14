package steam.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import steam.data.market.MarketCurrency;
import steam.data.market.MarketItem;
import steam.data.market.MarketItemKey;
import steam.data.market.MarketListing;

public class DataStore {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private final EntityManager em;
	private final PersistenceUnitUtil util;

	public DataStore(EntityManagerFactory emf) {
		logger.trace("Created DataStorage '{}'", this);
		this.em = emf.createEntityManager();
		this.util = emf.getPersistenceUnitUtil();
	}

	@PreDestroy
	public void close() {
		this.em.close();
	}

	public <T> T find(Class<T> classToFind, Object primaryKey) {
		return this.em.find(classToFind, primaryKey);
	}

	private void persistMarketListings(Collection<MarketListing> listings) {
		// we delete the pre-existing items, because we don't want to have old
		// entries in the DB (timestamp is old)
		persistObjects(listings, MarketListing.class, true);
	}

	public void persistMarketItems(Collection<MarketItem> toPersist) {
		persistObjects(toPersist, MarketItem.class, false);
	}

	public void persistWatchedMarketItems(Collection<WatchedMarketItem> toPersist) {
		persistObjects(toPersist, WatchedMarketItem.class, false);
	}

	public <T> void persistObjects(Collection<T> ts, Class<T> clazz, boolean deleteWithSameIntegrity) {
		beginTransaction();

		// entities to ignore in order to not get a SqlIntegrityViolationExc
		Collection<T> ignoreList = new ArrayList<T>();

		// Field jpaIdField = getFieldWithIdAnnotation(clazz);
		for (T t : ts) {
			logger.trace("finding : " + t);
			T persistedT = em.find(clazz, util.getIdentifier(t));
			if (persistedT != null) {
				// remove already present listings
				if (deleteWithSameIntegrity) {
					em.remove(persistedT);
				} else {
					ignoreList.add(t);
				}
			}
		}
		if (deleteWithSameIntegrity) {
			em.flush();
		}
		for (T t : ts) {
			// now persist.
			if (!ignoreList.contains(t)) {
				em.persist(t);
			}
		}
		endTransaction();
	}

	public List<MarketItem> queryNotWatchedItemWithPriceBetween(int maxResults, double minPrice, double maxPrice, MarketCurrency currency, Collection<MarketItemKey> ignoreItems) {
		Collection<String> ignoredItemNames = new HashSet<>();
		for(MarketItemKey key : ignoreItems) {
			ignoredItemNames.add(key.getItemName());
		}
		// add some non-existent itemNames and GameIds that the query wont fail on an empty collection
		ignoredItemNames.add("");
		// create the query.
		TypedQuery<MarketItem> q = em.createNamedQuery(WatchedMarketItem.QUERY_NOT_WATCHED_WITH_PRICE_RANGE,
				MarketItem.class);
		q.setMaxResults(maxResults);
		q.setParameter("minPrice", minPrice);
		q.setParameter("maxPrice", maxPrice);
		q.setParameter("currency", currency);
		q.setParameter("ignoredItemNames", ignoredItemNames);
		List<MarketItem> results = q.getResultList();
		return results;
	}

	public double queryHighestPriceSince(MarketItemKey itemKey, Calendar since) {
		beginTransaction();
		Query q = getQueryWithParameters(MarketListing.QUERY_HIGHEST_PRICE_IN_X_DAYS, itemKey.getGameId(),
				itemKey.getItemName(), since);
		double result = (double) q.getSingleResult();
		endTransaction();
		return result;
	}

	public List<MarketListing> queryAllSteamListings() {
		return queryByNamedQueryName(MarketListing.QUERY_ALL);
	}

	public void deleteAllWatchedMarketItems() {
		beginTransaction();
		em.createQuery("DELETE FROM WatchedMarketItem w").executeUpdate();
		endTransaction();
	}

	public void executeUpdate(String jpqlQuery) {
		beginTransaction();
		em.createQuery(jpqlQuery).executeUpdate();
		endTransaction();
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> queryByNamedQueryName(String namedQueryName, Object... parameters) {
		beginTransaction();
		Query q = getQueryWithParameters(namedQueryName, parameters);
		List<T> results = q.getResultList();
		endTransaction();
		return results;
	}

	private Query getQueryWithParameters(String namedQueryName, Object... parameters) {
		Query q = em.createNamedQuery(namedQueryName);
		setParameters(q, parameters);
		return q;
	}

	private void setParameters(Query q, Object... parameters) {
		for (int i = 1; i <= parameters.length; i++) {
			Object parameter = parameters[i - 1];
			if (parameter instanceof Calendar) {
				q.setParameter(i, (Calendar) parameter, TemporalType.TIMESTAMP);
			} else {
				q.setParameter(i, parameter);
			}
		}
	}

	private void beginTransaction() {
		em.getTransaction().begin();
	}

	private void endTransaction() {
		em.getTransaction().commit();
	}

	// public static DataStorage getNewInstance() {
	// // System.getProperties will allow us to override some jpa
	// // properties.
	// EntityManagerFactory factory =
	// Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME,
	// System.getProperties());
	// return new DataStorage(factory);
	// }
}
