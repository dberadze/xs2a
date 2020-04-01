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

package de.adorsys.psd2.consent.domain.consent;

import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.InstanceDependableEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Data
@Entity(name = "consent")
@EqualsAndHashCode(callSuper = true)
public class ConsentEntity extends InstanceDependableEntity implements Authorisable {

    @Id
    @Column(name = "consent_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consent_generator")
    @SequenceGenerator(name = "consent_generator", sequenceName = "consent_id_seq",
        allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "consent_status", nullable = false)
    private ConsentStatus consentStatus;

    @Column(name = "consent_type", nullable = false)
    private String consentType;

    @Column(name = "frequency_per_day", nullable = false)
    private int frequencyPerDay;

    @Column(name = "recurring_indicator", nullable = false)
    private boolean recurringIndicator;

    @Column(name = "multilevel_sca_required", nullable = false)
    private boolean multilevelScaRequired;

    @Lob
    @Column(name = "checksum")
    private byte[] checksum;

    @Lob
    @Column(name = "data")
    private byte[] data;

    @Column(name = "creation_timestamp", nullable = false)
    private OffsetDateTime creationTimestamp = OffsetDateTime.now();

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "last_action_date")
    private LocalDate lastActionDate;

    // TODO: change type to OffsetDateTime https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1220
    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestDateTime;

    @Column(name = "status_change_timestamp")
    private OffsetDateTime statusChangeTimestamp;

    @Column(name = "internal_request_id")
    private String internalRequestId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "authorisation_template_id", nullable = false)
    private AuthorisationTemplateEntity authorisationTemplate = new AuthorisationTemplateEntity();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_tpp_information_id", nullable = false)
    private ConsentTppInformationEntity tppInformation = new ConsentTppInformationEntity();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "ais_consent_psu_data",
        joinColumns = @JoinColumn(name = "ais_consent_id"),
        inverseJoinColumns = @JoinColumn(name = "psu_data_id"))
    private List<PsuData> psuDataList = new ArrayList<>();

    @OneToMany(mappedBy = "consent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AisConsentUsage> usages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    private List<TppAccountAccess> tppAccountAccesses = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "ais_aspsp_account_access", joinColumns = @JoinColumn(name = "consent_id"))
    private List<AspspAccountAccess> aspspAccountAccesses = new ArrayList<>();

    @Column(name = "owner_name_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType ownerNameType = AdditionalAccountInformationType.NONE;

    @Column(name = "trusted_beneficiaries_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private AdditionalAccountInformationType trustedBeneficiariesType = AdditionalAccountInformationType.NONE;

    @Transient
    private ConsentStatus previousConsentStatus;

    @PostLoad
    public void consentPostLoad() {
        previousConsentStatus = consentStatus;
    }

    @PreUpdate
    public void consentPreUpdate() {
        if (previousConsentStatus != consentStatus) {
            statusChangeTimestamp = OffsetDateTime.now();
        }
    }

    @PrePersist
    public void consentPrePersist() {
        if (Objects.isNull(statusChangeTimestamp)) {
            statusChangeTimestamp = creationTimestamp;
        }
    }

    public boolean isConfirmationExpired(long expirationPeriodMs) {
        if (EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED).contains(consentStatus)) {
            return creationTimestamp.plus(expirationPeriodMs, ChronoUnit.MILLIS)
                       .isBefore(OffsetDateTime.now());
        }
        return false;
    }

    public void addUsage(AisConsentUsage aisConsentUsage) {
        if (usages == null) {
            usages = new ArrayList<>();
        }
        usages.add(aisConsentUsage);
    }

    public boolean isWrongConsentData() {
        return CollectionUtils.isEmpty(psuDataList)
                   || tppInformation == null
                   || tppInformation.getTppInfo() == null;
    }

    public boolean isExpiredByDate() {
        return LocalDate.now().compareTo(validUntil) > 0;
    }

    public boolean shouldConsentBeExpired() {
        return !this.getConsentStatus().isFinalisedStatus()
                   && (this.isExpiredByDate() || this.isNonReccuringAlreadyUsed());
    }

    /**
     * Checks, whether the consent is non-recurring and was used any time before today. Currently non-recurring consent
     * allows to perform read operations only within the day, it was used first time.
     *
     * @return Returns true if consent is non-recurrent and has no usages before today, false otherwise.
     */
    public boolean isNonReccuringAlreadyUsed() {
        return !recurringIndicator && usages.stream()
                                          .anyMatch(u -> u.getUsageDate().isBefore(LocalDate.now()));
    }

    public boolean isOneAccessType() {
        return !recurringIndicator;
    }

    @Override
    public String getInternalRequestId(AuthorisationType authorisationType) {
        if (authorisationType == AuthorisationType.AIS) {
            return internalRequestId;
        }

        throw new IllegalArgumentException("Invalid authorisation type: " + authorisationType);
    }
}
