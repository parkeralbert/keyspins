 
public class ArtistInfo {
	private String artistName;
	private boolean singleOnly;
	private String album;
	private String[] songs;
	private String label;
	public String getAlbum() { 
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String[] getSongs() {
		return songs;
	}
	public void setSongs(String[] songs) {
		this.songs = songs;
	}
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}
	public boolean isSingleOnly() {
		return singleOnly;
	}
	public void setSingleOnly(boolean singleOnly) {
		this.singleOnly = singleOnly;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}
