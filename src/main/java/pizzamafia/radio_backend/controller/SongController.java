package pizzamafia.radio_backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.enums.Subgenre;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.payloads.NewSongDTO;
import pizzamafia.radio_backend.payloads.SongRespDTO;
import pizzamafia.radio_backend.payloads.UpdateSongDTO;
import pizzamafia.radio_backend.services.SongService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/song")
public class SongController {

    @Autowired
    private SongService songService;

    // 1️⃣ ADD SONGS (ritorna SongRespDTO)
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<List<SongRespDTO>> addSongs(
            @Valid @ModelAttribute NewSongDTO newSongDTO
    ) {
        List<SongRespDTO> createdSongs = songService.addSongs(newSongDTO);
        return new ResponseEntity<>(createdSongs, HttpStatus.CREATED);
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
            @RequestBody UpdateSongDTO dto) {

        if (dto.getRating() != null && (dto.getRating() < 0 || dto.getRating() > 10)) {
            throw new BadRequestException("Il rating deve essere compreso tra 0 e 10.");
        }
        if (dto.getLevel() != null && (dto.getLevel() < 0 || dto.getLevel() > 100)) {
            throw new BadRequestException("Il level deve essere compreso tra 0 e 100.");
        }

        songService.updateSong(
                id,
                dto.getTitolo(),
                dto.getRating(),
                dto.getLevel(),
                dto.getSubgenre()
        );
        return ResponseEntity.ok("✅ Canzone aggiornata");
    }



}
