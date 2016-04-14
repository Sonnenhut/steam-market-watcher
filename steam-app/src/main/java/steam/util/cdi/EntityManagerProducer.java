package steam.util.cdi;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerProducer {

	private static final String PERSISTENCE_UNIT_NAME = "jpa";
	
	private EntityManagerFactory emfInstance = null;

	@Produces
	public EntityManagerFactory produceEMF() {
		return getEMF();
	}

	public void disposeEntityManager(@Disposes EntityManagerFactory emf) {
		emf.close();
	}

	private EntityManagerFactory getEMF() {
		if (emfInstance == null) {
			emfInstance = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, System.getProperties());
		}
		return emfInstance;
	}
}
