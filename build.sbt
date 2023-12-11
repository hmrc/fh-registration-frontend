import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

lazy val appDependencies: Seq[ModuleID] = compile ++ test()

val monocleVersion = "2.1.0"
val bootstrapVersion = "7.12.0"
val playVersion = "play-28"
val hmrcMongoVersion = "1.3.0"


val compile = Seq(
  ws,
  "uk.gov.hmrc"                 %% s"bootstrap-frontend-$playVersion" % bootstrapVersion,
  "uk.gov.hmrc"                 %% "play-frontend-hmrc"               % s"7.4.0-$playVersion",
  "uk.gov.hmrc"                 %% "govuk-template"                   % "5.78.0-play-28",
  "uk.gov.hmrc"                 %% "play-ui"                          % "9.11.0-play-28",
  "uk.gov.hmrc"                 %% "play-partials"                    % "8.3.0-play-28",
  "uk.gov.hmrc"                 %% "play-hmrc-api"                    % "7.1.0-play-28",
  "uk.gov.hmrc"                 %% "http-caching-client"              % "10.0.0-play-28",
  "uk.gov.hmrc"                 %% "play-conditional-form-mapping"    % "1.12.0-play-28",
  "com.typesafe.play"           %% "play-json"                        % "2.9.4",
  "org.typelevel"               %% "cats-core"                        % "2.9.0",
  "org.typelevel"               %% "cats-kernel"                      % "2.9.0",
  "com.github.tototoshi"        %% "play-json-naming"                 % "1.5.0",
  "org.julienrf"                %% "play-json-derived-codecs"         % "10.1.0",
  "com.github.julien-truffaut"  %% "monocle-core"                     % monocleVersion,
  "com.github.julien-truffaut"  %% "monocle-macro"                    % monocleVersion,
  "org.mindrot"                  % "jbcrypt"                          % "0.4",
  "com.eclipsesource"           %% "play-json-schema-validator"       % "0.9.5",
  "uk.gov.hmrc.mongo"           %% s"hmrc-mongo-$playVersion"          % hmrcMongoVersion,

  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.12" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.12" % Provided cross CrossVersion.full
)

def test(scope: String = "test,it") = Seq(
  "uk.gov.hmrc"                 %% "bootstrap-test-play-28"         % bootstrapVersion % scope,
  "org.scalatest"               %% "scalatest"                      % "3.2.15"          % scope,
  "org.scalatestplus.play"      %% "scalatestplus-play"             % "5.1.0"          % scope,
  "com.vladsch.flexmark"         % "flexmark-all"                   % "0.64.6"        % scope,
  "org.scalatestplus"           %% "mockito-3-4"                    % "3.2.10.0"        % scope,
  "com.github.tomakehurst"       % "wiremock-standalone"            % "2.27.2"         % scope,
  "org.pegdown"                  % "pegdown"                        % "1.6.0"          % scope,
  "org.jsoup"                    % "jsoup"                          % "1.15.4"         % scope,
  "org.scalacheck"              %% "scalacheck"                     % "1.17.0"         % scope,
  "com.github.julien-truffaut"  %% "monocle-law"                    % monocleVersion   % scope,
  "uk.gov.hmrc.mongo"           %% s"hmrc-mongo-test-$playVersion"  % hmrcMongoVersion % scope
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
  .settings(scalaVersion := "2.13.10")
  .settings(defaultSettings(): _*)
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.fhregistrationfrontend.views.html._"
    )
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
  .settings(scalacOptions += "-Wconf:cat=lint-multiarg-infix:silent")
  .settings(scalacOptions += "-P:silencer:globalFilters=Unused import")
  .settings(Global / lintUnusedKeysOnLoad := false)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
