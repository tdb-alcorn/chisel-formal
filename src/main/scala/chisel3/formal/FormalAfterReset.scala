package chisel3.formal

import chisel3._
import chisel3.internal.sourceinfo.SourceInfo

trait FormalAfterReset extends Formal {
  this: Module =>

  override def assert(predicate: Bool, message: String = "")
            (implicit sourceInfo: SourceInfo,
             compileOptions: CompileOptions): Unit = {
    afterReset {
      super.assert(predicate, message)
    }
  }

  override def assume(predicate: Bool, message: String = "")
            (implicit sourceInfo: SourceInfo,
             compileOptions: CompileOptions): Unit = {
    afterReset {
      super.assume(predicate, message)
    }
  }

  override def cover(predicate: Bool, message: String = "")
            (implicit sourceInfo: SourceInfo,
             compileOptions: CompileOptions): Unit = {
    afterReset {
      super.cover(predicate, message)
    }
  }
}