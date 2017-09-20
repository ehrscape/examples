package com.marand.thinkmed.medications.rule;

import java.util.Locale;
import javax.annotation.Nonnull;

import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationRuleHandler
{
  RuleResult applyMedicationRule(
      @Nonnull RuleParameters ruleParameters,
      @Nonnull DateTime actionTimestamp,
      @Nonnull Locale locale);
}
