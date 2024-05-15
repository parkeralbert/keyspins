import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
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
		SimpleDateFormat dateToString = new SimpleDateFormat("MMMMM'&day='d'&year='yyyy");
		String date = null;				
		//System.out.println("first date is:" + dateToString.format(firstDayOfWeek));
		//System.out.println("last date is:" + dateToString.format(lastDayOfWeek));
		String lastDate = dateToString.format(lastDayOfWeek);
		Calendar c = Calendar.getInstance();
		c.setTime(firstDayOfWeek);
		Date currentDate = c.getTime();
		int hourCount = 1;
		boolean pm = false;
		
		while (isDateInRange(firstDayOfWeek, lastDayOfWeek, currentDate) || date.equals(lastDate)) {
			date = dateToString.format(c.getTime()); 
			//System.out.println("currentdate is " + date);
			String hour=String.valueOf(hourCount); 

			if (pm) {
				dateList.add(date + "&hour=" + hour + "%3A00&ampm=PM");
				System.out.println("date is: " + date + "&hour=" + hour + "%3A00&ampm=PM");
			}
			else {
				dateList.add(date + "&hour=" + hour + "%3A00&ampm=AM");
				System.out.println("date is: " + date + "&hour=" + hour + "%3A00&ampm=AM");
			}


			
			if(hourCount == 12 && pm == false) {
				hourCount = 1;
				c.add(Calendar.DATE, 1);  
				pm = true;
			}
			else if(hourCount == 12 && pm == true) {
				hourCount = 1;
				pm = false;
			}
			else {
				hourCount++;
			}
			currentDate = c.getTime();
			date = dateToString.format(c.getTime()); 
		}
		//System.out.println("last date is: " + dateToString.format(c.getTime()));
		return dateList;
	}
	//https://www.kexp.org/playlist/?month=October&day=20&year=2023&hour=1%3A00&ampm=AM&offset=0
	
	
	
	public	ArrayList <String[]> getSpinData(ArrayList <String> artistNames, ArrayList <String> urls) throws Exception {
		WebDriver driver = new ChromeDriver();
		String artist = null;
		String song = null;
		String date = null;
		SimpleDateFormat formatter = new SimpleDateFormat("MMMMM'&day='d'&year='yyyy'&hour='h'%3A00&ampm='a");
		SimpleDateFormat dateToString = new SimpleDateFormat("yyyy-MM-dd");

		ArrayList <String[]> spinData = new ArrayList<>();
		try {
		for (String url : urls) {
			System.out.println("url is:" + url);
			Date spinDate = formatter.parse(url);
		try {
			String completeUrl = "https://www.kexp.org/playlist/?month=" + url + "&offset=0";
			driver.get(completeUrl);
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
			Thread.sleep(1000);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='MediaObject-body']")));
			driver.findElement(By.id("playlist-plays"));	
		}
		
		catch(org.openqa.selenium.TimeoutException e){
			try{
				String completeUrl = "https://www.kexp.org/playlist/?month=" + url + "&offset=0";
				driver.get(completeUrl);
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
				Thread.sleep(1000);
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='MediaObject-body']")));
				driver.findElement(By.id("playlist-plays"));	
			}
			catch(org.openqa.selenium.TimeoutException f){
				try {
					String completeUrl = "https://www.kexp.org/playlist/?month=" + url + "&offset=0";
					driver.get(completeUrl);
					WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
					Thread.sleep(1000);
					wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='MediaObject-body']")));
					driver.findElement(By.id("playlist-plays"));
				}
				catch(org.openqa.selenium.TimeoutException g){
					//System.out.println("Check for spins at url https://www.kexp.org/playlist/?month=" + url + "&offset=0");	
					continue;
				}

			}
		}

		//u-postitionRelative
				WebElement table = driver.findElement(By.id("playlist-plays"));	
				WebElement showTable = driver.findElement(By.id("show-detail"));	
				WebElement djTable = showTable.findElements(By.xpath("./child::*")).get(1);
				WebElement djName = djTable.findElements(By.xpath("./child::*")).get(1);
				String djData = djName.getText().trim();
				String [] nameAndShow =	djName.getText().split("\n");
				String show = nameAndShow[0];
				String dj = nameAndShow[1];
				
				List <WebElement> trs = table.findElements(By.xpath("./child::*"));
				
				for (WebElement tr : trs) {
					String clsVal = tr.getAttribute("class");
					if (!clsVal.equalsIgnoreCase("PlaylistItem u-mb1")) {
						continue;
					}
					WebElement td = tr.findElements(By.xpath("./child::*")).get(2);
					
					WebElement timeTable = tr.findElements(By.xpath("./child::*")).get(1);
					String time = timeTable.findElements(By.xpath("./child::*")).get(0).getText();
					WebElement songTitle = td.findElements(By.xpath("./child::*")).get(0);
					if (songTitle.getText().equalsIgnoreCase("Air Break")) {
						continue;
					}
					try {
						
					}
					catch(org.openqa.selenium.NoSuchElementException | IndexOutOfBoundsException e) {
						continue;
					}
					WebElement artistsName = td.findElements(By.xpath("./child::*")).get(1);
					WebElement albumTitle = td.findElements(By.xpath("./child::*")).get(2);

					
					for (String artistName : artistNames) {
						if (artistsName.getText().equalsIgnoreCase(artistName)) {
							String [] spin = {artistsName.getText(), songTitle.getText(), dj, dateToString.format(spinDate)};
							System.out.println("KEXP spin: " + "|" + spin[0] + "|" + spin[1] + "|" + spin[2] + "|" + spin[3]);
							spinData.add(spin);
						}
					}
					
				}
		}
		}
		catch(org.openqa.selenium.NoSuchElementException | IndexOutOfBoundsException e){
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
		String artist = "-";
		String song = "-";
		String host = "-";
		String spinToAdd;
		boolean alreadyAdded;
		
		for (String[] spin : spinData) {
			alreadyAdded = false;
			if (spin[0].equalsIgnoreCase(currentArtist)) {
				artist = spin[0];
				String album = spin[1];
				song = spin[1];
				host = spin[2];
				spinToAdd = "Key" + "|" + artist + "|" + song + "|" + "KEXP" + "|" + "Seattle" + "|" + host + "|" + spin[3];
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
			if(rawSpins.size() > 0) {
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
				for (String rawSpin : rawSpins) {
					writer.write(rawSpin);
					writer.newLine();
				}
				writer.close();
			}
	}

}
