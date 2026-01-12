package com.b216.umrs.features.forms.movements.repository;

import com.b216.umrs.features.forms.movements.domain.MovementsFormSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для сущности MovementsFormSubmission.
 */
public interface MovementsFormSubmissionRepository extends JpaRepository<MovementsFormSubmission, Long> {
}
