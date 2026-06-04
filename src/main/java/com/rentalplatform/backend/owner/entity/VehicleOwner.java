package com.rentalplatform.backend.owner.entity;

import com.rentalplatform.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "vehicle_owners")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    @Column(nullable = false)
    private String businessName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String verifiedStatus;

    @Column(nullable = false)
    private BigDecimal ratingAvg;

    @Column(nullable = false)
    private Integer totalVehicles;

    @CreationTimestamp
    private Instant createdAt;
}
