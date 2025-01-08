package ch.epfl.lap.wishbone_gen.bus.arbiter

import ch.epfl.lap.wishbone_gen._
import ch.epfl.lap.wishbone_gen.bus._
import chisel3._
import chisel3.util.switch
import chisel3.util.is
import chisel3.util.OHToUInt

object ArbiterState extends ChiselEnum{
  val DECIDE, SERVICE, COLLECT = Value
}

class ArbiterModule(masterDescriptions: Map[Int, MasterComponent])
  extends Module {
  import ArbiterState._
  
  val state = RegInit(DECIDE)

  // Interface for subclasses 
  val arbiterInputs = masterDescriptions.map({case (i, master) => 
    i -> IO(Input(Bool()).suggestName(s"${master.name}_cyc"))
  })

  val grants = masterDescriptions.map( {case (i, master) => {
    i -> Wire(Bool()).suggestName(s"s_${master.name}_gnt")
  }})
  private val hasDecided = grants.foldLeft(false.B)({ case (or, (i, grant)) => or | grant})
  
  
  // Outputs (should ideally only be used by this abstract class)
  val grantsOut = masterDescriptions.map( {case (i, master) => {
    i -> IO(Output(Bool()).suggestName(s"${master.name}_gnt"))
  }})

  val grantsReg = masterDescriptions.map( {case (i, master) => {
    val grantReg = RegInit(false.B).suggestName(s"s_${master.name}_gntReg")
    grantsOut(i) := grantReg
    i -> grantReg
  }})
  private val transactionEnd = grantsReg.foldLeft(false.B)({ case (or, (i, grantReg)) => {
    (grantReg & ~arbiterInputs(i)) | or
  }})

  val gntId = IO(Output(UInt()))

  val cyc_out = IO(Output(Bool()))
    
  // private val delay = 0.U
  // private val delay_collect = RegInit(delay)

  // State machine
  switch (state) {
    is(DECIDE) {
      when (hasDecided) {
        state := SERVICE
      }
    }
    is(SERVICE) {
      when (transactionEnd) {
        state := DECIDE
      }
    }
    // is(COLLECT) {
    //   when (delay_collect === 0.U) {
    //     // TODO add option for delay (to let the master component start a new request before next round)
    //     state := DECIDE
    //   }
    // }
  }
  
  // Outputs logic
  private val is_deciding = (state === DECIDE)
  private val is_servicing = (state === SERVICE)
  grantsReg.foreach({ case (i, grantReg) => 
      when (is_deciding) {
        grantReg := grants(i)
      }.elsewhen (is_servicing) {
        grantReg := grantReg
        // delay_collect := delay
      }
      // .otherwise {
      //   when (!(delay_collect === 0.U)) {
      //     delay_collect := delay_collect-1.U
      //   }.otherwise {
      //     delay_collect := delay_collect
      //   }
      //   grantReg := false.B
      // }
    })

  private val grantsVec = Wire(Vec(grantsOut.size, Bool()))
  grantsOut.foreach({case (i, grantOut) => grantsVec(i) := grantOut})
  gntId := OHToUInt(grantsVec)

  cyc_out := arbiterInputs.foldLeft(false.B)({ case (or, (_, cyc)) => or | cyc})
}
