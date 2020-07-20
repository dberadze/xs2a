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

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(value = {SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {
    TppInfoMapperImpl.class,
    AuthorisationTemplateMapperImpl.class,
    PsuDataMapper.class,
    ConsentTppInformationMapperImpl.class
})
class ConfirmationOfFundsMapperTest {
    private static final Map<String, Integer> USAGE_COUNTER = Collections.singletonMap("/confirmation-of-funds", 9);

    @Autowired
    private AuthorisationTemplateMapper authorisationTemplateMapper;
    @Autowired
    private PsuDataMapper psuDataMapper;
    @Autowired
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AisConsentUsageService aisConsentUsageService;

    private ConfirmationOfFundsMapper mapper;
    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        mapper = new ConfirmationOfFundsMapper(psuDataMapper, tppInfoMapper, authorisationTemplateMapper, aisConsentUsageService);
    }

    @Test
    void mapToCmsConfirmationOfFundsConsent() {
        ConsentEntity consent = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/consent-entity-empty-authorisations.json", ConsentEntity.class);
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);
        List<AuthorisationEntity> authorisations = Collections.singletonList(jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/authorisation-entity.json",
                                                                                                          AuthorisationEntity.class));

        CmsConfirmationOfFundsConsent actual = mapper.mapToCmsConfirmationOfFundsConsent(consent, authorisations);

        CmsConfirmationOfFundsConsent expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/cms-piis-consent-with-authorisations.json", CmsConfirmationOfFundsConsent.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsConfirmationOfFundsConsent_emptyAuthorisations() {
        ConsentEntity consent = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/consent-entity-empty-authorisations.json", ConsentEntity.class);
        when(aisConsentUsageService.getUsageCounterMap(consent)).thenReturn(USAGE_COUNTER);

        CmsConfirmationOfFundsConsent actual = mapper.mapToCmsConfirmationOfFundsConsent(consent, null);

        CmsConfirmationOfFundsConsent expected = jsonReader.getObjectFromFile("json/service/mapper/piis-consent-mapper/cms-piis-consent-empty-authorisations.json", CmsConfirmationOfFundsConsent.class);
        assertEquals(expected, actual);
    }
}
