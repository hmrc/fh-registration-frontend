import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val playVersion = "play-30"
  val bootstrapVersion = "9.7.0"
  val hmrcMongoVersion = "2.5.0"
  val monocleVersion = "3.3.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                %% s"bootstrap-frontend-$playVersion"            % bootstrapVersion,
    "uk.gov.hmrc"                %% s"play-frontend-hmrc-$playVersion"            % "11.9.0",
    "uk.gov.hmrc"                %% s"play-partials-$playVersion"                 % "10.0.0",
    "uk.gov.hmrc"                %% s"play-hmrc-api-$playVersion"                 % "8.1.0",
    "uk.gov.hmrc"                %% s"http-caching-client-$playVersion"           % "12.1.0",
    "uk.gov.hmrc"                %% s"play-conditional-form-mapping-$playVersion" % "3.2.0",
    "uk.gov.hmrc.mongo"          %% s"hmrc-mongo-$playVersion"                    % hmrcMongoVersion,
    "org.typelevel"              %% "cats-core"                                   % "2.13.0",
    "org.typelevel"              %% "cats-kernel"                                 % "2.13.0",
    "com.typesafe.play"          %% "play-json"                                   % "2.10.6",
    "dev.optics"                 %% "monocle-core"                                % monocleVersion,
    "dev.optics"                 %% "monocle-macro"                               % monocleVersion,
    "org.mindrot"                %  "jbcrypt"                                     % "0.4"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"          %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.pegdown"                %  "pegdown"                       % "1.6.0",
    "org.jsoup"                  %  "jsoup"                         % "1.18.3",
    "dev.optics"                 %% "monocle-law"                   % monocleVersion,
    "com.github.java-json-tools" %  "json-schema-validator"         % "2.2.14",
    "org.scalatestplus.play"     %% "scalatestplus-play"            % "7.0.1"
  ).map(_ % "test, it")

  val all: Seq[ModuleID] = compile ++ test
}