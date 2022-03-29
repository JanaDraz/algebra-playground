package paramFAprototype

import java.io.File

fun getResultFor1paramFromPython( biosystemStr : String, rStr : String, eDirStr : String, eOriStr : String, pminStr : String, pmaxStr : String, deltaStr : String, maxT : String ) : MutableList<QueueItem> {
    
    //println("Calling Python:"+biosystemStr+" "+rStr+" "+eDirStr+" "+eOriStr+" "+pminStr+" "+pmaxStr+" "+deltaStr+" "+maxT)
    val process = Runtime.getRuntime().exec( arrayOf("/usr/bin/python3", "/home/jfabriko/PROGRAMOVANI/kotlin-pokusy/pythQDApar.py", biosystemStr, rStr, eDirStr, eOriStr, pminStr, pmaxStr, deltaStr, maxT ))
    val output1 = process.inputStream.bufferedReader().readLines()
    //println("Python output:")
    //println( output1 )
    return convertOutputToListOfSuccs( output1 )//dig results out somehow...
}

//TODO
//assumes +1 -1 facet orientations
//output from the Python script into the list of successors
fun convertOutputToListOfSuccs( pythonOutput : List<String>) : MutableList<QueueItem> {
    var result : MutableList<QueueItem> = mutableListOf<QueueItem>()
   //input = rectangle facet parset 
   //po : List<String> = mutableListOf<String>( "1,1 0,0 0.1,0.5",
   //                                            "0,0 1,1 0.1,0.5" )
    
    for( line in pythonOutput ){
        val parts = line.split(" ")
        
        val rparts = parts[0].split(",")
        var coordList = mutableListOf<Int>()
        for( rp in rparts ){
            coordList.add( rp.toInt() )
        }
        
        val eparts = parts[1].split(",")
        val edir = eparts[0].toInt()
        val eor = eparts[1].toInt()
        
        var interList = mutableListOf<IntervalDouble>()
        val parparts = parts[2].split(";") //we should separate the intervals by ;
        for( parint in parparts ){
            val ends = parint.split(",")
            val inter = IntervalDouble( ends[0].toDouble(), true, false, ends[1].toDouble(), true, false )
            interList.add( inter )
        }
        
        result.add( QueueItem( coordList.toTypedArray(), edir, eor, SortedListOfDisjunctIntervalsDouble( interList ) ) )
    }
    
    return result
}
