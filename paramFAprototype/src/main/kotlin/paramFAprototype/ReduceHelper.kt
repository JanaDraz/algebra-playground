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
    
    return "$resultPart \nWhole result: $resultAll" //trying to find the relevant qel part
}

fun getResultFromReduce(commandsForReduce: ArrayList<String>): List<String> {
    val tempFile = File.createTempFile("input", ".txt")
    tempFile.writeText(commandsForReduce.joinToString(separator = "\n"))
    val process = Runtime.getRuntime().exec(arrayOf("/home/jfabriko/PROGRAMOVANI/reduce-algebra/bin/call_reduce_with_input.sh",
        tempFile.absolutePath))
    val output1 = process.inputStream.bufferedReader().readLines()

    return output1 //digResultFromReduceOutput(output1)
}

/* If the commands contain "off nat" it should produce the output on a
 * line (or more lines string) ended with "$"...?
 */
fun parseQelResultFromReduceOutput( output : List<String> ) : String {
    return "~~"
}


fun getQelResultForFormula( formula : String ) : String {
    return parseQelResultFromReduceOutput( getResultFromReduce( createReduceCommandsForFormula( formula ) ) )
}

fun parseRootsFromReduceOutput( output : List<String> ): List<NumQ> {
    var rootList : List<NumQ> = listOf<NumQ>()//empty at first
    var rootLine : String = "~"
    //identify the line with $ character
    for( line in output ) {
        if( line.contains("\$") ) rootLine = line
    }
    //parse roots if any
    if( !rootLine.contains("~") ) {
        if( rootLine.contains("{}$") ){
            return rootList //empty list, zero polynomial
        }else{
        //there are some roots "{x = 0,x = i,x =  - i}$"
            rootLine = rootLine.filter({c -> !(c.isWhitespace())}) //"{x=0,x=i,x=-i}$"
            val start = rootLine.indexOf('{')+1
            val end = rootLine.indexOf('}')
            rootLine = rootLine.substring( start, end ) //"x=0,x=i,x=-i"
            var rootStrs = rootLine.split(",") //"x=0","x=i","x=-i"
            for( rs in rootStrs){//"x=0" 
                val rsParts = rs.split("=") //"x","0"
                if( !rsParts[1].contains("i") ) {//real roots, not complex
                    rootList += listOf( strToQ( rsParts[1] ) ) //"0" -> QZERO
                }
            }
        } 
    }
    
    return rootList
}

/* get the roots of a polynomial by reduce
 * sort the roots from smallest to largest
 * assuming the string argument is a univariate polynomial over Q
 */
fun getRoots( polyn : String ) :  List<NumQ> {
    var resultList : List<NumQ> = listOf()
    var strList : List<String> = listOf()
    
    val command = ArrayList<String>().apply {
        add("load_package redlog;")
        add("rlset reals;")
        add("off rlverbose;")
        add("off nat;")         //add("on rounded;")?
        add("root_val($polyn);")
        add("quit;")
    }
    
    val reduceOutput : List<String> = getResultFromReduce( command )
    println( reduceOutput )
    
    resultList = parseRootsFromReduceOutput( reduceOutput )
    
    return resultList
}
