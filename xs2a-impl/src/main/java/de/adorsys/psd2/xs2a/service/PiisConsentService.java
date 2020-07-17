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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.model.ConsentsConfirmationOfFunds;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiatePiisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentService {
    private final Xs2aPiisConsentService xs2aPiisConsentService;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final RequestProviderService requestProviderService;
    private final PiisConsentSpi piisConsentSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;

    public ResponseObject<Xs2aConfirmationOfFundsResponse> createPiisConsentWithResponse(ConsentsConfirmationOfFunds request, PsuIdData psuData, boolean explicitPreferred) {
        xs2aEventService.recordTppRequest(EventType.CREATE_PIIS_CONSENT_REQUEST_RECEIVED, request);

        TppInfo tppInfo = tppService.getTppInfo();
        Optional<Xs2aCreatePiisConsentResponse> xs2aCreatePiisConsentResponseOptional = xs2aPiisConsentService.createConsent(request, psuData, tppInfo);

        if (xs2aCreatePiisConsentResponseOptional.isEmpty()) {
            return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                       .fail(ErrorType.PIIS_400, of(MessageErrorCode.RESOURCE_UNKNOWN_400))
                       .build();
        }

        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = xs2aCreatePiisConsentResponseOptional.get();
        PiisConsent piisConsent = xs2aCreatePiisConsentResponse.getPiisConsent();
        String encryptedConsentId = xs2aCreatePiisConsentResponse.getConsentId();

        SpiResponse<SpiInitiatePiisConsentResponse> spiInitiatePiisConsentResponseSpiResponse = initiatePiisConsent(psuData, tppInfo, piisConsent);

        if (spiInitiatePiisConsentResponseSpiResponse.hasError()) {
            xs2aPiisConsentService.updateConsentStatus(encryptedConsentId, ConsentStatus.REJECTED);
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiInitiatePiisConsentResponseSpiResponse, ServiceType.PIIS);
            log.info("Consent-ID: [{}]. Create piis consent  with response failed. Consent rejected. Couldn't initiate PIIS consent at SPI level: {}",
                     encryptedConsentId, errorHolder);
            return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiInitiatePiisConsentResponse spiInitiatePiisConsentResponse = spiInitiatePiisConsentResponseSpiResponse.getPayload();

        SpiAccountReference spiAccountReference = spiInitiatePiisConsentResponse.getSpiAccountReference();
        AccountAccess accountAccess = new AccountAccess(Collections.singletonList(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(spiAccountReference)), Collections.emptyList(), Collections.emptyList(), null);
        accountReferenceUpdater.rewriteAccountAccess(encryptedConsentId, accountAccess);

        ConsentStatus consentStatus = piisConsent.getConsentStatus();
        Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse = new Xs2aConfirmationOfFundsResponse(consentStatus.getValue(), encryptedConsentId, false, requestProviderService.getInternalRequestIdString());
        return ResponseObject.<Xs2aConfirmationOfFundsResponse>builder().body(xs2aConfirmationOfFundsResponse).build();
    }

    public ResponseObject<PiisConsent> getPiisConsentById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentById.isEmpty()) {
            log.info("Consent-ID: [{}]. Get PIIS consent failed: initial consent not found by id", consentId);
            return ResponseObject.<PiisConsent>builder()
                       .fail(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        PiisConsent piisConsent = piisConsentById.get();
        SpiResponse<SpiConsentStatusResponse> spiResponse = getConsentStatusFromSpi(piisConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            log.info("Get PIIS consent status failed: Couldn't get PIIS consent status at SPI level: {}", errorHolder);
            return ResponseObject.<PiisConsent>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        ConsentStatus consentStatus = spiResponse.getPayload().getConsentStatus();
        piisConsent.setConsentStatus(consentStatus);
        xs2aPiisConsentService.updateConsentStatus(consentId, consentStatus);
        return ResponseObject.<PiisConsent>builder().body(piisConsent).build();
    }

    public ResponseObject<ConsentStatusResponse> getPiisConsentStatusById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
        ResponseObject.ResponseBuilder<ConsentStatusResponse> responseBuilder = ResponseObject.builder();

        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentById.isEmpty()) {
            log.info("Consent-ID: [{}]. Get PIIS consent failed: initial consent not found by id", consentId);
            return responseBuilder
                       .fail(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                       .build();
        }

        PiisConsent piisConsent = piisConsentById.get();
        SpiResponse<SpiConsentStatusResponse> spiResponse = getConsentStatusFromSpi(piisConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS);
            log.info("Get PIIS consent status failed: Couldn't get PIIS consent status at SPI level: {}", errorHolder);
            return responseBuilder
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiConsentStatusResponse spiPayload = spiResponse.getPayload();
        ConsentStatus consentStatus = spiPayload.getConsentStatus();
        xs2aPiisConsentService.updateConsentStatus(consentId, consentStatus);

        return responseBuilder
                   .body(new ConsentStatusResponse(consentStatus, spiPayload.getPsuMessage()))
                   .build();
    }

    private SpiResponse<SpiConsentStatusResponse> getConsentStatusFromSpi(PiisConsent piisConsent, String consentId) {
        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);
        SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);
        return piisConsentSpi.getConsentStatus(spiContextDataProvider.provide(), spiPiisConsent, aspspDataProvider);
    }

    private SpiResponse<SpiInitiatePiisConsentResponse> initiatePiisConsent(PsuIdData psuData, TppInfo tppInfo, PiisConsent piisConsent) {
        SpiContextData contextData = spiContextDataProvider.provide(psuData, tppInfo);
        SpiPiisConsent spiPiisConsent = xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent);
        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider();
        return piisConsentSpi.initiatePiisConsent(contextData, spiPiisConsent, aspspConsentDataProvider);
    }
}