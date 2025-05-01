package pizzamafia.radio_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.enums.Subgenre;
import pizzamafia.radio_backend.services.SongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private SongService songService;

    // 1️⃣ ADD SONGS to an album
    @PostMapping("/add/{albumId}")
    public ResponseEntity<List<Song>> addSongs(
            @PathVariable UUID albumId,
            @RequestParam("songs") List<MultipartFile> songs) {

        List<Song> addedSongs = songService.addSongs(albumId, songs);
        return new ResponseEntity<>(addedSongs, HttpStatus.CREATED);
    }

    // 2️⃣ GET ALL SONGS
    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs() {
        List<Song> songs = songService.getAllSongs();
        return ResponseEntity.ok(songs);
    }

    // 3️⃣ GET SONG BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable UUID id) {
        Song song = songService.getSongById(id);
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
    public ResponseEntity<Song> updateSong(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Subgenre subgenre) {

        Song updatedSong = songService.updateSong(id, title, rating, level, subgenre);
        return ResponseEntity.ok(updatedSong);
    }
}

