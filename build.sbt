name := """akkaHttp-angular2-typescript"""
version := "0.1.0-SNAPSHOT"
lazy val root = (project in file(".")).enablePlugins(SbtWeb).settings(projectAddOns.settings)

scalaVersion := "2.11.8"
incOptions := incOptions.value.withNameHashing(true)
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

libraryDependencies ++= Seq(
  // binding logback as the underlying logging framework for SLF4J
  "ch.qos.logback" % "logback-classic" % "1.1.3",

  //akka dependencies
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "com.typesafe.akka" %% "akka-stream" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.4",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.4",

  //angular2 dependencies
  "org.webjars.npm" % "angular2" % "2.0.0-beta.17",
  "org.webjars.npm" % "systemjs" % "0.19.26",
  "org.webjars.npm" % "todomvc-common" % "1.0.2",
  "org.webjars.npm" % "rxjs" % "5.0.0-beta.7",
  "org.webjars.npm" % "es6-promise" % "3.1.2",
  "org.webjars.npm" % "es6-shim" % "0.35.0",
  "org.webjars.npm" % "reflect-metadata" % "0.1.3",
  "org.webjars.npm" % "zone.js" % "0.6.12",
  "org.webjars.npm" % "typescript" % "1.8.10",

  //tslint dependency
  "org.webjars.npm" % "tslint-eslint-rules" % "1.2.0",
  "org.webjars.npm" % "codelyzer" % "0.0.19"
)
dependencyOverrides += "org.webjars.npm" % "minimatch" % "3.0.0"

// the typescript typing information is by convention in the typings directory
// It provides ES6 implementations. This is required when compiling to ES5.
typingsFile := Some(baseDirectory.value / "typings" / "browser.d.ts")

// use the webjars npm directory (target/web/node_modules ) for resolution of module imports of angular2/core etc
resolveFromWebjarsNodeModulesDir := true

// use the combined tslint and eslint rules plus ng2 lint rules
(rulesDirectories in tslint) := Some(List(tslintEslintRulesDir.value,ng2LintRulesDir.value))

// If true, sources from the project's base directory are included as main sources.
sourcesInBase := false

// adds the assets resulting from the assets pipeline to the classpath.  // TODO avoid the webjar generated for export be included in the class path.  
(managedClasspath in Runtime) += WebKeys.assets.in(Assets).value
// alternatively, you can use the jar version of the assets.
//(managedClasspath in Runtime) += (packageBin in Assets).value


// adds, to the runtime classpath, the directories where the webjars are extracted (with META-INF and version stripped).
(managedClasspath in Runtime) ++= WebKeys.nodeModuleDirectories.in(Assets).value
(managedClasspath in Runtime) += WebKeys.webJarsDirectory.in(Assets).value  


// run the application in a separate JVM (instead of using the same than SBT)
fork in run := true
