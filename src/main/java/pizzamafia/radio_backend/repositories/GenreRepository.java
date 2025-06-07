package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pizzamafia.radio_backend.entities.Genre;

import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
