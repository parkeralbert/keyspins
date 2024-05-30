import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
public class RunSearch {

	
	public static void main(String[] args) throws Exception
    {
		System.setProperty("webdriver.chrome.driver", "/opt/WebDriver/bin/chromedriver");
		
		String inputPath = "artist_input.txt";
		String outputPath = "data_key.txt";
		String allOutput = "data_all.txt";
		String stationInput = "station_input.txt";
		XpnSearch xpn = new XpnSearch();
        WfmuSearch wfmu = new WfmuSearch();
        WmbrSearch wmbr = new WmbrSearch();
        KexpSearch kexp = new KexpSearch();
        WruwSearch wruw = new WruwSearch();
        OldKcrwSearch kcrw = new OldKcrwSearch();
        WdetSearch wdet = new WdetSearch();
        WfuvSearch wfuv = new WfuvSearch();
        BoxSearch box = new BoxSearch();
        Date firstDayOfWeek = xpn.getFirstDayOfWeek(inputPath);
        Date lastDayOfWeek = xpn.getLastDayOfWeek(inputPath);
        ArrayList <String> artistNames =  wmbr.getArtistList(inputPath);
        KcrwSearch newKcrw = new KcrwSearch();
		/*BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
		writer.newLine();
		writer.close();*/
        if (args.length == 1 && args[0].equalsIgnoreCase("all")){
    		System.out.println("This week is " + firstDayOfWeek + " - " + lastDayOfWeek);
            wfmu.spinSearch("https://www.track-blaster.com/wmbr/", artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
            xpn.spinSearch("https://xpn.org/playlists/playlist-search", artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
            wmbr.spinSearch("https://www.track-blaster.com/wmbr/", artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
            wruw.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
            kcrw.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
            wdet.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
            wfuv.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
            newKcrw.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
            kexp.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
        }
        
		if (args.length == 1 && args[0].equalsIgnoreCase("wxpn")){
	         xpn.spinSearch("https://xpn.org/playlists/playlist-search", artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
		}
        
        if (args.length == 1 && args[0].equalsIgnoreCase("wruw")){
	         wruw.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("wfmu")){
	         wfmu.spinSearch("https://www.track-blaster.com/wmbr/", artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
		}
        
		if (args.length == 1 && args[0].equalsIgnoreCase("wmbr")){
	         wmbr.spinSearch("https://www.track-blaster.com/wmbr/", artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
		}
        
		if (args.length == 1 && args[0].equalsIgnoreCase("kcrw")){
			 kcrw.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("wdet")){
			 wdet.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
		}
        
		if (args.length == 1 && args[0].equalsIgnoreCase("kexp")){
			 kexp.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("newKcrw")){
	         newKcrw.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, outputPath, allOutput);
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("wfuv")){
			 wfuv.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, allOutput);
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("box")){
			 box.spinSearch(artistNames, firstDayOfWeek, lastDayOfWeek, inputPath, outputPath, stationInput, allOutput);
		}
		
    }
}
