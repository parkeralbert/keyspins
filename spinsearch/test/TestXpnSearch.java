import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.GregorianCalendar;
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
		String artistInfoToStore = "Nothing <> The Great Dismal <> Relapse Records";
		ArrayList<ArtistInfo> artistInfos = new ArrayList<ArtistInfo>();
		SpinSearch.addArtistInfo(artistInfoToStore, false, "<>", artistInfos);
		ArtistInfo artistInfo = artistInfos.get(0);
		assertEquals(artistInfo.getArtistName(), "Nothing");
	}
	
	void addArtistInfoStoresAlbumCorrectly() {
		String artistInfoToStore = "Nothing <> The Great Dismal <> Relapse Records";
		ArrayList<ArtistInfo> artistInfos = new ArrayList<ArtistInfo>();
		SpinSearch.addArtistInfo(artistInfoToStore, false, "<>", artistInfos);
		ArtistInfo artistInfo = artistInfos.get(0);
		assertEquals(artistInfo.getAlbum(), "The Great Dismal");
	}
	
	void addArtistInfoStoresSongsCorrectly() {
		String artistInfoToStore = "Nothing <> Bernie Sanders + Say Less <> Relapse Records";
		ArrayList<ArtistInfo> artistInfos = new ArrayList<ArtistInfo>();
		SpinSearch.addArtistInfo(artistInfoToStore, true, "<>", artistInfos);
		ArtistInfo artistInfo = artistInfos.get(0);
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
