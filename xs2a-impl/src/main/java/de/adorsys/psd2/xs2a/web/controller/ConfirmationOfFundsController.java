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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.v2.ConfirmationOfFundsApi;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.model.Authorisations;
import de.adorsys.psd2.model.ConsentsConfirmationOfFunds;
import de.adorsys.psd2.model.ScaStatusResponse;
import de.adorsys.psd2.model.StartScaprocessResponse;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.PiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.header.ConsentHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.PiisConsentModelMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

@Slf4j
@RestController
@AllArgsConstructor
public class ConfirmationOfFundsController implements ConfirmationOfFundsApi {
    private final PiisConsentService piisConsentService;
    private final ResponseErrorMapper responseErrorMapper;
    private final PiisConsentModelMapper piisConsentModelMapper;
    private final ResponseMapper responseMapper;
    private final AspspProfileServiceWrapper profileService;
    private final ConsentHeadersBuilder consentHeadersBuilder;

    @Override
    public ResponseEntity createConsentConfirmationOfFunds(UUID xRequestID, ConsentsConfirmationOfFunds body, String digest, String signature, byte[] tpPSignatureCertificate,
                                                           String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType,
                                                           String tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI, String tpPExplicitAuthorisationPreferred,
                                                           String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                           String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        if (profileService.getPiisConsentSupported() != PiisConsentSupported.TPP_CONSENT_SUPPORTED) {
            MessageError messageError = new MessageError(ErrorType.PIIS_405, of(MessageErrorCode.SERVICE_INVALID_405));
            return responseErrorMapper.generateErrorResponse(messageError);
        }

        PsuIdData psuData = new PsuIdData(PSU_ID, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress,
                                          new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID));

        CreatePiisConsentRequest createPiisConsentRequest = piisConsentModelMapper.toCreatePiisConsentRequest(body);
        ResponseObject<Xs2aConfirmationOfFundsResponse> xs2aConfirmationOfFundsResponseResponseObject = piisConsentService.createPiisConsentWithResponse(createPiisConsentRequest, psuData, BooleanUtils.toBoolean(tpPExplicitAuthorisationPreferred));
        if (xs2aConfirmationOfFundsResponseResponseObject.hasError()) {
            return responseErrorMapper.generateErrorResponse(xs2aConfirmationOfFundsResponseResponseObject.getError());
        }

        Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse = xs2aConfirmationOfFundsResponseResponseObject.getBody();

        ResponseHeaders headers = consentHeadersBuilder.buildCreateConsentHeaders(xs2aConfirmationOfFundsResponse.getAuthorizationId(),
                                                                                  Optional.ofNullable(xs2aConfirmationOfFundsResponse.getLinks().getSelf())
                                                                                      .map(HrefType::getHref)
                                                                                      .orElseThrow(() -> new IllegalArgumentException("Wrong href type in self link")));

        return responseMapper.created(xs2aConfirmationOfFundsResponseResponseObject, piisConsentModelMapper::mapToConsentsConfirmationOfFundsResponse, headers);
    }

    @Override
    public ResponseEntity<Void> deleteConsentConfirmationOfFunds(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null;
    }

    @Override
    public ResponseEntity getConsentConfirmationOfFunds(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<PiisConsent> piisConsentResponseObject = piisConsentService.getPiisConsentById(consentId);
        return piisConsentResponseObject.hasError()
                   ? responseErrorMapper.generateErrorResponse(piisConsentResponseObject.getError())
                   : responseMapper.ok(piisConsentResponseObject, piisConsentModelMapper::mapToConsentConfirmationOfFundsContentResponse);
    }

    @Override
    public ResponseEntity getConsentConfirmationOfFundsStatus(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<ConsentStatusResponse> accountConsentsStatusByIdResponse = piisConsentService.getPiisConsentStatusById(consentId);
        return accountConsentsStatusByIdResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(accountConsentsStatusByIdResponse.getError())
                   : responseMapper.ok(accountConsentsStatusByIdResponse, piisConsentModelMapper::mapToConsentConfirmationOfFundsStatusResponse);
    }

    @Override
    public ResponseEntity<Authorisations> getConsentAuthorisation(String consentId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null;
    }

    @Override
    public ResponseEntity<ScaStatusResponse> getConsentScaStatus(String consentId, String authorisationId, UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null;
    }

    @Override
    public ResponseEntity<StartScaprocessResponse> startConsentAuthorisation(UUID xRequestID, String consentId, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String tpPRedirectPreferred, String tpPRedirectURI, String tpPNokRedirectURI, String tpPNotificationURI, String tpPNotificationContentPreferred, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null;
    }

    @Override
    public ResponseEntity<Object> updateConsentsPsuData(UUID xRequestID, String consentId, String authorisationId, Object body, String digest, String signature, byte[] tpPSignatureCertificate, String PSU_ID, String psUIDType, String psUCorporateID, String psUCorporateIDType, String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return null;
    }
}