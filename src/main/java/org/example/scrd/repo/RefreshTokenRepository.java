package org.example.scrd.repo;

import org.example.scrd.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {


}
