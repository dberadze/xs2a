/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_BLOCKED;

/**
 * Common validator for validating update PSU data in payments and executing request-specific business validation afterwards.
 * Should be used for all update PIS PSU data requests.
 *
 * @param <T> type of object to be checked
 */
@Slf4j
@Component
public abstract class AbstractUpdatePisPsuDataValidator<T extends UpdatePisPsuDataPO> extends AbstractPisValidator<T> {
    private final PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    private final PisAuthorisationValidator pisAuthorisationValidator;
    private final PisAuthorisationStatusValidator pisAuthorisationStatusValidator;
    private final PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator;
    private final AuthorisationStageCheckValidator authorisationStageCheckValidator;

    public AbstractUpdatePisPsuDataValidator(PisEndpointAccessCheckerService pisEndpointAccessCheckerService,
                                             PisAuthorisationValidator pisAuthorisationValidator,
                                             PisAuthorisationStatusValidator pisAuthorisationStatusValidator,
                                             PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator,
                                             AuthorisationStageCheckValidator authorisationStageCheckValidator) {
        this.pisEndpointAccessCheckerService = pisEndpointAccessCheckerService;
        this.pisAuthorisationValidator = pisAuthorisationValidator;
        this.pisAuthorisationStatusValidator = pisAuthorisationStatusValidator;
        this.pisPsuDataUpdateAuthorisationCheckerValidator = pisPsuDataUpdateAuthorisationCheckerValidator;
        this.authorisationStageCheckValidator = authorisationStageCheckValidator;
    }

    /**
     * Validates update PSU Data in payment authorisation request by checking whether:
     * <ul>
     * <li>endpoint is accessible for given authorisation</li>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(UpdatePisPsuDataPO paymentObject) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = paymentObject.getUpdateRequest();
        String authorisationId = request.getAuthorisationId();
        boolean confirmationCodeReceived = StringUtils.isNotBlank(request.getConfirmationCode());

        if (!pisEndpointAccessCheckerService.isEndpointAccessible(authorisationId, confirmationCodeReceived)) {
            log.info("Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data  has failed: endpoint is not accessible for authorisation", authorisationId);
            return ValidationResult.invalid(ErrorType.PIS_403, SERVICE_BLOCKED);
        }

        ValidationResult transactionStatusValidationResult = validateTransactionStatus(paymentObject);
        if (transactionStatusValidationResult.isNotValid()) {
            return transactionStatusValidationResult;
        }

        PisCommonPaymentResponse pisCommonPaymentResponse = paymentObject.getPisCommonPaymentResponse();

        ValidationResult authorisationValidationResult = pisAuthorisationValidator.validate(authorisationId, pisCommonPaymentResponse);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        Optional<Authorisation> authorisationOptional = pisCommonPaymentResponse.findAuthorisationInPayment(authorisationId);

        if (authorisationOptional.isEmpty()) {
            return ValidationResult.invalid(ErrorType.PIS_403, RESOURCE_UNKNOWN_403);
        }

        Authorisation authorisation = authorisationOptional.get();

        ValidationResult validationResult = pisPsuDataUpdateAuthorisationCheckerValidator
                                                .validate(request.getPsuData(), authorisation.getPsuIdData());

        if (validationResult.isNotValid()) {
            return validationResult;
        }

        ValidationResult authorisationStatusValidationResult = pisAuthorisationStatusValidator.validate(authorisation.getScaStatus(), confirmationCodeReceived);
        if (authorisationStatusValidationResult.isNotValid()) {
            return authorisationStatusValidationResult;
        }

        ValidationResult authorisationStageCheckValidatorResult = authorisationStageCheckValidator.validate(request, authorisation.getScaStatus(), getAuthorisationServiceType());
        if (authorisationStageCheckValidatorResult.isNotValid()) {
            return authorisationStageCheckValidatorResult;
        }

        return ValidationResult.valid();
    }

    protected ValidationResult validateTransactionStatus(UpdatePisPsuDataPO paymentObject) {
        return ValidationResult.valid();
    }

    protected abstract AuthorisationServiceType getAuthorisationServiceType();
}
