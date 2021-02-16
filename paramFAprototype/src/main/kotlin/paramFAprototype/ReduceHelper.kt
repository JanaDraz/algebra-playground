package paramFAprototype

import java.io.File
import kotlin.collections.ArrayList


fun createReduceCommandsForFormula(formula: String): ArrayList<String> {
    val command = ArrayList<String>().apply {
        add("load_package redlog;")
        add("rlset reals;")
        add("off rlverbose;")
        add("phi := $formula;")
        add("psi := rlqe phi;")
        add("rldecdeg1(psi,{p});")
        add("quit;")
    }
    return command
}


/*
 Join all the lines of the output, return them
x Assuming the 14th line in the output of reduce is the result (we only init reduce, set the formula and call rlqe)
x In case the output doesn't have fourteen rows, the result is an empty string
x CHECK if always valid assumption?
 */
fun digResultFromReduceOutput(output1: List<String>): String {
    var i1 : Int = 0
    var resultPart : String = "" //the true or false part
    var resultAll : String = "" //everything
    var result5 : String = "" //after the line with 5:
    var eval5 : Int = 0
    output1.forEach {
        if(it.contains("true") or it.contains("false"))
            resultPart = it
        if(eval5 == 1)
            result5 = it
        if(it.contains("5:"))
        //result5 = it
            eval5 = 1
        resultAll += it+"\n"
        i1++
    }
    return "$resultPart"// Whole result: $resultAll"
}

fun getResultFromReduce(commandsForReduce: ArrayList<String>): String {
    val tempFile = File.createTempFile("input", ".txt")
    tempFile.writeText(commandsForReduce.joinToString(separator = "\n"))
    val process = Runtime.getRuntime().exec(arrayOf("/home/jfabriko/PROGRAMOVANI/reduce-algebra/bin/call_reduce_with_input.sh",
        tempFile.absolutePath))
    val output1 = process.inputStream.bufferedReader().readLines()

    return digResultFromReduceOutput(output1)
}
