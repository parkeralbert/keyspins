import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class WruwSearch extends SpinSearch{
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
				if (line.contains("sunny-day") || line.contains("chasing-infinity")) {
					urls.add(line.trim());
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
		String artist = "-";
		String song = "-";
		String album = "-";
		String host = "-";

		ArrayList <String[]> spinData = new ArrayList<>();
		
		try {
			for (String url : urls) {
				
				String date = getDate(url);
				
				if (url.contains("sunny-day")) {
					host = "Annie";
				}
				
				if (url.contains("chasing-infinity")) {
					host = "Ed";
				}
				driver.get(url);
				WebDriverWait wait = new WebDriverWait(driver, 1000);
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id("show-playlist")));
				WebElement list = driver.findElement(By.id("show-playlist"));	
				List <WebElement> playlist = list.findElements(By.xpath("./child::*"));
				
				
				for (WebElement spin : playlist) {
					if (spin == playlist.get(0)) {
						continue;
					}
					WebElement artistData = spin.findElements(By.xpath("./child::*")).get(1);
					WebElement albumData = spin.findElements(By.xpath("./child::*")).get(2);
							
					artist = artistData.findElements(By.xpath("./child::*")).get(0).getText();
					song = artistData.findElements(By.xpath("./child::*")).get(1).getText();
					album = albumData.findElements(By.xpath("./child::*")).get(0).getText();
					
					for (String artistName : artistNames) {
						if (artist.equalsIgnoreCase(artistName)) {
							
							String [] spinInfo = {artist, song, album, date, host};
							System.out.println("WRUW spin: " + spinInfo[0] + "|" + spinInfo[1] + "|" + spinInfo[2] + "|" + spinInfo[3] + "|" + spinInfo[4]);
							spinData.add(spinInfo);
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
		String artist = "-";
		String song = "-";
		String album = "-";
		String date = "-";
		String host = "-";
		String spinToAdd;
		boolean alreadyAdded;
		
		for (String[] spin : spinData) {
			if (spin[0].equalsIgnoreCase(currentArtist)) { 
				alreadyAdded = false;
				artist = spin[0];
				song = spin[1];
				album = spin[2];
				date = spin[3];
				host = spin[4];
				spinToAdd = "Key" + "|" + artist + "|" + album + "|" + song + "|" + "WRUW" + "|" + "Cleveland" + "|" + host + "|" + date + "|" + "-" + "|" + "-";
				spins.add(spinToAdd);
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
