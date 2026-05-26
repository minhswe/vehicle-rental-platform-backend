package com.rentalplatform.backend.user.repository;

import com.rentalplatform.backend.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID>{
    List<Address> findByUserId(UUID userId);

    List<Address> findByUserIdOrderByIsDefaultDesc(UUID userId);

    Optional<Address> findByIdAndUserId(UUID id, UUID userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(UUID userId);

    Optional<Address> findFirstByUserIdOrderByIdAsc(UUID userId);

    @Modifying
    @Query("""
        UPDATE Address a
        SET a.isDefault = false
        WHERE a.userId = :userId
    """)
    void clearDefaultAddresses(@Param("userId") UUID userId);

    long countByUserId(UUID userId);
}
