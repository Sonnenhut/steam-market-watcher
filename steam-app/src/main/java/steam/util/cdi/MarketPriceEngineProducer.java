package steam.util.cdi;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import steam.engine.Configurable;
import steam.engine.MarketPriceConfigurableEngine;
import steam.engine.MarketPriceEngine;

public class MarketPriceEngineProducer {

	@Produces
	@Configurable
	public MarketPriceEngine produce(InjectionPoint ip) {
		Configurable configurable = ip.getAnnotated().getAnnotation(Configurable.class);

		return new MarketPriceConfigurableEngine(configurable.profitablePrice());
	}
}