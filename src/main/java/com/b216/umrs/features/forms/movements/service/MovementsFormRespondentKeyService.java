package com.b216.umrs.features.forms.movements.service;

import com.b216.umrs.features.forms.movements.domain.InvalidRespondentKeyException;
import com.b216.umrs.features.forms.movements.domain.MovementsFormRespondentKey;
import com.b216.umrs.features.forms.movements.repository.MovementsFormRespondentKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovementsFormRespondentKeyService {

    private final MovementsFormRespondentKeyRepository respondentKeyRepository;

    public MovementsFormRespondentKey getActiveRespondentKey(String respondentKey) {
        return respondentKeyRepository.findByKeyValueAndActiveTrue(respondentKey)
            .orElseThrow(() -> new InvalidRespondentKeyException("Respondent key is invalid or inactive"));
    }
}
