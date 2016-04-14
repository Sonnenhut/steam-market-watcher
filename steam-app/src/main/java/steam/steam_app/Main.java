package steam.steam_app;

import steam.util.cdi.WeldContext;

public class Main {

	public static void main(String[] args) {
		// bootstrap the application...
		final SteamAgent agent =  WeldContext.INSTANCE.getBean(SteamAgent.class);
		// use run() in order to get the ThreadContext started...
		agent.run();
	}

}
