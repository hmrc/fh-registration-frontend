package uk.gov.hmrc.fhregistrationfrontend.services.mapping.data

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object LtdLargeUk {
  val application = LimitedCompanyApplication(
    MainBusinessAddress(
      "Less than 3 years",
      Some(true),
      Some(Address(
        "Flat 1A",
        None,
        None,
        Some("TestTown"),
        "ZZ11 1ZZ",
        None
      ))
    ),
    ContactPerson(
      "Cosmin",
      "Marian",
      "Director",
      "11123",
      "a@w.ro",
      true,
      Some(true),
      Some(Address(
        "Flat 1B",
        None,
        None,
        Some("Testtown"),
        "ZZ11 1ZZ",
        None
      )),
      None
    ),
    CompanyRegistrationNumber("12345678"),
    DateOfIncorporation(LocalDate.of(2014, 3, 20)),
    TradingName(true, Some("DodgyCo")),
    VatNumber(true, Some("123456789")),
    List(CompanyOfficer(
      CompanyOfficerType.Individual,
      CompanyOfficerIndividual(
        "Cosmin",
        "M",
        true,
        Some("AA123123A"),
        None, None, None,
        "Company Secretary"
      )),
      CompanyOfficer(
        CompanyOfficerType.Individual,
        CompanyOfficerIndividual(
          "Vlad",
          "M",
          true,
          Some("AA123231"),
          None, None, None,
          "Director"
        )),
      CompanyOfficer(
        CompanyOfficerType.Company,
        CompanyOfficerCompany(
          "Some Company",
          true,
          Some("123456789"),
          None,
          "Company Secretary"
        )
    )),
    BusinessStatus(true, Some(LocalDate.of(2018,6, 30))),
    ImportingActivities(true, Some(EoriNumber("1234123132", true))),
    BusinessCustomers("Over 100"),
    OtherStoragePremises(true, List(
      StoragePremise(
        Address(
          "1 Some High Street",
          None,
          None,
          Some("Sometown"),
          "Z99 2YY",
          None),
        false),
      StoragePremise(
        Address(
          "25 Testing Close",
          None,
          None,
          Some("Othertown"),
          "Z9 3WW",
          None),
        true)
    ))
  )

  val declaration = Declaration(
    "C",
    "Director",
    false,
    None,
    Some(AlternativeEmail(
      "a@a.ro",
      "a@a.ro"
    ))
  )
}
