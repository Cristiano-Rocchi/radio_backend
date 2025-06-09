package pizzamafia.radio_backend.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pizzamafia.radio_backend.entities.Playlist;
import pizzamafia.radio_backend.entities.PlaylistSong;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.payloads.NewPlaylistDTO;
import pizzamafia.radio_backend.payloads.PlaylistRespDTO;
import pizzamafia.radio_backend.payloads.SongRespDTO;
import pizzamafia.radio_backend.repositories.PlaylistRepository;
import pizzamafia.radio_backend.repositories.PlaylistSongRepository;
import pizzamafia.radio_backend.repositories.SongRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongService songService;

    @Autowired
    private PlaylistSongRepository playlistSongRepository;


    // 🔹 CREA playlist
    public PlaylistRespDTO createPlaylist(NewPlaylistDTO dto) {
        List<UUID> songIds = dto.getSongIds();

        Playlist playlist = new Playlist();
        playlist.setName(dto.getName());

        // 🔁 Costruisci PlaylistSong nell'ordine ricevuto
        List<PlaylistSong> playlistSongs = new ArrayList<>();
        for (int i = 0; i < songIds.size(); i++) {
            UUID songId = songIds.get(i);
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + songId));

            PlaylistSong ps = new PlaylistSong();
            ps.setPlaylist(playlist);
            ps.setSong(song);
            ps.setPosition(i); // ✅ Mantieni l'ordine esatto
            playlistSongs.add(ps);
        }

        playlist.setPlaylistSongs(playlistSongs);

        Playlist saved = playlistRepository.save(playlist);
        playlistSongRepository.saveAll(playlistSongs);

        return toRespDTO(saved);
    }



    // 🔹 GET playlist by ID
    public PlaylistRespDTO getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Playlist non trovata con ID: " + id));

        return toRespDTO(playlist);
    }

    // 🔹 GET tutte le playlist
    public List<PlaylistRespDTO> getAllPlaylists() {
        return playlistRepository.findAll().stream()
                .map(this::toRespDTO)
                .collect(Collectors.toList());
    }

    // 🔹 DELETE playlist
    @Transactional
    public void deletePlaylist(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Playlist non trovata con ID: " + id));

        // Carica esplicitamente tutti i PlaylistSong
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistOrderByPositionAsc(playlist);

        // Elimina direttamente i PlaylistSong associati
        playlistSongRepository.deleteAll(playlistSongs);

        // Elimina la playlist
        playlistRepository.delete(playlist);
    }


    @Transactional
    public void removeSongFromPlaylist(Long playlistId, UUID songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist non trovata"));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata"));

        playlist.getPlaylistSongs().removeIf(ps -> ps.getSong().equals(song));

        playlistRepository.save(playlist);
    }





    // mapping interno
    private PlaylistRespDTO toRespDTO(Playlist playlist) {
        List<SongRespDTO> tracks = playlist.getPlaylistSongs().stream()
                .sorted((ps1, ps2) -> Integer.compare(ps1.getPosition(), ps2.getPosition()))
                .map(ps -> {
                    Song song = ps.getSong();
                    return new SongRespDTO(
                            song.getId(),
                            song.getTitolo(),
                            songService.generatePresignedUrl(song.getBucketName(), song.getFileName()),
                            song.getBucketName(),
                            song.getDuration(),
                            song.getRating(),
                            song.getLevel(),
                            song.getAlbum().getId(),
                            song.getAlbum().getTitle(),
                            song.getAlbum().getArtist(),
                            playlistSongRepository.countBySongId(song.getId())
                    );
                })
                .toList();


        return new PlaylistRespDTO(playlist.getId(), playlist.getName(), tracks);
    }

    // 🔹 MODIFICA NOME playlist
    public void updatePlaylistName(Long id, String name) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Playlist non trovata"));
        playlist.setName(name);
        playlistRepository.save(playlist);
    }

    // 🔹 Aggiunge Song
    public void addSongsToPlaylist(Long playlistId, List<UUID> songIds) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist non trovata"));

        int startPosition = playlist.getPlaylistSongs().size();

        for (int i = 0; i < songIds.size(); i++) {
            UUID songId = songIds.get(i);
            Song song = songRepository.findById(songId)
                    .orElseThrow(() -> new NotFoundException("Canzone non trovata"));

            PlaylistSong ps = new PlaylistSong();
            ps.setPlaylist(playlist);
            ps.setSong(song);
            ps.setPosition(startPosition + i);
            playlist.getPlaylistSongs().add(ps);
        }

        playlistRepository.save(playlist);
    }


    // 🔹MODIFICA ORDNE
    public void updatePlaylistOrder(Long playlistId, List<UUID> orderedSongIds) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist non trovata"));

        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylistOrderByPositionAsc(playlist);

        // Mappa per accesso rapido
        Map<UUID, PlaylistSong> songIdToPs = playlistSongs.stream()
                .collect(Collectors.toMap(ps -> ps.getSong().getId(), ps -> ps));

        for (int i = 0; i < orderedSongIds.size(); i++) {
            UUID songId = orderedSongIds.get(i);
            PlaylistSong ps = songIdToPs.get(songId);
            if (ps != null) {
                ps.setPosition(i);
            }
        }

        playlistSongRepository.saveAll(playlistSongs);
    }

}
