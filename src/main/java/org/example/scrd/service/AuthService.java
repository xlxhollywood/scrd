package org.example.scrd.service;


import lombok.RequiredArgsConstructor;
import org.example.scrd.domain.User;
import org.example.scrd.dto.UserDto;
import org.example.scrd.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    // 카카오 로그인 로직
    public UserDto kakaoLogin(UserDto dto) {
        // 카카오 ID로 기존 사용자를 조회, 없으면 새로운 사용자 생성
        User user =
                userRepository
                        .findByKakaoId(dto.getKakaoId()) // 카카오 ID로 사용자를 조회
                        .orElseGet(() -> userRepository.save(User.from(dto))); // 없으면 새로운 사용자 저장
        user.setEmail(dto.getEmail());
        user.setProfileImageUrl(dto.getProfileImageUrl());  // 프로필 이미지를 업데이트
        user.setName(dto.getName());  // 이름 업데이트

        // 업데이트된 사용자 정보를 UserDto로 변환하여 반환
        return UserDto.from(user);
    }

    // 사용자 ID로 로그인한 사용자 정보 조회
    public User getLoginUser(Long userId) {
        // 사용자 ID로 사용자를 조회, 없으면 예외 발생
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
    }
}

