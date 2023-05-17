package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class ContactPersonControllerISpec
  extends Specifications with TestConfiguration {

  "GET /fhdds/contactPerson" should {

    "render the Contact Person's Details page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/contactPerson ")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Contact person's details")
          page.getElementsByTag("h1").text() must include("Contact person's details")
        }
      }
    }
  }

  "POST /fhdds/contactPerson" when {
    "details are entered \n" +
      "Is this the address you want to use? Yes" should {
      "return 200 with contact person's details" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson ")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName"               -> Seq("John"),
              "lastName"                -> Seq("Smith"),
              "jobTitle"                -> Seq("Astronaut"),
              "telephone"               -> Seq("0123456789"),
              "usingSameContactAddress" -> Seq("true")
            ))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,true,None,None,None)"
          }
        }
      }
    }

    "details are entered, \n" +
      "Is this the address you want to use? No \n" +
      "Is the contact address in the UK? Yes \n" +
      "Only mandatory fields entered" should {
      "return 200 with contact person's details message" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName"                                     -> Seq("John"),
              "lastName"                                      -> Seq("Smith"),
              "jobTitle"                                      -> Seq("Astronaut"),
              "telephone"                                     -> Seq("0123456789"),
              "usingSameContactAddress"                       -> Seq("false"),
              "isUkAddress"                                   -> Seq("true"),
              "otherUkContactAddress_contactAddress.Line1"    -> Seq("Flat 1"),
              "otherUkContactAddress_contactAddress.postcode" -> Seq("AB1 2YZ")
            ))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,false,Some(true),Some(Address(Flat 1,None,None,None,AB1 2YZ,None,None)),None)"
          }
        }
      }
    }

    "details are entered, \n" +
      "Is this the address you want to use? No \n" +
      "Is the contact address in the UK? Yes \n" +
      "All fields entered" should {
      "return 200 with contact person's details message" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName" -> Seq("John"),
              "lastName" -> Seq("Smith"),
              "jobTitle" -> Seq("Astronaut"),
              "telephone" -> Seq("0123456789"),
              "usingSameContactAddress" -> Seq("false"),
              "isUkAddress" -> Seq("true"),
              "otherUkContactAddress_contactAddress.Line1" -> Seq("Flat 1"),
              "otherUkContactAddress_contactAddress.Line2" -> Seq("5 High Street"),
              "otherUkContactAddress_contactAddress.Line3" -> Seq("Worthing"),
              "otherUkContactAddress_contactAddress.Line4" -> Seq("West Sussex"),
              "otherUkContactAddress_contactAddress.postcode" -> Seq("AB1 2YZ")
            ))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,false,Some(true),Some(Address(Flat 1,Some(5 High Street),Some(Worthing),Some(West Sussex),AB1 2YZ,None,None)),None)"
          }
        }
      }
    }

      /*"details are entered, \n" +
        "Is this the address you want to use? No \n" +
        "Is the contact address in the UK? No \n" +
        "All fields entered" should {
        "return 200 with contact person's details message" in {
          given
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/contactPerson")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId,
                "Csrf-Token" -> "nocheck")
              .post(Map(
                "firstName"                                                   -> Seq("John"),
                "lastName"                                                    -> Seq("Smith"),
                "jobTitle"                                                    -> Seq("Astronaut"),
                "telephone"                                                   -> Seq("0123456789"),
                "usingSameContactAddress"                                     -> Seq("false"),
                "isUkAddress"                                                 -> Seq("false"),
                "otherInternationalContactAddress_contactAddress.Line1"       -> Seq("Flat 1"),
                "otherInternationalContactAddress_contactAddress.Line2"       -> Seq("Le Street"),
                "otherInternationalContactAddress_contactAddress.Line3"       -> Seq("Paris"),
                "otherInternationalContactAddress_contactAddress.countryCode" -> Seq("FR")
              ))

            whenReady(result) { res =>
              println("RES = " + Jsoup.parse(res.body))
              res.status mustBe 200
              res.body mustBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,false,Some(true),Some(Address(Flat 1,Some(5 High Street),Some(Worthing),Some(West Sussex),AB1 2YZ,None,None)),None)"
            }
          }
        }
    }*/

    "no data entered" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName"               -> Seq.empty,
              "lastName"                -> Seq.empty,
              "jobTitle"                -> Seq.empty,
              "telephone"               -> Seq.empty,
              "usingSameContactAddress" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter your contact person's first name " +
              "Enter your contact person's last name " +
              "Enter your contact person's job title " +
              "Enter your contact person's telephone number " +
              "Select whether you want to use the contact address")
          }
        }
      }
    }

    "Contact Details entered \n" +
      "Is this the contact address you want to use? No \n" +
      "Is the contact address in the UK? Not selected" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName"                                     -> Seq("John"),
              "lastName"                                      -> Seq("Smith"),
              "jobTitle"                                      -> Seq("Astronaut"),
              "telephone"                                     -> Seq("0123456789"),
              "usingSameContactAddress"                       -> Seq("false"),
              "isUkAddress"                                   -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether the contact address you want to use is in the UK")
          }
        }
      }
    }

    "Contact Details entered \n" +
      "Is this the contact address you want to use? No \n" +
      "Is the contact address in the UK? Yes \n" +
      "Address details not entered" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName"                                     -> Seq("John"),
              "lastName"                                      -> Seq("Smith"),
              "jobTitle"                                      -> Seq("Astronaut"),
              "telephone"                                     -> Seq("0123456789"),
              "usingSameContactAddress"                       -> Seq("false"),
              "isUkAddress"                                   -> Seq("true"),
              "otherUkContactAddress_contactAddress.Line1"    -> Seq.empty,
              "otherUkContactAddress_contactAddress.Line2"    -> Seq.empty,
              "otherUkContactAddress_contactAddress.Line3"    -> Seq.empty,
              "otherUkContactAddress_contactAddress.Line4"    -> Seq.empty,
              "otherUkContactAddress_contactAddress.postcode" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter address line 1 Enter the postcode of the address")
          }
        }
      }
    }

    "Contact Details entered \n" +
      "Is this the contact address you want to use? No \n" +
      "Is the contact address in the UK? No \n" +
      "Address details not entered" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/contactPerson")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "firstName"                                                   -> Seq("John"),
              "lastName"                                                    -> Seq("Smith"),
              "jobTitle"                                                    -> Seq("Astronaut"),
              "telephone"                                                   -> Seq("0123456789"),
              "usingSameContactAddress"                                     -> Seq("false"),
              "isUkAddress"                                                 -> Seq("false"),
              "otherInternationalContactAddress_contactAddress.Line1"       -> Seq.empty,
              "otherInternationalContactAddress_contactAddress.Line2"       -> Seq.empty,
              "otherInternationalContactAddress_contactAddress.Line3"       -> Seq.empty,
              "otherInternationalContactAddress_contactAddress.countryCode" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter address line 1 Enter a country and select from the list")
          }
        }
      }
    }
  }
}