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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsAuthorisation;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConfirmationOfFundsMapper {
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final AisConsentUsageService aisConsentUsageService;

    public CmsConfirmationOfFundsConsent mapToCmsConfirmationOfFundsConsent(ConsentEntity consent, List<AuthorisationEntity> authorisations) {
        ConsentTppInformationEntity tppInformation = consent.getTppInformation();
        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(consent);

        return new CmsConfirmationOfFundsConsent(
            consent.getExternalId(),
            consent.getValidUntil(),
            consent.getExpireDate(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            tppInformation.isTppRedirectPreferred(),
            psuDataMapper.mapToPsuIdDataList(consent.getPsuDataList()),
            tppInfoMapper.mapToTppInfo(tppInformation.getTppInfo()),
            authorisationTemplateMapper.mapToAuthorisationTemplate(consent.getAuthorisationTemplate()),
            consent.isMultilevelScaRequired(),
            usageCounterMap,
            consent.getCreationTimestamp(),
            consent.getStatusChangeTimestamp(),
            mapToAuthorisations(authorisations)
        );
    }

    private List<CmsConfirmationOfFundsAuthorisation> mapToAuthorisations(List<AuthorisationEntity> consentAuthorisations) {
        if (CollectionUtils.isEmpty(consentAuthorisations)) {
            return Collections.emptyList();
        }

        return consentAuthorisations.stream()
                   .map(auth -> new CmsConfirmationOfFundsAuthorisation(
                       auth.getExternalId(),
                       psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                       auth.getScaStatus()
                   ))
                   .collect(Collectors.toList());
    }
}
