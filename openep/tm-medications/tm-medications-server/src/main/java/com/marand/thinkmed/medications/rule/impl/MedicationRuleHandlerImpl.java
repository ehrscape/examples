package com.marand.thinkmed.medications.rule.impl;

import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.marand.thinkmed.medications.rule.MedicationRule;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.MedicationRuleHandler;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;

/**
 * @author Nejc Korasa
 */
public class MedicationRuleHandlerImpl implements MedicationRuleHandler
{
  private Map<MedicationRuleEnum, MedicationRule<RuleParameters, RuleResult>> medicationRules;

  @Required
  public void setMedicationRules(final Map<MedicationRuleEnum, MedicationRule<RuleParameters, RuleResult>> medicationRules)
  {
    this.medicationRules = medicationRules;
  }

  @Override
  public RuleResult applyMedicationRule(
      @Nonnull final RuleParameters ruleParameters,
      @Nonnull final DateTime actionTimestamp,
      @Nonnull final Locale locale)
  {
    Preconditions.checkNotNull(ruleParameters, "ruleParameters must not be null");
    Preconditions.checkNotNull(ruleParameters.getMedicationRuleEnum(), "medicationRuleEnum must not be null");
    Preconditions.checkNotNull(actionTimestamp, "actionTimestamp must not be null");
    Preconditions.checkNotNull(locale, "locale must not be null");

    final MedicationRule<RuleParameters, RuleResult> medicationRule = medicationRules.get(ruleParameters.getMedicationRuleEnum());

    if (medicationRule != null)
    {
      return medicationRule.applyRule(ruleParameters, actionTimestamp, locale);
    }

    return null;
  }
}
