package com.marand.thinkmed.medications.warnings;

import java.util.Comparator;
import java.util.Objects;

import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medicationsexternal.WarningSeverity;
import com.marand.thinkmed.medicationsexternal.WarningType;
import com.marand.thinkmed.medicationsexternal.dto.MedicationsWarningDto;
import com.marand.thinkmed.medicationsexternal.service.MedicationsExternalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.marand.maf.core.SpringProxiedJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

@RunWith(MockitoJUnitRunner.class)
public class TherapyWarningsProviderTest
{
  @InjectMocks
  private TherapyWarningsProvider therapyWarningsProvider = new TherapyWarningsProvider();

  @Mock
  private MedicationsDao medicationsDao;

  @Mock
  private MedicationsExternalService medicationsExternalService;

  @Mock
  private MentalHealthWarningsHandler mentalHealthWarningsHandler;

  @Test
  public void compareSameMedicationsWarnings()
  {
    final Comparator<MedicationsWarningDto> comparator = therapyWarningsProvider.getMedicationsWarningDtoComparator();

    final MedicationsWarningDto o1 = getMedicationsWarningDto(WarningType.ALLERGY, WarningSeverity.SIGNIFICANT);
    final MedicationsWarningDto o2 = getMedicationsWarningDto(WarningType.ALLERGY, WarningSeverity.SIGNIFICANT);

    final int compare = Objects.compare(o1, o2, comparator);

    assertEquals(0, compare);
  }

  @Test
  public void compareUnmatchedWithOtherWarningType()
  {
    final Comparator<MedicationsWarningDto> comparator = therapyWarningsProvider.getMedicationsWarningDtoComparator();

    final MedicationsWarningDto o1 = getMedicationsWarningDto(WarningType.UNMATCHED, null);
    final MedicationsWarningDto o2 = getMedicationsWarningDto(WarningType.ALLERGY, WarningSeverity.SIGNIFICANT);

    final int compare = Objects.compare(o1, o2, comparator);
    final int reverseCompare = Objects.compare(o2, o1, comparator);

    assertTrue(compare < 0);
    assertEquals(0, compare + reverseCompare);
  }

  @Test
  public void compareSameSeverityDifferentWarningType()
  {
    final Comparator<MedicationsWarningDto> comparator = therapyWarningsProvider.getMedicationsWarningDtoComparator();

    final MedicationsWarningDto o1 = getMedicationsWarningDto(WarningType.DUPLICATE, WarningSeverity.SIGNIFICANT);
    final MedicationsWarningDto o2 = getMedicationsWarningDto(WarningType.ALLERGY, WarningSeverity.SIGNIFICANT);

    final int compare = Objects.compare(o1, o2, comparator);
    final int reverseCompare = Objects.compare(o2, o1, comparator);

    assertTrue(compare > 0);
    assertEquals(0, compare + reverseCompare);
  }

  @Test
  public void compareSameWarningTypeDifferentSeverity()
  {
    final Comparator<MedicationsWarningDto> comparator = therapyWarningsProvider.getMedicationsWarningDtoComparator();

    final MedicationsWarningDto o1 = getMedicationsWarningDto(WarningType.DUPLICATE, WarningSeverity.LOW);
    final MedicationsWarningDto o2 = getMedicationsWarningDto(WarningType.DUPLICATE, WarningSeverity.SIGNIFICANT);

    final int compare = Objects.compare(o1, o2, comparator);
    final int reverseCompare = Objects.compare(o2, o1, comparator);

    assertTrue(compare > 0);
    assertEquals(0, compare + reverseCompare);
  }

  @Test
  public void compareSameWarningTypeDifferentSeverity2()
  {
    final Comparator<MedicationsWarningDto> comparator = therapyWarningsProvider.getMedicationsWarningDtoComparator();

    final MedicationsWarningDto o1 = getMedicationsWarningDto(WarningType.DUPLICATE, WarningSeverity.LOW);
    final MedicationsWarningDto o2 = getMedicationsWarningDto(WarningType.DUPLICATE, WarningSeverity.HIGH);

    final int compare = Objects.compare(o1, o2, comparator);
    final int reverseCompare = Objects.compare(o2, o1, comparator);

    assertTrue(compare > 0);
    assertEquals(0, compare + reverseCompare);
  }

  private MedicationsWarningDto getMedicationsWarningDto(final WarningType warningType, final WarningSeverity severity)
  {
    final MedicationsWarningDto warningDto = new MedicationsWarningDto();
    warningDto.setSeverity(severity);
    warningDto.setType(warningType);
    return warningDto;
  }
}
