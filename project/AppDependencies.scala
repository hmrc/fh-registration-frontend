import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val monocleVersion = "2.1.0"
  val bootstrapVersion = "8.4.0"
  val playVersion = "play-28"
  val hmrcMongoVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % s"7.29.0-$playVersion",
    "uk.gov.hmrc" %% "govuk-template" % "5.80.0-play-28",
    "uk.gov.hmrc" %% "play-ui" % "9.12.0-play-28",
    "uk.gov.hmrc" %% "play-partials" % "8.4.0-play-28",
    "uk.gov.hmrc" %% "play-hmrc-api" % "7.2.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client" % "10.0.0-play-28",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "com.typesafe.play" %% "play-json" % "2.9.4",
    "org.typelevel" %% "cats-core" % "2.9.0",
    "org.typelevel" %% "cats-kernel" % "2.9.0",
    "com.github.tototoshi" %% "play-json-naming" % "1.5.0",
    "org.julienrf" %% "play-json-derived-codecs" % "10.1.0",
    "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
    "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
    "org.mindrot" % "jbcrypt" % "0.4",
    "com.eclipsesource" %% "play-json-schema-validator" % "0.9.5",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion" % hmrcMongoVersion,
    "uk.gov.hmrc" %% s"crypto-json-$playVersion" % "7.6.0",

    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.12" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.12" % Provided cross CrossVersion.full
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % scope,
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
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  )

  def apply(): Seq[ModuleID] = compile ++ test()
}