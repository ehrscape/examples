package com.marand.thinkmed.medications.connector.impl.local.provider;

import com.marand.maf.core.hibernate.query.Alias;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalAllergy;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCareProvider;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCase;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalDiseaseType;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEncounter;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalEpisode;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalMedicalStaff;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalOrganization;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatient;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalUser;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalUserCareProviderMember;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalPatientAllergy;
import com.marand.thinkmed.medications.connector.impl.local.model.ExternalCentralCaseDisease;

/**
 * @author Bostjan Vester
 */
public interface ExternalAliases
{
  Alias.Permanent<ExternalCentralCaseDisease> externalCentralCaseDisease = Alias.forPermanentEntity(ExternalCentralCaseDisease.class);
  Alias.Permanent<ExternalAllergy> externalAllergy = Alias.forPermanentEntity(ExternalAllergy.class);
  Alias.Permanent<ExternalCareProvider> externalCareProvider = Alias.forPermanentEntity(ExternalCareProvider.class);
  Alias.Permanent<ExternalCentralCase> externalCentralCase = Alias.forPermanentEntity(ExternalCentralCase.class);
  Alias.Permanent<ExternalDiseaseType> externalDiseaseType = Alias.forPermanentEntity(ExternalDiseaseType.class);
  Alias.Permanent<ExternalEncounter> externalEncounter = Alias.forPermanentEntity(ExternalEncounter.class);
  Alias.Effective<ExternalEpisode> externalEpisode = Alias.forEffectiveEntity(ExternalEpisode.class);
  Alias.Permanent<ExternalMedicalStaff> externalMedicalStaff = Alias.forPermanentEntity(ExternalMedicalStaff.class);
  Alias.Permanent<ExternalOrganization> externalOrganization = Alias.forPermanentEntity(ExternalOrganization.class);
  Alias.Permanent<ExternalPatient> externalPatient = Alias.forPermanentEntity(ExternalPatient.class);
  Alias.Effective<ExternalUser> externalUser = Alias.forEffectiveEntity(ExternalUser.class);
  Alias.Permanent<ExternalUserCareProviderMember> externalUserCareProviderMember = Alias.forPermanentEntity(ExternalUserCareProviderMember.class);
  Alias.Effective<ExternalPatientAllergy> externalPatientAllergy = Alias.forEffectiveEntity(ExternalPatientAllergy.class);
}
