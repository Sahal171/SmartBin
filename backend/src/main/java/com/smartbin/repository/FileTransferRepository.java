package com.smartbin.repository;

import com.smartbin.entity.FileTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileTransferRepository extends JpaRepository<FileTransfer, UUID> {

    Optional<FileTransfer> findByTransferCode(String transferCode);

    boolean existsByTransferCode(String transferCode);

    List<FileTransfer> findAllByExpiresAtBefore(Instant now);

    @Modifying
    @Query("DELETE FROM FileTransfer f WHERE f.transferCode = :code")
    void deleteByTransferCode(@Param("code") String code);
}
