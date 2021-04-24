import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BoxSearch extends SpinSearch{
	public void spinSearch(ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String inputPath, String outputPath, String stationInput, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, stationInput);
		outputSpinsByArtist(outputPath, spinsByArtist, allOutput);
	}
	
	public Map<String, ArrayList <String>> getSpins(ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String inputPath, String stationInput) throws Exception {
		int days = getDays(firstDayOfWeek, lastDayOfWeek);
		Map<String, ArrayList <String>> urls = getUrls(stationInput, days);
		Map<String, ArrayList <String>> allSpins = new HashMap<String, ArrayList <String>>();
		
		Map<String, ArrayList <String[]>> allSpinData = getSpinData(artistNames, urls);
		for (String currentArtist : artistNames) {
			addSpin(allSpinData, currentArtist, firstDayOfWeek, lastDayOfWeek, allSpins);
		}
		
		return allSpins;

	}
	
	public	Map<String, ArrayList <String[]>> getSpinData(ArrayList <String> artistNames, Map<String, ArrayList <String>> allUrls) throws Exception {
		WebDriver driver = new ChromeDriver();

		Map<String, ArrayList <String[]>> allSpinData = new HashMap<>();
		
		try {
			for (String station : allUrls.keySet()) {
				ArrayList <String[]> spinData = new ArrayList<>();
				for (String url : allUrls.get(station)) {
					driver.get(url);
					WebDriverWait wait = new WebDriverWait(driver, 1000);
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@class = 'ajax']")));
					List <WebElement> spins= driver.findElements(By.tagName("tr"));
					WebElement dateTab= driver.findElement(By.xpath("//li[@class = 'active']//child::span[1]"));
					
					String date = dateTab.getText();
					date = date.replaceAll("[\\n]", "");
					date = date.substring(3);
					
					for (WebElement spin : spins) {
						List <WebElement> spinStuff= spin.findElements(By.xpath("./child::*"));
						List <WebElement> timeInfo= spinStuff.get(0).findElements(By.xpath("./child::*"));
						WebElement spinTime = timeInfo.get(0);
						if (spinStuff.size()<2) {
							continue;
						}
						
						List <WebElement> trackInfo = spinStuff.get(1).findElements(By.xpath("./child::*"));
						
						WebElement track = null;
						if (trackInfo.size() > 0) {
							track = trackInfo.get(0);
						}
						else {
							track = spinStuff.get(1);
						}

						String time = spinTime.getText();
						String [] spinBits = track.getText().split(" - ");
						if (spinBits.length == 1 && spinBits[0].contains("-")) {
							spinBits = track.getText().split("-");
						}
						for (String artistName : artistNames) {
							if (spinBits.length == 1) {
								if (spinBits[0].equalsIgnoreCase(artistName)) {
									String[] spinInfo = {date, station, spinBits[0], time};
									System.out.println("RadioBox spin for " + station + ":" + date + "|" + spinBits[0] + "|" + time);
									spinData.add(spinInfo);
								}
							}
							if (spinBits.length == 2) {
								if (spinBits[0].equalsIgnoreCase(artistName)) {
									String[] spinInfo = {date, station, spinBits[0], spinBits[1], time};
									System.out.println("RadioBox spin for " + station + ":" + date + "|" + spinBits[0] + "|" + spinBits[1]+ "|" + time);
									spinData.add(spinInfo);
								}
								if (spinBits[1].equalsIgnoreCase(artistName)) {
									String[] spinInfo = {date, station, spinBits[1], spinBits[0], time};
									System.out.println("RadioBox spin for " + station + ":" + date + "|" + spinBits[0] + "|" + spinBits[1]+ "|" + time);
									spinData.add(spinInfo);
								}
							}
							
							if (spinBits.length == 3) {
								if (spinBits[1].equalsIgnoreCase(artistName)) {
									String[] spinInfo = {date, station, spinBits[1], spinBits[0], spinBits[2], time};
									System.out.println("RadioBox spin for " + station + ":" + date + "|" + spinBits[0] + "|" + spinBits[1] + "|" + spinBits[2] + "|" + time);
									spinData.add(spinInfo);
									continue;
								}
								if (spinBits[0].equalsIgnoreCase(artistName)) {
									String[] spinInfo = {date, station, spinBits[0], spinBits[1], spinBits[2], time};
									System.out.println("RadioBox spin for " + station + ":" + date + "|" + spinBits[0] + "|" + spinBits[1] + "|" + spinBits[2] + "|" + time);
									spinData.add(spinInfo);
									continue;
								}
								if (spinBits[2].equalsIgnoreCase(artistName)) {
									String[] spinInfo = {date, station, spinBits[2], spinBits[0], spinBits[1], time};
									System.out.println("RadioBox spin for " + station + ":" + date + "|" + spinBits[0] + "|" + spinBits[1] + "|" + spinBits[2] + "|" + time);
									spinData.add(spinInfo);
									continue;
								}
							}
						}
					}
				}
				allSpinData.put(station, spinData);
			}
		}
		catch(org.openqa.selenium.NoSuchElementException e){
			e.printStackTrace();
		}
		finally {
			driver.quit();
		}

		return allSpinData;
	}
		
	public Map <String, ArrayList <String>> getUrls (String inputPath, int days){
		String line = null;
		Map <String, ArrayList <String>> allUrls = new HashMap<>();
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(inputPath));
			while ((line = reader.readLine()) != null)
			{
				ArrayList <String> urls = createUrls(line, days);
				allUrls.put(line, urls);
			}
			reader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
		}
		return allUrls;
	}
	
	public ArrayList<String> createUrls(String line, int days){
		ArrayList <String> urls = new ArrayList<>();
		for (int i = 0; i<days; i++) {
			String dayNum = Integer.toString(i);  
			if (i==0) {
				dayNum = "";
			}
			String url = "https://onlineradiobox.com/us/" + line + "/playlist/" + dayNum + "?cs=us." + line + "&useStationLocation=1";
			System.out.println("Url is:" + url); 
			urls.add(url);
		}
		return urls;
	}
	
	public int getDays(Date firstDayOfWeek, Date lastDayOfWeek){
	    Date date = new Date();  
		double difference_In_Time = date.getTime() - firstDayOfWeek.getTime();
		double difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24));
		
		int days = (int) Math.round(Math.ceil(difference_In_Days));
		return days;
	}
	  
	public void addSpin(Map<String, ArrayList <String[]>> allSpinData, String currentArtist, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, ArrayList <String>> allSpins) throws Exception   {
		
		ArrayList <String> spins = new ArrayList<>();
		Date parsedTime = null;
		String date ="-";
		String song = "-";
		String artist = "-";
		String album = "-";
		String time = "-";
		String spinToAdd;
		boolean alreadyAdded;
		
		for (String station : allSpinData.keySet()) {
			for (String[] spin : allSpinData.get(station)) {
				alreadyAdded = false;
				if (spin[2].equalsIgnoreCase(currentArtist)) { 
					SimpleDateFormat parser = new SimpleDateFormat("dd.MM hh:mm a yyyy");
					SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
					SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");
					
					SimpleDateFormat timeParser = new SimpleDateFormat("kk:mm");
					SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
					
					
					if (spin.length == 4) {
						artist = spin[2];
						if (!time.equalsIgnoreCase("live")) {
							parsedTime = timeParser.parse(spin[3]);
							time = timeFormatter.format(parsedTime);
						}
						String dateAndTime = spin[0] + " " + time + " " + yearFormatter.format(firstDayOfWeek);
						Date dateTime = parser.parse(dateAndTime);
						date = formatter.format(dateTime);
						if (isDateInRange(firstDayOfWeek, lastDayOfWeek, dateTime)) {
							spinToAdd = "RadioBox" + "|" + artist +  "|" + "-" + "|" + "-" + station + "|" + "-" + "|" + "-" + "|" + date + "|" + time + "|" +  "-";
							for (String addedSpin: spins) {
								if (addedSpin.equalsIgnoreCase(spinToAdd)) {
									alreadyAdded = true;
								}
							}
							if (!alreadyAdded) {
								spins.add(spinToAdd);
							}
						}
					}
					else if (spin.length == 5){
						artist = spin[2];
						song = spin[3];
						time = spin[4];
						if (!time.equalsIgnoreCase("live")) {
							parsedTime = timeParser.parse(time);
							time = timeFormatter.format(parsedTime);
						}
						String dateAndTime = spin[0] + " " + time + " " + yearFormatter.format(firstDayOfWeek);
						Date dateTime = parser.parse(dateAndTime);
						date = formatter.format(dateTime);
						if (isDateInRange(firstDayOfWeek, lastDayOfWeek, dateTime)) {
							spinToAdd = "RadioBox" + "|" + artist + "|" + "-" + "|" + song + "|" + station + "|" + "-" + "|" + "-" + "|" + date + "|" + time + "|" + "-";
							for (String addedSpin: spins) {
								if (addedSpin.equalsIgnoreCase(spinToAdd)) {
									alreadyAdded = true;
								}
							}
							if (!alreadyAdded) {
								spins.add(spinToAdd);
							}
						}
					}
					else {
						artist = spin[2];
						album = spin[4];
						song = spin[3];
						time = spin[5];
						if (!time.equalsIgnoreCase("live")) {
							parsedTime = timeParser.parse(spin[5]);
							time = timeFormatter.format(parsedTime);
						}
						String dateAndTime = spin[0] + " " + time + " " + yearFormatter.format(firstDayOfWeek);
						Date dateTime = parser.parse(dateAndTime);
						parsedTime = timeParser.parse(time);
						date = formatter.format(dateTime);
						if (isDateInRange(firstDayOfWeek, lastDayOfWeek, dateTime)) {
							spinToAdd = "RadioBox" + "|" + artist + "|" + album + "|" + song + "|" + station + "|" + "-" + "|" + "-" + "|" + date + "|" + time + "|" + "-";
							for (String addedSpin: spins) {
								if (addedSpin.equalsIgnoreCase(spinToAdd)) {
									alreadyAdded = true;
								}
							}
							if (!alreadyAdded) {
								spins.add(spinToAdd);
							}
						}
					}
				}

			}
		}
		allSpins.put(currentArtist, spins);
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