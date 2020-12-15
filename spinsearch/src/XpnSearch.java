import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class XpnSearch extends SpinSearch {
	
	public Map<String, List<Spin>> getSpins(String url, Map <String, ArtistInfo> artistInfos, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, Spin> allSpins = new HashMap<>();
		
	    for (String artistToPull : artistInfos.keySet()) {
	    	ArtistInfo currentArtist = artistInfos.get(artistToPull);
				for (String song : currentArtist.getSongs()) {
					Elements spinData = getSpinData(currentArtist, url, song);
					addSpin(spinData, currentArtist, firstDayOfWeek, lastDayOfWeek, allSpins, song);
				}
	        
	    }
		
		Map<String, List<Spin>> spinsByArtist = getSpinsByArtist(allSpins.values());
		
		return spinsByArtist;

	}
	
	public  Elements getSpinData(ArtistInfo currentArtist, String url, String songName) throws Exception {
		Map<String, String> postData = new HashMap<>();
		String artist = (String) currentArtist.getArtistName();
		postData.put("val", "search");
		postData.put("search", artist);
		postData.put("playlist", "all");
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = null;
		songName = StringEscapeUtils.escapeEcmaScript(songName);
		spinData = (page.select(String.format("td:containsOwn(%s)", songName)));
		
		return spinData;
	}
	
	public void addSpin(Elements spinData, ArtistInfo artistInfo, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, Spin> allSpins, String songToMatch) throws Exception   {
		if(spinData != null) {
			for (Element e : spinData) {
				boolean correctSong;
				boolean correctArtist;
				String[] segments = null;
				if(e.text().split(" - ").length > 1) {
					segments = e.text().split(" - ");
					//only use for testing: System.out.println("Retrieved " + artistInfo.getArtistName() + " spins: " + spinData.text() + " for " + segments[1]);
				}
				else {
					return;
				}
				
				SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
				Date spinDate = formatter.parse(segments[0].substring(0, 19));
				String song = segments[1];
				String artistName = segments[0].substring(20).trim();
				
				correctSong = song.equalsIgnoreCase(songToMatch);
				correctArtist = artistName.equalsIgnoreCase(artistInfo.getArtistName());
				
		if(isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
					
			if (correctSong && correctArtist)	{
			
				if (artistName.equalsIgnoreCase(artistInfo.getArtistName())){
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

					System.out.println("*** SPIN TO WRITE: " + e.text());
					allSpins.put(key, spin);
				}
			}
		}

			}
		}

	}
	
	public void outputSpinsByArtist(String filePath, Map<String, List<Spin>> spinsByArtist) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
		writer.write("WXPN");
		writer.newLine();
		writer.close();
		
		for (List<Spin> spinsToPrint : spinsByArtist.values()) {
			writeSpinsToFile(spinsToPrint, filePath);
		}
	}
}

