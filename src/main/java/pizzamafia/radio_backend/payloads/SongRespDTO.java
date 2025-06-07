package pizzamafia.radio_backend.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongRespDTO {
    private UUID id;
    private String titolo;
    private String presignedUrl;
    private String bucketName;

    private Integer duration;
    private Integer rating;  
    private Integer level;
    private UUID albumId;
    private String albumTitle;
    private String albumArtist;


}
