package uk.gov.hmrc.fhregistrationfrontend.testsupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{configureFor, reset, resetAllScenarios}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import models.UserAnswers
import org.mongodb.scala.bson.BsonDocument
import org.scalatest.concurrent.{IntegrationPatience, PatienceConfiguration}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{Call, CookieHeaderEncoding, Session, SessionCookieBaker}
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType.BusinessPartnerTypes
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnerTypePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.health.HealthController
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes
trait TestConfiguration
  extends GuiceOneServerPerSuite
    with IntegrationPatience
    with PatienceConfiguration
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  me: Suite with TestSuite =>

  val newBusinessPartnersFlowEnabled: Boolean = true

  val wiremockHost: String = "localhost"
  val wiremockPort: Int = Port.randomAvailable

  val baseUrl = s"http://localhost:$port/fhdds"

  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val xSessionId: (String, String) = "X-Session-ID" -> sessionId
  val xRequestId: (String, String) = "X-Request-ID" -> sessionId
  val AUTHORIZE_HEADER_VALUE =
    "Bearer BXQ3/Treo4kQCZvVcCqKPhhpBYpRtQQKWTypn1WBfRHWUopu5V/IFWF5phY/fymAP1FMqQR27MmCJxb50Hi5GD6G3VMjMtSLu7TAAIuqDia6jByIpXJpqOgLQuadi7j0XkyDVkl0Zp/zbKtHiNrxpa0nVHm3+GUC4H2h4Ki8OjP9KwIkeIPK/mMlBESjue4V"

  val sessionBaker: SessionCookieBaker = app.injector.instanceOf[SessionCookieBaker]
  val cookieHeaderEncoding: CookieHeaderEncoding = app.injector.instanceOf[CookieHeaderEncoding]
  val sessionCookieCrypto: SessionCookieCrypto = app.injector.instanceOf[SessionCookieCrypto]

  def createSessionCookieAsString(sessionData: Map[String, String]): String = {
    val sessionCookie = sessionBaker.encodeAsCookie(Session(sessionData))
    val encryptedSessionCookieValue =
      sessionCookieCrypto.crypto.encrypt(PlainText(sessionCookie.value)).value
    val encryptedSessionCookie =
      sessionCookie.copy(value = encryptedSessionCookieValue)
    cookieHeaderEncoding.encodeCookieHeader(Seq(encryptedSessionCookie))
  }
  val authData = Map("authToken" -> AUTHORIZE_HEADER_VALUE)
  val sessionAndAuth  = Map("authToken" -> AUTHORIZE_HEADER_VALUE, "sessionId" -> sessionId)

  val authCookie: String = createSessionCookieAsString(authData).substring(5)
  val authAndSessionCookie: String = createSessionCookieAsString(sessionAndAuth).substring(5)
  abstract override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = Span(4, Seconds),
      interval = Span(50, Millis))

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(replaceWithWiremock(Seq(
      "auth",
      "auth.bas-gateway",
      "fhdds",
      "business-customer-frontend",
      "cachable.short-lived-cache",
      "cachable.session-cache",
      "address-lookup"
    )))
    .build()
        app.injector.instanceOf[HealthController]


  private def replaceWithWiremock(services: Seq[String]) = {

    services.foldLeft(Map.empty[String, Any]) { (configMap, service) =>
      configMap ++ Map(
        s"microservice.services.$service.host" -> wiremockHost,
        s"microservice.services.$service.port" -> wiremockPort,
        s"microservice.services.cachable.short-lived-cache.domain" -> "save4later",
        s"microservice.services.cachable.session-cache.domain" -> "keystore",
        s"play.filters.csrf.header.bypassHeaders.X-Requested-With" -> "*",
        s"play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
        s"json.encryption.key" -> "fqpLDZ4sumDsekHkeEBlCA==",
        s"json.encryption.previousKeys" -> List.empty,
        "mongodb.uri" -> "mongodb://localhost:27017/fh-registration-frontend-integration",
        "business-partners-new-enabled" -> s"$newBusinessPartnersFlowEnabled"
      )
    } ++
      Map(s"auditing.consumer.baseUri.host" -> wiremockHost, s"auditing.consumer.baseUri.port" -> wiremockPort)
  }

  val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  val sessionCache: SessionRepository = app.injector.instanceOf[SessionRepository]
  override def beforeAll() = {
    wireMockServer.stop()
    wireMockServer.start()
    configureFor(wiremockHost, wiremockPort)
  }

  override def beforeEach() = {
    Await.result(sessionCache.collection.deleteMany(BsonDocument()).toFuture(),Duration(3,TimeUnit.SECONDS))
    resetAllScenarios()
    reset()
  }

  override protected def afterAll(): Unit = {
    wireMockServer.stop()
  }

  override def afterEach(): Unit = {
    println("===== REQUESTS =====")
    wireMockServer.getAllServeEvents.asScala.toList
      .sortBy(_.getRequest.getLoggedDate)
      .map(_.getRequest).map(r => s"${r.getLoggedDate.toInstant.toEpochMilli}\t${r.getMethod}\t${r.getUrl}")
      .foreach(println)
    println("===== END =====")
  }

  def emptyUserAnswers: UserAnswers = UserAnswers("some-id")

  def userAnswersWithBusinessPartnerType(partnerType: BusinessPartnerType.Value, index: Int = 1): UserAnswers =
    emptyUserAnswers.set(PartnerTypePage(index), partnerType).success.value
  def getUserAnswersFromSession: Option[UserAnswers] = {
    Await.result(sessionCache.get("some-id"), Duration(3,TimeUnit.SECONDS))
  }

  def addUserAnswersToSession(userAnswers: UserAnswers): Boolean = {
    Await.result(sessionCache.set(userAnswers), Duration(3,TimeUnit.SECONDS))
  }


  def buildRequest(path: String,
                   followRedirects: Boolean = false): WSRequest = {
    ws.url(s"$baseUrl$path")
      .withFollowRedirects(followRedirects)
  }

  def buildRequestFromRoute(route: Call,
                   followRedirects: Boolean = false): WSRequest = {
    ws.url(s"http://localhost:$port${route.url}")
      .withFollowRedirects(followRedirects)
  }

  val startCall = routes.Application.main()

  val multipleAddresses: Map[String, Address] = {
    val address1 = Address(
      addressLine1 = "1 Romford Road",
      addressLine2 = Some("Wellington"),
      addressLine3 = Some("Telford"),
      addressLine4 = None,
      postcode = "TF1 4ER",
      countryCode = None,
      lookupId = None
    )
    val address2 = address1.copy(addressLine1 = "2 Romford Road")

    Map("1" -> address1, "2" -> address2)
  }

  val singleAddress: Map[String, Address] =
    Map("1" -> Address(
      addressLine1 = "1 Romford Road",
      addressLine2 = Some("Wellington"),
      addressLine3 = Some("Telford"),
      addressLine4 = None,
      postcode = "TF1 4ER",
      countryCode = None,
      lookupId = None)
    )

  def seedCacheWithUKAddressLookup(addressList: Map[String, Address]): UserAnswers = emptyUserAnswers
    .set[UkAddressLookup](
      UkAddressLookupPage(1),
      UkAddressLookup(
        Some("1 Romford Road"),
        "TF1 4ER",
        addressList
      ))
    .success
    .value
}
