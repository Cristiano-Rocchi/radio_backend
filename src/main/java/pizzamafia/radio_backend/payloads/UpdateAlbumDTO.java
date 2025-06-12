package pizzamafia.radio_backend.payloads;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateAlbumDTO {
    private String title;
    private String artist;
    private String date;
    private List<UUID> songsToRemove;
    private List<MultipartFile> newSongs;
}
