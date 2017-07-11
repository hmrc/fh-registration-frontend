import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "fhdds-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "7.26.0",
    "uk.gov.hmrc" %% "play-partials" % "5.4.0",
    "uk.gov.hmrc" %% "play-auth" % "1.2.0",
    "uk.gov.hmrc" %% "play-config" % "4.3.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.2.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "play-ui" % "7.4.0",
    "uk.gov.hmrc" %% "passcode-verification" % "4.1.0",
    "com.typesafe.play" %% "play-json" % "2.5.12",
    "org.pegdown" % "pegdown" % "1.4.2",
    "uk.gov.hmrc" %% "http-caching-client" % "6.2.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0",
    "uk.gov.hmrc" %% "time" % "3.0.0",
    "uk.gov.hmrc" %% "play-whitelist-filter" % "2.0.0",
    "org.typelevel" %% "cats" % "0.9.0"
  )

  def test(scope: String = "test") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
    "org.scalatest" % "scalatest_2.11" % "3.0.0" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
    "org.scalacheck" %% "scalacheck" % "1.12.6" % scope,
    "org.mockito" % "mockito-core" % "2.7.22" % scope,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % scope
  )

}
