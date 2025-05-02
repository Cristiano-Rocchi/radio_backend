package pizzamafia.radio_backend.payloads;

import java.util.UUID;

public class SongRespDTO {

    private UUID id;
    private String titolo;
    private String presignedUrl;

    public SongRespDTO(UUID id, String titolo, String presignedUrl) {
        this.id = id;
        this.titolo = titolo;
        this.presignedUrl = presignedUrl;
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getPresignedUrl() {
        return presignedUrl;
    }

    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }
}
