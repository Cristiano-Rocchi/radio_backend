package pizzamafia.radio_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.entities.Album;
import pizzamafia.radio_backend.services.AlbumService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    // 1️⃣ CREATE ALBUM (upload album + tracce)
    @PostMapping
    public ResponseEntity<Album> createAlbum(
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam UUID genreId,
            @RequestParam("songs") List<MultipartFile> songs) {

        Album createdAlbum = albumService.createAlbumFromUpload(title, artist, genreId, songs);
        return new ResponseEntity<>(createdAlbum, HttpStatus.CREATED);
    }

    // 2️⃣ GET ALL ALBUMS
    @GetMapping
    public ResponseEntity<List<Album>> getAllAlbums() {
        List<Album> albums = albumService.getAllAlbums();
        return ResponseEntity.ok(albums);
    }

    // 3️⃣ GET ALBUM BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Album> getAlbumById(@PathVariable UUID id) {
        Album album = albumService.getAlbumById(id);
        return ResponseEntity.ok(album);
    }

    // 4️⃣ DELETE ALBUM
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable UUID id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.noContent().build();
    }

    // 5️⃣ UPDATE ALBUM
    @PutMapping("/{id}")
    public ResponseEntity<Album> updateAlbum(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Integer rating) {

        Album updatedAlbum = albumService.updateAlbum(id, title, artist, rating);
        return ResponseEntity.ok(updatedAlbum);
    }
}

