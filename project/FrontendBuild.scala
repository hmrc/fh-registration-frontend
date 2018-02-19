import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "fh-registration-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % "8.11.0",
    "uk.gov.hmrc" %% "play-partials" % "6.1.0",
    "uk.gov.hmrc" %% "auth-client" % "2.4.0",
    "uk.gov.hmrc" %% "passcode-verification" % "4.1.0",
    "com.typesafe.play" %% "play-json" % "2.5.12",
    "org.pegdown" % "pegdown" % "1.4.2",
    "uk.gov.hmrc" %% "play-hmrc-api" % "1.3.0",
    "uk.gov.hmrc" %% "http-caching-client" % "7.1.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0",
    "uk.gov.hmrc" %% "time" % "3.0.0",
    "uk.gov.hmrc" %% "play-whitelist-filter" % "2.0.0",
    "uk.gov.hmrc" %% "domain" % "4.1.0",
    "org.typelevel" %% "cats" % "0.9.0",
    "com.github.tototoshi" %% "play-json-naming" % "1.1.0",
    "org.typelevel" %% "cats-core" % "1.0.1",
    "org.typelevel" %% "cats-kernel" % "1.0.1"
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
    "org.scalatest" % "scalatest_2.11" % "3.0.0" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.8.1" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
    "org.scalacheck" %% "scalacheck" % "1.12.6" % scope,
    "org.mockito" % "mockito-core" % "2.7.22" % scope,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % scope,
    "com.github.tomakehurst"  %  "wiremock"  % "2.6.0" % scope
  )

}
