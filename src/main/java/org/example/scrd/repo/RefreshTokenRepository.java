package org.example.scrd.repo;

import org.example.scrd.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
