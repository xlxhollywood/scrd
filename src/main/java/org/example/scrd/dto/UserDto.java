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
    private String phoneNumber;
    private String profileImageUrl;
    private Integer income;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .kakaoId(user.getKakaoId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .profileImageUrl(user.getProfileImageUrl())
                .income(user.getIncome())
                .build();
    }

    public static ArrayList<UserDto> from(ArrayList<User> users) {
        ArrayList<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(UserDto.builder()
                    .id(user.getId())
                    .kakaoId(user.getKakaoId())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .profileImageUrl(user.getProfileImageUrl())
                    .income(user.getIncome())
                    .build());
        }
        return userDtos;
    }
}
