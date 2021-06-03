import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
val monocleVersion = "2.1.0"

val compile = Seq(
  ws,
  "uk.gov.hmrc"                 %% "bootstrap-frontend-play-26"       % "5.3.0",
  "uk.gov.hmrc"                 %% "govuk-template"                   % "5.66.0-play-26",
  "uk.gov.hmrc"                 %% "play-ui"                          % "9.4.0-play-26",
  "uk.gov.hmrc"                 %% "play-partials"                    % "8.1.0-play-26",
  "uk.gov.hmrc"                 %% "play-hmrc-api"                    % "6.2.0-play-26",
  "uk.gov.hmrc"                 %% "http-caching-client"              % "9.5.0-play-26",
  "uk.gov.hmrc"                 %% "play-conditional-form-mapping"    % "1.9.0-play-26",
  "uk.gov.hmrc"                 %% "time"                             % "3.24.0",
  "com.typesafe.play"           %% "play-json"                        % "2.9.2",
  "org.typelevel"               %% "cats-core"                        % "2.6.1",
  "org.typelevel"               %% "cats-kernel"                      % "2.6.1",
  "org.pegdown"                  % "pegdown"                          % "1.6.0",
  "com.github.tototoshi"        %% "play-json-naming"                 % "1.5.0",
  "org.julienrf"                %% "play-json-derived-codecs"         % "10.0.2",
  "com.github.julien-truffaut"  %%  "monocle-core"                    % monocleVersion,
  "com.github.julien-truffaut"  %%  "monocle-macro"                   % monocleVersion,
  "com.github.julien-truffaut"  %%  "monocle-law"                     % monocleVersion,
  "org.mindrot"                  %  "jbcrypt"                         % "0.4",
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.5" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.5" % Provided cross CrossVersion.full
)

def test(scope: String = "test,it") = Seq(
  "org.scalatest"               %% "scalatest"                        % "3.0.9"   % scope,
  "org.pegdown"                  % "pegdown"                          % "1.6.0"   % scope,
  "org.jsoup"                    % "jsoup"                            % "1.13.1"   % scope,
  "org.scalatestplus.play"       %% "scalatestplus-play"              % "3.1.3"   % scope,
  "org.scalacheck"              %% "scalacheck"                       % "1.15.4"  % scope,
  "org.mockito"                  % "mockito-core"                     % "3.10.0"  % scope,
  "org.scalamock"               %% "scalamock-scalatest-support"      % "3.6.0"   % scope,
  "com.github.tomakehurst"       %  "wiremock-jre8"                   % "2.28.0"   % scope,
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
    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins : _*)
  .settings(majorVersion := 0)
  .settings(PlayKeys.playDefaultPort := 1118)
  .settings(playSettings : _*)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(scalaVersion := "2.12.13")
  .settings(defaultSettings(): _*)
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    IntegrationTest / resourceDirectory := baseDirectory.value / "it/resources",
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false,
    IntegrationTest / scalafmtOnCompile := true)
  .settings(resolvers ++= Seq(
    "emueller-bintray" at "https://dl.bintray.com/emueller/maven"
  ))
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scalacOptions += "-P:silencer:pathFilters=routes")
  .settings(scalacOptions += "-P:silencer:globalFilters=Unused import")
  .settings(Global / lintUnusedKeysOnLoad := false)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
