package steam.web.handler;

import org.junit.Test;

import junit.framework.TestCase;

public class SteamHandlerTest extends TestCase {

	private static final String url = "http://steamcommunity.com/market/listings/730/StatTrak%E2%84%A2%20XM1014%20%7C%20Red%20Python%20%28Well-Worn%29";
	private static final String okUrl = "/listings/730/";
	
	@Test
	public void testGameIdRegex() {
		int res = SteamHandler.extractGameIdFromUrl(url);
		assertEquals(730, res);
//		res = SteamHandler.extractGameId(okUrl);
		assertEquals(730, res);
	}
	
}
