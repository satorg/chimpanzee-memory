name := "chimpanzee-memory"
version := "0.1"

scalaVersion := "2.11.8"

enablePlugins(ScalaJSPlugin)
persistLauncher in Compile := true
persistLauncher in Test := false

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
