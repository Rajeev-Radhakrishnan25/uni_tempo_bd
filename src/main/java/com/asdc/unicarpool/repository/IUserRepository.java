package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByBannerId(String bannerId);

    Optional<User> findByBannerIdAndEmailVerifiedIsFalse(String bannerId);

    boolean existsByEmail(String email);

    boolean existsByBannerId(String bannerId);

    List<User> findByRolesContainingAndIsAvailableTrue(UserRole role);
}
