package chisel3.formal


case class VerificationResult(
                               moduleName: String,
                               rtlFilename: String,
                               rtl: String,
                               sbyFilename: String,
                               sby: String,
                               returnCode: Int,
                               output: String,
                               error: String,
                             )
