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

	//reads and stores albums and singles to search
	public ArrayList <ArtistInfo> getArtistList(String albumInputFilePath, String delim){
		
		String line = null;
		Map<String, ArrayList<ArtistInfo>> artistInfos = new HashMap<>(); 
		boolean singleOnly = false;
		try
		{
			BufferedReader albumReader = new BufferedReader(new FileReader(albumInputFilePath));
			while ((line = albumReader.readLine()) != null)
			{
				
				if (line.equalsIgnoreCase("singles:")) {
					singleOnly = true;
				}
				
				ArtistInfo currentArtist = addAlbumInfo(line, singleOnly, delim, artistInfos);
				
			}
			albumReader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			e.printStackTrace();
		}
		return artistInfos;
        
	}
	
	public void storeSongs(String songsInAlbumsFilePath, String delim, ArrayList<ArtistInfo> artistInfos){
		Map<String, List<Spin>> songsForEachAlbum= new HashMap<>();
		String line = null;
		String artist = null;
		String album = null;
		String song = null;
		try
		{
			BufferedReader albumReader = new BufferedReader(new FileReader(songsInAlbumsFilePath));
			while ((line = albumReader.readLine()) != null)
			{
				
				if (line.contains("Album")) {
					album = line.split(" " + delim + " ")[1];
					
				}
				if(album != null && line.contains(delim)) {
					song = line.split(" " + delim + " ")[1];
					artistInfos.get(artist).add(song);
				}
				ArtistInfo currentArtist = addAlbumInfo(line, singleOnly, delim, artistInfos);
				
			}
			albumReader.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e);
			e.printStackTrace();
		}

		
	}
	
	public static ArtistInfo addAlbumInfo(String line, boolean singleOnly, String delim, Map<String, ArrayList<ArtistInfo>> artistInfos) {
		ArrayList <String> fullAlbum = null;
		
		if (line.indexOf(delim) != -1) {
			ArtistInfo artistInfo = new ArtistInfo();
			String[] segments = splitArtistNameAndProjectName(line, delim, artistInfo); 
			
			if(segments.length >= 3) {
				artistInfo.setLabel(segments[2]);
			}
			
			if (!singleOnly) {
			
			artistInfo.setAlbum(segments[1]);
			artistInfo.setSongs(fullAlbum);
			artistInfos.add(artistInfo);
			
			if(segments.length >= 3) {
				artistInfo.setLabel(segments[2]);
			}
		}
			
		else {
			
			artistInfo.setAlbum("Singles");
			
			setSongs(segments[1], artistInfo);
			
			ArrayList <String> songs = artistInfo.getSongs();
			
			removeQuotes(artistInfo, songs);
			
			artistInfo.setSongs(songs);
			artistInfo.setSingleOnly(true);		
			artistInfos.add(artistInfo);
		}
			return artistInfo;
	}
		
		return null;
	}
	
	
	public void spinSearch(String url, ArrayList <ArtistInfo> artistInfos, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
		Map<String, List<Spin>> spinsByArtist = getSpins(url, artistInfos, firstDayOfWeek, lastDayOfWeek, filePath);
		outputSpinsByArtist(filePath, spinsByArtist);
	}
	
	public static Date parseFirstDayOfWeek(String line) {
		Date firstDayOfWeek = null;
		if(line.indexOf("Date:") != -1) {
			String[] segments = line.substring(6).split(" - ");
			firstDayOfWeek = parseDateAndTime(segments[0]);
		}
		return firstDayOfWeek;
	}
	
	public static Date parseLastDayOfWeek(String line) {
		Date lastDayOfWeek = null;
		if(line.indexOf("Date:") != -1) {
			String[] segments = line.substring(6).split(" - ");
			lastDayOfWeek = parseDateAndTime(segments[1]);
		}
		return lastDayOfWeek;
	}

	
	public Map<String, List<Spin>> getSpins(String url, ArrayList <ArtistInfo> artistInfos, Date firstDayOfWeek, Date lastDayOfWeek, String filePath) throws Exception {
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
		
		return spinsByArtist;

	}

	public void outputSpinsByArtist(String filePath, Map<String, List<Spin>> spinsByArtist) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		writer.write("WXPN");
		writer.newLine();
		writer.close();
		
		for (List<Spin> spinsToPrint : spinsByArtist.values()) {
			writeSpinsToFile(spinsToPrint, filePath);
		}
	}

	public	Elements getSpinData(ArtistInfo currentArtist, String url, String songOrAlbumName) throws Exception {
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

	private static void removeQuotes(ArtistInfo artistInfo, ArrayList <String> songs) {
		for (int i = 0; i < artistInfo.getSongs().size(); i++) {
			System.out.println("Parsing song: " + songs.get(i));
			songs.set(i, songs.get(i).substring(1, songs.get(i).length()-1));
		}
	}

	private static void setSongs(String songs, ArtistInfo artistInfo) {
		if(songs.indexOf(" + ") != -1) {
			for (String song : songs.split("\s\\+\s")) {
				artistInfo.addSong(song);
			}
		}
		
		else {
			artistInfo.addSong(songs);
		}
	}
	
	private static String[] splitArtistNameAndProjectName(String line, String delim, ArtistInfo artistInfo) {
		String[] segments = line.split(" " + delim + " ");
		artistInfo.setArtistName(segments[0]);
		return segments;
	}
	
	

	private void addSpin(Elements spinData, ArtistInfo artistInfo, Date firstDayOfWeek, Date lastDayOfWeek, Map<String, Spin> allSpins) throws Exception   {
		for (Element e : spinData) {
			String[] segments = e.text().split(" - ");
			String song = segments[1];
			SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
			Date spinDate = formatter.parse(segments[0].substring(0, 19));
			
			if (isDateInRange(firstDayOfWeek, lastDayOfWeek, spinDate)){
				System.out.println("song is " + song + " date is " + spinDate);
				String key = artistInfo.getArtistName() + artistInfo.getAlbum() + song;
				Spin spin = allSpins.get(key);
				
				if (spin == null) {
					spin = new Spin(artistInfo.getArtistName(), song, artistInfo.getAlbum(), spinDate, spinDate);
					spin.setDj("Kristen Kurtis");
				} 
				else {
					if (spinDate.before(spin.getFirstPlayDate())) {
						spin.setFirstPlayDate(spinDate);
					}
					if (spinDate.after(spin.getLastPlayDate())) {
						spin.setLastPlayDate(spinDate);
					}
				}
				
				spin.incrementCount();

				System.out.println("Spin: " + e.text());
				allSpins.put(key, spin);
			}

		}
	}
	
	public static boolean isDateInRange(Date firstDayOfWeek, Date lastDayOfWeek, Date spinDate) {
		if ((spinDate.after(firstDayOfWeek) || spinDate.equals(firstDayOfWeek)) && (spinDate.before(lastDayOfWeek) || spinDate.equals(lastDayOfWeek))){
			return true;
		}
		else {
			return false;
		}
	}

	private static Map<String, List<Spin>> getSpinsByArtist(Collection<Spin> values) {
		
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

	public void writeSpinsToFile(List<Spin> values, String filePath) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
		
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
	

	private String formatWrittenDate(Spin processedSpin){
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

	private static String removeZerosFromDate(Date inputDate) {
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