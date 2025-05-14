package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizzamafia.radio_backend.entities.Playlist;
import pizzamafia.radio_backend.entities.PlaylistSong;

import java.util.List;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylistOrderByPositionAsc(Playlist playlist);
}
