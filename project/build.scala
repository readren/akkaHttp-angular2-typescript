import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files

import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb

import sbt._
import sbt.Keys._
import WebKeys._

/**
 * Nests the definition of project settings that are too complex to be put in the "build.sbt" file.
 * There is only one setting for now: the mirrorNodeModules task.
 */
object projectAddOns {
  val mirrorNodeModules = TaskKey[File]("mirrorNodeModules", "Typescript modules resolution mechanism requires that the 'mode_modules' folder be a sibbling of a folder that contains all the typescript source files. And SBT requires that managed files be inside the 'target' folder. So, to comply with both we let SBT generate the 'node_modules' folder where he wants, and create a virtual folder that mirrors said folder in a place where the typescript's module resolution mechanisms can find it: inside the parent folder of the assets's sourceDirectoy. This task creates said virtual folder.")

  private val unscopedSettings: Seq[Setting[_]] = Seq(
    mirrorNodeModules := createNodeModulesMirror(webJarsNodeModulesDirectory.value, sourceDirectory.value, streams.value.log),
    webJarsNodeModules <<= webJarsNodeModules.dependsOn(mirrorNodeModules)) // the goal of this line is that the mirrorNodeModules task be automaticaly executed every time the webJarsNodeModules task is executed

  val settings = inConfig(Assets)(unscopedSettings) ++ inConfig(TestAssets)(unscopedSettings)

  private def createNodeModulesMirror(realNodeModulesDirectory: File, sourceDirectory: File, logger: Logger): File = {
    val virtualNodeModulesDirectory = sourceDirectory.getParentFile / "node_modules"
    createVirtualDirectory(realNodeModulesDirectory, virtualNodeModulesDirectory, logger)
    virtualNodeModulesDirectory
  }

  /**
   * Creates a virtual directory that mirrors the contens of another directory. If the virtual directory already exists, nothings happens.
   * @param of a file indicating the real directory path, the one to be imitated
   * @param at a file indicating the virtual directory path
   */
  private def createVirtualDirectory(of: File, at: File, logger: Logger): Unit = {
    if (!at.exists) {
      try {
        Files.createSymbolicLink(at.toPath(), of.toPath)
      } catch {
        // the createSymbolicLink method fails on windows OS if the user has not enough privileges. The mklink command is less demanding. 
        case e: IOException if System.getProperty("os.name").toLowerCase.contains("win") =>
          val cmd = "mklink /J \"" + at + "\" \"" + of + "\"\n"
          val pb = Process.apply("cmd") #< new ByteArrayInputStream(cmd.getBytes())
          pb.lines.find(_.startsWith("Junction created")) foreach { logger.info(_) }
      }
    }
  }

}