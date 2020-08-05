# Chisel Formal Verification

This package contains a set of tools and helpers for formally verifying Chisel
modules. To use it:

1. `import chisel3.formal._`
2. Set up your Chisel module to extend the `Formal` trait
3. Within your module, write `assert(false.B)` somewhere
4. Set up a test class for your module that extends the `FormalSpec` abstract base class
5. Within the test class, call `verify` on an instance of your module
6. Run the test class

You should get a single failure on the line at which you added the `assert`. 
This means that your setup is working! Now remove that failing assert and add
your own verification logic.

## A worked example

Suppose we have a module `KeepMax` whose purpose is to remember the maximum
value it has seen since the last reset and output it. Here's one possible
Chisel implementation of `KeepMax`.

```scala
import chisel3._

class KeepMax(width: Int) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(width.W))
    val out = Output(UInt(width.W))
  })

  val max = RegInit(0.U(width.W))
  when (io.in > max) {
    max := io.in
  }
  io.out := max
}
```

Now we want to add some formal checks to this module. We start by mixing in the
Formal trait, which is a "batteries-included" set of tools and helpers for this
purpose.

```scala
...
import chisel3.formal.Formal

class KeepMax(width: Int) extends Module with Formal {
...
```

Let's assert that the value of `io.out` will never decrease from one timestep to
the next. If this property doesn't hold true, `KeepMax` is definitely broken
(i.e. this is a necessary but not sufficient property). To do this, we'll need
to make use of the `past` helper and the `assert` check.

```scala
class KeepMax(width: Int) extends Module with Formal {
  ...
  // get the value of io.out from 1 cycle in the past
  past(io.out, 1) (pastIoOut => {
    assert(io.out >= pastIoOut)
  })
}
```

Now we'll actually run a formal test of this assertion. We'll create a class
`KeepMaxFormalSpec` to handle this operation (note: I recommend naming formal
tests with the suffix `FormalSpec` to distinguish them from your other unit
tests for the given module).

```scala
import chisel3.formal.FormalSpec

class KeepMaxFormalSpec extends FormalSpec {
  Seq(
    () => new KeepMax(1),
    () => new Keepmax(8),
  ).map(verify(_))
}
```

The `FormalSpec` abstract base class provides the method `verify` which can be
called on any Chisel module to elaborate the module and run it through the
Symbiyosys model checker. If your module has parameters it's a good idea to
make a list of all the important instances and map `verify` across all of them.

Now simply run `KeepMaxFormalSpec` to see the result.

```
sbt> testOnly example.package.KeepMaxFormalSpec
```

The formal check passed! We've established a key property on the way to proving
the correctness of this module.
