name := """akka-http-angular2-typescript"""
version := "0.1.0-SNAPSHOT"
lazy val root = (project in file(".")).enablePlugins(SbtWeb).settings(projectAddOns.settings)

scalaVersion := "2.11.8"
incOptions := incOptions.value.withNameHashing(true)
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

lazy val akkaVersion = "2.4.6"
/** Note that all webjars dependencies are marked as "provided". This is to avoid them be added to the runtime class path. Just for efficiency. It's not needed.
  * If they weren't marked as provided, they would be added to the runtime class path with the "META-INF/resources/webjars" path prefix. But since the SBT-Web's "webJarsNodeModules" task adds copies of them with the mentioned prefix and module version striped, which is more convenient, we can discard the originals. */  
libraryDependencies ++= Seq(
  // binding logback as the underlying logging framework for SLF4J
  "ch.qos.logback" % "logback-classic" % "1.1.7", 

  //akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,

  //angular2 framework and utilities capabilities
  "org.webjars.npm" % "angular2" % "2.0.0-beta.17" % "provided",
  "org.webjars.npm" % "systemjs" % "0.19.29" % "provided", // A dynamic module loader compatible with the ES2015 module specification. There are other viable choices including the well-regarded webpack. SystemJS happens to be the one we use in the documentation samples. It works.
  
  // polyfills required by angular2
  "org.webjars.npm" % "rxjs" % "5.0.0-beta.8" % "provided",// a polyfill for the Observables specification currently before the TC39 committee that determines standards for the JavaScript language. Developers should be able to pick a preferred version of rxjs (within a compatible version range) without waiting for Angular updates.
  // "org.webjars.npm" % "es6-promise" % "3.1.2" % "provided",
  "org.webjars.npm" % "es6-shim" % "0.35.1" % "provided", // monkey patches the global context (window) with essential features of ES2015 (ES6). Developers may substitute an alternative polyfill that provides the same core APIs. This dependency should go away once these APIs are implemented by all supported ever-green browsers.
  "org.webjars.npm" % "reflect-metadata" % "0.1.3" % "provided", // a dependency shared between Angular and the TypeScript compiler. Developers should be able to update a TypeScript package without upgrading Angular, which is why this is a dependency of the application and not a dependency of Angular.
  "org.webjars.npm" % "zone.js" % "0.6.12" % "provided", // a polyfill for the Zone specification currently before the TC39 committee that determines standards for the JavaScript language. Developers should be able to pick a preferred version of zone.js to use (within a compatible version range) without waiting for Angular updates.
  "org.webjars.npm" % "bootstrap" % "3.3.6" % "provided",

  // typescript dependencies
  "org.webjars.npm" % "typescript" % "1.8.10" % "provided",

  //tslint dependency
  "org.webjars.npm" % "tslint-eslint-rules" % "1.2.0" % "provided",
  "org.webjars.npm" % "codelyzer" % "0.0.19" % "provided"
)
dependencyOverrides += "org.webjars.npm" % "minimatch" % "3.0.0" % "provided"

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
