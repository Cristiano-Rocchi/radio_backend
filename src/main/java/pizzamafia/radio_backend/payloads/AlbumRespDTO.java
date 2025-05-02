package pizzamafia.radio_backend.payloads;

import java.util.List;
import java.util.UUID;

public class AlbumRespDTO {

    private UUID id;
    private String title;
    private String artist;
    private Integer rating;
    private Integer year;
    private List<SongRespDTO> songs;

    public AlbumRespDTO(UUID id, String title, String artist, Integer rating, Integer year, List<SongRespDTO> songs) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.rating = rating;
        this.year = year;
        this.songs = songs;
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<SongRespDTO> getSongs() {
        return songs;
    }

    public void setSongs(List<SongRespDTO> songs) {
        this.songs = songs;
    }
}
