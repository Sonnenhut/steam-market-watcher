package steam.util.cdi;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.jboss.weld.environment.se.contexts.ThreadScoped;
import org.slf4j.Logger;

import steam.data.DataStore;

public class DataStoreProducer {

	@Inject
	private Logger logger;

	// @Produces @Threadsafe @ThreadScoped
	// public DataStorage produceTS(EntityManager em, PersistenceUnitUtil util)
	// {
	// return createDS(null, em, util);
	// }
//
	@Produces
	@ThreadScoped
	public DataStore produce(EntityManagerFactory emf) {
		return createDS(null, emf);
	}

	private DataStore createDS(InjectionPoint injectionPoint, EntityManagerFactory emf) {
		DataStore res = new DataStore(emf);
		logger.trace("Created DataStorage '{}' for InjectionPoint '{}'", res, injectionPoint);
		return res;
	}
}
