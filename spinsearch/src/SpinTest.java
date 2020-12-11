
import java.util.ArrayList;
import java.util.Date;
public class SpinTest {

	
	public static void main(String[] args) throws Exception
    {
		String readPath = "new_spin_inputs.txt";
		String writePath = "spins.txt";
		String delim = "<>";
        XpnSearch xpn = new XpnSearch();
        WfmuSearch wfmu = new WfmuSearch();
        Date firstDayOfWeek = xpn.getFirstDayOfWeek(readPath);
        Date lastDayOfWeek = xpn.getLastDayOfWeek(readPath);
        if(firstDayOfWeek == null) {
        	System.out.println("No first date found");
        }
        if(lastDayOfWeek == null) {
        	System.out.println("No last date found");
        }
		System.out.println("This week is " + firstDayOfWeek + " - " + lastDayOfWeek);
        ArrayList <ArtistInfo> xpnSearchList =  xpn.getArtistList(readPath, delim);
        ArrayList <ArtistInfo> wfmuSearchList =  wfmu.getArtistList(readPath, delim);
        wfmu.spinSearch("https://wfmu.org/search.php?action=searchbasic", wfmuSearchList, firstDayOfWeek, lastDayOfWeek, writePath);
        xpn.spinSearch("https://xpn.org/playlists/playlist-search", xpnSearchList, firstDayOfWeek, lastDayOfWeek, writePath);
    }
}
