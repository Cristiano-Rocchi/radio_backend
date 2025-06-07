package pizzamafia.radio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzamafia.radio_backend.payloads.GenreRespDTO;
import pizzamafia.radio_backend.payloads.NewGenreDTO;
import pizzamafia.radio_backend.services.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genre")
public class GenreController {

    @Autowired
    private GenreService genreService;

    // 1️⃣ CREATE GENRE
    @PostMapping
    public ResponseEntity<GenreRespDTO> createGenre(@RequestBody NewGenreDTO newGenreDTO) {
        GenreRespDTO createdGenre = genreService.createGenre(newGenreDTO);
        return new ResponseEntity<>(createdGenre, HttpStatus.CREATED);
    }

    // 2️⃣ GET ALL GENRES
    @GetMapping
    public ResponseEntity<List<GenreRespDTO>> getAllGenres() {
        List<GenreRespDTO> genres = genreService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    // 3️⃣ GET GENRE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<GenreRespDTO> getGenreById(@PathVariable Long id) {
        GenreRespDTO genre = genreService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    // 4️⃣ UPDATE GENRE
    @PutMapping("/{id}")
    public ResponseEntity<GenreRespDTO> updateGenre(@PathVariable Long id, @RequestBody NewGenreDTO updatedData) {
        GenreRespDTO updatedGenre = genreService.updateGenre(id, updatedData);
        return ResponseEntity.ok(updatedGenre);
    }

    // 5️⃣ DELETE GENRE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}
