package pizzamafia.radio_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pizzamafia.radio_backend.entities.Genre;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.repositories.GenreRepository;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class GenreService {

    private static final Logger LOGGER = Logger.getLogger(GenreService.class.getName());

    @Autowired
    private GenreRepository genreRepository;

    // 1️⃣ CREATE GENRE
    public Genre createGenre(Genre genre) {
        if (genre.getName() == null) {
            throw new BadRequestException("Il nome del genere (enum) è obbligatorio.");
        }
        Genre savedGenre = genreRepository.save(genre);
        LOGGER.info("✅ Genere creato: " + savedGenre.getName());
        return savedGenre;
    }

    // 2️⃣ GET ALL GENRES
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    // 3️⃣ GET GENRE BY ID
    public Genre getGenreById(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + id));
    }

    // 4️⃣ UPDATE GENRE
    public Genre updateGenre(Long id, Genre updatedData) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + id));

        if (updatedData.getName() != null) {
            genre.setName(updatedData.getName());
        }

        Genre updatedGenre = genreRepository.save(genre);
        LOGGER.info("✅ Genere aggiornato: " + updatedGenre.getName());
        return updatedGenre;
    }

    // 5️⃣ DELETE GENRE (con check album)
    public void deleteGenre(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + id));

        if (genre.getAlbums() != null && !genre.getAlbums().isEmpty()) {
            throw new BadRequestException("Non puoi eliminare un genere che ha album associati.");
        }

        genreRepository.delete(genre);
        LOGGER.info("✅ Genere eliminato con successo: " + genre.getName());
    }
}
