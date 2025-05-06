package pizzamafia.radio_backend.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumRespDTO {
    private UUID id;
    private String title;
    private String artist;
    private Integer rating;
    private Integer date;
    private Long genreId;  // 👈 AGGIUNGI QUESTO
    private List<SongRespDTO> songs;
}

