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
//package de.adorsys.psd2.xs2a.service.validator.ais.account;
//
//import de.adorsys.psd2.core.data.AccountAccess;
//import de.adorsys.psd2.core.data.ais.AisConsent;
//import de.adorsys.psd2.core.data.ais.AisConsentData;
//import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
//import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
//import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
//import de.adorsys.psd2.xs2a.core.error.ErrorType;
//import de.adorsys.psd2.xs2a.core.error.MessageError;
//import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
//import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
//import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
//import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
//import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
//import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountBalanceRequestObject;
//import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
//import de.adorsys.xs2a.reader.JsonReader;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.OffsetDateTime;
//import java.util.Collections;
//
//import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
//import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class GetCardBalancesReportValidatorTest {
//    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
//    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
//    private static final String REQUEST_URI = "/accounts";
//    private static final String ACCOUNT_ID = "11111-999999999";
//
//    private static final MessageError TPP_VALIDATION_ERROR =
//        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
//
//    private static final MessageError CONSENT_INVALID_ERROR =
//        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));
//
//    @Mock
//    private AccountConsentValidator accountConsentValidator;
//    @Mock
//    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
//    @Mock
//    private AccountReferenceAccessValidator accountReferenceAccessValidator;
//    @Mock
//    private OauthConsentValidator oauthConsentValidator;
//
//    @InjectMocks
//    private GetCardBalancesReportValidator getCardBalancesReportValidator;
//
//    private JsonReader jsonReader = new JsonReader();
//    private AccountAccess accountAccess;
//    private AccountAccess cardAccountAccess;
//
//    @BeforeEach
//    void setUp() {
//        cardAccountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access-pan.json", AccountAccess.class);
//        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access.json", AccountAccess.class);
//
//        // Inject pisTppInfoValidator via setter
//        getCardBalancesReportValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
//    }
//
//    @Test
//    void validate_withValidConsentObject_shouldReturnValid() {
//        // Given
//        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO, cardAccountAccess);
//        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
//            .thenReturn(ValidationResult.valid());
//        when(accountReferenceAccessValidator.validate(aisConsent.getAccess(), aisConsent.getAccess().getBalances(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
//        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
//            .thenReturn(ValidationResult.valid());
//        when(oauthConsentValidator.validate(aisConsent))
//            .thenReturn(ValidationResult.valid());
//
//        // When
//        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));
//
//        // Then
//        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
//
//        assertNotNull(validationResult);
//        assertTrue(validationResult.isValid());
//        assertNull(validationResult.getMessageError());
//    }
//
//    @Test
//    void validate_withInvalidAccountReferenceAccess_error() {
//        // Given
//        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO, cardAccountAccess);
//        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
//            .thenReturn(ValidationResult.valid());
//        when(accountReferenceAccessValidator.validate(aisConsent.getAccess(), aisConsent.getAccess().getBalances(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
//            .thenReturn(ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID));
//
//        // When
//        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));
//
//        // Then
//        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
//
//        assertNotNull(validationResult);
//        assertFalse(validationResult.isValid());
//
//        verify(accountConsentValidator, never()).validate(any(AisConsent.class), anyString());
//    }
//
//    @Test
//    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
//        // Given
//        AisConsent aisConsent = buildCardAccountConsent(INVALID_TPP_INFO, cardAccountAccess);
//        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
//            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
//
//        // When
//        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));
//
//        // Then
//        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
//
//        assertNotNull(validationResult);
//        assertTrue(validationResult.isNotValid());
//        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
//    }
//
//    @Test
//    void validate_withInvalidAccessInConsent_shouldReturnConsentInvalidError() {
//        // Given
//        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO, accountAccess);
//        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
//            .thenReturn(ValidationResult.valid());
//
//        // When
//        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));
//
//        // Then
//        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());
//
//        assertNotNull(validationResult);
//        assertTrue(validationResult.isNotValid());
//        assertEquals(CONSENT_INVALID_ERROR, validationResult.getMessageError());
//    }
//
//    private static TppInfo buildTppInfo(String authorisationNumber) {
//        TppInfo tppInfo = new TppInfo();
//        tppInfo.setAuthorisationNumber(authorisationNumber);
//        return tppInfo;
//    }
//
//    private AisConsent buildCardAccountConsent(TppInfo tppInfo, AccountAccess accountAccess) {
//        AisConsent aisConsent = new AisConsent();
//        aisConsent.setId("id");
//        AisConsentData consentData = new AisConsentData(accountAccess, accountAccess, false);
//        aisConsent.setConsentData(consentData);
//        aisConsent.setFrequencyPerDay(0);
//        aisConsent.setPsuIdDataList(Collections.emptyList());
//        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
//        consentTppInformation.setTppInfo(tppInfo);
//        aisConsent.setConsentTppInformation(consentTppInformation);
//        aisConsent.setAuthorisations(Collections.emptyList());
//        aisConsent.setUsages(Collections.emptyMap());
//        aisConsent.setCreationTimestamp(OffsetDateTime.now());
//
//        return aisConsent;
//    }
//}
