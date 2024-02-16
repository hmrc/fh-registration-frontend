import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val monocleVersion = "2.1.0"
  val bootstrapVersion = "8.4.0"
  val playVersion = "play-30"
  val hmrcMongoVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc-$playVersion" % "8.0.0",
    "uk.gov.hmrc" %% s"govuk-template-$playVersion" % "7.1.0",
    "uk.gov.hmrc" %% s"play-partials-$playVersion" % "9.1.0",
    "uk.gov.hmrc" %% s"play-ui-$playVersion" % s"11.0.0",
    "uk.gov.hmrc" %% s"play-hmrc-api-$playVersion" % "8.0.0",
    "uk.gov.hmrc" %% s"http-caching-client-$playVersion" % "11.2.0",
    "uk.gov.hmrc" %% s"play-conditional-form-mapping-$playVersion" % "2.0.0",
    "com.typesafe.play" %% "play-json" % "2.9.4",
    "org.typelevel" %% "cats-core" % "2.9.0",
    "org.typelevel" %% "cats-kernel" % "2.9.0",
    "com.github.tototoshi" %% "play-json-naming" % "1.5.0",
    "org.julienrf" %% "play-json-derived-codecs" % "10.1.0",
    "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
    "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
    "org.mindrot" % "jbcrypt" % "0.4",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion" % hmrcMongoVersion,
    "uk.gov.hmrc" %% s"crypto-json-$playVersion" % "7.6.0",

    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.14" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.14" % Provided cross CrossVersion.full
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% s"bootstrap-test-$playVersion" % bootstrapVersion % scope,
    "org.scalatest" %% "scalatest" % "3.2.15" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.6" % scope,
    "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % scope,
    "com.github.tomakehurst" % "wiremock-standalone" % "2.27.2" % scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.jsoup" % "jsoup" % "1.15.4" % scope,
    "org.scalacheck" %% "scalacheck" % "1.17.0" % scope,
    "com.github.julien-truffaut" %% "monocle-law" % monocleVersion % scope,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion % scope,
    "org.playframework" %% "play-test" % PlayVersion.current % scope,
  )

  def apply(): Seq[ModuleID] = compile ++ test()
}