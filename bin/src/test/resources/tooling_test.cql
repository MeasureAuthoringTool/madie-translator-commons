library CMS986v4 version '1.0.000'

using QDM version '5.6'

include MATGlobalCommonFunctionsQDM version '8.0.000' called Global

codesystem "ICD10CM": 'urn:oid:2.16.840.1.113883.6.90'
codesystem "SNOMEDCT": 'urn:oid:2.16.840.1.113883.6.96'
codesystem "LOINC": 'urn:oid:2.16.840.1.113883.6.1' 

valueset "Encounter Inpatient": 'urn:oid:2.16.840.1.113883.3.666.5.307'
valueset "Ethnicity": 'urn:oid:2.16.840.1.114222.4.11.837'
valueset "Hospital Dietitian Referral": 'urn:oid:2.16.840.1.113762.1.4.1095.91'
valueset "Malnutrition Diagnosis": 'urn:oid:2.16.840.1.113762.1.4.1095.55'
valueset "Malnutrition Risk Screening": 'urn:oid:2.16.840.1.113762.1.4.1095.92'
valueset "Malnutrition Screening Finding of At Risk Result": 'urn:oid:2.16.840.1.113762.1.4.1095.89'
valueset "Malnutrition Screening Finding of Not At Risk Result": 'urn:oid:2.16.840.1.113762.1.4.1095.34'
valueset "Nutrition Assessment": 'urn:oid:2.16.840.1.113762.1.4.1095.21'
valueset "Nutrition Assessment Status Finding of Moderately Malnourished": 'urn:oid:2.16.840.1.113762.1.4.1095.47'
valueset "Nutrition Assessment Status Finding of Severely Malnourished": 'urn:oid:2.16.840.1.113762.1.4.1095.43'
valueset "Nutrition Assessment Status Finding of Well Nourished or Not Malnourished or Mildly Malnourished": 'urn:oid:2.16.840.1.113762.1.4.1095.96'
valueset "Nutrition Care Plan": 'urn:oid:2.16.840.1.113762.1.4.1095.93'
valueset "ONC Administrative Sex": 'urn:oid:2.16.840.1.113762.1.4.1'
valueset "Payer Type": 'urn:oid:2.16.840.1.114222.4.11.3591'
valueset "Race": 'urn:oid:2.16.840.1.114222.4.11.836'

code "Status post administration of tPA (rtPA) in a different facility within the last 24 hours prior to admission to current facility": 'Z92.82' from "ICD10CM" display 'Status post administration of tPA (rtPA) in a different facility within the last 24 hours prior to admission to current facility'
code "Angina pectoris with documented spasm": 'I20.1' from "ICD10CM" display 'Angina pectoris with documented spasm'
code "Takotsubo syndrome": 'I51.81' from "ICD10CM" display 'Takotsubo syndrome'


parameter "Measurement Muggle" Interval<DateTime>

parameter "Measurement Period" Interval<DateTime>

context Patient

define "SDE Payer":
  ["Patient Characteristic Payer": "Payer Type"]

define "SDE Ethnicity":
  ["Patient Characteristic Ethnicity": "Ethnicity"]

define "SDE Race":
  ["Patient Characteristic Race": "Race"]

define "SDE Sex":
  ["Patient Characteristic Sex": "ONC Administrative Sex"]

define "Measure Population":
  "Initial Population"

define "Initial Population":
  ["Encounter, Performed": "Encounter Inpatient"] EncounterInpatient
    where AgeInYearsAt(date from start of EncounterInpatient.relevantPeriod) >= 65
      and duration in hours of EncounterInpatient.relevantPeriod >= 24
      and EncounterInpatient.relevantPeriod during day of "Measurement Period"

define "Encounter with Hospital Dietitian Referral":
  "Initial Population" QualifyingEncounter
    with ["Intervention, Order": "Hospital Dietitian Referral"] HospitalDietitianReferral
      such that HospitalDietitianReferral.authorDatetime during Global."HospitalizationWithObservation" ( QualifyingEncounter )

define "Encounter with Malnutrition Risk Screening and Identified Result or with Hospital Dietitian Referral":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Malnutrition Risk Screening"] MalnutritionRiskScreening
      such that Coalesce(start of Global."NormalizeInterval"(MalnutritionRiskScreening.relevantDatetime, MalnutritionRiskScreening.relevantPeriod), MalnutritionRiskScreening.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and ( ( MalnutritionRiskScreening.result in "Malnutrition Screening Finding of At Risk Result" )
            or ( MalnutritionRiskScreening.result in "Malnutrition Screening Finding of Not At Risk Result" )
        )
        or exists ( "Encounter with Hospital Dietitian Referral" )

define "Encounter with Malnutrition Screening Not At Risk Result":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Malnutrition Risk Screening"] MalnutritionRiskScreening
      such that Coalesce(start of Global."NormalizeInterval"(MalnutritionRiskScreening.relevantDatetime, MalnutritionRiskScreening.relevantPeriod), MalnutritionRiskScreening.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and MalnutritionRiskScreening.result in "Malnutrition Screening Finding of Not At Risk Result"

define "Encounter with Malnutrition Not at Risk Result and without Hospital Dietitian Referral":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Malnutrition Risk Screening"] MalnutritionRiskScreening
      such that exists ( "Encounter with Malnutrition Screening Not At Risk Result" )
        and not exists ( "Encounter with Hospital Dietitian Referral" )

define "Encounter with Malnutrition Screening At Risk Result":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Malnutrition Risk Screening"] MalnutritionRiskScreening
      such that Coalesce(start of Global."NormalizeInterval"(MalnutritionRiskScreening.relevantDatetime, MalnutritionRiskScreening.relevantPeriod), MalnutritionRiskScreening.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and MalnutritionRiskScreening.result in "Malnutrition Screening Finding of At Risk Result"
        and not ( exists "Encounter with Malnutrition Screening Not At Risk Result" )

define "Encounter with Nutrition Assessment and Identified Status":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Nutrition Assessment"] NutritionAssessment
      such that Coalesce(start of Global."NormalizeInterval"(NutritionAssessment.relevantDatetime, NutritionAssessment.relevantPeriod), NutritionAssessment.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and ( NutritionAssessment.result in "Nutrition Assessment Status Finding of Well Nourished or Not Malnourished or Mildly Malnourished"
            or ( NutritionAssessment.result in "Nutrition Assessment Status Finding of Moderately Malnourished" )
            or ( NutritionAssessment.result in "Nutrition Assessment Status Finding of Severely Malnourished" )
        )
        and ( exists "Encounter with Malnutrition Screening At Risk Result"
            or exists ( "Encounter with Hospital Dietitian Referral" )
        )

define "Encounter with Nutrition Assessment Status Not or Mildly Malnourished":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Nutrition Assessment"] NutritionAssessment
      such that Coalesce(start of Global."NormalizeInterval"(NutritionAssessment.relevantDatetime, NutritionAssessment.relevantPeriod), NutritionAssessment.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and NutritionAssessment.result in "Nutrition Assessment Status Finding of Well Nourished or Not Malnourished or Mildly Malnourished"

define "Encounter with Nutrition Assessment Status Finding of Moderately Malnourished":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Nutrition Assessment"] NutritionAssessment
      such that Coalesce(start of Global."NormalizeInterval"(NutritionAssessment.relevantDatetime, NutritionAssessment.relevantPeriod), NutritionAssessment.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and NutritionAssessment.result in "Nutrition Assessment Status Finding of Moderately Malnourished"

define "Encounter with Nutrition Assessment Status Finding of Severely Malnourished":
  "Initial Population" QualifyingEncounter
    with ["Assessment, Performed": "Nutrition Assessment"] NutritionAssessment
      such that Coalesce(start of Global."NormalizeInterval"(NutritionAssessment.relevantDatetime, NutritionAssessment.relevantPeriod), NutritionAssessment.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )
        and NutritionAssessment.result in "Nutrition Assessment Status Finding of Severely Malnourished"

define "Encounter with Malnutrition Diagnosis":
  "Initial Population" QualifyingEncounter
    with ["Diagnosis": "Malnutrition Diagnosis"] MalnutritionDiagnosis
      such that MalnutritionDiagnosis.prevalencePeriod overlaps Global."HospitalizationWithObservation" ( QualifyingEncounter )

define "Encounter with Nutrition Care Plan":
  "Initial Population" QualifyingEncounter
    with ["Intervention, Performed": "Nutrition Care Plan"] NutritionCarePlan
      such that Coalesce(start of Global."NormalizeInterval"(NutritionCarePlan.relevantDatetime, NutritionCarePlan.relevantPeriod), NutritionCarePlan.authorDatetime) during Global."HospitalizationWithObservation" ( QualifyingEncounter )

define function "Measure Observation 1"(Encounter "Encounter, Performed"):
  if ( "Encounter with Malnutrition Risk Screening and Identified Result or with Hospital Dietitian Referral" contains Encounter
      and ( "Encounter with Malnutrition Screening Not At Risk Result" contains Encounter
          or ( "Encounter with Malnutrition Screening At Risk Result" contains Encounter )
      )
      or exists ( "Encounter with Hospital Dietitian Referral" )
  ) then 1 
    else 0

define function "Measure Observation 2"(Encounter "Encounter, Performed"):
  if ( "Encounter with Malnutrition Not at Risk Result and without Hospital Dietitian Referral" ) contains Encounter then 0 
    else if ( "Encounter with Nutrition Assessment and Identified Status" contains Encounter
      and ( ( "Encounter with Nutrition Assessment Status Not or Mildly Malnourished" contains Encounter
            or "Encounter with Nutrition Assessment Status Finding of Moderately Malnourished" contains Encounter
            or "Encounter with Nutrition Assessment St...