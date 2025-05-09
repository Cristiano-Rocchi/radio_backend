package pizzamafia.radio_backend.payloads;

import lombok.Data;
import pizzamafia.radio_backend.enums.Subgenre;

@Data
public class UpdateSongDTO {
    private String titolo;
    private Integer rating;
    private Integer level;
    private Subgenre subgenre;
}

