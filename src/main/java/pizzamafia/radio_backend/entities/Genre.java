package pizzamafia.radio_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import pizzamafia.radio_backend.enums.GenreType;

import java.util.UUID;


@Entity
@Table(name = "GENRE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenreType name;


}
