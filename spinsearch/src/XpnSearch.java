import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class XpnSearch extends SpinSearch {
	
	public  Elements getSpinData(ArtistInfo currentArtist, String url, String songOrAlbumName) throws Exception {
		Map<String, String> postData = new HashMap<>();
		String artist = (String) currentArtist.getArtistName();
		postData.put("val", "search");
		postData.put("search", artist);
		postData.put("playlist", "all");
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = null;
		
		spinData = (page.select(String.format("td:containsOwn(%s)", songOrAlbumName)));
		System.out.println("*** Retrieasfved " + artist + " spins: " + spinData.text());
		
		
		return spinData;
	}
	
	public void addSpin(Elements spinData, ArtistInfo artistInfo, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, Spin> allSpins) throws Exception   {
		for (Element e : spinData) {
			boolean correctAlbum = true;
			String[] segments = e.text().split(" - ");
			if(artistInfo.getArtistName() == artistInfo.getAlbum()) {
				if(segments[2] != artistInfo.getAlbum()) {
					correctAlbum = false;
				}
			}
		if (correctAlbum)	{
			String song = segments[1];
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
			Date spinDate = formatter.parse(segments[0].substring(0, 19));
			
			if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)){
				System.out.println("song is " + song + " date is " + spinDate);
				String key = artistInfo.getArtistName() + artistInfo.getAlbum() + song;
				Spin spin = allSpins.get(key);
				
				if (spin == null) {
					spin = new Spin(artistInfo.getArtistName(), song, artistInfo.getAlbum(), spinDate, spinDate);
					spin.setDj("Kristen Kurtis");
				} 
				else {
					if (spinDate.before(spin.getFirstPlayDate())) {
						spin.setFirstPlayDate(spinDate);
					}
					if (spinDate.after(spin.getLastPlayDate())) {
						spin.setLastPlayDate(spinDate);
					}
				}
				
				spin.incrementCount();

				System.out.println("Spin: " + e.text());
				allSpins.put(key, spin);
			}
		}

		}
	}
}

