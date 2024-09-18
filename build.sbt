import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import play.sbt.routes.RoutesKeys

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
    ScoverageKeys.coverageExcludedFiles := "<empty>;.*javascript.*;.*models.*;.*Routes.*;.*viewmodels.*;.*testonly.*;.*controllers.AdminPageController.*;" +
      ".*controllers.AuthenticationController.*",
    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(majorVersion := 0)
  .settings(libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
  .settings(PlayKeys.playDefaultPort := 1118)
  .settings(playSettings : _*)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(scalaVersion := "2.13.12",
    RoutesKeys.routesImport ++= Seq(
      "models._"
    ))
  .settings(defaultSettings(): _*)
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .settings(
    libraryDependencies ++= AppDependencies.all,
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.fhregistrationfrontend.views.html._",
      "viewmodels.govuk.all._"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    IntegrationTest / resourceDirectory := baseDirectory.value / "it/resources",
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false,
    IntegrationTest / scalafmtOnCompile := true)
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scalacOptions += "-P:silencer:pathFilters=routes")
  .settings(scalacOptions += "-Wconf:cat=lint-multiarg-infix:silent")
  .settings(scalacOptions += "-P:silencer:globalFilters=Unused import")
  .settings(Global / lintUnusedKeysOnLoad := false)
