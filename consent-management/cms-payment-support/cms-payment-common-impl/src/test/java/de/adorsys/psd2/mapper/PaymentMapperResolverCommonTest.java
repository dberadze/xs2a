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

package de.adorsys.psd2.mapper;

import de.adorsys.psd2.consent.api.pis.CmsCommonPaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentMapperResolverCommonTest {
    private static final String PAYMENT_PRODUCT = "product";

    @InjectMocks
    private PaymentMapperResolverCommon resolver;
    @Mock
    private CmsCommonPaymentMapperImpl cmsCommonPaymentMapper;

    @Test
    void getCmsCommonPaymentMapper_Common() {
        // When
        CmsCommonPaymentMapper actual = resolver.getCmsCommonPaymentMapper(PAYMENT_PRODUCT);

        // Then
        assertThat(actual).isEqualTo(cmsCommonPaymentMapper);
    }
}
