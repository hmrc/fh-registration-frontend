import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import play.sbt.routes.RoutesKeys

val appName = "fh-registration-frontend"

lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = Seq(
    coverageExcludedPackages := Seq(
      "uk.gov.hmrc.BuildInfo",".*Routes.*",".*RoutesPrefix","Reverse[^.]*",
      "uk.gov.hmrc.fhregistrationfrontend.views.html.helpers",
      "uk.gov.hmrc.fhregistrationfrontend.views.html.*").mkString(","),
    coverageExcludedFiles :=
      Seq("<empty>",".*javascript.*",".*models.*",".*Routes.*",".*viewmodels.*",".*testonly.*",".*controllers.AdminPageController.*",""+
        ".*controllers.AuthenticationController.*").mkString(","),
    coverageMinimumStmtTotal := 74.00,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    Test / parallelExecution := false
  )

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .settings(majorVersion := 0)
  .settings(libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
  .settings(PlayKeys.playDefaultPort := 1118)
  .settings(playSettings : _*)
  .settings(scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(scalaVersion := "3.7.1",
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
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(scalacOptions ++= Seq("-Wconf:msg=lint-multiarg-infix:silent",
    "-Wconf:src=target/scala.*/twirl/.*:s",
    "-Wconf:msg=Flag.*repeatedly:s",
    "-Wconf:msg=composePrism:silent",
    "-Wconf:msg=unused:silent",
    "-Wconf:msg=Unreachable:silent",
    "-Wconf:cat=deprecation:silent")
  )
  .settings(Global / lintUnusedKeysOnLoad := false)
