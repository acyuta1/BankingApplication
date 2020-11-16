package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.service.KycRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static com.achyutha.bankingapp.common.Constants.KYC_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class KycConverter implements Converter<String, Kyc> {

    private final KycRepository kycRepository;

    /**
     * Fetches a user when the id is provided.
     *
     * @param id String
     * @return user.
     */
    @Override
    public Kyc convert(String id) {
        System.out.println("here");
        System.out.println(id);
        return kycRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, KYC_NOT_FOUND));
    }
}

