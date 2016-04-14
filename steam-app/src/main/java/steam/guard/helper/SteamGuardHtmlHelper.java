package steam.guard.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

final class SteamGuardHtmlHelper {

	private SteamGuardHtmlHelper() {
	}

	/**
	 * looks for the SteamGuard Code inside the given html
	 * 
	 * @param html
	 *            the html to look for the code
	 * @return steamguard code, null if not found
	 */
	public static String getSteamGuardCodeFromHtml(String html) {
		String res = null;
		if (html != null) {
			Document doc = Jsoup.parse(html);
			Elements elements =  doc.select("tr > td > div > span");
			Element codeElement = elements.first();
			if(codeElement != null) {
				res = codeElement.text();
			}
		}
		return res;
	}
}
