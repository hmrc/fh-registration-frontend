import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val monocleVersion = "2.1.0"
  val bootstrapVersion = "8.4.0"
  val playVersion = "play-30"
  val hmrcMongoVersion = "1.7.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"play-frontend-hmrc-$playVersion" % "8.5.0",
    "uk.gov.hmrc" %% s"play-partials-$playVersion" % "9.1.0",
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
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.14" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.14" % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.pegdown" % "pegdown" % "1.6.0",
    "org.jsoup" % "jsoup" % "1.15.4",
    "com.github.julien-truffaut" %% "monocle-law" % monocleVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "com.github.java-json-tools" % "json-schema-validator" % "2.2.14"
  ).map(_ % "test, it")

  val all: Seq[ModuleID] = compile ++ test
}