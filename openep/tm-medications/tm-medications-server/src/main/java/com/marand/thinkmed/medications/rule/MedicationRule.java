package com.marand.thinkmed.medications.rule;

import java.util.Locale;

import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationRule<P extends RuleParameters, R extends RuleResult>
{
  R applyRule(P parameters, DateTime actionTimestamp, Locale locale);
}
