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
    private Long id;

    private Long kakaoId;

    @Setter
    @Column(columnDefinition = "varchar(200)")
    private String name;

    @Setter
    @Column(columnDefinition = "varchar(30)")
    private String email;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String profileImageUrl;

    private String tier;
    private String gender;
    private String birth;

    // ***추후 성별, 생일 받으면 빌더 타입 수정해야함.
    public static User from(UserDto dto){
        return User.builder()
                .kakaoId(dto.getKakaoId())
                .name(dto.getName())
                .email(dto.getEmail())
                .profileImageUrl(dto.getProfileImageUrl())
                .build();
    }

}
