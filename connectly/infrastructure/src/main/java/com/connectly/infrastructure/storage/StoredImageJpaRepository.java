package com.connectly.infrastructure.storage;
import org.springframework.data.jpa.repository.JpaRepository;

interface StoredImageJpaRepository extends JpaRepository<StoredImageEntity, String> {}
