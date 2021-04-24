import java.io.BufferedWriter;
import java.io.FileWriter;
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
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KcrwSearch extends SpinSearch {
	public void spinSearch( ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(artistNames, firstDayOfWeek, lastDayOfWeek, filePath);
		outputSpinsByArtist(filePath, spinsByArtist, allOutput);
	}
	
	public Map<String, ArrayList <String>> getSpins(ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		ArrayList <String> urls = formatUrls(firstDayOfWeek, lastDayOfWeek, artistNames);
		
		ArrayList <String[]> spinData = getSpinData(artistNames, urls);
		for (String currentArtist : artistNames) {
			addSpin(spinData, currentArtist, allSpinData, firstDayOfWeek, lastDayOfWeek);
		}
		
		return allSpinData;

	}
	
	public ArrayList <String> formatUrls(Date firstDayOfWeek, Date lastDayOfWeek, ArrayList <String> artistNames) throws Exception{
		ArrayList <String> urls = new ArrayList<>();
		SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = dateToString.format(firstDayOfWeek); 
		String endDate = dateToString.format(lastDayOfWeek);
		String url = "https://www.kcrw.com/playlists/playlists?channel=Simulcast&host=&show=&hour=&advanced=on&album_song_artist=input&label=&from_date=input&to_date=";
		String newUrl = null;
		String[] segments = url.split("input");
		for (String artist : artistNames) {
			artist = artist.replaceAll(" ", "%20");
			newUrl = segments[0] + artist + segments[1] + startDate + segments[2] + endDate;
			urls.add(newUrl);
		}
		return urls;
	}
	
	
	public	ArrayList <String[]> getSpinData(ArrayList <String> artistNames, ArrayList <String> urls) throws Exception {
		WebDriver driver = new ChromeDriver();
		String artist = null;
		String song = null;
		String show = null;
		String date = null;
		String time = null;
		
		ArrayList <String[]> spinData = new ArrayList<>();
		try {
		for (String url : urls) {
			WebDriverWait wait = new WebDriverWait(driver, 1000);
			driver.get(url);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[@class = 'song-title'] | //p[contains(text(), 'Tracklist information is not available.')]")));
			List <WebElement> message = driver.findElements(By.xpath("//p[contains(text(), 'We have run out of songs to show you. Please select a different Show or date above.')]"));
			List <WebElement> tds = driver.findElements(new ByChained(By.id("playlist-entries"),By.xpath("//div[@class= 'show-head'] | //div[@class='track clearfix']")));
			if (tds.size() == 0) {
				continue;
			}
			WebElement spinsShowing = driver.findElement(By.id("playlist-count"));
			String[] countSegs = spinsShowing.getText().split(" of ");
			int showing =Integer.parseInt(countSegs[0].substring(8));
			int total =Integer.parseInt(countSegs[1].substring(0, countSegs[1].indexOf("matching")-1));
			
			if (tds.size() >= 20 && showing < total) {
				while (message.size() == 0 && showing <= total) {
					if (!(showing >= total)) {
						try {
							driver.findElement((By.xpath("//a[contains(text(), 'Load More')]")));
						}
						catch(org.openqa.selenium.NoSuchElementException e){
							System.out.println("No load button for url: " + url);
							break;
						}
						wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Load More')]")));
						driver.findElement(By.xpath("//a[contains(text(), 'Load More')]")).click();
					}
					
					message = driver.findElements(By.xpath("//p[contains(text(), 'We have run out of songs to show you. Please select a different Show or date above.')]"));
					tds = driver.findElements(new ByChained(By.id("playlist-entries"), By.xpath("//div[@class= 'show-head'] | //div[@class='track clearfix']")));
					spinsShowing = driver.findElement(By.id("playlist-count"));
					countSegs = spinsShowing.getText().split(" of ");
					showing =Integer.parseInt(countSegs[0].substring(8));
					total =Integer.parseInt(countSegs[1].substring(0, countSegs[1].indexOf("matching")-1));
				}	
			}
			
			for (WebElement td : tds) {
				String className =  td.getAttribute("class");
				
				if (className.equalsIgnoreCase("show-head")){
					WebElement showElement = td.findElement(By.xpath(".//a[@class='show-name']"));
					show = showElement.getText(); 
					WebElement dateElement = td.findElement(By.xpath(".//div[@class='show-date-time']"));
					String[] segments = dateElement.getText().split(" • ");
					date = segments[0]; 
				}
				
				if (className.equalsIgnoreCase("track clearfix")){
					WebElement timeElement = td.findElement(By.xpath(".//div[@class='timestamp']"));
					time = timeElement.getText();
					WebElement songElement = td.findElement(By.xpath(".//span[@class='song-title']"));
					song = songElement.getText();
					WebElement artistElement = td.findElement(By.xpath(".//span[@class='artist-name']"));
					artist = artistElement.getText();
					String [] spin = {artist, song, date, time, show};
					System.out.println("KCRW spin: " + "|" + spin[0] + "|" + spin[1] + "|" + spin[2] + "|" + spin[3] + "|" + spin[4]);
					spinData.add(spin);
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
	
	public void addSpin(ArrayList <String[]> spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
		ArrayList <String> spins = new ArrayList<>();
		String date = "";
		Date spinDate = null;
		String artist = "-";
		String song = "-";
		String show = "-";
		String timeAndDate = "-";
		String spinToAdd;
		boolean alreadyAdded;
		SimpleDateFormat formatter = new SimpleDateFormat("EEEEE, MMMMM d, yyyy hh:mm a");
		for (String[] spin : spinData) {
			alreadyAdded = false;
			if (spin[0].equalsIgnoreCase(currentArtist)) {
				timeAndDate = spin[2] + " " + spin[3];
				spinDate = formatter.parse(timeAndDate);
				SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd|hh:mm a");
				date = dateToString.format(spinDate);
				artist = spin[0];
				song = spin[1];
				show = spin[4];
				if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
					spinToAdd = "Key" + "|" + artist + "|" + "-" + "|" + song + "|" + "KCRW" + "|" + "Los Angeles" + "|" + show + "|" + date + "|" + "-";
					for (int i = 0; i<spins.size(); i++) {
						if (spins.get(i).equalsIgnoreCase(spinToAdd)) {
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
