package uk.gov.hmrc.fhregistrationfrontend.services.mapping.data

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object LtdMinimum {
  val application = LimitedCompanyApplication(
    MainBusinessAddress(
      "3-5 years",
      None,
      None
    ),
    ContactPerson(
      "C",
      "M",
      "director",
      "07231111",
      "a@a.ro",
      false,
      None,
      None,
      None
    ),
    CompanyRegistrationNumber("12345678"),
    DateOfIncorporation(LocalDate.of(2015, 3, 20)),
    TradingName(false, None),
    VatNumber(false, None),
    List(CompanyOfficer(
      CompanyOfficerType.Individual,
      CompanyOfficerIndividual(
        "C",
        "M",
        true,
        Some("AA123456A"),
        None, None, None,
        "Director"
      )
    )),
    BusinessStatus(false, None),
    ImportingActivities(false, None),
    BusinessCustomers("1-10"),
    OtherStoragePremises(false, List.empty)
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
