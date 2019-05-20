import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "fh-registration-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()
  val monocleVersion = "1.5.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"                 %% "bootstrap-play-26"                % "0.39.0",
    "uk.gov.hmrc"                 %% "govuk-template"                   % "5.35.0-play-26",
    "uk.gov.hmrc"                 %% "play-ui"                          % "7.39.0-play-26",

    "uk.gov.hmrc"                 %% "play-partials"                    % "6.9.0-play-26",
    "uk.gov.hmrc"                 %% "play-hmrc-api"                    % "3.4.0-play-26",
    "uk.gov.hmrc"                 %% "http-caching-client"              % "8.3.0",
    "uk.gov.hmrc"                 %% "play-conditional-form-mapping"    % "0.2.0",
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
    "com.typesafe.play"           %% "play-test"                        % PlayVersion.current % scope,
    "uk.gov.hmrc"                 %% "hmrctest"                         % "3.8.0-play-26"   % scope,
    "org.scalatest"                % "scalatest_2.11"                   % "3.0.7"   % scope,
    "org.pegdown"                  % "pegdown"                          % "1.6.0"   % scope,
    "org.jsoup"                    % "jsoup"                            % "1.12.1"   % scope,
    "org.scalatestplus.play"       %% "scalatestplus-play"              % "4.0.2"   % scope,
    "org.scalacheck"              %% "scalacheck"                       % "1.14.0"  % scope,
    "org.mockito"                  % "mockito-core"                     % "2.27.0"  % scope,
    "org.scalamock"               %% "scalamock-scalatest-support"      % "3.5.0"   % scope,
    "com.github.tomakehurst"       %  "wiremock"                        % "2.23.2"   % scope,
    "com.eclipsesource"           %% "play-json-schema-validator"       % "0.8.9"   % scope
  )

}
