package pizzamafia.radio_backend.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistRespDTO {
    private long id;
    private String name;
    private List<SongRespDTO> tracks;
}
