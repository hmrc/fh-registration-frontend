import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val playVersion = "play-30"
  val bootstrapVersion = "9.6.0"
  val hmrcMongoVersion = "2.4.0"
  val monocleVersion = "2.1.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                %% s"bootstrap-frontend-$playVersion"            % bootstrapVersion,
    "uk.gov.hmrc"                %% s"play-frontend-hmrc-$playVersion"            % "11.9.0",
    "uk.gov.hmrc"                %% s"play-partials-$playVersion"                 % "10.0.0",
    "uk.gov.hmrc"                %% s"play-hmrc-api-$playVersion"                 % "8.0.0",
    "uk.gov.hmrc"                %% s"http-caching-client-$playVersion"           % "12.1.0",
    "uk.gov.hmrc"                %% s"play-conditional-form-mapping-$playVersion" % "3.2.0",
    "uk.gov.hmrc.mongo"          %% s"hmrc-mongo-$playVersion"                    % hmrcMongoVersion,
    "org.typelevel"              %% "cats-core"                                   % "2.12.0",
    "org.typelevel"              %% "cats-kernel"                                 % "2.12.0",
    "com.typesafe.play"          %% "play-json"                                   % "2.10.6",
    "com.github.tototoshi"       %% "play-json-naming"                            % "1.5.0",
    "org.julienrf"               %% "play-json-derived-codecs"                    % "10.1.0",
    "com.github.julien-truffaut" %% "monocle-core"                                % monocleVersion,
    "com.github.julien-truffaut" %% "monocle-macro"                               % monocleVersion,
    "org.mindrot"                %  "jbcrypt"                                     % "0.4",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.14" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.14" % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"          %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.pegdown"                %  "pegdown"                       % "1.6.0",
    "org.jsoup"                  %  "jsoup"                         % "1.18.1",
    "com.github.julien-truffaut" %% "monocle-law"                   % monocleVersion,
    "com.github.java-json-tools" %  "json-schema-validator"         % "2.2.14"
  ).map(_ % "test, it")

  val all: Seq[ModuleID] = compile ++ test
}