package pizzamafia.radio_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pizzamafia.radio_backend.entities.Playlist;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.payloads.NewPlaylistDTO;
import pizzamafia.radio_backend.payloads.PlaylistRespDTO;
import pizzamafia.radio_backend.payloads.SongRespDTO;
import pizzamafia.radio_backend.repositories.PlaylistRepository;
import pizzamafia.radio_backend.repositories.SongRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongService songService;


    // 🔹 CREA playlist
    public PlaylistRespDTO createPlaylist(NewPlaylistDTO dto) {
        List<Song> songs = songRepository.findAllById(dto.getSongIds());

        Playlist playlist = new Playlist();
        playlist.setName(dto.getName());
        playlist.setSongs(songs);

        Playlist saved = playlistRepository.save(playlist);

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
    public void deletePlaylist(Long id) {
        if (!playlistRepository.existsById(id)) {
            throw new NotFoundException("Playlist non trovata con ID: " + id);
        }
        playlistRepository.deleteById(id);
    }

    // 🔁 mapping interno
    private PlaylistRespDTO toRespDTO(Playlist playlist) {
        List<SongRespDTO> tracks = playlist.getSongs().stream()
                .map(song -> new SongRespDTO(
                        song.getId(),
                        song.getTitolo(),
                        songService.generatePresignedUrl(song.getBucketName(), song.getFileName()),
                        song.getBucketName(),
                        song.getDuration(),
                        song.getRating(),
                        song.getLevel(),
                        song.getAlbum().getId()
                ))
                .toList();

        return new PlaylistRespDTO(playlist.getId(), playlist.getName(), tracks);
    }

}
