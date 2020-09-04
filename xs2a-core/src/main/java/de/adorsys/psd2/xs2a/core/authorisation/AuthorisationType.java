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

package de.adorsys.psd2.xs2a.core.authorisation;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum AuthorisationType {
    // TODO remove in 8.0 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1320
    @Deprecated AIS,
    CONSENT,
    PIS_CREATION,
    PIS_CANCELLATION,
    SIGNING_BASKET;

    private static final Map<String, AuthorisationType> HOLDER = new HashMap<>();

    static {
        for (AuthorisationType authorisationType : AuthorisationType.values()) {
            HOLDER.put(authorisationType.name().toLowerCase(), authorisationType);
        }
    }

    /**
     * Maps textual representation to AuthorisationType enum-value.
     * Mapping is performed case-insensitive.
     *
     * @param text - text to be mapped.
     * @return Enum value mapped. Null otherwise.
     */
    @JsonCreator
    public static AuthorisationType fromValue(String text) {
        return HOLDER.get(text.trim().toLowerCase());
    }
}
