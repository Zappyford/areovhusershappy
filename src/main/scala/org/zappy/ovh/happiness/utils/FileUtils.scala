package org.zappy.ovh.happiness.utils

import sys.process._
import java.net.URL
import java.io.{File, FileInputStream, FileOutputStream, IOException}
import java.util.zip.{ZipEntry, ZipInputStream}

import org.apache.log4j.Logger

import scala.io.Source

object FileUtils {

  val logger: Logger = Logger.getLogger(FileUtils.getClass)

  def fileDownloader(url: String, filePath: String): Unit = {
    logger.debug("Checking if the file already exists...")
    if(!new File(filePath).exists()){
      logger.debug("Checking if the folders exist...")
      val folder = new File(new File(filePath).getParent)
      if(!folder.exists()){
        logger.debug("Folders not found creating them...")
        folder.mkdirs()
      }
      logger.debug("Downloading the zip file...")
      new URL(url) #> new File(filePath) !!
    } else {
      logger.debug("File already exists, skipping download !")
    }

  }

  def fileUnzipper(zipFile: String, outputFolder: String): Unit ={
    val buffer = new Array[Byte](1024)
    try {
      logger.debug("Opening Zip file...")
      //zip file content
      val zis: ZipInputStream = new ZipInputStream(new FileInputStream(zipFile))
      //get the zipped file list entry
      var ze: ZipEntry = zis.getNextEntry

      while (ze != null) {

        val fileName = ze.getName
        val newFile = new File(outputFolder + File.separator + fileName)
        logger.debug("Checking if the file "+newFile.getName+" already exists...")
        if(!newFile.exists()){
          logger.debug("Unzipping the file : "+newFile.getName)
          val fos = new FileOutputStream(newFile)

          var len: Int = zis.read(buffer)

          while (len > 0) {

            fos.write(buffer, 0, len)
            len = zis.read(buffer)
          }

          fos.close()
        }
        ze = zis.getNextEntry
      }

      zis.closeEntry()
      zis.close()
      logger.debug("Unzipping finished !")
    } catch {
      case e: IOException => println("exception caught: " + e.getMessage)
    }

  }

  def loadFile(filePath: String): Seq[String] = {
    Source.fromFile(filePath).getLines().toSeq
  }

}
