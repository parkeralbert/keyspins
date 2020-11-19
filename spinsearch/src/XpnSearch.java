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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class XpnSearch {
	// We'll use a fake USER_AGENT so the web server thinks the robot is a normal
	// web browser.
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";

	
	/**
	 * This performs all the work. It makes an HTTP request, checks the response,
	 * and then gathers up all the links on the page. Perform a searchForWord after
	 * the successful crawl
	 * 
	 * @param url - The URL to visit
	 * @return whether or not the crawl was successful
	 */
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

	//reads first date on input file and stores it
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

	static Date parseFirstDayOfWeek(String line) {
		Date firstDayOfWeek = null;
		if(line.indexOf("Date:") != -1) {
			String[] segments = line.substring(6).split(" - ");
			firstDayOfWeek = parseDateAndTime(segments[0]);
		}
		return firstDayOfWeek;
	}
	
	static Date parseLastDayOfWeek(String line) {
		Date lastDayOfWeek = null;
		if(line.indexOf("Date:") != -1) {
			String[] segments = line.substring(6).split(" - ");
			lastDayOfWeek = parseDateAndTime(segments[1]);
		}
		return lastDayOfWeek;
	}


	
	//scrape the webpage for spins, organize them, write them to file
	public void getSpins(String url, ArrayList <ArtistInfo> artistInfos, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write("WXPN");
		writer.newLine();
		writer.close();
		//String dj = "Kristen Kurtis";
		ArrayList<ArtistInfo> artistsToSearch = artistInfos;
		Map<String, Spin> allSpins = new HashMap<>();

		for (ArtistInfo currentArtist : artistsToSearch) {

			if (currentArtist.isSingleOnly()) {
				for (String song : currentArtist.getSongs()) {
					Elements spinData = getSpinData(currentArtist, url, song);
					addSpin(spinData, currentArtist, firstDayOfWeek, lastDayOfWeek, allSpins);
				}
			}
			else {
				Elements spinData = getSpinData(currentArtist, url, currentArtist.getAlbum());
				addSpin(spinData, currentArtist, firstDayOfWeek, lastDayOfWeek, allSpins);
			}
		}
		
		Map<String, List<Spin>> spinsByArtist = getSpinsByArtist(allSpins.values());

		for (List<Spin> spinsToPrint : spinsByArtist.values()) {
			writeSpinsToFile(spinsToPrint, filePath);
		}
	}

	//searches website by artist and pulls any spins for relevant song or album
	static Elements getSpinData(ArtistInfo currentArtist, String url, String songOrAlbumName) throws Exception {
		Map<String, String> postData = new HashMap<>();
		String artist = (String) currentArtist.getArtistName();
		postData.put("val", "search");
		postData.put("search", artist);
		postData.put("playlist", "all");
		
		Document page = Jsoup.connect(url).userAgent(USER_AGENT).data(postData).post();
		
		Elements spinData = null;
		
		spinData = (page.select(String.format("td:containsOwn(%s)", songOrAlbumName)));
		System.out.println("*** Retrieved " + artist + " spins: " + spinData.text());
		
		return spinData;
	}

	//reads and stores albums and singles to search
	public ArrayList <ArtistInfo> getArtistList(String filePath, String delim){
		
		String line = null;
		ArrayList<ArtistInfo> artistInfos = new ArrayList<ArtistInfo>(); 
		boolean singleOnly = false;
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			while ((line = reader.readLine()) != null)
			{
				
				if (line.equalsIgnoreCase("singles:")) {
					singleOnly = true;
				}
				
				addArtistInfo(line, singleOnly, delim, artistInfos);
			}
			reader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			e.printStackTrace();
		}
		return artistInfos;
        
	}
	
	private static void addArtistInfo(String line, boolean singleOnly, String delim, ArrayList<ArtistInfo> artistInfos) {
		String[] fullAlbum = {};
		
		if (line.indexOf(delim) != -1) {
			
			if (!singleOnly) {
			ArtistInfo artistInfo = new ArtistInfo();
			String[] segments = line.split(" " + delim + " ");
			artistInfo.setArtistName(segments[0]); 
			artistInfo.setAlbum(segments[1]);
			
			artistInfo.setSongs(fullAlbum);
			artistInfos.add(artistInfo);
			
			if(segments.length >= 3) {
				artistInfo.setLabel(segments[2]);
			}
		}
			
		else {
			
			ArtistInfo artistInfo = new ArtistInfo();
			String[] segments = line.split(" " + delim + " ");
			artistInfo.setArtistName(segments[0]); 
			artistInfo.setAlbum("Singles");
			
			if(segments[1].indexOf(" + ") != -1) {
				artistInfo.setSongs(segments[1].split("\s\\+\s"));
			}
			
			else {
				artistInfo.setSongs(new String[] {segments[1]});
			}
			
			String[] songs = artistInfo.getSongs();
			
			//removing quotes from each stored song name
			for (int i = 0; i < artistInfo.getSongs().length; i++) {
				System.out.println("Parsing song: " + songs[i]);
				songs[i] = songs[i].substring(1, songs[i].length()-1);
			}
			
			artistInfo.setSongs(songs);
			artistInfo.setSingleOnly(true);
			
			if(segments.length >= 3) {
				artistInfo.setLabel(segments[2]);
			}
			
			artistInfos.add(artistInfo);
		}
	}
	}
	

	//uses data from webpage to create a spin for each promoted song within date range and returns all spins
	private void addSpin(Elements spinData, ArtistInfo artistInfo, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, Spin> allSpins) throws Exception   {
		for (Element e : spinData) {
			String[] segments = e.text().split(" - ");
			String song = segments[1];
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
			Date date = formatter.parse(segments[0].substring(0, 19));
			
			if ((date.after(firstDayOfWeek) || date.equals(firstDayOfWeek)) && (date.before(lastDayOfWeek) || date.equals(lastDayOfWeek))){
				System.out.println("song is " + song + " date is " + date);
				String key = artistInfo.getArtistName() + artistInfo.getAlbum() + song;
				Spin spin = allSpins.get(key);
				
				if (spin == null) {
					spin = new Spin();
					spin.setFirstPlayDate(date);
					spin.setLastPlayDate(date);
					spin.setAlbum(artistInfo.getAlbum());
					spin.setSong(song);
					spin.setArtist(artistInfo.getArtistName());
					spin.setDj("Kristen Kurtis");
				} 
				else {
					if (date.before(spin.getFirstPlayDate())) {
						spin.setFirstPlayDate(date);
					}
					if (date.after(spin.getLastPlayDate())) {
						spin.setLastPlayDate(date);
					}
				}
				
				spin.incrementCount();

				System.out.println("Spin: " + e.text());
				allSpins.put(key, spin);
			}

		}
	}
	//create a map of spin lists organized by artist names
	private Map<String, List<Spin>> getSpinsByArtist(Collection<Spin> values) {
		
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

	//write each spin to file organized by artist name
	private void writeSpinsToFile(List<Spin> values, String filePath) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/parkeralbert/Downloads/spins.txt", true));
		
		if(values.size() > 0) {
			writer.write(values.get(0).getArtist());
			writer.newLine();
		}
		for (Spin processedSpin : values) {

			String formattedDate = formatWrittenDate(processedSpin);
			writer.write(
					"Spun " + "\"" + processedSpin.getSong() + "\" x" + processedSpin.getCount() + " " + formattedDate);
			writer.newLine();
		}
		writer.newLine();
		writer.close();
	}
	
	//format date to be written out to file
	String formatWrittenDate(Spin processedSpin){
		String formattedDate;

		if (processedSpin.getFirstPlayDate() != processedSpin.getLastPlayDate()) {
			String firstDate = 	removeZerosFromDate(processedSpin.getFirstPlayDate());
			String lastDate = removeZerosFromDate(processedSpin.getLastPlayDate());

			formattedDate = firstDate + "-" + lastDate;
		} else {

			formattedDate = removeZerosFromDate(processedSpin.getFirstPlayDate());
		}

		return formattedDate;
	}

	private String removeZerosFromDate(Date inputDate) {
		SimpleDateFormat secondFormatter = new SimpleDateFormat("MM/dd");
		
		String newDate = secondFormatter.format(inputDate);

		String[] segments = newDate.split("/");
		int i = 0;
		for (String segment : segments) {
			if (segment.indexOf('0') == 0) {
				segments[i] = segments[i].substring(1);
			}
			i++;
		}
		newDate = segments[0] + "/" + segments[1];
		return newDate;
	}

}