import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class TestXpnSearch {

	@Test
	void parseFirstDayOfWeekReturnsDayCorrectly() {
		String lineToParse = "Date: 10/27/20 1:30 PM EST - 11/3/20 1:00 PM EST";
		GregorianCalendar expectedDate = new GregorianCalendar(2020, 9, 27, 13, 30);
		expectedDate.setTimeZone(TimeZone.getTimeZone("EST"));
		//MM/dd/yy h:mm a z
		assertEquals(expectedDate.getTime(), SpinSearch.parseFirstDayOfWeek(lineToParse));
	}

	void parseLastDayOfWeekReturnsDayCorrectly() {
		String lineToParse = "Date: 10/27/20 1:30 PM EST - 11/3/20 1:00 PM EST";
		GregorianCalendar expectedDate = new GregorianCalendar(2020, 10, 3, 13, 0);
		expectedDate.setTimeZone(TimeZone.getTimeZone("EST"));
		//MM/dd/yy h:mm a z
		assertEquals(expectedDate.getTime(), SpinSearch.parseLastDayOfWeek(lineToParse));
	}
	
	void addArtistInfoStoresArtistNameCorrectly() {
		String artistName = "Nothing";
		Map <String, ArtistInfo> artistInfos = new HashMap<>();
		//SpinSearch.addAlbumInfo(artistInfoToStore, false, "<>", artistInfos);
		ArtistInfo artistInfo = artistInfos.get(artistName);
		assertEquals(artistInfo.getArtistName(), "Nothing");
	}
	
	void addArtistInfoStoresAlbumCorrectly() {
		String artistName = "Nothing";
		Map <String, ArtistInfo> artistInfos = new HashMap<>();
		//SpinSearch.addAlbumInfo(artistInfoToStore, false, "<>", artistInfos);
		ArtistInfo artistInfo = artistInfos.get(artistName);
		assertEquals(artistInfo.getAlbum(), "The Great Dismal");
	}
	
	void addArtistInfoStoresSongsCorrectly() {
		String artistName = "Nothing";
		Map <String, ArtistInfo> artistInfos = new HashMap<>();
		//SpinSearch.addAlbumInfo(artistInfoToStore, true, "<>", artistInfos);
		ArtistInfo artistInfo = artistInfos.get(artistName);
		String[] songs = {"Bernie Sanders" , "Say Less"};
		assertEquals(artistInfo.getSongs(), songs);
	}
	void isDateInRangeIdentifiesRangeCorrectly() {
		GregorianCalendar firstDayOfWeek = new GregorianCalendar(2020, 9, 27, 13, 30);
		firstDayOfWeek.setTimeZone(TimeZone.getTimeZone("EST"));
		GregorianCalendar lastDayOfWeek = new GregorianCalendar(2020, 10, 3, 13, 0);
		lastDayOfWeek.setTimeZone(TimeZone.getTimeZone("EST"));
		GregorianCalendar spinDate = new GregorianCalendar(2020, 9, 29, 13, 30);
		spinDate.setTimeZone(TimeZone.getTimeZone("EST"));
		SpinSearch.isDateInRange(firstDayOfWeek.getTime(), lastDayOfWeek.getTime(), spinDate.getTime());
	}
}
