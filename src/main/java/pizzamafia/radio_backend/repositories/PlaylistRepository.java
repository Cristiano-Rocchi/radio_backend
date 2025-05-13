package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizzamafia.radio_backend.entities.Playlist;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
