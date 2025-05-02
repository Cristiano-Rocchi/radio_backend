package pizzamafia.radio_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import pizzamafia.radio_backend.enums.Subgenre;

import java.util.UUID;

@Entity
@Table(name = "SONG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false)
    private String titolo;

    @Column
    private int rating;

    @Column
    private int level;

    @Enumerated(EnumType.STRING)
    @Column
    private Subgenre subgenre;

    @ManyToOne
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;


    @Column
    private String bucketName;

    @Column
    private String fileName;

}
