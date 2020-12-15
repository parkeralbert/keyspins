
import java.util.Date;
import java.util.Map;
public class SpinTest {

	
	public static void main(String[] args) throws Exception
    {
		String albumsInputPath = "album_input.txt";
		String songsInputPath = "song_input.txt";
		String writePath = "spins.txt";
		String delim = "<>";
        XpnSearch xpn = new XpnSearch();
        WfmuSearch wfmu = new WfmuSearch();
        Date firstDayOfWeek = xpn.getFirstDayOfWeek(albumsInputPath);
        Date lastDayOfWeek = xpn.getLastDayOfWeek(albumsInputPath);
        if(firstDayOfWeek == null) {
        	System.out.println("No first date found");
        }
        if(lastDayOfWeek == null) {
        	System.out.println("No last date found");
        }
		System.out.println("This week is " + firstDayOfWeek + " - " + lastDayOfWeek);
		Map <String, ArtistInfo> xpnSearchList =  xpn.getArtistList(albumsInputPath, songsInputPath, delim);
		Map <String, ArtistInfo> wfmuSearchList =  wfmu.getArtistList(albumsInputPath, songsInputPath, delim);
        wfmu.spinSearch("https://wfmu.org/search.php?action=searchbasic", wfmuSearchList, firstDayOfWeek, lastDayOfWeek, writePath);
        //xpn.spinSearch("https://xpn.org/playlists/playlist-search", xpnSearchList, firstDayOfWeek, lastDayOfWeek, writePath);
    }
}
