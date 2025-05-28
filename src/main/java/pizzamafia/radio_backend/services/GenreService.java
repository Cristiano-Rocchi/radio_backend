package pizzamafia.radio_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pizzamafia.radio_backend.entities.Genre;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.payloads.GenreRespDTO;
import pizzamafia.radio_backend.payloads.NewGenreDTO;
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
    public GenreRespDTO createGenre(NewGenreDTO newGenreDTO) {
        if (newGenreDTO.getName() == null || newGenreDTO.getName().isBlank()) {
            throw new BadRequestException("Il nome del genere è obbligatorio.");
        }

        Genre genre = new Genre();
        genre.setName(newGenreDTO.getName().toUpperCase());


        Genre savedGenre = genreRepository.save(genre);
        LOGGER.info("✅ Genere creato: " + savedGenre.getName());

        return new GenreRespDTO(savedGenre.getId(), savedGenre.getName());

    }


    // 2️⃣ GET ALL GENRES
    public List<GenreRespDTO> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(g -> new GenreRespDTO(g.getId(), g.getName()))

                .toList();
    }


    // 3️⃣ GET GENRE BY ID
    public GenreRespDTO getGenreById(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + id));

        return new GenreRespDTO(genre.getId(), genre.getName());

    }


    // 4️⃣ UPDATE GENRE
    public GenreRespDTO updateGenre(Long id, NewGenreDTO updatedData) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + id));

        if (updatedData.getName() != null && !updatedData.getName().isBlank()) {
            genre.setName(updatedData.getName().toUpperCase());

        }

        Genre updatedGenre = genreRepository.save(genre);
        LOGGER.info("✅ Genere aggiornato: " + updatedGenre.getName());

        return new GenreRespDTO(updatedGenre.getId(), updatedGenre.getName());

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
