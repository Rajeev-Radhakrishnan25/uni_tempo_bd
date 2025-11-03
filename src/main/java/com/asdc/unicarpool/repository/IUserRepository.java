package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByBannerId(String bannerId);

    Optional<User> findByBannerIdAndEmailVerifiedIsFalse(String bannerId);

    boolean existsByEmail(String email);

    boolean existsByBannerId(String bannerId);


}
