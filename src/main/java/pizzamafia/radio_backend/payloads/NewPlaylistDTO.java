package pizzamafia.radio_backend.payloads;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class NewPlaylistDTO {
    private String name;
    private List<UUID> songIds;
}
