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
import org.jsoup.select.Elements;


	public class WmbrSearch extends SpinSearch {
		
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
		
		
		public	 Elements getSpinData(String currentArtist, String url) throws Exception {
			Map<String, String> postData = new HashMap<>();
			postData.put("artistonly", currentArtist);
			Elements spinData = new Elements();
			
		//	Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
			
			if(currentArtist.indexOf("'") != -1) {
				currentArtist = StringEscapeUtils.escapeEcmaScript(currentArtist);
			}

			
			String queryName = currentArtist.replaceAll(" ", "+");	
			queryName = queryName.replaceAll("&", "%26");
			String sortUrl = "https://track-blaster.com/wmbr/search.php?whichForm=findbands&field=artist&exact=is&key=" + queryName + "&rows=10&topfield=artists&topYear=all&action=Go%21&program=0&dj=0&alias=1";
			Document page = Jsoup.connect(sortUrl).userAgent(USER_AGENT).data(postData).post();
			Elements rawData = page.select(String.format("div:contains(%s)", currentArtist));
			
			if(rawData.size() > 1) {
				spinData = new Elements(rawData.subList(0, rawData.size()/5));
			}
			
			
			
			return spinData;
		}
		
		public void addSpin(Elements spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
			ArrayList <String> spins = new ArrayList<>();
			boolean foundSpin = false;
			SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
			Date spinDate = null;
			String date = "-";
			String spinToAdd;
			boolean alreadyAdded;
			
			if(!spinData.text().contains("Date:")) {
				return;
			}
			String [] splitSpinData = spinData.text().split("Date:");
			String singleSpin = null;
			ArrayList<String> separateSpinData = new ArrayList<String>();
			for (int i = 0; i<splitSpinData.length; i++ ) {
				if(splitSpinData[i].indexOf("What:") == -1) {
					singleSpin = splitSpinData[i].substring(0, splitSpinData[i].indexOf("DJ:"));
					separateSpinData.add(singleSpin);
				}
			}
			
			for (String spin : separateSpinData) {
				alreadyAdded = false;
				spinDate = formatter.parse(spin.substring(1, spin.indexOf(",")+6));
				SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");
				date = dateToString.format(spinDate);
				
				if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
					foundSpin = true;
					String artistName = spin.substring(spin.indexOf("Artist:"), spin.indexOf("Song:"));
					artistName = artistName.substring(8).trim();
					
					if (artistName == "") {
						artistName = "-";
					}
					
					String song = replaceSmartQuotes(spin.substring(spin.indexOf("Song:"), spin.indexOf("Album:")));	
					song = song.substring(6).trim();
					if (song == "") {
						song = "-";
					}
					
					String album = replaceSmartQuotes(spin.substring(spin.indexOf("Album:"), spin.indexOf("Label:")));
					album = album.substring(7).trim();
					if (album == "") {
						album = "-";
					}
					String show = spin.substring(spin.indexOf("Show:") + 6).trim();
		
					spinToAdd = "Key" + "|" + artistName + "|" + album + "|" + song + "|" + "WMBR" + "|" + "Cambridge" + "|" + show + "|" + date + "|" + "-" + "|" + "-";
					spins.add(spinToAdd);
					System.out.println("spin is: " + spinToAdd);
				}
				if (!isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate) && foundSpin) {
					break;
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
