package pizzamafia.radio_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.entities.Album;
import pizzamafia.radio_backend.entities.Genre;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.exceptions.InternalServerErrorException;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.repositories.AlbumRepository;
import pizzamafia.radio_backend.repositories.GenreRepository;
import pizzamafia.radio_backend.repositories.SongRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class AlbumService {

    private static final Logger LOGGER = Logger.getLogger(AlbumService.class.getName());

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private GenreRepository genreRepository;

    // 1️⃣ CREATE: crea album + canzoni da upload cartella
    public Album createAlbumFromUpload(String albumName,
                                       String artist,
                                       UUID genreId,
                                       List<MultipartFile> songs) {

        // ✅ Check input obbligatori
        if (albumName == null || albumName.isBlank()) {
            throw new BadRequestException("Il titolo dell'album è obbligatorio.");
        }
        if (songs == null || songs.isEmpty()) {
            throw new BadRequestException("Devi caricare almeno una traccia audio.");
        }

        // Trova il genere
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + genreId));

        // Crea album
        Album album = new Album();
        album.setTitle(albumName);
        album.setArtist(artist != null ? artist : "Unknown Artist");
        album.setGenre(genre);
        album.setDate(LocalDate.now().getYear());
        album.setRating(0);

        albumRepository.save(album);

        // Salva ogni traccia
        for (MultipartFile file : songs) {
            try {
                String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
                String titolo = originalFilename.substring(0, originalFilename.lastIndexOf('.'));

                File savedFile = saveFileLocally(file);

                Song song = new Song();
                song.setTitolo(titolo);
                song.setRating(0);
                song.setLevel(0);
                song.setSubgenre(null);
                song.setAlbum(album);

                songRepository.save(song);

                LOGGER.info("✅ Traccia salvata: " + titolo + " (" + savedFile.getAbsolutePath() + ")");

            } catch (Exception e) {
                throw new InternalServerErrorException("Errore nel salvataggio fisico della traccia: " + e.getMessage());
            }
        }

        LOGGER.info("✅ Album creato con successo: " + albumName + " con " + songs.size() + " tracce.");
        return album;
    }

    private File saveFileLocally(MultipartFile file) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads";

        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            boolean created = uploadPath.mkdirs();
            LOGGER.info("📂 Cartella uploads creata: " + created);
        }

        File savedFile = new File(uploadPath, file.getOriginalFilename());
        file.transferTo(savedFile);
        return savedFile;
    }

    // 2️⃣ GET ALL ALBUMS
    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    // 3️⃣ GET ALBUM BY ID
    public Album getAlbumById(UUID id) {
        return albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + id));
    }

    // 4️⃣ DELETE ALBUM (DB + FILES)
    public void deleteAlbum(UUID id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + id));

        List<Song> songs = album.getSongs();
        for (Song song : songs) {
            String filename = song.getTitolo();
            // Prova a trovare il file con estensione fissa (.mp3)
            File file = new File("uploads/" + filename + ".mp3");
            if (file.exists()) {
                boolean deleted = file.delete();
                LOGGER.info(deleted ? "✅ File eliminato: " + file.getName() : "⚠ File non trovato: " + file.getName());
            } else {
                LOGGER.info("⚠ File non trovato (skip): " + file.getName());
            }
        }

        albumRepository.delete(album);
        LOGGER.info("✅ Album e tracce eliminate dal DB e dal file system.");
    }

    // 5️⃣ UPDATE ALBUM
    public Album updateAlbum(UUID id, String newTitle, String newArtist, Integer newRating) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + id));

        if (newTitle != null && !newTitle.isBlank()) {
            album.setTitle(newTitle);
        }
        if (newArtist != null && !newArtist.isBlank()) {
            album.setArtist(newArtist);
        }
        if (newRating != null) {
            album.setRating(newRating);
        }

        Album updatedAlbum = albumRepository.save(album);
        LOGGER.info("✅ Album aggiornato con successo: " + updatedAlbum.getId());
        return updatedAlbum;
    }
}
