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

package de.adorsys.psd2.consent.integration.aspsp;

import de.adorsys.psd2.consent.ConsentManagementStandaloneApp;
import de.adorsys.psd2.consent.config.WebConfig;
import de.adorsys.psd2.consent.integration.ApiIntegrationTestConfig;
import de.adorsys.psd2.consent.integration.UrlBuilder;
import de.adorsys.psd2.event.persist.EventReportRepository;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test"})
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = ConsentManagementStandaloneApp.class)
@ContextConfiguration(classes = {WebConfig.class, ApiIntegrationTestConfig.class})
public class CmsAspspEventControllerIT /*extends ApiIntegrationTest*/ {

    private static final String INSTANCE_ID = "bank-instance-id";
    private static final String START_DATE = "2010-01-01T00:00:00Z";
    private static final String END_DATE = "2030-01-01T00:00:00Z";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventReportRepository eventReportRepository;

    private final JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add("start-date", START_DATE);
        httpHeaders.add("end-date", END_DATE);
        httpHeaders.add("instance-id", INSTANCE_ID);
    }

    @Test
    void getEventsForDates() throws Exception {
        ReportEvent reportEvent = jsonReader.getObjectFromFile("json/consent/integration/aspsp/report-event.json", ReportEvent.class);
        given(eventReportRepository.getEventsForPeriod(
            OffsetDateTime.parse(START_DATE),
            OffsetDateTime.parse(END_DATE),
            INSTANCE_ID
        )).willReturn(Collections.singletonList(reportEvent));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.getEventsForDatesUrl());
        requestBuilder.headers(httpHeaders);
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile("json/consent/integration/aspsp/expect/aspsp-event.json")));
    }
}
