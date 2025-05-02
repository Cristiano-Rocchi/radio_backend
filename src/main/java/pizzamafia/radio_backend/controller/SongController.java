package pizzamafia.radio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.enums.Subgenre;
import pizzamafia.radio_backend.payloads.SongRespDTO;
import pizzamafia.radio_backend.services.SongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private SongService songService;

    // 1️⃣ ADD SONGS (ritorna SongRespDTO)
    @PostMapping("/add/{albumId}")
    public ResponseEntity<List<SongRespDTO>> addSongs(
            @PathVariable UUID albumId,
            @RequestParam("songs") List<MultipartFile> songs) {

        List<SongRespDTO> addedSongs = songService.addSongs(albumId, songs);
        return new ResponseEntity<>(addedSongs, HttpStatus.CREATED);
    }

    // 2️⃣ GET ALL SONGS (ritorna SongRespDTO)
    @GetMapping
    public ResponseEntity<List<SongRespDTO>> getAllSongs() {
        List<SongRespDTO> songs = songService.getAllSongs();
        return ResponseEntity.ok(songs);
    }

    // 3️⃣ GET SONG BY ID (ritorna SongRespDTO)
    @GetMapping("/{id}")
    public ResponseEntity<SongRespDTO> getSongById(@PathVariable UUID id) {
        SongRespDTO song = songService.getSongById(id);
        return ResponseEntity.ok(song);
    }

    // 4️⃣ DELETE SONG
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable UUID id) {
        songService.deleteSong(id);
        return ResponseEntity.noContent().build();
    }

    // 5️⃣ UPDATE SONG
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSong(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Subgenre subgenre) {

        songService.updateSong(id, title, rating, level, subgenre);
        return ResponseEntity.ok("✅ Canzone aggiornata");
    }
}
