package pizzamafia.radio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzamafia.radio_backend.entities.Genre;
import pizzamafia.radio_backend.services.GenreService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/genre")
public class GenreController {

    @Autowired
    private GenreService genreService;

    // 1️⃣ CREATE GENRE
    @PostMapping
    public ResponseEntity<Genre> createGenre(@RequestBody Genre genre) {
        Genre createdGenre = genreService.createGenre(genre);
        return new ResponseEntity<>(createdGenre, HttpStatus.CREATED);
    }

    // 2️⃣ GET ALL GENRES
    @GetMapping
    public ResponseEntity<List<Genre>> getAllGenres() {
        List<Genre> genres = genreService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    // 3️⃣ GET GENRE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Genre> getGenreById(@PathVariable UUID id) {
        Genre genre = genreService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    // 4️⃣ UPDATE GENRE
    @PutMapping("/{id}")
    public ResponseEntity<Genre> updateGenre(@PathVariable UUID id, @RequestBody Genre updatedData) {
        Genre updatedGenre = genreService.updateGenre(id, updatedData);
        return ResponseEntity.ok(updatedGenre);
    }

    // 5️⃣ DELETE GENRE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}
