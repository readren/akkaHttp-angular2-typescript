name := """akkaHttp-angular2-typescript"""
version := "0.1.0-SNAPSHOT"
lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"
incOptions := incOptions.value.withNameHashing(true)
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

libraryDependencies ++= Seq(
  cache,
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
dependencyOverrides += "org.webjars.npm" % "minimatch" % "2.0.10"

// the typescript typing information is by convention in the typings directory
// It provides ES6 implementations. This is required when compiling to ES5.
typingsFile := Some(baseDirectory.value / "typings" / "browser.d.ts")

// use the webjars npm directory (target/web/node_modules ) for resolution of module imports of angular2/core etc
resolveFromWebjarsNodeModulesDir := true

// use the combined tslint and eslint rules plus ng2 lint rules
(rulesDirectories in tslint) := Some(List(tslintEslintRulesDir.value,ng2LintRulesDir.value))

routesGenerator := InjectedRoutesGenerator
