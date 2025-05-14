package pizzamafia.radio_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pizzamafia.radio_backend.payloads.NewPlaylistDTO;
import pizzamafia.radio_backend.payloads.PlaylistRespDTO;
import pizzamafia.radio_backend.services.PlaylistService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    // 🔹 CREA playlist
    @PostMapping
    public ResponseEntity<PlaylistRespDTO> createPlaylist(@RequestBody NewPlaylistDTO dto) {
        PlaylistRespDTO created = playlistService.createPlaylist(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // 🔹 GET tutte le playlist
    @GetMapping
    public ResponseEntity<List<PlaylistRespDTO>> getAllPlaylists() {
        List<PlaylistRespDTO> playlists = playlistService.getAllPlaylists();
        return ResponseEntity.ok(playlists);
    }

    // 🔹 GET playlist per ID
    @GetMapping("/{id}")
    public ResponseEntity<PlaylistRespDTO> getPlaylistById(@PathVariable Long id) {
        PlaylistRespDTO playlist = playlistService.getPlaylistById(id);
        return ResponseEntity.ok(playlist);
    }

    // 🔹 DELETE playlist
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{playlistId}/order")
    public ResponseEntity<Void> updatePlaylistOrder(
            @PathVariable Long playlistId,
            @RequestBody List<UUID> songIds
    ) {
        playlistService.updatePlaylistOrder(playlistId, songIds);
        return ResponseEntity.ok().build();
    }

}
