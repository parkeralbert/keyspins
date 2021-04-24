import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class WdetSearch extends SpinSearch{
	public void spinSearch( ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String inputPath, String outputPath, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath);
		outputSpinsByArtist(outputPath, spinsByArtist, allOutput);
	}
	
	public Map<String, ArrayList <String>> getSpins(ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String inputPath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		ArrayList<String> urls = getUrls(inputPath);
		
		ArrayList <String[]> spinData = getSpinData(artistNames, urls);
		for (String currentArtist : artistNames) {
			addSpin(spinData, currentArtist, allSpinData, firstDayOfWeek, lastDayOfWeek);
		}
		
		return allSpinData;

	}
	
	public ArrayList <String> getUrls (String inputPath){
		String line = null;
		ArrayList <String> urls = new ArrayList<>();
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputPath));
			while ((line = reader.readLine()) != null && urls.size()<2)
			{
				if (line.contains("5797") || line.contains("5182")) {
					urls.add(line.trim());
					System.out.println("url is: " + line);
				}
			}
			reader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
		}
		return urls;
	}
	
	public	ArrayList <String[]> getSpinData(ArrayList <String> artistNames, ArrayList <String> urls) throws Exception {
		WebDriver driver = new ChromeDriver();
		String line = null;

		ArrayList <String[]> spinData = new ArrayList<>();
		
		try {
			for (int h = 0; h<2; h++) {
				
				String artist = null;
				String album = null;
				String song = null;
				String show = null;
				String date = null;
				
				driver.get(urls.get(h));
				WebElement list = driver.findElement(By.tagName("body"));	
				String[] tracks = list.getText().split("\"id\":");
				for (int i = 0; i<tracks.length; i++) {
					if (i == 0) {
						System.out.println(tracks[i]);
						show = tracks[i].substring(tracks[i].indexOf("\"name\":") +8, tracks[i].indexOf("\",\"program_format"));
						//System.out.println("show is: " + show);
					}
					else {
						if (tracks[i].indexOf("\",\"collec") != -1) {
							artist = tracks[i].substring(tracks[i].indexOf("\"artistName\":") + 14, tracks[i].indexOf("\",\"collec"));
						}
						else {
							artist = tracks[i].substring(tracks[i].indexOf("\"artistName\":") + 14, tracks[i].indexOf("\",\"buy"));
						}
						//System.out.println("artist is: " + artist);
						
						song = tracks[i].substring(tracks[i].indexOf("\"trackName\":") + 13, tracks[i].indexOf("\",\"artistName"));
						//System.out.println("song is: " + song);
						
						
						if (h==0) {
							album = tracks[i].substring(tracks[i].indexOf("\"collectionName\":") + 18, tracks[i].indexOf("\",\"buy"));
							//System.out.println("album is: " + album);
						}
						else {
							if (tracks[i].indexOf("\",\"label") !=-1) {
								album = tracks[i].substring(tracks[i].indexOf("\"collectionName\":") + 18, tracks[i].indexOf("\",\"label"));
								//System.out.println("album is: " + album);
							}
							else if (tracks[i].indexOf("\"collectionName\":") != -1) {
								System.out.println("Chunk is" + tracks[i]);
								album = tracks[i].substring(tracks[i].indexOf("\"collectionName\":") + 18, tracks[i].indexOf("\",\"album_art"));
								//System.out.println("album is: " + album);
							}

						}
						
						date = tracks[i].substring(tracks[i].indexOf("\"_start_time\":") + 15, tracks[i].indexOf("\",\"track"));
						//System.out.println("date is: " + date);
						
						for (String artistName : artistNames) {
							if (artist.equalsIgnoreCase(artistName)) {
								String [] spin = {show, artist, song, album, date};
								System.out.println("WDET spin found:" + "|" + date + "|" + spin[0] + "|" + spin[1] + "|" + spin[2] + "|" + spin[3]);
								spinData.add(spin);
							}
						}
					}
				}	
				}
		}
		catch(org.openqa.selenium.NoSuchElementException e){
			e.printStackTrace();
		}
		finally {
			driver.quit();
		}

		return spinData;
	}
		
	public String getDate(String url) {
		String date = null;
		
		if (url.contains("chasing-infinity")) {
			date = url.substring(43);
		}
		
		if (url.contains("sunny-day")) {
			date = url.substring(36);
		}
		
		return date;
}
	
	
	
	
	public void addSpin(ArrayList <String[]> spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
		ArrayList <String> spins = new ArrayList<>();
		Date parsedDate = null;
		String date = null;
		String show = "-";
		String artist = "-";
		String song = "-";
		String album = "-";
		String spinToAdd;
		boolean alreadyAdded;
		
		for (String[] spin : spinData) {
			alreadyAdded = false;
			if (spin[1].equalsIgnoreCase(currentArtist)) { 
				SimpleDateFormat parser = new SimpleDateFormat("MM-dd-yyyy kk:mm:ss");
				parsedDate = parser.parse(spin[4]);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd|hh:mm");
				date = formatter.format(parsedDate);
				show = spin[0];
				artist = spin[1];
				song = spin[2];
				album = spin[3];
				if (isDateInRange(firstDayOfWeek, lastDayOfWeek, parsedDate)) {
					spinToAdd = "Key" + "|" + artist + "|" + album + "|" + song + "|" + "WDET" + "|" + "Detroit" + "|" + show + "|" + date  + "|" + "-";
					for (String addedSpin: spins) {
						if (addedSpin.equalsIgnoreCase(spinToAdd)) {
							alreadyAdded = true;
							break;
						}
					}
					if (!alreadyAdded) {
						spins.add(spinToAdd);
					}
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
