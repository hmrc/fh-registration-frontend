import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
val monocleVersion = "1.5.0"

val compile = Seq(
  ws,
  "uk.gov.hmrc"                 %% "bootstrap-play-26"                % "1.3.0",
  "uk.gov.hmrc"                 %% "govuk-template"                   % "5.48.0-play-26",
  "uk.gov.hmrc"                 %% "play-ui"                          % "8.8.0-play-26",
  "uk.gov.hmrc"                 %% "play-partials"                    % "6.9.0-play-26",
  "uk.gov.hmrc"                 %% "play-hmrc-api"                    % "4.1.0-play-26",
  "uk.gov.hmrc"                 %% "http-caching-client"              % "9.0.0-play-26",
  "uk.gov.hmrc"                 %% "play-conditional-form-mapping"    % "1.2.0-play-26",
  "uk.gov.hmrc"                 %% "time"                             % "3.6.0",
  "com.typesafe.play"           %% "play-json"                        % "2.6.14",
  "org.typelevel"               %% "cats-core"                        % "1.6.1",
  "org.typelevel"               %% "cats-kernel"                      % "1.6.1",
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
  "org.scalacheck"              %% "scalacheck"                       % "1.14.3"  % scope,
  "org.mockito"                  % "mockito-core"                     % "3.2.4"  % scope,
  "org.scalamock"               %% "scalamock-scalatest-support"      % "3.6.0"   % scope,
  "com.github.tomakehurst"       %  "wiremock-jre8"                   % "2.26.0"   % scope,
  "com.eclipsesource"           %% "play-json-schema-validator"       % "0.9.4"   % scope
)

val appName = "fh-registration-frontend"

lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages :=
      """uk\.gov\.hmrc\.BuildInfo;
        |.*\.Routes;
        |.*\.RoutesPrefix;
        |.*\.Reverse[^.]*;
        |uk\.gov\.hmrc\.fhregistrationfrontend\.views\.html\.helpers;
        |uk\.gov\.hmrc\.fhregistrationfrontend\.views\.html\..*;
        |uk\.gov\.hmrc\.fhregistrationfrontend\.views\.html\..*;
        |""".stripMargin,
    ScoverageKeys.coverageExcludedFiles := "<empty>;.*javascript.*;.*models.*;.*Routes.*;.*testonly.*;.*controllers.AdminPageController.*;" +
      ".*controllers.AuthenticationController.*",
    ScoverageKeys.coverageMinimum := 80.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins : _*)
  .settings(majorVersion := 0)
  .settings(PlayKeys.playDefaultPort := 1118)
  .settings(playSettings : _*)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources")
  .settings(
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest := (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    resourceDirectory in IntegrationTest := baseDirectory.value / "it/resources",
    addTestReportOption(IntegrationTest, "int-test-reports"),
    testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
    parallelExecution in IntegrationTest := false,
    scalafmtOnCompile in IntegrationTest := true)
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo,
    "emueller-bintray" at "http://dl.bintray.com/emueller/maven"
  ))


def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
