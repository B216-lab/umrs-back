package com.b216.umrs.features.forms.movements.repository;

import com.b216.umrs.features.forms.movements.domain.MovementsFormRespondentKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovementsFormRespondentKeyRepository extends JpaRepository<MovementsFormRespondentKey, Long> {
    Optional<MovementsFormRespondentKey> findByKeyValueAndActiveTrue(String keyValue);
}
