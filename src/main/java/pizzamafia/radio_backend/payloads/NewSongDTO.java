package pizzamafia.radio_backend.payloads;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(0)
    @Max(5)
    private Integer rating;//  0 di default

    @Min(0)
    @Max(100)
    private Integer level;
    // es: 0 di default
    private Subgenre subgenre;   //  null di default
}
