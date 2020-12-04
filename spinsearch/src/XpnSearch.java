import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class XpnSearch extends SpinSearch {
	private	static Elements getSpinData(ArtistInfo currentArtist, String url, String songOrAlbumName) throws Exception {
		Map<String, String> postData = new HashMap<>();
		String artist = (String) currentArtist.getArtistName();
		postData.put("val", "search");
		postData.put("search", artist);
		postData.put("playlist", "all");
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = null;
		
		spinData = (page.select(String.format("td:containsOwn(%s)", songOrAlbumName)));
		System.out.println("*** Retrieved " + artist + " spins: " + spinData.text());
		
		
		return spinData;
	}
}
