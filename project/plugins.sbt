/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"        % "3.24.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables"    % "2.6.0")
addSbtPlugin("org.playframework" % "sbt-plugin"            % "3.0.7")
addSbtPlugin("org.scalastyle"    % "scalastyle-sbt-plugin" % "1.0.0" exclude ("org.scala-lang.modules", "scala-xml_2.12"))
addSbtPlugin("org.scoverage"     % "sbt-scoverage"         % "2.3.1")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"           % "0.6.4")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"          % "2.5.4")
