import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KexpSearch extends SpinSearch{
	
	public void spinSearch( ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath, String allOutput) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(artistNames, firstDayOfWeek, lastDayOfWeek, filePath);
		outputSpinsByArtist(filePath, spinsByArtist, allOutput);
	}
	
	public Map<String, ArrayList <String>> getSpins(ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		ArrayList <String> urls = formatUrls(firstDayOfWeek, lastDayOfWeek);
		
		ArrayList <String[]> spinData = getSpinData(artistNames, urls);
		for (String currentArtist : artistNames) {
			addSpin(spinData, currentArtist, allSpinData, firstDayOfWeek, lastDayOfWeek);
		}
		
		return allSpinData;

	}
	
	public ArrayList <String> formatUrls(Date firstDayOfWeek, Date lastDayOfWeek) throws Exception{
		ArrayList <String> dateList = new ArrayList<>();
		SimpleDateFormat dateToString = new SimpleDateFormat("yyyy/MM/dd");
		String date = null;	
		Calendar c = Calendar.getInstance();
		c.setTime(firstDayOfWeek);
		Date currentDate = c.getTime();
		int hourCount = 0;
		
		while (isDateInRange(firstDayOfWeek, lastDayOfWeek, currentDate)) {
			date = dateToString.format(c.getTime()); 
			String hour=String.valueOf(hourCount); 
			if (hourCount <10 ) {
				dateList.add(date + "/" + "0" + hour );
			}
			else {
				dateList.add(date + "/" + hour);
			}
			
			if(hourCount == 23) {
				hourCount = 0;
				c.add(Calendar.DATE, 1);  
			}
			else {
				hourCount++;
			}
			currentDate = c.getTime();
		}
		return dateList;
	}
	
	
	public	ArrayList <String[]> getSpinData(ArrayList <String> artistNames, ArrayList <String> urls) throws Exception {
		WebDriver driver = new ChromeDriver();
		String artist = null;
		String song = null;
		String dj = null;
		String date = null;

		ArrayList <String[]> spinData = new ArrayList<>();
		try {
		for (String url : urls) {
		try {
			String completeUrl = "http://www.kexplorer.com/playlist/" + url;
			driver.get(completeUrl);
			WebDriverWait wait = new WebDriverWait(driver, 1000);
			Thread.sleep(2000);
			driver.findElement(By.id("plist"));	
		}
		
		catch(org.openqa.selenium.NoSuchElementException e){
			System.out.println("Missed data at url: " + url);
			continue;		
		}


				WebElement table = driver.findElement(By.id("plist"));	
				WebElement tbody = table.findElements(By.xpath("./child::*")).get(0);
				List <WebElement> trs = tbody.findElements(By.xpath("./child::*"));
				
				for (WebElement tr : trs) {
					if (tr == trs.get(0)) {
						continue;
					}
					WebElement artistTd = tr.findElements(By.xpath("./child::*")).get(1);
					WebElement spinDiv = artistTd.findElements(By.xpath("./child::*")).get(0);
					WebElement detailsDiv = spinDiv.findElements(By.xpath("./child::*")).get(1);
					WebElement artistHref = detailsDiv.findElements(By.xpath("./child::*")).get(2);
					
					for (String artistName : artistNames) {
						if (artistHref.getText().equalsIgnoreCase(artistName)) {
							WebElement dateAndDj = tr.findElements(By.xpath("./child::*")).get(0);
							WebElement songHref = detailsDiv.findElements(By.xpath("./child::*")).get(0);
							String dateDj = dateAndDj.getText().replaceAll("[\\n]", "");
							String[] segments = dateDj.split("DJ:");
							
							String [] spin = {artistHref.getText(), songHref.getText(), segments[0].trim(), segments[1].trim()};
							System.out.println("KEXP spin: " + "|" + spin[0] + "|" + spin[1] + "|" + spin[2] + "|" + spin[3]);
							spinData.add(spin);
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
	
	public void addSpin(ArrayList <String[]> spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
		ArrayList <String> spins = new ArrayList<>();
		String date = "";
		Date spinDate = null;
		String artist = "-";
		String song = "-";
		String host = "-";
		String spinToAdd;
		boolean alreadyAdded;
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy hh:mm a");
		
		for (String[] spin : spinData) {
			alreadyAdded = false;
			if (spin[0].equalsIgnoreCase(currentArtist)) {
				spinDate = formatter.parse(spin[2]);
				SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd|hh:mm a");
				date = dateToString.format(spinDate);
				artist = spin[0];
				song = spin[1];
				host = spin[3];
				if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
					spinToAdd = "Key" + "|" + artist + "|" + "-" + "|" + song + "|" + "KEXP" + "|" + "Seattle" + "|" + host + "|" + date + "|" + "-";
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
