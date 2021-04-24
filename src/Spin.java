import java.util.Date;

public class Spin {
	private String artist;
	private String song;
	private String album;
	private Date firstPlayDate;
	private Date lastPlayDate;
	private int count;
	private String dj;
	
	public Spin(String artist, String song, String album, Date firstPlayDate, Date lastPlayDate) {
		this.artist = artist;
		this.song = song;
		this.album = album;
		this.firstPlayDate = firstPlayDate;
		this.lastPlayDate = lastPlayDate;
	}
	
	public String getArtist() {
		return artist;  
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public Date getFirstPlayDate() {
		return firstPlayDate;
	}
	public void setFirstPlayDate(Date firstPlayDate) {
		this.firstPlayDate = firstPlayDate;
	}
	public Date getLastPlayDate() {
		return lastPlayDate;
	}
	public void setLastPlayDate(Date lastPlayDate) {
		this.lastPlayDate = lastPlayDate;
	}
	public int getCount() {
		return count;
	}
	public void incrementCount() {
		count++;
	}
	public String getDj() {
		return dj;
	}
	public void setDj(String dj) {
		this.dj = dj;
	}
	public String getSong() {
		return song;
	}
	public void setSong(String song) {
		this.song = song;
	}
	
	
}
