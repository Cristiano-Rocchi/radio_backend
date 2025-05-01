package pizzamafia.radio_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pizzamafia.radio_backend.entities.Album;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {


}
