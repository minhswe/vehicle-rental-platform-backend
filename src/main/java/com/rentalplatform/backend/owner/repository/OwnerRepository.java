package com.rentalplatform.backend.owner.repository;

import com.rentalplatform.backend.owner.entity.VehicleOwner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<VehicleOwner, UUID> {
    Optional<VehicleOwner> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Query("""
    select o.id
    from VehicleOwner o
    where o.user.id = :userId
""")
    Optional<UUID> findOwnerIdByUserId(UUID userId);

}
