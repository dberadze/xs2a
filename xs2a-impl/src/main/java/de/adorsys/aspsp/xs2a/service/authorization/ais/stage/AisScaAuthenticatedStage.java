/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.authorization.ais.stage;

import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aScaStatus;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;

@Service("AIS_SCAMETHODSELECTED")
public class AisScaAuthenticatedStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {

    public AisScaAuthenticatedStage(AisConsentService aisConsentService, AisConsentDataService aisConsentDataService, AisConsentSpi aisConsentSpi, Xs2aAisConsentMapper aisConsentMapper, SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper);
    }

    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());

        SpiPsuData psuData = new SpiPsuData(null, null, null, null);    // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<SpiResponse.VoidResponse> spiResponse = aisConsentSpi.verifyAuthorisationCodeAndExecuteRequest(psuData, aisConsentMapper.mapToSpiScaConfirmation(request), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            response.setErrorCode(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
            return response;
        }

        response.setScaAuthenticationData(request.getScaAuthenticationData());
        response.setScaStatus(Xs2aScaStatus.FINALISED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
        aisConsentService.updateConsentStatus(request.getConsentId(), SpiConsentStatus.VALID);
        return response;
    }
}
