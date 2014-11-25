package com.marand.thinkmed.fdb;

import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.fdb.dto.FdbEnums;
import com.marand.thinkmed.fdb.dto.FdbPatientDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningDto;
import com.marand.thinkmed.fdb.dto.FdbScreeningResultDto;
import com.marand.thinkmed.fdb.dto.FdbTerminologyDto;
import com.marand.thinkmed.fdb.rest.FdbRestService;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

/**
 * @author Mitja Lapajne
 */

public class FdbTestTool
{
  private FdbTestTool()
  {
  }

  public static void main(final String[] args)
  {
    final FdbRestService service;
    final HttpClientContext context = HttpClientContext.create();
    final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(
        new AuthScope("fdbe-mddfrest.amaze.com", 80),
        new UsernamePasswordCredentials("trialuser", "trial"));
    context.setCredentialsProvider(credentialsProvider);

    final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
        new ApacheHttpClient4Engine(
            HttpClientBuilder.create().setConnectionManager(
                new PoolingHttpClientConnectionManager()).build(), context)).build();
    final ResteasyWebTarget target = client.target("http://fdbe-mddfrest.amaze.com/mddfrest/screening/reports");
    service = target.proxy(FdbRestService.class);

    final FdbScreeningDto screeningDto = buildTestFdbScreeningDto();
    final String json = JsonUtil.toJson(screeningDto);
    //final String json = "{\"ScreeningModules\":[4,64,16,2,1,32,128],\"PatientInformation\":{\"Gender\":{\"Name\":\"Male\",\"Value\":1},\"Age\":69,\"Weight\":20.0,\"BodySurfaceArea\":1.0},\"CurrentDrugs\":[{\"Id\":915147010,\"Name\":\"Salbutamol 100micrograms/dose inhaler\",\"Terminology\":{\"Name\":\"MDDF\",\"Value\":1},\"ConceptType\":{\"Name\":\"Product\",\"Value\":2}},{\"Id\":744663863,\"Name\":\"Nuelin SA 250 tablets (Meda Pharmaceuticals Ltd)\",\"Terminology\":{\"Name\":\"MDDF\",\"Value\":1},\"ConceptType\":{\"Name\":\"Product\",\"Value\":2}}],\"ProspectiveDrugs\":[],\"CheckAllDrugs\":true,\"ValidateInput\":true,\"MinimumConditionAlertSeverity\":{\"Name\":\"Precaution\",\"Value\":2},\"MinimumInteractionAlertSeverity\":{\"Name\":\"LowRisk\",\"Value\":1}}";
    //final String json1 = "{\"ScreeningModules\":[4,64,16,2,1,32,128],\"PatientInformation\":{\"Gender\":{\"Name\":\"Male\",\"Value\":1},\"Age\":35,\"Weight\":80.0,\"BodySurfaceArea\":1.5},\"CurrentDrugs\":[],\"ProspectiveDrugs\":[{\"Id\":\"38268001\",\"Name\":\"Ibuprofen\",\"Terminology\":{\"Name\":\"SNoMedCT\",\"Value\":2},\"ConceptType\":{\"Name\":\"Drug\",\"Value\":1}},{\"Id\":\"48603004\",\"Name\":\"Warfarine\",\"Terminology\":{\"Name\":\"SNoMedCT\",\"Value\":2},\"ConceptType\":{\"Name\":\"Drug\",\"Value\":1}}],\"CheckAllDrugs\":true,\"ValidateInput\":true,\"MinimumConditionAlertSeverity\":{\"Name\":\"Precaution\",\"Value\":2},\"MinimumInteractionAlertSeverity\":{\"Name\":\"LowRisk\",\"Value\":1}}";

    //System.out.println(json);
    //System.out.println(json1);
    final String warningJson = service.scanForWarnings(json);
    final FdbScreeningResultDto resultDto = JsonUtil.fromJson(warningJson, FdbScreeningResultDto.class);

    System.out.println(warningJson);
  }

  private static FdbScreeningDto buildTestFdbScreeningDto()
  {
    final FdbScreeningDto screeningDto = new FdbScreeningDto();

    screeningDto.getScreeningModules().add(4);
    screeningDto.getScreeningModules().add(64);
    screeningDto.getScreeningModules().add(16);
    screeningDto.getScreeningModules().add(2);
    screeningDto.getScreeningModules().add(1);
    screeningDto.getScreeningModules().add(32);
    screeningDto.getScreeningModules().add(128);

    final FdbPatientDto patientInformation = new FdbPatientDto();
    patientInformation.setGender(FdbEnums.GENDER_MALE.getNameValue());
    patientInformation.setAge(35L);
    patientInformation.setWeight(80.0);
    patientInformation.setBodySurfaceArea(1.5);
    screeningDto.setPatientInformation(patientInformation);

    //final FdbDrugDto currentDrug1 = new FdbDrugDto();
    //currentDrug1.setId("2002914");
    //currentDrug1.setName("Salbutamol 100 microgram");
    //currentDrug1.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug1.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug1);
    //
    //final FdbDrugDto currentDrug2 = new FdbDrugDto();
    //currentDrug2.setId("1013679");
    //currentDrug2.setName("Serovent Evohaler");
    //currentDrug2.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug2.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug2);
    //
    //final FdbDrugDto currentDrug3 = new FdbDrugDto();
    //currentDrug3.setId("1014947");
    //currentDrug3.setName("Spiriva 18microgram");
    //currentDrug3.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug3.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug3);
    //
    //final FdbDrugDto currentDrug4 = new FdbDrugDto();
    //currentDrug4.setId("1001191");
    //currentDrug4.setName("Nuelin 250");
    //currentDrug4.setTerminology(FdbEnums.MDDF_TERMINOLOGY.getNameValue());
    //currentDrug4.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getCurrentDrugs().add(currentDrug4);



    //final FdbDrugDto prospectiveDrug1 = new FdbDrugDto();
    //prospectiveDrug1.setId("38268001");
    //prospectiveDrug1.setName("Ibuprofen");
    //prospectiveDrug1.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug1.setConceptType(FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug1);
    //
    //final FdbDrugDto prospectiveDrug2 = new FdbDrugDto();
    //prospectiveDrug2.setId("48603004");
    //prospectiveDrug2.setName("Warfarine");
    //prospectiveDrug2.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug2.setConceptType(FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug2);




    //final FdbDrugDto prospectiveDrug1 = new FdbDrugDto();
    //prospectiveDrug1.setId("329708004");
    //prospectiveDrug1.setName("Ibuprofen 800mg tablets");
    //prospectiveDrug1.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug1.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug1);
    //
    //final FdbDrugDto prospectiveDrug2 = new FdbDrugDto();
    //prospectiveDrug2.setId("319735007");
    //prospectiveDrug2.setName("Warfarin 5mg tablets");
    //prospectiveDrug2.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug2.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug2);



    //
    //final FdbTerminologyDto prospectiveDrug1 = new FdbTerminologyDto();
    //prospectiveDrug1.setId("892211000001107");
    //prospectiveDrug1.setName("Ibuprofen 200mg tablets (Aspar Pharmaceuticals Ltd)");
    //prospectiveDrug1.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    //prospectiveDrug1.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    //screeningDto.getProspectiveDrugs().add(prospectiveDrug1);

    final FdbTerminologyDto prospectiveDrug2 = new FdbTerminologyDto();
    //prospectiveDrug2.setId("27658006");
    prospectiveDrug2.setId("384611000001100");
    prospectiveDrug2.setName("Amoxicillin");
    prospectiveDrug2.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    prospectiveDrug2.setConceptType(FdbEnums.PRODUCT_CONCEPT_TYPE.getNameValue());
    screeningDto.getProspectiveDrugs().add(prospectiveDrug2);

    final FdbTerminologyDto allergen = new FdbTerminologyDto();
    allergen.setId("91936005");
    allergen.setName("Penicillin");
    allergen.setTerminology(FdbEnums.SNOMED_TERMINOLOGY.getNameValue());
    allergen.setConceptType(FdbEnums.DRUG_CONCEPT_TYPE.getNameValue());
    screeningDto.getAllergens().add(allergen);

    screeningDto.setCheckAllDrugs(true);
    screeningDto.setValidateInput(true);
    screeningDto.setMinimumConditionAlertSeverity(FdbEnums.MINIMUM_CONDITION_ALERT_SEVERITY_PRECAUTION.getNameValue());
    screeningDto.setMinimumInteractionAlertSeverity(FdbEnums.MINIMUM_INTERACTION_ALERT_SEVERITY_LOW_RISK.getNameValue());
    return screeningDto;
  }
}
