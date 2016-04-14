package steam.util;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EclipseLinkSessionLog4j extends AbstractSessionLog {

	private static Logger logger = LoggerFactory.getLogger("JPA");

	@Override
	public void log(SessionLogEntry entry) {
		final int entryLevel = entry.getLevel();

		switch (entryLevel) {
		case SEVERE:
			logger.error(computeMessage(entry), entry.getException());

			break;

		case WARNING:
			logger.warn(computeMessage(entry, entry.getParameters()));

			break;

		case INFO:
		case CONFIG:
			logger.info(computeMessage(entry, entry.getParameters()));

			break;

		case FINE:
		case FINER:
		case FINEST:
			logger.debug(computeMessage(entry, entry.getParameters()));

			break;

		case ALL:
			logger.trace(computeMessage(entry, entry.getParameters()));

			break;

		case OFF:
			break;
		}
	}

	private String computeMessage(SessionLogEntry entry, Object... parameters) {
		String res = entry.getMessage();
		if (parameters != null) {
			for (Object parameter : parameters) {
				res += " " + parameter;
			}
		}
		return res;
	}

	private String computeMessage(SessionLogEntry entry) {
		String res = entry.getMessage();
		return res;
	}

}
