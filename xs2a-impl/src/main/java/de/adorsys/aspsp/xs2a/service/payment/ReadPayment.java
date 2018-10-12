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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ReadPayment<T> {
    @Autowired
    protected PaymentSpi paymentSpi;
    @Autowired
    protected PeriodicPaymentSpi periodicPaymentSpi;
    @Autowired
    protected PaymentMapper paymentMapper;
    @Autowired
    protected PisConsentDataService pisConsentDataService;

    public abstract T getPayment(String paymentId, String paymentProduct);
}
