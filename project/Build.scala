import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "rupics"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "com.google.zxing" % "core" % "2.1",
    "com.google.zxing" % "javase" % "2.1",
    "commons-io" % "commons-io" % "2.0",
    "org.apache.httpcomponents" % "httpclient" % "4.2.3",
      "org.jsoup" % "jsoup" % "1.7.2"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
