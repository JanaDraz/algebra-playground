package paramFAprototype

import java.io.File

fun getResultFromMaxima(commandsForDreal: ArrayList<String>): List<String> {
    val tempFile = File.createTempFile("maximainput", ".txt")
    tempFile.writeText(commandsForDreal.joinToString(separator = "\n"))
    
    val process = Runtime.getRuntime().exec(arrayOf("/usr/bin/maxima", /*"--very-quiet",*/"-b", 
        tempFile.absolutePath))
    val output1 = process.inputStream.bufferedReader().readLines()

    return output1 //dig results out somehow...
}

fun digResultLineFromMaxima(maximaOutput : List<String>) : String{
    var result : String = ""
    for( line in maximaOutput ){
        if( line.contains("(%o4)") ) result = line.substringAfter("(%o4) ")
    }
    return result
}

//[1/2,[i = -1,h = 0,g = 0,f = 0,e = 1/2,d = -1/2,c = 0,b = 0,a = 1]]
//read the coefs into rings?? sometime afterwards
fun getCoefMapFromResultLine( line : String ) : MutableMap< String, String > {
    var map = mutableMapOf< String, String >()
    val left1 : Int = line.indexOf( "[" )
    val left2 : Int = line.indexOf( "[", startIndex = (left1 +1 ))
    val comma1 : Int = line.indexOf( "," )
    val right2 : Int = line.indexOf( "]" )
    val right1 : Int = line.indexOf( "]", startIndex = (right2 + 1 ))
    var goal : String = line.substring( left1+1, comma1 )
    var line2 = line.substring( left2+1, right2 )
    var tokens : List<String> = line2.split(",")
    map["goal"] = goal
    //val coefs = listOf( "a","b","c","d","e","f","g","h","i" )
    for( t in tokens ){
        var s : String = t.filter{ it -> !it.isWhitespace() }
        var sides : List<String> = s.split("=")
        map[sides[0]]=sides[1]
    }
    return map
} 

//create Maxima formula for an LP task of approximating a set of points
//with Float coordinates by a semialgebraic set given by inequalities
//-boundaries of the interval
//-p(pars)>=1 (coz je na intervalu nezaporny polynom)
//output the polynomial (and other conditions from the interval)
//G...number of grid points for conditions p o alpha >= 0
fun createCommandsForMaximaLPTaskPar2Deg2( px : List<Double>, py : List<Double>, G : Int ) : ArrayList<String> {
    val command = ArrayList<String>().apply {
        add("load(\"simplex\")$") // /$ ??
        add("display2d:false$") // /$ ??
        //generate interval
        var intervalBounds : ArrayList<Double> = getIntervalBounds( listOf(px, py) )
        //generate alpha and p o alpha 
        //alpha = A t - B
        val min1 : Double = intervalBounds[0]
        val max1 : Double = intervalBounds[1]
        val min2 : Double = intervalBounds[2]
        val max2 : Double = intervalBounds[3]
        val A1 : Double = 2.0f/(max1 - min1)
        val B1 : Double = max1 / (max1 - min1)
        val A2 : Double = 2.0f/(max2 - min2)
        val B2 : Double = max2 / (max2 - min2)
        //p o alpha for 2 pars is a function
        //create lp task
        val goal : String = "a+i+e"
        add("minimize_lp("+goal+",[") 
        //inside points
        for( i in 0..(px.size-1) ){
            val pxi : Double = px[i]
            val pyi : Double = py[i]
            val pOalpha : String = pOalphaPar2(A1,B1,A2,B2,str(pxi),str(pyi))
            add( pOalpha+">=1," )
        }
        //interval grid points
        val gx : Double = (max1-min1)/G
        val gy : Double = (max2-min2)/G
        for( i in 0..G ){
            val gxi : Double = min1 + i*gx
            for( j in 0..G ){
                val gyi : Double = min2 +j*gy
                val pOalpha : String = pOalphaPar2(A1,B1,A2,B2,str(gxi),str(gyi))
                val ineq = pOalpha+">=0" 
                if( (i < (G-1) ) || (j < (G-1) ) ){ 
                    add(ineq + ",") 
                }else{
                    add(ineq+"]);")
                }
            }
        }
        add("quit();")
    }
    return command
}

//p o alpha will be the string left side of LP inequalities
fun pOalphaPar2( A1 : Double, B1 : Double, A2 : Double, B2 : Double, px : String, py : String ) : String {
        val xstr : String = "("+A1+"*"+px+"-"+B1+")"
        val ystr : String = "("+A2+"*"+py+"-"+B2+")"
        var result : String = "(a+(b+d)*"+xstr+"+(c+g)*"+ystr+"+(h+f)*"+xstr+"*"+ystr+"+e*"+xstr+"*"+xstr+"+i*"+ystr+"*"+ystr+")"
        return result
}

//output is a list of 2m bounds min, max, min, max, ... etc.
fun getIntervalBounds( pts : List<List<Double>> ) : ArrayList<Double> {
    var bounds = ArrayList<Double>().apply {
        for( coords in pts ){
            var min : Double = coords[0]
            var max : Double = coords[0]
            for( a in coords ){
                if( a > max ) max = a
                if( a < min ) min = a                
            }
            add(min)
            add(max)
        }
    }   
    return bounds
}

fun createCommandsForMaximaLPTask(par : Int, deg : Int, pts : ArrayList<List<Double>>, G : Int ) : ArrayList<String> {
    var commands : ArrayList<String> = ArrayList<String>(0)
    if( (par == 2) && (deg == 2) ){
        return createCommandsForMaximaLPTaskPar2Deg2(pts[0],pts[1], G)
    }else{
        //generate interval
        var boundsList : ArrayList<Double> = getIntervalBounds( pts )
        //generate alpha and p o alpha 
        //create lp task
            //create goal
            //create inequalities for points inside
            //create inequalities for points on grid in the interval
        return commands
    }
}

//compuete lp task by Maxima
//dig in the output for p o alpha coeficients
//output these

//from the p o alpha and the interval create
//the interval boundaries conditions
//conjunct with the p o alpha >= 1 condition
//for the semialgebraic set

