import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild {

  lazy val appDependencies: Seq[ModuleID] = compile ++ test()
  val monocleVersion = "1.5.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"                 %% "bootstrap-play-26"                % "1.3.0",
    "uk.gov.hmrc"                 %% "govuk-template"                   % "5.48.0-play-26",
    "uk.gov.hmrc"                 %% "play-ui"                          % "8.7.0-play-26",
    "uk.gov.hmrc"                 %% "play-partials"                    % "6.9.0-play-26",
    "uk.gov.hmrc"                 %% "play-hmrc-api"                    % "4.1.0-play-26",
    "uk.gov.hmrc"                 %% "http-caching-client"              % "9.0.0-play-26",
    "uk.gov.hmrc"                 %% "play-conditional-form-mapping"    % "1.2.0-play-26",
    "uk.gov.hmrc"                 %% "time"                             % "3.6.0",
    "com.typesafe.play"           %% "play-json"                        % "2.6.13",
    "org.typelevel"               %% "cats-core"                        % "1.6.0",
    "org.typelevel"               %% "cats-kernel"                      % "1.6.0",
    "org.pegdown"                  % "pegdown"                          % "1.6.0",
    "com.github.tototoshi"        %% "play-json-naming"                 % "1.2.0",
    "org.julienrf"                %% "play-json-derived-codecs"         % "4.0.1",
    "com.github.julien-truffaut"  %%  "monocle-core"                    % monocleVersion,
    "com.github.julien-truffaut"  %%  "monocle-macro"                   % monocleVersion,
    "com.github.julien-truffaut"  %%  "monocle-law"                     % monocleVersion,
    "org.mindrot"                  % "jbcrypt"                          % "0.4"
  )

  def test(scope: String = "test,it") = Seq(
    "org.scalatest"               %% "scalatest"                        % "3.0.8"   % scope,
    "org.pegdown"                  % "pegdown"                          % "1.6.0"   % scope,
    "org.jsoup"                    % "jsoup"                            % "1.12.1"   % scope,
    "org.scalatestplus.play"       %% "scalatestplus-play"              % "3.1.2"   % scope,
    "org.scalacheck"              %% "scalacheck"                       % "1.14.2"  % scope,
    "org.mockito"                  % "mockito-core"                     % "3.1.0"  % scope,
    "org.scalamock"               %% "scalamock-scalatest-support"      % "3.6.0"   % scope,
    "com.github.tomakehurst"       %  "wiremock-jre8"                   % "2.25.1"   % scope,
    "com.eclipsesource"           %% "play-json-schema-validator"       % "0.9.4"   % scope
  )
}
