resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(
  Resolver.ivyStylePatterns)
resolvers += "HMRC Releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns)
resolvers += Resolver.jcenterRepo
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")


addSbtPlugin("uk.gov.hmrc"        % "sbt-auto-build"        % "3.24.0")
addSbtPlugin("uk.gov.hmrc"        % "sbt-distributables"    % "2.6.0")
addSbtPlugin("org.playframework"  % "sbt-plugin"            % "3.0.8")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"         % "2.3.0")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin" % "1.0.0" exclude("org.scala-lang.modules", "scala-xml_2.12"))
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"          % "2.5.2")
addSbtPlugin("io.github.irundaia" % "sbt-sassify"           % "1.5.2")