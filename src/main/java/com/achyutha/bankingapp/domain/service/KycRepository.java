package com.achyutha.bankingapp.domain.service;


import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.KycVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Kyc Repository.
 */
@Repository
public interface KycRepository  extends JpaRepository<Kyc, String> {

    /**
     * To find whether a kyc information already exists.
     * @param userName The customer user name.
     * @return The kyc information, if present.
     */
    Optional<Kyc> findByUserName(String userName);


    /**
     * To fetch a list of kycs with status provided, to be viewed by the employee.
     * @return The list kyc.
     */
    List<Kyc> findAllByKycVerificationStatus(KycVerificationStatus kycVerificationStatus);
}
