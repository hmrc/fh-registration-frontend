import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "fh-registration-frontend"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()

  val compile = Seq(
    ws,
    "uk.gov.hmrc"           %% "bootstrap-play-25"                % "1.5.0",
    "uk.gov.hmrc"           %% "govuk-template"                   % "5.19.0",
    "uk.gov.hmrc"           %% "play-ui"                          % "7.14.0",

    "uk.gov.hmrc"           %% "play-partials"                    % "6.1.0",
    "uk.gov.hmrc"           %% "play-hmrc-api"                    % "1.3.0",
    "uk.gov.hmrc"           %% "http-caching-client"              % "7.1.0",
    "uk.gov.hmrc"           %% "play-conditional-form-mapping"    % "0.2.0",
    "uk.gov.hmrc"           %% "time"                             % "3.1.0",

    "com.typesafe.play"     %% "play-json"                        % "2.5.12",
    "org.typelevel"         %% "cats-core"                        % "1.0.1",
    "org.typelevel"         %% "cats-kernel"                      % "1.0.1",

    "org.pegdown"            % "pegdown"                          % "1.6.0",
    "com.github.tototoshi"  %% "play-json-naming"                 % "1.1.0",
    "org.julienrf"          %% "play-json-derived-codecs"         % "3.3"
  )

  def test(scope: String = "test,it") = Seq(
    "com.typesafe.play"     %% "play-test"                        % PlayVersion.current % scope,
    "uk.gov.hmrc"           %% "hmrctest"                         % "3.0.0"   % scope,
    "org.scalatest"          % "scalatest_2.11"                   % "3.0.0"   % scope,
    "org.pegdown"            % "pegdown"                          % "1.6.0"   % scope,
    "org.jsoup"              % "jsoup"                            % "1.8.1"   % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"              % "2.0.0"   % scope,
    "org.scoverage"          %  "scalac-scoverage-runtime_2.11"   % "1.3.1"   % scope,
    "org.scalacheck"        %% "scalacheck"                       % "1.12.6"  % scope,
    "org.mockito"            % "mockito-core"                     % "2.7.22"  % scope,
    "org.scalamock"         %% "scalamock-scalatest-support"      % "3.5.0"   % scope,
    "com.github.tomakehurst" %  "wiremock"                        % "2.6.0"   % scope,
    "com.eclipsesource"     %% "play-json-schema-validator"       % "0.8.9"   % scope
  )

}
