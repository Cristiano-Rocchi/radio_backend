package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pizzamafia.radio_backend.entities.Playlist;
import pizzamafia.radio_backend.entities.PlaylistSong;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylistOrderByPositionAsc(Playlist playlist);

    // Serve per rimuovere i legami prima di eliminare la playlist
    void deleteAllByPlaylist(Playlist playlist);

    @Query("SELECT COUNT(ps) FROM PlaylistSong ps WHERE ps.song.id = :songId")
    int countBySongId(@Param("songId") UUID songId);
}
