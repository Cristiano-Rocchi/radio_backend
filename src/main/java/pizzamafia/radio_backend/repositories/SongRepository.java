package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pizzamafia.radio_backend.entities.Song;

import java.util.List;
import java.util.UUID;

public interface SongRepository extends JpaRepository<Song, UUID> {
    List<Song> findByTitoloContainingIgnoreCase(String titolo);

}
