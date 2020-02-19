// TODO: 19.02.2020 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1170
///*
// * Copyright 2018-2020 adorsys GmbH & Co KG
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package de.adorsys.psd2.xs2a.service.validator.ais.account.common;
//
//import de.adorsys.psd2.core.data.AccountAccess;
//import de.adorsys.psd2.core.data.ais.AisConsent;
//import de.adorsys.psd2.core.data.ais.AisConsentData;
//import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
//import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
//import de.adorsys.psd2.xs2a.core.error.ErrorType;
//import de.adorsys.psd2.xs2a.core.error.MessageError;
//import de.adorsys.psd2.xs2a.service.RequestProviderService;
//import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.Collections;
//
//import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AccountConsentValidatorTest {
//    private static final String AUTHORISATION_NUMBER = "authorisation number";
//    private static final String REQUEST_URI = "/accounts";
//
//    private static final MessageError AIS_CONSENT_EXPIRED_ERROR =
//        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_EXPIRED));
//    private static final MessageError AIS_CONSENT_INVALID_ERROR =
//        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
//    private static final MessageError AIS_CONSENT_ACCESS_EXCEEDED_ERROR =
//        new MessageError(ErrorType.AIS_429, TppMessageInformation.of(ACCESS_EXCEEDED));
//    private static final MessageError AIS_CONSENT_INVALID_REVOKED_BY_PSU =
//        new MessageError((ErrorType.AIS_401), TppMessageInformation.of(CONSENT_INVALID_REVOKED));
//
//    @InjectMocks
//    private AccountConsentValidator accountConsentValidator;
//    @Mock
//    private RequestProviderService requestProviderService;
//
//    @Test
//    void testValidate_shouldReturnValid() {
//        // Given
//        AisConsent accountConsent = buildAccountConsentValid();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isValid());
//    }
//
//    @Test
//    void testValidateInvalid_shouldReturnInvalidErrorWithTextConsentRevoked() {
//        // Given
//        AisConsent accountConsent = buildAccountConsentInvalidRevokedByPsu();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isNotValid());
//        assertEquals(AIS_CONSENT_INVALID_REVOKED_BY_PSU, actual.getMessageError());
//    }
//
//    @Test
//    void testValidateExpired_shouldReturnExpiredError() {
//        // Given
//        AisConsent accountConsent = buildAccountConsentExpired();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isNotValid());
//        assertEquals(AIS_CONSENT_EXPIRED_ERROR, actual.getMessageError());
//    }
//
//    @Test
//    void testValidateInvalid_shouldReturnInvalidError() {
//        // Given
//        AisConsent accountConsent = buildAccountConsentInvalid();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isNotValid());
//        assertEquals(AIS_CONSENT_INVALID_ERROR, actual.getMessageError());
//    }
//
//    @Test
//    void testValidateAccessExceeded_shouldReturnExceededError() {
//        // Given
//        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
//        AisConsent accountConsent = buildAccountConsentAccessExceeded();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isNotValid());
//        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
//    }
//
//    @Test
//    void testValidateAccessExceeded_shouldReturnValid_UsageCounterMapNotContainsRequestUri() {
//        // Given
//        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
//        AisConsent accountConsent = buildAccountConsentAccessExceededIsOneAccessType();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isValid());
//    }
//
//
//    @Test
//    void testValidateAccessExceeded_shouldReturnValid() {
//        // Given
//        when(requestProviderService.isRequestFromPsu()).thenReturn(true);
//        AisConsent accountConsent = buildAccountConsentAccessExceeded();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isValid());
//    }
//
//    @Test
//    void testValidateAccessExceeded_oneOff_shouldReturnExceededError() {
//        // Given
//        when(requestProviderService.isRequestFromPsu()).thenReturn(false);
//        AisConsent accountConsent = buildOneOffAccountConsentAccessExceeded();
//
//        // When
//        ValidationResult actual = accountConsentValidator.validate(accountConsent, REQUEST_URI);
//
//        // Then
//        assertTrue(actual.isNotValid());
//        assertEquals(AIS_CONSENT_ACCESS_EXCEEDED_ERROR, actual.getMessageError());
//    }
//
//    private AisConsent buildAccountConsentValid() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setConsentStatus(ConsentStatus.VALID);
//        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
//        aisConsent.setUsages(Collections.singletonMap(REQUEST_URI, 10));
//        return aisConsent;
//    }
//
//    private AisConsent buildAccountConsentExpired() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setValidUntil(LocalDate.now().minusDays(1));
//        return aisConsent;
//    }
//
//    private AisConsent buildAccountConsentInvalid() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setId("id");
//        aisConsent.setConsentData(new AisConsentData(AccountAccess.EMPTY_ACCESS, AccountAccess.EMPTY_ACCESS, false));
//        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
//        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
//        return aisConsent;
//    }
//
//    private AisConsent buildAccountConsentInvalidRevokedByPsu() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setConsentStatus(ConsentStatus.REVOKED_BY_PSU);
//        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
//        return aisConsent;
//    }
//
//    private AisConsent buildOneOffAccountConsentAccessExceeded() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
//        aisConsent.setConsentStatus(ConsentStatus.VALID);
//        aisConsent.setFrequencyPerDay(1);
//        aisConsent.setUsages(Collections.singletonMap(REQUEST_URI, 0));
//        return aisConsent;
//    }
//
//    private AisConsent buildAccountConsentAccessExceeded() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
//        aisConsent.setUsages(Collections.singletonMap(REQUEST_URI, 0));
//        aisConsent.setConsentStatus(ConsentStatus.VALID);
//        aisConsent.setRecurringIndicator(true);
//        return aisConsent;
//    }
//
//    private AisConsent buildAccountConsentAccessExceededIsOneAccessType() {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setValidUntil(LocalDate.now().plusYears(1));
//        aisConsent.setFrequencyPerDay(0);
//        aisConsent.setConsentStatus(ConsentStatus.VALID);
//        aisConsent.setUsages(Collections.emptyMap());
//        return aisConsent;
//    }
//}
