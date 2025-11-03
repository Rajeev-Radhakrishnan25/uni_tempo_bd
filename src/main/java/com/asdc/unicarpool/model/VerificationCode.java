package com.asdc.unicarpool.model;

import com.asdc.unicarpool.constant.AppConstant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "verification_codes")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(name = "code", nullable = false)
    private Integer code;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = AppConstant.VerificationStatus.ACTIVE.name();

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "expire_at", nullable = false)
    private Instant expireAt;
}
