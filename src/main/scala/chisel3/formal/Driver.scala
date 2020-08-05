package chisel3.formal

import java.io.{File, PrintWriter}
import java.nio.file.Paths

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage, DesignAnnotation}

import scala.collection.mutable


object Driver {
  def symbiyosysTemplate(filename: String, module: String) =
    s"""[tasks]
       |cover
       |bmc
       |prove
       |
       |[options]
       |cover:
       |mode cover
       |depth 20
       |--
       |bmc:
       |mode bmc
       |depth 20
       |--
       |prove:
       |mode prove
       |depth 20
       |--
       |[engines]
       |smtbmc
       |
       |[script]
       |read -formal ResetCounter.sv
       |read -formal TestCase.sv
       |read -formal $filename
       |prep -top $module
       |
       |[files]
       |ResetCounter.sv
       |TestCase.sv
       |$filename
       |""".stripMargin

  def apply[T <: RawModule](dutGen: () => T, targetDir: String = "build"): VerificationResult = {
    val rtl = generateRTL(dutGen, targetDir = targetDir)
    val module = moduleName(dutGen)
    val filename = module + ".sv"
    val sbyFilename = s"$module.sby"
    val sbyFileContent = symbiyosysTemplate(filename, module)
    new PrintWriter("build/" + sbyFilename) {
      write(sbyFileContent)
      close()
    }
    val symbiyosys = new ProcessBuilder("sby", "-f",
      sbyFilename).directory(new File(targetDir)).start()
    val returnCode = symbiyosys.waitFor()
    VerificationResult(
      module,
      filename,
      rtl,
      sbyFilename,
      sbyFileContent,
      returnCode,
      // Note: this is actually the _output_ stream of the subprocess. Yeah, scala...
      io.Source.fromInputStream(symbiyosys.getInputStream).mkString,
      io.Source.fromInputStream(symbiyosys.getErrorStream).mkString,
    )
  }

  def generateRTL[T <: RawModule](dutGen: () => T, targetDir: String = "build",
                                 outputFile: String = ""): String = {
    val args = new mutable.ArrayBuffer[String]
    args ++= Array("--target-dir", targetDir)
    val stage = new ChiselStage()
    val modName = moduleName(dutGen)
    val rtl = stage.emitSystemVerilog(dutGen(), args.toArray)
    val suffix = "sv"
    val currentPath = Paths.get(System.getProperty("user.dir"))
    val out = if (outputFile.isEmpty) {
      modName + "." + suffix
    } else {
      outputFile
    }
    val filePath = Paths.get(currentPath.toString, targetDir, out)
    new PrintWriter(filePath.toString) {
      print(rtl)
      close()
    }
    rtl
  }

  def moduleName[T <: RawModule](dutGen: () => T): String = {
    val annos = ChiselGeneratorAnnotation(dutGen).elaborate
    val designAnno = annos.last
    designAnno match {
      case DesignAnnotation(dut) => dut.name
    }
  }
}
