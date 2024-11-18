package org.example.scrd.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.scrd.dto.UserDto;

@Entity
@Getter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private Long kakaoId;

    @Setter
    @Column(columnDefinition = "varchar(200)")
    private String name;

    @Setter
    @Column(columnDefinition = "varchar(20)")
    private String phoneNumber;

    @Setter
    @Column(columnDefinition = "varchar(500)")
    private String profileImageUrl;

    @Setter
    private Integer income;

    public static User from(UserDto dto){
        return User.builder()
                .kakaoId(dto.getKakaoId())
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .profileImageUrl(dto.getProfileImageUrl())
                .income(dto.getIncome())
                .build();
    }

}
