package org.example.scrd.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.scrd.domain.User;

import java.util.ArrayList;

@Getter
@Builder
public class UserDto {
    private Long id;
    private Long kakaoId;
    private String name;
    private String email;
    private String profileImageUrl;
    private String tier;
    private String gender;
    private String birth;

    // ***추후 성별, 생일 받으면 빌더 타입 수정해야함.
    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    public static ArrayList<UserDto> from(ArrayList<User> users) {
        ArrayList<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(UserDto.builder()
                    .id(user.getId())
                    .kakaoId(user.getKakaoId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build());
        }
        return userDtos;
    }
}
