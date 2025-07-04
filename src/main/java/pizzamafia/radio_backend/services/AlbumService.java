package pizzamafia.radio_backend.services;

import com.mpatric.mp3agic.Mp3File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.config.BackblazeB2Config;
import pizzamafia.radio_backend.entities.Album;
import pizzamafia.radio_backend.entities.Genre;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.exceptions.InternalServerErrorException;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.payloads.AlbumRespDTO;
import pizzamafia.radio_backend.payloads.NewAlbumDTO;
import pizzamafia.radio_backend.payloads.SongRespDTO;
import pizzamafia.radio_backend.payloads.UpdateAlbumDTO;
import pizzamafia.radio_backend.repositories.AlbumRepository;
import pizzamafia.radio_backend.repositories.GenreRepository;
import pizzamafia.radio_backend.repositories.PlaylistSongRepository;
import pizzamafia.radio_backend.repositories.SongRepository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.*;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AlbumService {

    private static final Logger LOGGER = Logger.getLogger(AlbumService.class.getName());

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private Map<String, S3Client> backblazeAccounts;

    @Autowired
    private Map<String, String> keyIdMapping;

    @Autowired
    private BackblazeB2Config backblazeB2Config;

    @Autowired
    private PlaylistSongRepository playlistSongRepository;

    // 1️⃣ CREATE: crea album + carica canzoni su Backblaze
    public Album createAlbumFromUpload(NewAlbumDTO newAlbumDTO) {

        String albumName = newAlbumDTO.getTitle();
        String artist = newAlbumDTO.getArtist();
        Long genreId = newAlbumDTO.getGenreId();
        Integer albumDate = newAlbumDTO.getDate();
        List<MultipartFile> songs = newAlbumDTO.getSongs();

        if (albumName == null || albumName.isBlank()) {
            throw new BadRequestException("Il titolo dell'album è obbligatorio.");
        }
        if (songs == null || songs.isEmpty()) {
            throw new BadRequestException("Devi caricare almeno una traccia audio.");
        }

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Genere non trovato con ID: " + genreId));

        Album album = new Album();
        album.setTitle(albumName);
        album.setArtist(artist != null ? artist : "Unknown Artist");
        album.setGenre(genre);
        album.setDate(albumDate);
        album.setRating(0);

        albumRepository.save(album);

        for (MultipartFile file : songs) {
            try {
                String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
                String titolo = originalFilename.substring(0, originalFilename.lastIndexOf('.'));

                // Normalizza nome file
                String estensione = originalFilename.substring(originalFilename.lastIndexOf('.'));
                String normalizedFilename = titolo
                        .trim()
                        .replaceAll("\\s+", "-")
                        .replaceAll("[^a-zA-Z0-9\\-]", "")
                        + "-" + UUID.randomUUID() + estensione;

                // Salva temporaneamente il file originale
                File tempFile = File.createTempFile("upload_", estensione);
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(file.getBytes());
                }

                // 👇 Convertiamo se NON è già mp3
                File fileToUpload;
                if (!estensione.equalsIgnoreCase(".mp3")) {
                    LOGGER.info("🔄 Converto " + originalFilename + " in mp3...");
                    fileToUpload = convertToMp3(tempFile);
                    // Aggiorniamo il nome del file (ora è mp3)
                    normalizedFilename = titolo
                            .trim()
                            .replaceAll("\\s+", "-")
                            .replaceAll("[^a-zA-Z0-9\\-]", "")
                            + "-" + UUID.randomUUID() + ".mp3";
                } else {
                    fileToUpload = tempFile;
                }

                long fileSize = fileToUpload.length();

                // Trova il bucket giusto
                String bucketName = determineBucket(fileSize);
                LOGGER.info("📦 Bucket selezionato: " + bucketName);

                S3Client s3Client = backblazeAccounts.get(bucketName);
                if (s3Client == null) {
                    throw new InternalServerErrorException("❌ Nessun client S3 trovato per il bucket: " + bucketName);
                }

                // Upload su Backblaze
                PutObjectRequest uploadRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(normalizedFilename)
                        .contentType("audio/mpeg")  // mp3 MIME type
                        .build();

                s3Client.putObject(uploadRequest, RequestBody.fromFile(fileToUpload));

                // 👇 Calcola durata dopo la conversione (fileToUpload è sempre mp3)
                Integer duration = getAudioDurationInSeconds(fileToUpload);
                LOGGER.info("⏱️ Durata estratta: " + duration + " secondi");

// Crea Song nel DB
                Song song = new Song();
                song.setTitolo(titolo);
                song.setBucketName(bucketName);
                song.setFileName(normalizedFilename);
                song.setRating(0);
                song.setLevel(0);
                song.setSubgenre(null);
                song.setAlbum(album);
                song.setDuration(duration);

                songRepository.save(song);


                // Pulizia file temporanei
                tempFile.delete();
                if (fileToUpload != tempFile) {
                    fileToUpload.delete();
                }

            } catch (Exception e) {
                throw new InternalServerErrorException("Errore durante l'upload della traccia: " + e.getMessage());
            }
        }

        LOGGER.info("✅ Album creato con successo: " + albumName + " con " + songs.size() + " tracce.");
        return album;
    }


    // Converte in mp3
    private File convertToMp3(File inputFile) throws IOException, InterruptedException {
        String inputFileName = inputFile.getName();
        String outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf('.')) + ".mp3";
        File outputFile = new File(inputFile.getParent(), outputFileName);

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y", // overwrite output file if exists
                "-i", inputFile.getAbsolutePath(),
                "-vn", // no video
                "-ar", "44100", // set audio rate
                "-ac", "2", // set number of audio channels
                "-b:a", "192k", // set audio bitrate
                outputFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true); // merge error and output streams
        Process process = pb.start();

        // Log output di FFmpeg (opzionale)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Errore durante la conversione audio. Exit code: " + exitCode);
        }

        LOGGER.info("✅ Conversione completata: " + outputFile.getAbsolutePath());
        return outputFile;
    }

    // estrapola durata song
    private Integer getAudioDurationInSeconds(File audioFile) {
        try {
            Mp3File mp3file = new Mp3File(audioFile);
            return (int) mp3file.getLengthInSeconds();
        } catch (Exception e) {
            LOGGER.warning("⚠️ Impossibile leggere la durata del file audio: " + audioFile.getName());
            return null;
        }
    }


    // 🔍 Determina il bucket corretto
    private String determineBucket(long fileSize) {
        List<String> buckets = new ArrayList<>(keyIdMapping.keySet());

        if (buckets.isEmpty()) {
            throw new InternalServerErrorException("❌ Nessun bucket disponibile per l'upload!");
        }

        for (String bucket : buckets) {
            long usedSpace = getUsedStorage(bucket);
            long totalSpace = 10_000_000_000L; // 10 GB
            long availableSpace = totalSpace - usedSpace;

            LOGGER.info("📦 Bucket: " + bucket + " | Usato: " + usedSpace + " bytes | Disponibile: " + availableSpace + " bytes");

            if (fileSize <= availableSpace) {
                LOGGER.info("✅ Spazio sufficiente nel bucket " + bucket);
                return bucket;
            } else {
                LOGGER.warning("⚠ Spazio insufficiente in " + bucket + ": richiede " + fileSize + ", restano " + availableSpace);
            }
        }

        throw new InternalServerErrorException("❌ Nessun bucket ha spazio sufficiente per la traccia di " + fileSize + " bytes.");
    }

    // 🔄 Calcola lo spazio usato
    private long getUsedStorage(String bucketName) {
        S3Client s3Client = backblazeAccounts.get(bucketName);
        if (s3Client == null) {
            LOGGER.warning("⚠ Nessun client S3 trovato per il bucket: " + bucketName);
            return 0L;
        }

        try {
            long totalSize = s3Client.listObjectsV2(builder -> builder.bucket(bucketName))
                    .contents()
                    .stream()
                    .mapToLong(obj -> obj.size())
                    .sum();

            LOGGER.info("📦 Spazio usato nel bucket " + bucketName + ": " + totalSize + " bytes");
            return totalSize;
        } catch (Exception e) {
            LOGGER.warning("⚠ Errore nel recupero dello spazio usato per " + bucketName + ": " + e.getMessage());
            return 0L;
        }
    }

    // 🚀 Upload su Backblaze (solo upload, non restituisce URL perché i presigned vengono dopo)
    private void uploadToBackblazeB2(S3Client s3Client, String bucketName, File file, String newFileName) throws IOException {
        LOGGER.info("🚀 Inizio caricamento file: " + newFileName + " su Backblaze (Bucket: " + bucketName + ")");

        PutObjectRequest uploadRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(newFileName)
                .contentType("audio/mp4")
                .build();


        s3Client.putObject(uploadRequest, RequestBody.fromFile(file));

        LOGGER.info("✅ Upload completato: " + newFileName);
    }

    // ✅ GET ALL con presigned URL
    public List<AlbumRespDTO> getAllAlbumsWithPresignedUrls() {
        List<Album> albums = albumRepository.findAll();

        return albums.stream().map(album -> {
            List<SongRespDTO> songDtos = album.getSongs().stream().map(song -> {
                String presignedUrl = generatePresignedUrl(song.getBucketName(), song.getFileName());
                return new SongRespDTO(
                        song.getId(),
                        song.getTitolo(),
                        presignedUrl,
                        song.getBucketName(),
                        song.getDuration(),
                        song.getRating(),
                        song.getLevel(),
                        song.getAlbum().getId(),
                        song.getAlbum().getTitle(),
                        song.getAlbum().getTitle(),
                        playlistSongRepository.countBySongId(song.getId())
                );
            }).collect(Collectors.toList());

            return new AlbumRespDTO(
                    album.getId(),
                    album.getTitle(),
                    album.getArtist(),
                    album.getRating(),
                    album.getDate(),
                    album.getGenre().getId(),
                    songDtos
            );
        }).collect(Collectors.toList());
    }



    // ✅ GET ONE con presigned URL
    public AlbumRespDTO getAlbumByIdWithPresignedUrls(UUID id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + id));

        List<SongRespDTO> songDtos = album.getSongs().stream().map(song -> {
            String presignedUrl = generatePresignedUrl(song.getBucketName(), song.getFileName());
            return new SongRespDTO(
                    song.getId(),
                    song.getTitolo(),
                    presignedUrl,
                    song.getBucketName(),
                    song.getDuration(),
                    song.getRating(),
                    song.getLevel(),
                    song.getAlbum().getId(),
                    song.getAlbum().getTitle(),
                    song.getAlbum().getArtist(),
                    playlistSongRepository.countBySongId(song.getId())
            );
        }).collect(Collectors.toList());

        return new AlbumRespDTO(
                album.getId(),
                album.getTitle(),
                album.getArtist(),
                album.getRating(),
                album.getDate(),
                album.getGenre().getId(),
                songDtos
        );
    }



    // 🔑 Genera URL firmato
    public String generatePresignedUrl(String bucketName, String fileName) {
        String keyId = backblazeB2Config.keyIdMapping().get(bucketName);
        String applicationKey = backblazeB2Config.applicationKeyMapping().get(bucketName);

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create("https://s3.us-east-005.backblazeb2.com"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(keyId, applicationKey)))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(r ->
                    r.signatureDuration(Duration.ofHours(6))
                            .getObjectRequest(getObjectRequest));

            return presignedRequest.url().toString();
        }
    }
    // 4️⃣ DELETE ALBUM (elimina anche le tracce fisiche da Backblaze)
    public void deleteAlbum(UUID id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + id));

        List<Song> songs = album.getSongs(); // Assicurati che Album abbia la relazione mappedBy "songs"

        for (Song song : songs) {
            String bucketName = song.getBucketName();
            String fileName = song.getFileName();
            LOGGER.info("🗑 Eliminazione file da Backblaze: bucket=" + bucketName + ", file=" + fileName);

            try {
                S3Client s3Client = backblazeAccounts.get(bucketName);
                if (s3Client != null) {
                    s3Client.deleteObject(builder -> builder.bucket(bucketName).key(fileName));
                    LOGGER.info("✅ File eliminato da Backblaze: " + fileName);
                } else {
                    LOGGER.warning("⚠ Nessun S3Client trovato per il bucket: " + bucketName + ". Skip eliminazione fisica.");
                }
            } catch (Exception e) {
                LOGGER.warning("⚠ Errore durante l'eliminazione del file " + fileName + ": " + e.getMessage());
            }
        }

        albumRepository.delete(album);
        LOGGER.info("✅ Album eliminato: " + album.getTitle() + " (e tutte le sue tracce)");
    }

    // 5️⃣ UPDATE ALBUM (mantiene le tracce intatte)
    public void updateAlbum(UUID id, UpdateAlbumDTO dto) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + id));

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            album.setTitle(dto.getTitle());
        }
        if (dto.getArtist() != null && !dto.getArtist().isBlank()) {
            album.setArtist(dto.getArtist());
        }
        if (dto.getDate() != null) {
            album.setDate(dto.getDate());
        }

        // 🔜 In futuro:
        // if (dto.getSongsToRemove() != null) { ... }
        // if (dto.getNewSongs() != null) { ... }

        albumRepository.save(album);
        LOGGER.info("✅ Album aggiornato con successo: " + album.getId());
    }


    //cerca per titolo album
    public List<AlbumRespDTO> searchAlbumsByTitle(String query) {
        List<Album> albums = albumRepository.findByTitleContainingIgnoreCase(query);

        return albums.stream().map(album -> {
            List<SongRespDTO> songDtos = album.getSongs().stream().map(song -> {
                String presignedUrl = generatePresignedUrl(song.getBucketName(), song.getFileName());
                return new SongRespDTO(
                        song.getId(),
                        song.getTitolo(),
                        presignedUrl,
                        song.getBucketName(),
                        song.getDuration(),
                        song.getRating(),
                        song.getLevel(),
                        song.getAlbum().getId(),
                        song.getAlbum().getTitle(),
                        song.getAlbum().getArtist(),
                        playlistSongRepository.countBySongId(song.getId())
                );
            }).toList();

            return new AlbumRespDTO(
                    album.getId(),
                    album.getTitle(),
                    album.getArtist(),
                    album.getRating(),
                    album.getDate(),
                    album.getGenre().getId(),
                    songDtos
            );
        }).toList();
    }

    //cerca per artista
    public List<AlbumRespDTO> searchAlbumsByArtist(String artist) {
        List<Album> albums = albumRepository.findByArtistContainingIgnoreCase(artist);

        return albums.stream().map(album -> {
            List<SongRespDTO> songDtos = album.getSongs().stream().map(song -> {
                String presignedUrl = generatePresignedUrl(song.getBucketName(), song.getFileName());
                return new SongRespDTO(
                        song.getId(),
                        song.getTitolo(),
                        presignedUrl,
                        song.getBucketName(),
                        song.getDuration(),
                        song.getRating(),
                        song.getLevel(),
                        song.getAlbum().getId(),
                        song.getAlbum().getTitle(),
                        song.getAlbum().getArtist(),
                        playlistSongRepository.countBySongId(song.getId())
                );
            }).toList();

            return new AlbumRespDTO(
                    album.getId(),
                    album.getTitle(),
                    album.getArtist(),
                    album.getRating(),
                    album.getDate(),
                    album.getGenre().getId(),
                    songDtos
            );
        }).toList();
    }

    public List<String> getAllArtists() {
        return albumRepository.findDistinctArtists();
    }





    //ricerca per titolo album e artista
    public List<AlbumRespDTO> searchAlbumsByTitleAndArtist(String title, String artist) {
        List<Album> albums = albumRepository.findByTitleContainingIgnoreCaseAndArtistContainingIgnoreCase(title, artist);

        return albums.stream().map(album -> {
            List<SongRespDTO> songDtos = album.getSongs().stream().map(song -> {
                String presignedUrl = generatePresignedUrl(song.getBucketName(), song.getFileName());
                return new SongRespDTO(
                        song.getId(),
                        song.getTitolo(),
                        presignedUrl,
                        song.getBucketName(),
                        song.getDuration(),
                        song.getRating(),
                        song.getLevel(),
                        song.getAlbum().getId(),
                        song.getAlbum().getTitle(),
                        song.getAlbum().getArtist(),
                        playlistSongRepository.countBySongId(song.getId())
                );
            }).toList();

            return new AlbumRespDTO(
                    album.getId(),
                    album.getTitle(),
                    album.getArtist(),
                    album.getRating(),
                    album.getDate(),
                    album.getGenre().getId(),
                    songDtos
            );
        }).toList();
    }



}
