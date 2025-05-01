package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pizzamafia.radio_backend.entities.Song;

import java.util.UUID;

public interface SongRepository extends JpaRepository<Song, UUID> {
}
