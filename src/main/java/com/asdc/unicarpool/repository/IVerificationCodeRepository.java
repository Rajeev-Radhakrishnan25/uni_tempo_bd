package com.asdc.unicarpool.repository;

import com.asdc.unicarpool.model.User;
import com.asdc.unicarpool.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IVerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByUserIdAndStatusAndType(User userId, String status, String type);

}
