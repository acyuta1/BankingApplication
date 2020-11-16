package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.common.validation.group.KycGroup;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Validator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {


    private final KycRepository kycRepository;

    private final Validator validator;
    @Override
    public ResponseEntity<?> updateKyc(User user, UpdateAfterCreation updateAfterCreation) {
        var errors = validator.validate(updateAfterCreation, KycGroup.class);
        if(errors.isEmpty()) {
            var kycExisting = kycRepository.findByUserName(user.getUsername());
            if (kycExisting.isPresent()) {
                switch (kycExisting.get().getKycVerificationStatus()) {
                    case pending:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have a pending kyc request present already.");
                    case rejected: {
                        kycRepository.save(kycExisting.get()
                                .setAadharNumber(updateAfterCreation.getAadharNumber()).setPanCard(updateAfterCreation.getPanCard()).setUserName(user.getUsername()));
                        return ResponseEntity.ok("Submitted request.");
                    }
                    case verified:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your kyc is already verified.");
                }
            }
            kycRepository.save(new Kyc().setId(UUID.randomUUID().toString()).setUserName(user.getUsername()).setNewPassword(updateAfterCreation.getPassword()).setPanCard(updateAfterCreation.getPanCard()).setAadharNumber(updateAfterCreation.getAadharNumber()).setDob(updateAfterCreation.getDob()));
            return ResponseEntity.ok("Kyc submitted.");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
    }
}
