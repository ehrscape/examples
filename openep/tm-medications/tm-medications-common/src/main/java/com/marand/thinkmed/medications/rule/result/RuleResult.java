package com.marand.thinkmed.medications.rule.result;

/**
 * @author Nejc Korasa
 */
public interface RuleResult
{
  String getRule();

  void setRule(final String rule);

  String getErrorMessage();

  void setErrorMessage(final String errorMessage);
}
