/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.config;

import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.piis.PiisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import lombok.Data;

@Data
public class BankProfileSetting {

    /**
     * Contains all settings regarding to AIS
     */
    private AisAspspProfileSetting ais;

    /**
     * Contains all settings regarding to PIS
     */
    private PisAspspProfileSetting pis;

    /**
     * Contains all settings regarding to PIIS
     */
    private PiisAspspProfileSetting piis;

    /**
     * Contains common for consents and payments settings
     */
    private CommonAspspProfileSetting common;
}
