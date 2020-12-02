
import java.util.ArrayList;
import java.util.Date;
public class SpinTest {

	
	public static void main(String[] args) throws Exception
    {
		String readPath = "new_spin_inputs.txt";
		String writePath = "spins.txt";
		String delim = "<>";
        XpnSearch xpn = new XpnSearch();
        Date firstDayOfWeek = xpn.getFirstDayOfWeek(readPath);
        Date lastDayOfWeek = xpn.getLastDayOfWeek(readPath);
        if(firstDayOfWeek == null) {
        	System.out.println("No first date found");
        }
        if(lastDayOfWeek == null) {
        	System.out.println("No last date found");
        }
		System.out.println("This week is " + firstDayOfWeek + " - " + lastDayOfWeek);
        ArrayList <ArtistInfo> searchList =  xpn.getArtistList(readPath, delim);
        xpn.spinSearch("https://xpn.org/playlists/playlist-search", searchList, firstDayOfWeek, lastDayOfWeek, writePath);
    }
}
