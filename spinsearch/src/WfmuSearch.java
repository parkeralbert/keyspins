import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.text.StringEscapeUtils;

public class WfmuSearch extends SpinSearch {
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
	
	
	public	 Elements getSpinData(ArtistInfo currentArtist, String url, String songName) throws Exception {
		Map<String, String> postData = new HashMap<>();
		String artist = (String) currentArtist.getArtistName();
		postData.put("artistonly", artist);
		
	//	Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = new Elements();
		if(songName.indexOf("'") != -1) {
			songName = StringEscapeUtils.escapeEcmaScript(songName);
		}

		System.out.println("New song name: " + songName);
		
		String queryName = currentArtist.getArtistName().replaceAll(" ", "+");	
		queryName = queryName.replaceAll("&", "%26");
		String sortUrl = "https://wfmu.org/search.php?action=searchbasic&sinputs=%5B%22or%22%2C%7B%22Artist%22%3A%22starts%22%2C%22Song+title%22%3A%22starts%22%2C%22Album+title%22%3A%22starts%22%2C%22Comments%22%3A%22starts%22%7D%2Cnull%2C%7B%22Artist%22%3A%22" + queryName + "%22%7D%2Cnull%2C347%2C5%2C%22" + queryName + "%22%2Cnull%2Cnull%5D&page=0&sort=Playlist%20links";
		Document page = Jsoup.connect(sortUrl).userAgent(USER_AGENT).data(postData).post();
		Elements rawData = page.select(String.format("tr:contains(%s)", songName));
		
		if(rawData.size() > 1) {
			spinData = new Elements(rawData.subList(1, rawData.size()));
		}
		
		return spinData;
	}
	
	public void addSpin(Elements spinData, ArtistInfo artistInfo, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, Spin> allSpins, String songToMatch) throws Exception   {
		if (spinData.hasText()) {
			System.out.println("Retrieved " + artistInfo.getArtistName() + " spins: " + spinData.text());
		}
		for (Element e : spinData) {
			boolean correctArtist = false;
			boolean correctSong = false;
			Elements singleSpinData = e.children();
			
			correctArtist = singleSpinData.get(0).text().equalsIgnoreCase(artistInfo.getArtistName());
			correctSong = singleSpinData.get(1).text().equalsIgnoreCase(songToMatch);
			
			
		if (correctArtist && correctSong)	{
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
			Date spinDate = null;
			if(singleSpinData.size() == 4) {
				spinDate = formatter.parse(singleSpinData.get(3).text());
			}
			if (singleSpinData.size() == 5) {
				spinDate = formatter.parse(singleSpinData.get(4).text());
			}
			
			String artistName = replaceSmartQuotes(singleSpinData.get(0).text());
			
			if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate) && artistName.equalsIgnoreCase(artistInfo.getArtistName())){
				String song = singleSpinData.get(1).text().trim();
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

				System.out.println("*** SPIN TO WRITE: " + e.text());
				allSpins.put(key, spin);
			}
		}

		}
	}
	
	private static String replaceSmartQuotes(String input) {
		StringBuilder output = new StringBuilder();
		
		for (char c : input.toCharArray()) {
			if (c == 0x2018 || c == 0x2019) {
				output.append('\'');
			}
			else {
				output.append(c);
			}
		}
		return output.toString();
	}
	
	public void outputSpinsByArtist(String filePath, Map<String, List<Spin>> spinsByArtist) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write("WFMU");
		writer.newLine();
		writer.close();
		
		for (List<Spin> spinsToPrint : spinsByArtist.values()) {
			writeSpinsToFile(spinsToPrint, filePath);
		}
	}
}
