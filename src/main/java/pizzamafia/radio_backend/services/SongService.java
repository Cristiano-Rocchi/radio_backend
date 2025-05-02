package pizzamafia.radio_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pizzamafia.radio_backend.config.BackblazeB2Config;
import pizzamafia.radio_backend.entities.Album;
import pizzamafia.radio_backend.entities.Song;
import pizzamafia.radio_backend.enums.Subgenre;
import pizzamafia.radio_backend.exceptions.BadRequestException;
import pizzamafia.radio_backend.exceptions.InternalServerErrorException;
import pizzamafia.radio_backend.exceptions.NotFoundException;
import pizzamafia.radio_backend.payloads.SongRespDTO;
import pizzamafia.radio_backend.repositories.AlbumRepository;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class SongService {

    private static final Logger LOGGER = Logger.getLogger(SongService.class.getName());

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private Map<String, S3Client> backblazeAccounts;

    @Autowired
    private Map<String, String> keyIdMapping;

    @Autowired
    private BackblazeB2Config backblazeB2Config;

    // 1️⃣ ADD SONGS (aggiunge nuove tracce a un album esistente)
    public List<SongRespDTO> addSongs(UUID albumId, List<MultipartFile> songs) {

        if (songs == null || songs.isEmpty()) {
            throw new BadRequestException("Devi caricare almeno una traccia audio.");
        }

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new NotFoundException("Album non trovato con ID: " + albumId));

        List<SongRespDTO> savedSongs = new ArrayList<>();

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

                // Scrivi temporaneamente su disco
                File tempFile = File.createTempFile("upload_", null);
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(file.getBytes());
                }

                long fileSize = tempFile.length();
                List<String> buckets = new ArrayList<>(keyIdMapping.keySet());

                boolean uploaded = false;
                String bucketUsed = null;
                for (String bucket : buckets) {
                    long spazioUsato = getUsedStorage(bucket);
                    long spazioTotale = 10_000_000_000L;
                    long spazioDisponibile = spazioTotale - spazioUsato;

                    LOGGER.info("📦 Bucket: " + bucket + " | Usato: " + spazioUsato + " | Disponibile: " + spazioDisponibile);

                    if (fileSize <= spazioDisponibile) {
                        S3Client s3Client = backblazeAccounts.get(bucket);
                        if (s3Client == null) {
                            throw new InternalServerErrorException("❌ Nessun S3Client per bucket: " + bucket);
                        }

                        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(normalizedFilename)
                                .contentType("audio/mp4")
                                .build();


                        s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempFile));

                        LOGGER.info("✅ Caricato su bucket: " + bucket + " | file: " + normalizedFilename);
                        uploaded = true;
                        bucketUsed = bucket;
                        break;
                    } else {
                        LOGGER.warning("⚠ Spazio insufficiente su bucket " + bucket);
                    }
                }

                if (!uploaded) {
                    throw new InternalServerErrorException("❌ Nessun bucket ha spazio sufficiente per: " + normalizedFilename);
                }

                // Salva la canzone nel DB
                Song song = new Song();
                song.setTitolo(titolo);
                song.setRating(0);
                song.setLevel(0);
                song.setSubgenre(null);
                song.setAlbum(album);
                // Salva info bucket e fileName per ricreare il link firmato
                song.setBucketName(bucketUsed);
                song.setFileName(normalizedFilename);

                Song saved = songRepository.save(song);
                String presignedUrl = generatePresignedUrl(bucketUsed, normalizedFilename);

                savedSongs.add(new SongRespDTO(saved.getId(), saved.getTitolo(), presignedUrl));

                tempFile.delete();

            } catch (Exception e) {
                throw new InternalServerErrorException("Errore nel salvataggio su Backblaze: " + e.getMessage());
            }
        }

        LOGGER.info("✅ Aggiunte " + savedSongs.size() + " tracce all'album: " + album.getTitle());
        return savedSongs;
    }

    private long getUsedStorage(String bucketName) {
        S3Client s3Client = backblazeAccounts.get(bucketName);
        if (s3Client == null) {
            LOGGER.warning("⚠ Nessun S3Client trovato per: " + bucketName);
            return 0L;
        }
        try {
            return s3Client.listObjectsV2(builder -> builder.bucket(bucketName))
                    .contents()
                    .stream()
                    .mapToLong(obj -> obj.size())
                    .sum();
        } catch (Exception e) {
            LOGGER.warning("⚠ Errore nel recupero spazio su bucket " + bucketName + ": " + e.getMessage());
            return 0L;
        }
    }

    // 2️⃣ GET ALL SONGS (con presigned URL)
    public List<SongRespDTO> getAllSongs() {
        return songRepository.findAll().stream()
                .map(song -> new SongRespDTO(
                        song.getId(),
                        song.getTitolo(),
                        generatePresignedUrl(song.getBucketName(), song.getFileName())
                ))
                .collect(Collectors.toList());
    }

    // 3️⃣ GET SONG BY ID (con presigned URL)
    public SongRespDTO getSongById(UUID id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + id));

        return new SongRespDTO(
                song.getId(),
                song.getTitolo(),
                generatePresignedUrl(song.getBucketName(), song.getFileName())
        );
    }

    // 4️⃣ DELETE SONG
    public void deleteSong(UUID id) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + id));
        songRepository.delete(song);
        LOGGER.info("✅ Canzone eliminata dal DB: " + song.getTitolo());
    }

    // 5️⃣ UPDATE SONG
    public Song updateSong(UUID id,
                           String newTitle,
                           Integer newRating,
                           Integer newLevel,
                           Subgenre newSubgenre) {

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Canzone non trovata con ID: " + id));

        if (newTitle != null && !newTitle.isBlank()) {
            song.setTitolo(newTitle);
        }
        if (newRating != null) {
            song.setRating(newRating);
        }
        if (newLevel != null) {
            song.setLevel(newLevel);
        }
        if (newSubgenre != null) {
            song.setSubgenre(newSubgenre);
        }

        Song updatedSong = songRepository.save(song);
        LOGGER.info("✅ Canzone aggiornata: " + updatedSong.getId());
        return updatedSong;
    }

    public String generatePresignedUrl(String bucketName, String fileName) {
        if (bucketName == null || fileName == null) {
            return null;
        }
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
                    r.signatureDuration(Duration.ofHours(1))
                            .getObjectRequest(getObjectRequest));

            return presignedRequest.url().toString();
        }
    }
}
