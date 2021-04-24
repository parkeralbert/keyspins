import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

abstract public class SpinSearch {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";

	
	public static Date parseDate(String date) {
	     try {
	         return new SimpleDateFormat("MM/dd/yy").parse(date);
	     } catch (ParseException e) {
	         return null;
	     }
	  }
	
	public static Date parseDateAndTime(String date) {
	     try {
	         return new SimpleDateFormat("MM/dd/yy h:mm a z").parse(date);
	     } catch (ParseException e) {
	         return null;
	     }
	}

	public Date getFirstDayOfWeek(String filePath) {
		Date firstDayOfWeek = null;
		String line = null;
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			while ((line = reader.readLine()) != null && firstDayOfWeek == null)
			{
				firstDayOfWeek = parseFirstDayOfWeek(line);
			}
			reader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
		}

		return firstDayOfWeek;
	}
	
	//reads last date on input file and stores it
	public Date getLastDayOfWeek(String filePath) {
		Date lastDayOfWeek = null;
		String line = null;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			while ((line = reader.readLine()) != null && lastDayOfWeek == null)
			{
				lastDayOfWeek = parseLastDayOfWeek(line);
			}
			reader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
		}
		return lastDayOfWeek;
	}

	public ArrayList <String> getArtistList(String artistInputPath){
		String line = null;
		ArrayList<String> artistNames = new ArrayList<String>(); 
		try
		{
			BufferedReader artistReader = new BufferedReader(new FileReader(artistInputPath));
			while ((line = artistReader.readLine()) != null)
			{

				addArtistNames(line, artistNames);
				
			}
			artistReader.close();
			
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			e.printStackTrace();
		}
		
		return artistNames;
        
	}
	
	public static void addArtistNames(String line, ArrayList<String> artistInfo) {
		
		if(line.trim().length() > 0 && !line.contains("Last Day of Week:") && !line.contains("Date:") && !line.contains("https://")) {
			if (line.indexOf("*") == 0) {
				line = line.replace("*", "");
			}
			artistInfo.add(line.trim());
		}
		
	}
	
	
	public void spinSearch(String url, ArrayList<String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, ArrayList <String>> spinsByArtist = getSpins(url, artistNames, firstDayOfWeek, lastDayOfWeek, filePath);
		outputSpinsByArtist(filePath, spinsByArtist);
	}
	
	public static Date parseFirstDayOfWeek(String line) {
		Date firstDayOfWeek = null;
		if(line.indexOf("Date:") != -1) {
			if (line.contains("\"")) {
				line = line.replaceAll("\"", "");
			}
			String[] segments = line.trim().substring(6).split(" - ");
			firstDayOfWeek = parseDateAndTime(segments[0]);
		}
		return firstDayOfWeek;
	}
	
	public static Date parseLastDayOfWeek(String line) {
		Date lastDayOfWeek = null;
		if(line.indexOf("Date:") != -1) {
			if (line.contains("\"")) {
				line = line.replaceAll("\"", "");
			}
			String[] segments = line.trim().substring(6).split(" - ");
			lastDayOfWeek = parseDateAndTime(segments[1]);
		}
		return lastDayOfWeek;
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
	
	public Map<String, ArrayList <String>> getSpins(String url, ArrayList <String> artistNames, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, ArrayList <String>> allSpinData = new HashMap<>();
		
	    for (String currentArtist : artistNames) {
					Elements spinData = getSpinData(currentArtist, url);
					addSpin(spinData, currentArtist, allSpinData, firstDayOfWeek, lastDayOfWeek);
	    }
		
		return allSpinData;

	}

	public void outputSpinsByArtist(String filePath, Map<String, ArrayList <String>> spinsByArtist) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.newLine();
		writer.close();
		
		for (String currentArtist : spinsByArtist.keySet()) {
			writeSpinsToFile(currentArtist, spinsByArtist.get(currentArtist), filePath);
		}
	}

	public  Elements getSpinData(String currentArtist, String url) throws Exception {
		Map<String, String> postData = new HashMap<>();
		postData.put("val", "search");
		postData.put("search", currentArtist);
		postData.put("playlist", "all");
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = null;
		currentArtist = StringEscapeUtils.escapeEcmaScript(currentArtist);
		spinData = (page.select(String.format("td:containsOwn(%s)", currentArtist)));
		
		return spinData;
	}
	
	

	public void addSpin(Elements spinData, String currentArtist, Map<String, ArrayList <String>> allSpinData, Date firstDayOfWeek, Date lastDayOfWeek) throws Exception   {
		if(spinData != null) {
			ArrayList <String> spins = new ArrayList<>();
			for (Element e : spinData) {
				boolean correctArtist;
				String[] segments = null;
				if(e.text().split(" - ").length > 1) {
					segments = e.text().split(" - ");
					//only use for testing: System.out.println("Retrieved " + artistInfo.getArtistName() + " spins: " + spinData.text() + " for " + segments[1]);
				}
				else {
					return;
				}
				SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
				Date spinDate = formatter.parse(segments[0].substring(0, 19));
				String artistName = replaceSmartQuotes(segments[0].substring(20).trim());
				
				correctArtist = artistName.equalsIgnoreCase(currentArtist);
				
				if(isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)) {
					
					if (correctArtist)	{
					System.out.println("artist is " + artistName + " date is " + spinDate);
					spins.add(e.text());
					System.out.println("Raw Spin Data: " + e.text());
					}
				}
			}
			allSpinData.put(currentArtist, spins);
		}
	}
	
	public static boolean isDateInRange(Date firstDayOfWeek, Date lastDayOfWeek, Date spinDate) {
		if (spinDate != null && (spinDate.after(firstDayOfWeek) || spinDate.equals(firstDayOfWeek)) && (spinDate.before(lastDayOfWeek) || spinDate.equals(lastDayOfWeek))){
			return true;
		}
		else {
			return false;
		}
	}

	public static Map<String, List<Spin>> getSpinsByArtist(Collection<Spin> values) {
		
		Map<String, List<Spin>> spins = new HashMap<>();
		
		for(Spin processedSpin : values) {
			List<Spin> artistSpins = spins.get(processedSpin.getArtist());
			if(artistSpins == null){
				artistSpins = new ArrayList <Spin>();
				spins.put(processedSpin.getArtist(), artistSpins);
			}
			artistSpins.add(processedSpin);
		}

		
		return spins;
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