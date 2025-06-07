package pizzamafia.radio_backend.entities;

import jakarta.persistence.*;
import lombok.*;


import java.util.ArrayList;
import java.util.List;



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


    @Column(nullable = false)
    private String name;


    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL)
    private List<Album> albums = new ArrayList<>();



}
