import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class WfmuSearch extends SpinSearch {
	public	 Elements getSpinData(ArtistInfo currentArtist, String url, String songOrAlbumName) throws Exception {
		Map<String, String> postData = new HashMap<>();
		String artist = (String) currentArtist.getArtistName();
		postData.put("artistonly", artist);
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = new Elements();
		songOrAlbumName = "Black Dog";
		Elements rawData = page.select(String.format("tr:contains(%s)", songOrAlbumName));
		
		if(rawData.size() > 1) {
			spinData = new Elements(rawData.subList(1, rawData.size())) ;
		}
		
		return spinData;
	}
	
	public void addSpin(Elements spinData, ArtistInfo artistInfo, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, Spin> allSpins) throws Exception   {
		for (Element e : spinData) {
			boolean correctAlbum = true;
			Elements singleSpinData = e.children();
			
			System.out.println("*** Retrieved " + artistInfo.getArtistName() + " spins: " + singleSpinData.eachText());
			if(artistInfo.getArtistName() == artistInfo.getAlbum()) {
				if(!singleSpinData.get(2).text().equalsIgnoreCase(artistInfo.getAlbum())) {
					correctAlbum = false;
				}
			}
		if (correctAlbum)	{
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
			Date spinDate = null;
			if(singleSpinData.size() == 4) {
				spinDate = formatter.parse(singleSpinData.get(3).text());
			}
			if (singleSpinData.size() == 5) {
				spinDate = formatter.parse(singleSpinData.get(4).text());
			}
			
			if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)){
				String song = singleSpinData.get(1).text();
				System.out.println("song is " + song + " date is " + spinDate);
				String key = artistInfo.getArtistName() + artistInfo.getAlbum() + song;
				Spin spin = allSpins.get(key);
				
				if (spin == null) {
					spin = new Spin(artistInfo.getArtistName(), song, artistInfo.getAlbum(), spinDate, spinDate);
					spin.setDj("Olivia");
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
