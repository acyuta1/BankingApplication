package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.http.ResponseEntity;

public interface CustomerService {

    ResponseEntity<?> updateKyc(User user, UpdateAfterCreation updateAfterCreation);
}
