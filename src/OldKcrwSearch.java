import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class OldKcrwSearch extends SpinSearch{
	public void spinSearch( ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String inputPath, String outputPath, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath);
		outputSpinsByArtist(outputPath, spinsByArtist, allOutput);
	}
	
	public Map<String, ArrayList <String>> getSpins(ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String inputPath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		ArrayList<String> urls = createUrls(artistNames, inputPath);
		
		ArrayList <String[]> spinData = getSpinData(artistNames, urls);
		for (String currentArtist : artistNames) {
			addSpin(spinData, currentArtist, allSpinData, firstDayOfWeek, lastDayOfWeek);
		}
		
		return allSpinData;

	}
	
	
	public ArrayList <String> createUrls (ArrayList<String> artistNames, String inputPath){
		String line = null;
		String urlArtist = null;
		ArrayList <String> urls = new ArrayList<>();
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputPath));
			while ((line = reader.readLine()) != null)
			{
				if (line.contains("kcrw.com")) {
					String[] segments = line.split("artist");
					for (String artistName : artistNames) {
						urlArtist = artistName.replaceAll(" ", "%20" );
						urls.add(segments[0] + urlArtist + segments[1]);		
					}
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
		ArrayList<String> artists = new ArrayList<>();
		ArrayList<String> albums = new ArrayList<>();
		ArrayList<String> songs = new ArrayList<>();
		ArrayList<String> hosts = new ArrayList<>();
		ArrayList<String> dates = new ArrayList<>();
		ArrayList<String> times = new ArrayList<>();
		String line = null;

		ArrayList <String[]> spinData = new ArrayList<>();
		
		try {
			for (String url : urls) {
				
				String artistInput = null;
				String album = null;
				String song = null;
				String host = null;
				String date = null;
				String time = null;
				
				driver.get(url);
				WebElement list = driver.findElement(By.tagName("body"));	
				BufferedWriter writer = new BufferedWriter(new FileWriter("kcrw_json.txt"));
				writer.write(list.getText());
				writer.close();
				
				try
				{
					BufferedReader artistReader = new BufferedReader(new FileReader("kcrw_json.txt"));
					while ((line = artistReader.readLine()) != null)
					{
						if (line.contains("artist\":")) {
							artistInput = line.substring((line.indexOf("t\": ")+ 4), line.indexOf("\","));
							artistInput = artistInput.replaceAll("\"", "");
							//System.out.println("artist is:" + artistInput);
							artists.add(artistInput);
						}
						
						if (line.contains("album\":")) {
							album = line.substring((line.indexOf("m\": ") + 4), line.indexOf("\","));
							album = album.replaceAll("\"", "");
							//System.out.println("album is:" + album);
							albums.add(album);
						}
						
						if (line.contains("\"title\":")) {
							song = line.substring((line.indexOf("e\": ") + 4), line.indexOf("\","));
							song = song.replaceAll("\"", "");
							//System.out.println("song is:" + song);
							songs.add(song);
						}
						
						if (line.contains("host\":")) {
							host = line.substring((line.indexOf("t\": ") + 4), line.indexOf("\","));
							host = host.replaceAll("\"", "");
							//System.out.println("host:" + host);
							hosts.add(host);
						}
						
						if (line.contains("date\":")) {
							date = line.substring((line.indexOf("e\": ") + 4), line.indexOf("\","));
							date = date.replaceAll("\"", "");
							//System.out.println("date:" + date);
							dates.add(date);
						}
						
						if (line.contains("\"time\":")) {
							time = line.substring((line.indexOf("e\": ") + 4), line.indexOf("\","));
							time = time.replaceAll("\"", "");
							//System.out.println("date:" + date);
							times.add(time);
						}
						
					}
					artistReader.close();
					
				}
				catch (Exception e)
				{
					System.err.println("Error: " + e);
					e.printStackTrace();
				}
					
				}
			for (int i = 0; i<artists.size(); i++) {
				for (String artistName : artistNames) {
					if (artists.get(i).equalsIgnoreCase(artistName)) {
						String [] spin = {artists.get(i), songs.get(i), albums.get(i), hosts.get(i), dates.get(i), times.get(i)};
						spinData.add(spin);
						System.out.println("KCRW spin found: " + artists.get(i) + "|" +  albums.get(i) + "|" + songs.get(i) + "|" + hosts.get(i) + "|"  + dates.get(i) + "|" + times.get(i));
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
		
	public String changeTimeZone(String date, String time) {
		String dateTime = date + " " + time + " PDT";
		System.out.println("time was " + dateTime);
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd h:mm a z");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd|h:mm a");
		try {
			Date addedZone = parser.parse(dateTime);
			dateTime = formatter.format(addedZone);
			System.out.println("time is " + dateTime);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateTime;
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
		String artist = "-";
		String song = "-";
		String album = "-";
		String host = "-";
		String date = "-";
		String time = "-";
		
		for (String[] spin : spinData) {
			if (spin[0].equalsIgnoreCase(currentArtist)) { 
				artist = spin[0];
				song = spin[1];
				album = spin[2];
				host = spin[3];
				date = spin[4];
				time = spin[5];
				String dateAndTime = date + " " + time;
				Date spinDate = new SimpleDateFormat("yyyy-MM-dd hh:mm a").parse(dateAndTime);
				if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
					spins.add("Key" + "|" + artist + "|" + album + "|" + song + "|" + "KCRW" + "|" + "Los Angeles" + "|" + host + "|" + date + "|" + time + "|" + "-");
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
