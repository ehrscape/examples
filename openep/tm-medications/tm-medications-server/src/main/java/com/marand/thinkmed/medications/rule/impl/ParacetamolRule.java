package com.marand.thinkmed.medications.rule.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Opt;
import com.marand.maf.core.Pair;
import com.marand.maf.core.time.Intervals;
import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.openehr.medications.tdo.MedicationActionAction;
import com.marand.openehr.medications.tdo.MedicationAdministrationComposition;
import com.marand.openehr.medications.tdo.MedicationInstructionInstruction;
import com.marand.openehr.medications.tdo.MedicationOrderComposition;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.AdministrationResultEnum;
import com.marand.thinkmed.medications.AdministrationTypeEnum;
import com.marand.thinkmed.medications.administration.AdministrationFromEhrConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.business.util.MedicationsEhrUtils;
import com.marand.thinkmed.medications.business.util.TherapyIdUtils;
import com.marand.thinkmed.medications.business.util.TherapyUnitsConverter;
import com.marand.thinkmed.medications.converter.therapy.MedicationConverterSelector;
import com.marand.thinkmed.medications.converter.therapy.MedicationFromEhrConverter;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationHolderDto;
import com.marand.thinkmed.medications.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.ingredient.IngredientCalculator;
import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import com.marand.thinkmed.medications.rule.MedicationRule;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForAdministrationParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapiesParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapyParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleParameters;
import com.marand.thinkmed.medications.rule.result.ParacetamolRuleResult;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Nejc Korasa
 */
public class ParacetamolRule implements MedicationRule<ParacetamolRuleParameters, ParacetamolRuleResult>
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsDao medicationsDao;
  private ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder;
  private MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider;
  private MedicationsBo medicationsBo;
  private IngredientCalculator ingredientCalculator;
  private AdministrationFromEhrConverter administrationFromEhrConverter;
  private AdministrationUtils administrationUtils;
  private MedicationsTasksProvider medicationsTasksProvider;

  private static final double PARACETAMOL_RULE_UNDERAGE_MAX_MG_PER_KG_PER_DAY = 60;  // 60mg/kg/day
  private static final double PARACETAMOL_RULE_MAX_MG_PER_DAY_KG_LEVEL_1 = 2000;  // 2g per day
  private static final double PARACETAMOL_RULE_MAX_MG_PER_DAY_KG_LEVEL_2 = 4000;  // 4g per day
  private static final double PARACETAMOL_RULE_KG_LEVEL_LIMIT = 50;  // 50kg

  private static final double UNDERAGE_LIMIT = 16;
  private static final int PARACETAMOL_BETWEEN_DOSES_LIMIT = 4; // in hours

  @Required
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Required
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Required
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Required
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Required
  public void setMedicationDataProvider(final MedicationFromEhrConverter.MedicationDataProvider medicationDataProvider)
  {
    this.medicationDataProvider = medicationDataProvider;
  }

  @Required
  public void setMedicationsValueHolder(final ValueHolder<Map<Long, MedicationHolderDto>> medicationsValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
  }

  @Required
  public void setIngredientCalculator(final IngredientCalculator ingredientCalculator)
  {
    this.ingredientCalculator = ingredientCalculator;
  }

  @Required
  public void setAdministrationFromEhrConverter(final AdministrationFromEhrConverter administrationFromEhrConverter)
  {
    this.administrationFromEhrConverter = administrationFromEhrConverter;
  }

  @Required
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Override
  public ParacetamolRuleResult applyRule(
      @Nonnull final ParacetamolRuleParameters parameters,
      @Nonnull final DateTime actionTimestamp,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(parameters, "parameters must not be null");
    Preconditions.checkNotNull(actionTimestamp, "actionTimestamp must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final MedicationParacetamolRuleType medicationParacetamolRuleType = parameters.getMedicationParacetamolRuleType();

    if (medicationParacetamolRuleType == MedicationParacetamolRuleType.FOR_THERAPY)
    {
      final TherapyDto therapyDto = ((ParacetamolRuleForTherapyParameters)parameters).getTherapyDto();
      final List<MedicationDataDto> medicationDataDtoList = ((ParacetamolRuleForTherapyParameters)parameters).getMedicationDataDtoList();

      Preconditions.checkNotNull(therapyDto, "therapyDto must not be null");
      Preconditions.checkNotNull(medicationDataDtoList, "medicationDataDtoList must not be null");
      Preconditions.checkArgument(!medicationDataDtoList.isEmpty(), "medicationDataDtoList must not be empty");

      return applyRuleForTherapy(
          therapyDto,
          medicationDataDtoList,
          parameters.getPatientWeight(),
          parameters.getPatientAgeInYears(),
          locale);
    }
    else if (medicationParacetamolRuleType == MedicationParacetamolRuleType.FOR_THERAPIES)
    {
      final List<TherapyDto> therapies = ((ParacetamolRuleForTherapiesParameters)parameters).getTherapies();
      final Interval searchInterval = ((ParacetamolRuleForTherapiesParameters)parameters).getSearchInterval();
      final String patientId = ((ParacetamolRuleForTherapiesParameters)parameters).getPatientId();

      Preconditions.checkNotNull(therapies, "therapies must not be null");
      Preconditions.checkNotNull(searchInterval, "searchInterval must not be null");
      Preconditions.checkNotNull(patientId, "patientId must not be null");

      return applyRuleForTherapies(
          therapies,
          parameters.getPatientWeight(),
          parameters.getPatientAgeInYears(),
          actionTimestamp,
          patientId,
          locale);
    }
    else if (medicationParacetamolRuleType == MedicationParacetamolRuleType.FOR_ADMINISTRATION)
    {
      final TherapyDoseDto therapyDoseDto = ((ParacetamolRuleForAdministrationParameters)parameters).getTherapyDoseDto();
      final String administrationId = ((ParacetamolRuleForAdministrationParameters)parameters).getAdministrationId();
      final String taskId = ((ParacetamolRuleForAdministrationParameters)parameters).getTaskId();
      final TherapyDto therapyDto = ((ParacetamolRuleForAdministrationParameters)parameters).getTherapyDto();
      final Interval searchInterval = ((ParacetamolRuleForAdministrationParameters)parameters).getSearchInterval();
      final String patientId = ((ParacetamolRuleForAdministrationParameters)parameters).getPatientId();

      Preconditions.checkNotNull(searchInterval, "searchInterval must not be null");
      Preconditions.checkNotNull(patientId, "patientId must not be null");

      return applyRuleForAdministration(
          therapyDoseDto,
          administrationId,
          taskId,
          therapyDto,
          parameters.getPatientWeight(),
          parameters.getPatientAgeInYears(),
          searchInterval,
          actionTimestamp,
          patientId,
          locale);
    }
    else
    {
      throw new IllegalArgumentException("Not supported medication paracetamol rule type");
    }
  }

  private ParacetamolRuleResult applyRuleForTherapies(
      final List<TherapyDto> basketTherapies,
      final Double patientWeight,
      final Long patientAgeInYears,
      final DateTime when,
      final String patientId,
      final Locale locale)
  {
    if (patientWeight == null)
    {
      return createResultWithErrorMessage(Dictionary.getEntry("paracetamol.patient.weight.missing", locale));
    }

    final List<TherapyDto> basketTherapiesWithParacetamol = basketTherapies
        .stream()
        .filter(this::isTherapyWithParacetamol)
        .collect(Collectors.toList());

    if (basketTherapiesWithParacetamol.isEmpty())
    {
      final ParacetamolRuleResult medicationIngredientRuleDto = new ParacetamolRuleResult();
      medicationIngredientRuleDto.setQuantityOk(true);
      return medicationIngredientRuleDto;
    }

    final Set<Long> medicationIdsWithParacetamolIngredient =
        medicationsDao.getMedicationIdsWithIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE, when);

    final Collection<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(when), null)
            .stream()
            .filter(
                instructionPair ->
                {
                  final MedicationOrderComposition composition = instructionPair.getFirst();
                  final MedicationInstructionInstruction instruction = instructionPair.getSecond();

                  return !medicationsBo.isTherapySuspended(composition, instruction)
                      && !medicationsBo.isTherapyCancelledOrAborted(composition, instruction);
                })
            .collect(Collectors.toList());

    final Map<Long, MedicationDataDto> medicationDataDtoWithParacetamolMap =
        medicationsDao.getMedicationDataMap(medicationIdsWithParacetamolIngredient, null, when);

    final List<TherapyDto> therapies = new ArrayList<>(basketTherapiesWithParacetamol);
    therapies.addAll(extractTherapiesFromInstructions(medicationInstructions, when));

    final double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        therapies,
        medicationDataDtoWithParacetamolMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    final List<NamedExternalDto> basketParacetamolMedications = basketTherapiesWithParacetamol
        .stream()
        .flatMap(t -> t.getMedications().stream())
        .map(m -> new NamedExternalDto(String.valueOf(m.getId()), m.getName()))
        .collect(Collectors.toList());

    return calculateRuleResult(
        ingredientQuantityInTherapies,
        patientWeight,
        patientAgeInYears,
        basketParacetamolMedications,
        locale);
  }

  private boolean isTherapyWithParacetamol(final TherapyDto therapyDto)
  {
    return therapyDto.getMedications()
        .stream()
        .map(m -> medicationsValueHolder.getValue().get(m.getId()))
        .anyMatch(m -> m != null && m.getMedicationRules().contains(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE));
  }

  private ParacetamolRuleResult applyRuleForAdministration(
      final TherapyDoseDto currentAdministrationTherapyDoseDto,
      final String administrationId,
      final String taskId,
      final TherapyDto currentTherapyDto,
      final Double patientWeight,
      final Long patientAgeInYears,
      final Interval searchInterval,
      final DateTime when,
      final String patientId,
      final Locale locale)
  {
    if (patientWeight == null)
    {
      return createResultWithErrorMessage(Dictionary.getEntry(
          "paracetamol.patient.weight.missing",
          locale));
    }

    final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructions =
        medicationsOpenEhrDao.findMedicationInstructions(patientId, Intervals.infiniteFrom(searchInterval.getStart()), null);

    final Map<String, List<MedicationAdministrationComposition>> administrations =
        medicationsOpenEhrDao.getTherapiesAdministrations(patientId, instructions, null);

    final Set<Long> medicationIdsWithParacetamol = medicationsDao.getMedicationIdsWithIngredientRule(
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        when);

    final Multimap<String, AdministrationDto> paracetamolAdministrationDtoMap = extractGivenAdministrationsWithMedications(
        instructions,
        administrations,
        administrationId,
        medicationIdsWithParacetamol);

    final Map<Long, MedicationDataDto> paracetamolMedicationDataMap = medicationsDao.getMedicationDataMap(
        new HashSet<>(medicationIdsWithParacetamol),
        null,
        when);

    final Map<String, TherapyDto> therapyDtoMap = buildTherapyDtosForTherapyIds(
        when,
        instructions,
        paracetamolAdministrationDtoMap.keySet());

    final double ingredientQuantity = ingredientCalculator.calculateIngredientQuantityInAdministrations(
        currentAdministrationTherapyDoseDto,
        currentTherapyDto,
        paracetamolAdministrationDtoMap,
        therapyDtoMap,
        paracetamolMedicationDataMap,
        searchInterval,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    final Collection<String> therapyIdsWithParacetamol = getTherapyIdsWithMedicationIds(
        instructions,
        medicationIdsWithParacetamol);

    final Opt<DateTime> lastAdministrationTime = getMostRecentAdministrationTime(
        searchInterval,
        paracetamolAdministrationDtoMap.values(),
        EnumSet.of(AdministrationTypeEnum.START, AdministrationTypeEnum.ADJUST_INFUSION));

    final Opt<DateTime> mostRecentPlannedTaskTimeAfterLastAdministration = getMostRecentPlannedTaskTime(
        patientId,
        taskId,
        therapyIdsWithParacetamol,
        searchInterval,
        lastAdministrationTime,
        EnumSet.of(AdministrationTypeEnum.START, AdministrationTypeEnum.ADJUST_INFUSION));

    final Opt<DateTime> lastParacetamolActionTime = mostRecentPlannedTaskTimeAfterLastAdministration.or(() -> lastAdministrationTime);
    final boolean lastParacetamolActionAdministered = !mostRecentPlannedTaskTimeAfterLastAdministration.isPresent();

    return calculateAdministrationRuleResult(
        ingredientQuantity,
        searchInterval,
        patientWeight,
        patientAgeInYears,
        lastParacetamolActionTime,
        lastParacetamolActionAdministered,
        locale);
  }

  private Opt<DateTime> getMostRecentAdministrationTime(
      final Interval searchInterval,
      final Collection<AdministrationDto> administrations,
      final EnumSet<AdministrationTypeEnum> administrationTypes)
  {
    return Opt.from(
        administrations
            .stream()
            .filter(t -> administrationTypes.contains(t.getAdministrationType()))
            .map(AdministrationDto::getAdministrationTime)
            .filter(t -> searchInterval.contains(t) || searchInterval.getEnd().equals(t))
            .max(Comparator.naturalOrder()));
  }

  private Opt<DateTime> getMostRecentPlannedTaskTime(
      final String patientId,
      final String excludeTaskId,
      final Collection<String> therapyIds,
      final Interval searchInterval,
      final Opt<DateTime> after,
      final EnumSet<AdministrationTypeEnum> administrationTypes)
  {
    return Opt.from(
        medicationsTasksProvider.findAdministrationTasks(patientId, therapyIds, searchInterval, false)
            .stream()
            .filter(t -> administrationTypes.contains(t.getAdministrationTypeEnum()))
            .filter(t -> excludeTaskId == null || !excludeTaskId.equals(t.getTaskId()))
            .map(AdministrationTaskDto::getPlannedAdministrationTime)
            .filter(time -> after.isAbsent() || time.isAfter(after.get()))
            .max(Comparator.naturalOrder()));
  }

  private Collection<String> getTherapyIdsWithMedicationIds(
      final Collection<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> instructions,
      final Collection<Long> medicationIdsWithParacetamol)
  {
    return instructions
        .stream()
        .filter(pair -> medicationsBo.getMedicationIds(pair.getSecond().getOrder().get(0))
            .stream()
            .anyMatch(medicationIdsWithParacetamol::contains))
        .map(pair -> TherapyIdUtils.createTherapyId(pair.getFirst(), pair.getSecond()))
        .collect(Collectors.toList());
  }

  // returns map of therapyId and administrationDto List
  private Multimap<String, AdministrationDto> extractGivenAdministrationsWithMedications(
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      final Map<String, List<MedicationAdministrationComposition>> therapiesAdministrations,
      final String excludeAdministrationId,
      final Set<Long> medicationIdsWithParacetamol)
  {
    final Multimap<String, AdministrationDto> administrationDtoMultimap = ArrayListMultimap.create();
    final Map<String, List<AdministrationDto>> administrationDtoMap = new HashMap<>();

    for (final String therapyId : therapiesAdministrations.keySet())
    {
      List<MedicationAdministrationComposition> administrations = therapiesAdministrations.get(therapyId);
      administrations = administrations
          .stream()
          .filter(administration -> !administration.getMedicationDetail().getMedicationAction().isEmpty())
          .collect(Collectors.toList());

      for (final MedicationAdministrationComposition administrationComp : administrations)
      {
        final MedicationActionAction action = administrationComp.getMedicationDetail().getMedicationAction().get(0);
        final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstructionPair =
            MedicationsEhrUtils.findMedicationInstructionPairByTherapyId(therapyId, medicationInstructions);

        final AdministrationDto administrationDto = administrationFromEhrConverter.convertToAdministrationDto(
            administrationComp,
            medicationInstructionPair,
            therapyId);

        final boolean excludeAdministration = excludeAdministrationId == null
            || !excludeAdministrationId.equals(administrationDto.getAdministrationId());

        if (isGivenAndContainsMedication(action, medicationIdsWithParacetamol) && excludeAdministration)
        {
          administrationDtoMultimap.put(therapyId, administrationDto);
          addAdministrationToAdministrationDtoMap(administrationDtoMap, therapyId, administrationDto);
        }
      }
    }
    return administrationDtoMultimap;
  }

  private boolean isGivenAndContainsMedication(final MedicationActionAction action, final Set<Long> medicationIds)
  {
    final AdministrationResultEnum result = administrationUtils.getAdministrationResult(action);
    if (result == AdministrationResultEnum.GIVEN || result == AdministrationResultEnum.SELF_ADMINISTERED)
    {
      return medicationsBo.getMedicationIds(action).stream().anyMatch(medicationIds::contains);
    }
    return false;
  }

  final ParacetamolRuleResult calculateAdministrationRuleResult(
      final double dailyDosage,
      final Interval searchInterval,
      final double patientWeight,
      final Long patientAge,
      final Opt<DateTime> lastParacetamolActionTime,
      final boolean lastParacetamolActionAdministered,
      final Locale locale)
  {
    final ParacetamolRuleResult result = calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAge,
        Collections.emptyList(),
        locale);

    setBetweenDosesLimitRule(searchInterval, lastParacetamolActionTime, lastParacetamolActionAdministered, result);
    return result;
  }

  private void setBetweenDosesLimitRule(
      final Interval searchInterval,
      final Opt<DateTime> lastParacetamolActionTime,
      final boolean lastParacetamolActionAdministered,
      final ParacetamolRuleResult paracetamolRuleResult)
  {
    final DateTime lastAllowedAdministrationTime = searchInterval.getEnd().minusHours(PARACETAMOL_BETWEEN_DOSES_LIMIT);

    if (lastParacetamolActionTime.isPresent()
        && (lastParacetamolActionTime.get().isAfter(lastAllowedAdministrationTime)
        || lastParacetamolActionTime.get().isEqual(lastAllowedAdministrationTime)))
    {
      paracetamolRuleResult.setLastTaskTimestamp(lastParacetamolActionTime.get());
      paracetamolRuleResult.setBetweenDosesTimeOk(false);
      paracetamolRuleResult.setLastTaskAdministered(lastParacetamolActionAdministered);
    }
  }

  private Map<String, TherapyDto> buildTherapyDtosForTherapyIds(
      final DateTime when,
      final List<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      final Set<String> therapyIds)
  {
    final Map<String, TherapyDto> therapyDtoMap = new HashMap<>();
    for (final String therapyId : therapyIds)
    {
      final Pair<MedicationOrderComposition, MedicationInstructionInstruction> instructionPair =
          MedicationsEhrUtils.findMedicationInstructionPairByTherapyId(therapyId, medicationInstructions);

      final MedicationOrderComposition composition = instructionPair.getFirst();
      final MedicationInstructionInstruction instruction = instructionPair.getSecond();

      final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);

      final TherapyDto therapyDto = converter.createTherapyFromInstruction(
          instruction,
          composition.getUid().getValue(),
          instruction.getName().getValue(),
          DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
          when,
          medicationDataProvider);

      therapyDtoMap.put(therapyId, therapyDto);
    }
    return therapyDtoMap;
  }

  private void addAdministrationToAdministrationDtoMap(
      final Map<String, List<AdministrationDto>> administrationDtoMap,
      final String therapyId,
      final AdministrationDto administrationDto)
  {
    if (administrationDtoMap.containsKey(therapyId))
    {
      administrationDtoMap.get(therapyId).add(administrationDto);
    }
    else
    {
      final List<AdministrationDto> administrationDtoList = new ArrayList<>();
      administrationDtoList.add(administrationDto);
      administrationDtoMap.put(therapyId, administrationDtoList);
    }
  }

  private List<TherapyDto> extractTherapiesFromInstructions(
      final Collection<Pair<MedicationOrderComposition, MedicationInstructionInstruction>> medicationInstructions,
      final DateTime when)
  {
    final List<TherapyDto> therapies = new ArrayList<>();
    for (final Pair<MedicationOrderComposition, MedicationInstructionInstruction> medicationInstruction : medicationInstructions)
    {
      final MedicationOrderComposition composition = medicationInstruction.getFirst();
      final MedicationInstructionInstruction instruction = medicationInstruction.getSecond();
      final MedicationFromEhrConverter<?> converter = MedicationConverterSelector.getConverter(instruction);

      final TherapyDto therapyDto = converter.createTherapyFromInstruction(
          instruction,
          composition.getUid().getValue(),
          instruction.getName().getValue(),
          DataValueUtils.getDateTime(composition.getCompositionEventContext().getStartTime()),
          when,
          medicationDataProvider);

      therapies.add(therapyDto);
    }

    return therapies;
  }

  private ParacetamolRuleResult applyRuleForTherapy(
      final TherapyDto therapyDto,
      final List<MedicationDataDto> medicationDataDtoList,
      final Double patientWeight,
      final Long patientAgeInYears,
      final Locale locale)
  {
    if (patientWeight == null)
    {
      return createResultWithErrorMessage(Dictionary.getEntry("paracetamol.patient.weight.missing", locale));
    }

    final Map<Long, MedicationDataDto> medicationDataDtoMap = medicationDataDtoList
        .stream()
        .filter(medicationDataDto -> medicationDataDto != null)
        .collect(Collectors.toMap(
            medicationDataDto -> medicationDataDto.getMedication().getId(),
            medicationDataDto -> medicationDataDto,
            (m1, m2) -> m1));

    final double ingredientQuantityInTherapies = ingredientCalculator.calculateIngredientQuantityInTherapies(
        Collections.singletonList(therapyDto),
        medicationDataDtoMap,
        null,
        MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        "mg");

    final List<NamedExternalDto> medications = therapyDto.getMedications()
        .stream()
        .map(m -> new NamedExternalDto(String.valueOf(m.getId()), m.getName()))
        .collect(Collectors.toList());

    return calculateRuleResult(
        ingredientQuantityInTherapies,
        patientWeight,
        patientAgeInYears,
        medications,
        locale);
  }

  ParacetamolRuleResult calculateRuleResult(
      final double dose,
      final double patientWeight,
      final Long patientAge,
      final List<NamedExternalDto> medications,
      final Locale locale)
  {
    final ParacetamolRuleResult result = new ParacetamolRuleResult();
    result.setMedications(medications);

    final boolean patientUnderaged = isPatientUnderaged(patientAge);
    final double underageDoseLimit = getUnderageDoseLimit(patientWeight);
    final boolean underageOverdose = patientUnderaged && dose > underageDoseLimit;

    final double adultDoseLimit = getAdultDoseLimit(patientWeight);
    final boolean adultOverdose = dose > adultDoseLimit;

    result.setQuantityOk(!underageOverdose && !adultOverdose);
    result.setAdultRulePercentage(Math.ceil(dose / adultDoseLimit * 100));
    result.setUnderageRulePercentage(patientUnderaged ? Math.ceil(dose / underageDoseLimit * 100) : null);

    if (patientUnderaged && result.getUnderageRulePercentage() > result.getAdultRulePercentage())
    {
      result.setRule(getUnderageRuleDescription(locale));
    }
    if (adultOverdose)
    {
      result.setRule(getAdultRuleDescription(locale, adultDoseLimit));
    }

    return result;
  }

  private String getAdultRuleDescription(final Locale locale, final Double maxDailyLimit)
  {
    return TherapyUnitsConverter.convertToUnit(maxDailyLimit, "mg", "g")
        + "g "
        + Dictionary.getEntry("per.day", locale);
  }

  private String getUnderageRuleDescription(final Locale locale)
  {
    return (int)PARACETAMOL_RULE_UNDERAGE_MAX_MG_PER_KG_PER_DAY
        + "mg/kg/"
        + Dictionary.getEntry("day", locale);
  }

  private ParacetamolRuleResult createResultWithErrorMessage(final String errorMessage)
  {
    final ParacetamolRuleResult paracetamolRuleResult = new ParacetamolRuleResult();
    paracetamolRuleResult.setQuantityOk(false);
    paracetamolRuleResult.setErrorMessage(errorMessage);
    return paracetamolRuleResult;
  }

  private double getAdultDoseLimit(final double referenceWeight)
  {
    return referenceWeight > PARACETAMOL_RULE_KG_LEVEL_LIMIT ?
           PARACETAMOL_RULE_MAX_MG_PER_DAY_KG_LEVEL_2 :
           PARACETAMOL_RULE_MAX_MG_PER_DAY_KG_LEVEL_1;
  }

  private double getUnderageDoseLimit(final double patientWeight)
  {
    return patientWeight * PARACETAMOL_RULE_UNDERAGE_MAX_MG_PER_KG_PER_DAY;
  }

  private boolean isPatientUnderaged(final Long patientAgeInYears)
  {
    return patientAgeInYears != null && patientAgeInYears < UNDERAGE_LIMIT;
  }
}
