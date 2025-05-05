package pizzamafia.radio_backend.payloads;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.enums.Subgenre;

import java.util.List;
import java.util.UUID;

@Data
public class NewSongDTO {
    private UUID albumId;
    private List<MultipartFile> songs;

    // opzionali:
    private Integer rating;      // es: 0 di default
    private Integer level;       // es: 0 di default
    private Subgenre subgenre;   // es: null di default
}
