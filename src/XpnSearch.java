import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class XpnSearch extends SpinSearch {
	
	public void spinSearch(String url, ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(url, artistNames, firstDayOfWeek, lastDayOfWeek, filePath);
		outputSpinsByArtist(filePath, spinsByArtist, allOutput);
	}
	
	
	public Map<String, ArrayList <String>> getSpins(String url, ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		
	    for (String currentArtist : artistNames) {
					Elements spinData = getSpinData(currentArtist, url);
					addSpin(spinData, currentArtist, allSpinData, firstDayOfWeek, lastDayOfWeek);
	    }
		
		return allSpinData;

	}
	
	public  Elements getSpinData(String currentArtist, String url) throws Exception {
		Map<String, String> postData = new HashMap<>();
		postData.put("val", "search");
		postData.put("search", currentArtist);
		postData.put("playlist", "all");
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = null;
		currentArtist = StringEscapeUtils.escapeEcmaScript(currentArtist);
		//spinData = (page.select(String.format("td:containsOwn(%s)", currentArtist)));
		spinData = (page.select("td"));
		
		return spinData;
	}
	
	public void addSpin(Elements spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
		if(spinData != null) {
			ArrayList <String> spins = new ArrayList<>();
			String show = "-";
			String song = "-";
			String album = "-";
			String date = "-";
			String spinToAdd;
			boolean alreadyAdded;
			for (Element e : spinData) {
				alreadyAdded = false;
				String[] segments = null;
				if(e.text().split(" - ").length > 1) {
					segments = e.text().split(" - ");
					if (segments[0].contains("Your search for")) {
						String[] showAndSpin = segments[0].split(" playlists ");
						show = showAndSpin[0].substring(showAndSpin[0].indexOf("from the") + 9);
						segments[0] = showAndSpin[1];
						System.out.println("show is: " + show);
					}
					else {
					}
					//only use for testing: System.out.println("Retrieved " + artistInfo.getArtistName() + " spins: " + spinData.text() + " for " + segments[1]);
				}
				else {
					continue;
				}
				SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy kk:mm a");
				Date spinDate = formatter.parse(segments[0].substring(0, 19));
				String artistName = replaceSmartQuotes(segments[0].substring(20).trim());
				//date = date.replace('-', '/');
				
				if(isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate) && artistName.equalsIgnoreCase(currentArtist)) {
					SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd|hh:mm a");
					date = dateToString.format(spinDate);
					song = replaceSmartQuotes(segments[1]);
					album = replaceSmartQuotes(segments[2]);
					spinToAdd = "Key" + "|" + artistName + "|" + album + "|" + song + "|" + "WXPN" + "|" + "Philadelphia" + "|" + show + "|" + date + "|" + "-";
					
					for (String spin : spins) {
						if (spin.equalsIgnoreCase(spinToAdd)) {
							alreadyAdded = true;
							break;
						}
					}
					if (!alreadyAdded) {
						spins.add(spinToAdd);
						System.out.println("WXPN spins: " + date + "|" + artistName + "|" + song + "|" + album);
					}
				}
			}
				allSpinData.put(currentArtist, spins);
		}
	}
	
	public void outputSpinsByArtist(String filePath, Map<String, ArrayList <String>> spinsByArtist, String allOutput) throws Exception {
		for (String currentArtist : spinsByArtist.keySet()) {
			writeSpinsToFile(currentArtist, spinsByArtist.get(currentArtist), filePath);
		}
	}
	public void writeSpinsToFile(String currentArtist, ArrayList <String> rawSpins, String filePath) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
			if(rawSpins.size() > 0) {
				for (String rawSpin : rawSpins) {
					writer.write(rawSpin);
					writer.newLine();
				}
				writer.close();
			}
	}
}

