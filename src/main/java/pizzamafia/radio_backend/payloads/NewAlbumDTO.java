package pizzamafia.radio_backend.payloads;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Data
public class NewAlbumDTO {
    private String title;
    private String artist;
    private Long genreId;
    private List<MultipartFile> songs;
}
