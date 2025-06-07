package pizzamafia.radio_backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "playlist_song")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSong {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "playlist_id", nullable = false)
    private Playlist playlist;

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Column(name = "position")
    private int position;
}
