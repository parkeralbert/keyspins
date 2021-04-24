import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.apache.commons.text.StringEscapeUtils;

public class WfmuSearch extends SpinSearch {
	
	public void spinSearch(String url, ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(url, artistNames, firstDayOfWeek, lastDayOfWeek, filePath);
		outputSpinsByArtist(filePath, spinsByArtist, allOutput);
	}
	
	public Map<String, ArrayList <String>> getSpins(String url, ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		Map<String, ArrayList <String>> allRawSpinData = new HashMap<>();
		
	    for (String currentArtist : artistNames) {
					Elements spinData = getSpinData(currentArtist, url);
					addSpin(spinData, currentArtist, allSpinData, allRawSpinData, firstDayOfWeek, lastDayOfWeek);
	    }
		
		return allSpinData;

	}
	
	
	public	 Elements getSpinData(String currentArtist, String url) throws Exception {
		Map<String, String> postData = new HashMap<>();
		postData.put("artistonly", currentArtist);
		
	//	Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = new Elements();
		if(currentArtist.indexOf("'") != -1) {
			currentArtist = StringEscapeUtils.escapeEcmaScript(currentArtist);
		}

		
		String queryName = currentArtist.replaceAll(" ", "+");	
		queryName = queryName.replaceAll("&", "%26");
		String sortUrl = "https://wfmu.org/search.php?action=searchbasic&sinputs=%5B%22or%22%2C%7B%22Artist%22%3A%22starts%22%2C%22Song+title%22%3A%22starts%22%2C%22Album+title%22%3A%22starts%22%2C%22Comments%22%3A%22starts%22%7D%2Cnull%2C%7B%22Artist%22%3A%22" + queryName + "%22%7D%2Cnull%2C347%2C5%2C%22" + queryName + "%22%2Cnull%2Cnull%5D&page=0&sort=Playlist%20links";
		Document page = Jsoup.connect(sortUrl).userAgent(USER_AGENT).data(postData).post();
		Elements rawData = page.select(String.format("tr:contains(%s)", currentArtist));
		
		if(rawData.size() > 1) {
			spinData = new Elements(rawData.subList(1, rawData.size()));
		}
		
		return spinData;
	}
	
	public void addSpin(Elements spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Map<String, ArrayList <String>> allRawSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
		ArrayList <String> spins = new ArrayList<>();
		Date spinDate = null;
		String date = "-";
		String show = "-";
		String song = "-";
		String spinToAdd;
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy");
		
		for (Element e : spinData) {
			Elements singleSpinData = e.children();
			
			if(singleSpinData.size() == 4) {
				spinDate = formatter.parse(singleSpinData.get(3).text());
				SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");
				date = dateToString.format(spinDate);
				show = (singleSpinData.get(2).text());
			}
			if (singleSpinData.size() == 5) {
				spinDate = formatter.parse(singleSpinData.get(4).text());
				SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");
				date = dateToString.format(spinDate);
				show = (singleSpinData.get(3).text());
			}
			
			if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
				String artistName = replaceSmartQuotes(singleSpinData.get(0).text());
				
				song = replaceSmartQuotes(singleSpinData.get(1).text().trim());
				String album = "-";
				
				if (artistName.equalsIgnoreCase(currentArtist))	{
					if (singleSpinData.size() == 5) {
						if (!replaceSmartQuotes(singleSpinData.get(2).text().trim()).equals("")) {
							album = replaceSmartQuotes(singleSpinData.get(2).text().trim());
						}
					}
					spinToAdd = "Key" + "|" + artistName + "|" + album + "|" + song + "|" + "WFMU" + "|" + "Jersey City" + "|" + show + "|" + date + "|" + "-" + "|" + "-";
					spins.add(spinToAdd);
					System.out.println("WFMU spin:" + artistName + "|" + album + "|" + song + "|" + show + "|" + date);
				}
			}

		}
		allSpinData.put(currentArtist, spins);
	}
	
	public static String replaceSmartQuotes(String input) {
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
	
	public void outputSpinsByArtist(String filePath, Map<String, ArrayList <String>> spinsByArtist, String allOutput) throws Exception {
		for (String currentArtist : spinsByArtist.keySet()) {
			writeSpinsToFile(currentArtist, spinsByArtist.get(currentArtist), filePath);
			writeSpinsToFile(currentArtist, spinsByArtist.get(currentArtist), allOutput);
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
