package pizzamafia.radio_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.entities.Album;
import pizzamafia.radio_backend.payloads.AlbumRespDTO;
import pizzamafia.radio_backend.payloads.NewAlbumDTO;
import pizzamafia.radio_backend.services.AlbumService;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    // 1️⃣ CREATE ALBUM (upload album + tracce)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Album> createAlbum(
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam Long genreId,
            @RequestParam(value = "date", required = false) Integer date,
            @RequestParam("songs") List<MultipartFile> songs) {

        // Assembla il DTO a mano
        NewAlbumDTO albumDTO = new NewAlbumDTO();
        albumDTO.setTitle(title);
        albumDTO.setArtist(artist);
        albumDTO.setGenreId(genreId);
        albumDTO.setDate(date);
        albumDTO.setSongs(songs);

        Album createdAlbum = albumService.createAlbumFromUpload(albumDTO);
        return new ResponseEntity<>(createdAlbum, HttpStatus.CREATED);
    }




    // 2️⃣ GET ALL ALBUMS (con URL presigned)
    @GetMapping
    public ResponseEntity<List<AlbumRespDTO>> getAlbums(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String artist
    ) {
        List<AlbumRespDTO> albums;

        if (title != null && !title.isBlank() && artist != null && !artist.isBlank()) {
            // ricerca combinata
            albums = albumService.searchAlbumsByTitleAndArtist(title, artist);
        } else if (title != null && !title.isBlank()) {
            albums = albumService.searchAlbumsByTitle(title);
        } else if (artist != null && !artist.isBlank()) {
            albums = albumService.searchAlbumsByArtist(artist);
        } else {
            albums = albumService.getAllAlbumsWithPresignedUrls();
        }

        return ResponseEntity.ok(albums);
    }




    // 3️⃣ GET ALBUM BY ID (con URL presigned)
    @GetMapping("/{id}")
    public ResponseEntity<AlbumRespDTO> getAlbumById(@PathVariable UUID id) {
        AlbumRespDTO album = albumService.getAlbumByIdWithPresignedUrls(id);
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
