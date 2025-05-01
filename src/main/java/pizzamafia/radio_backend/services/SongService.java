package pizzamafia.radio_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.entities.Album;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.enums.Subgenre;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.exceptions.InternalServerErrorException;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.repositories.AlbumRepository;
import pizzamafia.radio_backend.repositories.SongRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class SongService {

    private static final Logger LOGGER = Logger.getLogger(SongService.class.getName());

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AlbumRepository albumRepository;

    // 1️⃣ ADD SONGS (aggiunge nuove tracce a un album esistente)
    public List<Song> addSongs(UUID albumId, List<MultipartFile> songs) {

        if (songs == null || songs.isEmpty()) {
            throw new BadRequestException("Devi caricare almeno una traccia audio.");
        }

        // Trova album
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + albumId));

        List<Song> savedSongs = new ArrayList<>();

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

                Song saved = songRepository.save(song);
                savedSongs.add(saved);

                LOGGER.info("✅ Nuova traccia aggiunta: " + titolo + " (" + savedFile.getAbsolutePath() + ")");

            } catch (Exception e) {
                throw new InternalServerErrorException("Errore nel salvataggio fisico della traccia: " + e.getMessage());
            }
        }

        LOGGER.info("✅ Aggiunte " + savedSongs.size() + " nuove tracce all'album: " + album.getTitle());
        return savedSongs;
    }

    private File saveFileLocally(MultipartFile file) throws IOException {
        String uploadDir = "uploads";
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            boolean created = uploadPath.mkdirs();
            LOGGER.info("📂 Cartella uploads creata: " + created);
        }

        File savedFile = new File(uploadPath, file.getOriginalFilename());
        file.transferTo(savedFile);
        return savedFile;
    }

    // 2️⃣ GET ALL SONGS
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    // 3️⃣ GET SONG BY ID
    public Song getSongById(UUID id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + id));
    }

    // 4️⃣ DELETE SONG (DB + file fisico)
    public void deleteSong(UUID id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + id));

        String filename = song.getTitolo();
        // Cerca file fisico (es: mp3)
        File file = new File("uploads/" + filename + ".mp3");
        if (file.exists()) {
            boolean deleted = file.delete();
            LOGGER.info(deleted ? "✅ File eliminato: " + file.getName() : "⚠ File non trovato: " + file.getName());
        } else {
            LOGGER.info("⚠ File non trovato (skip): " + file.getName());
        }

        songRepository.delete(song);
        LOGGER.info("✅ Canzone eliminata dal DB e dal file system: " + filename);
    }

    // 5️⃣ UPDATE SONG (titolo, rating, level, subgenre)
    public Song updateSong(UUID id,
                           String newTitle,
                           Integer newRating,
                           Integer newLevel,
                           Subgenre newSubgenre) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + id));

        if (newTitle != null && !newTitle.isBlank()) {
            song.setTitolo(newTitle);
        }
        if (newRating != null) {
            song.setRating(newRating);
        }
        if (newLevel != null) {
            song.setLevel(newLevel);
        }
        if (newSubgenre != null) {
            song.setSubgenre(newSubgenre);
        }

        Song updatedSong = songRepository.save(song);
        LOGGER.info("✅ Canzone aggiornata con successo: " + updatedSong.getId());
        return updatedSong;
    }
}
