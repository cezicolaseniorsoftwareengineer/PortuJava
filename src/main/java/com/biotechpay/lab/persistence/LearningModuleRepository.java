package com.biotechpay.lab.persistence;

import com.biotechpay.lab.domain.LearningModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearningModuleRepository extends JpaRepository<LearningModule, Long> {
    Optional<LearningModule> findByModuleCode(String moduleCode);
    List<LearningModule> findAllByOrderBySortOrderAsc();
    boolean existsByModuleCode(String moduleCode);
}
