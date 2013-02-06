import AssemblyKeys._

scalaVersion := "2.10.0"

name := "ScaladocCHM"

version :="1.0"

libraryDependencies += "javax.transaction" % "jta" % "1.1"

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.1"

assemblySettings

jarName in assembly := "ScaladocCHM.jar"
