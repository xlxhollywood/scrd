package org.example.scrd.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.scrd.dto.UserDto;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long kakaoId;

    @Setter
    @Column(columnDefinition = "varchar(200)")
    private String name;

    @Setter
    @Column(columnDefinition = "varchar(30)")
    private String email;

    @Setter
    @Column(columnDefinition = "varchar(500)")
    private String profileImageUrl;

    @Setter
    private Integer income;

    public static User from(UserDto dto){
        return User.builder()
                .kakaoId(dto.getKakaoId())
                .name(dto.getName())
                .email(dto.getEmail())
                .profileImageUrl(dto.getProfileImageUrl())
                .income(dto.getIncome())
                .build();
    }

}
