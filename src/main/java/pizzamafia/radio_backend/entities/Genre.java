package pizzamafia.radio_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import pizzamafia.radio_backend.enums.GenreType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "GENRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenreType name;

    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL)
    private List<Album> albums = new ArrayList<>();



}
