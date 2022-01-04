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

/* Get the result from Maxima output
 * assuming the result is exactly on one line, starting (%o4)
 */
fun digResultLineFromMaxima(maximaOutput : List<String>) : String{
    var result : String = ""
    for( line in maximaOutput ){
        if( line.contains("(%o4)") ) result = line.substringAfter("(%o4) ")
    }
    return result
}

/* Dig the result from Maxima output
 * assuming the result is on one or more lines
 * join the lines in one String and return.
 */
fun digResultLinesFromMaxima(maximaOutput : List<String>) : String{
    var result : String = ""
    //var unboundedResult : Boolean = false
    var beforeResult : Boolean = true
    var insideResult : Boolean = false
    var afterResult : Boolean = false
    var resultLines : MutableList<String> = mutableListOf<String>()
    
    for( line in maximaOutput ){
        if( line.contains("Problem not bounded!")){
                return line
        }
        if( line.contains("(%o4)") ){
            beforeResult = false
            insideResult = true
        }else if( line.contains("(%i5)") ){
            insideResult = false
            afterResult = true
        }
        if( afterResult ) break;//not interested in the rest of the output
        if( insideResult ){
            resultLines.add( line )
        }
    }
    
    result = resultLines.joinToString { it -> " "+it }
    result = result.substringAfter("(%o4) ")
    result = result.replace( ",,","," )
    
    
    //result = maximaOutput.joinToString{ it -> "Line> "+it }
    
    return result
}


//[1/2,[i = -1,h = 0,g = 0,f = 0,e = 1/2,d = -1/2,c = 0,b = 0,a = 1]]
//read the coefs into rings?? sometime afterwards
fun getCoefMapFromResultLine( line : String ) : MutableMap< String, String > {
    println( "LINE="+line )
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
        var sides : MutableList<String> = mutableListOf<String>()
        sides.addAll(s.split("="))
        //parenthesis around negative numbers
        if( sides[1][0] == '-' ){ sides[1] = "("+sides[1]+")" }
        map[sides[0]]=sides[1]
    }
    return map
} 

//function 2deg 2par approx for 2 lists of points (inside/outside)
//    pxin, pyin, pxout, pyout
//CALL THIS METHOD ONLY IF NONE OF THE ABOVE LISTS IS EMPTY
//IN THE OTHER CASE THE APPROXIMATION IS NONNECESSARY AND THIS 
//METHOD WILL NOT PRODUCE A NICE CODE
//create Maxima formula for an LP task of approximating a set of points
//with Double coordinates by a semialgebraic set given by inequalities
//-boundaries of the interval
//-p(pars)>=1 (coz je na intervalu nezaporny polynom)
//output the polynomial (and other conditions from the interval)
fun createCommandsForMaximaLPTaskPar2Deg2( pmin : Double, pmax : Double, qmin : Double, qmax : Double, pin : List<Double>, qin : List<Double>, pout : List<Double>, qout : List<Double>, modifiers : String ) : ArrayList<String> {
    val command = ArrayList<String>().apply {
        add("load(\"simplex\")$") // /$ ??
        add("display2d:false$") // /$ ??
        //generate interval
//        var intervalBounds : ArrayList<Double> = getIntervalBounds( listOf(pin,qin,pout,qout) )
        //generate alpha and p o alpha 
        //alpha = A t - B
//        val min1 : Double = intervalBounds[0]
//        val max1 : Double = intervalBounds[1]
//        val min2 : Double = intervalBounds[2]
//        val max2 : Double = intervalBounds[3]
        val A1 : Double = 2.0 / ( pmax - pmin )
        val B1 : Double = ( pmax + pmin ) / ( pmax - pmin )
        val A2 : Double = 2.0 / ( qmax - qmin )
        val B2 : Double = ( qmax + qmin ) / ( qmax - qmin)
        //p o alpha for 2 pars is a function
        //create lp task
        val goal : String = "a+i+e"
        add("minimize_lp("+goal+",[") 
        //inside points
        for( i in 0..(pin.size-1) ){
            val pi : Double = pin[i]
            val qi : Double = qin[i]
            val pOalpha : String = pOalphaPar2(A1,B1,A2,B2,str(pi),str(qi))
            add( pOalpha+">=1," )
        }
        //outside points
        for( i in 0..(pout.size-1) ){
            val pi : Double = pout[i]
            val qi : Double = qout[i]
            val pOalpha : String = pOalphaPar2(A1,B1,A2,B2,str(pi),str(qi))
            if( i < (pout.size-1) ){
                add(pOalpha+">=0,")
            }else{
                add(pOalpha+">=0]$modifiers );")//all nonnegative decision variables
            }
        }
        //end maxima input
        add("quit();")
    }
    println( command )
    return command
}


//NOT CORRECT...
//create Maxima formula for an LP task of approximating a set of points
//with Double coordinates by a semialgebraic set given by inequalities
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
//alpha(x) = Ax-B
fun pOalphaPar2( A1 : Double, B1 : Double, A2 : Double, B2 : Double, px : String, py : String ) : String {
        val sqrt3 : String = "1.73205080756888"
        val xstr : String = "("+A1+"*"+px+"-"+B1+")"
        val ystr : String = "("+A2+"*"+py+"-"+B2+")"
        
        val constant : String = "0.25*a"
        val linearX : String = "0.25*"+sqrt3+"*(b+d)*"+xstr
        val linearY : String = "0.25*"+sqrt3+"*(c+g)*"+ystr
        val degree2XY : String = "0.75*(h+f)*"+xstr+"*"+ystr
        val squareX : String = "0.75*e*"+xstr+"*"+xstr
        val squareY : String = "0.75*i*"+ystr+"*"+ystr
        
        var result : String = "("+constant+"+"+linearX+"+"+linearY+"+"+degree2XY+"+"+squareX+"+"+squareY+")"
        return result
}

/*Method for finding the lower and upper bounds of several
 * lists of coordinates.
 * input: list of lists of coords, like pins, qins, pouts, qouts...
 * -> pinmin,pinmax, qinmin,qinmax, poutmin,poutmax, qoutmin,qoutmax, ...
 * output is a list of 2m bounds min, max, min, max, ... etc.
 */
fun getIntervalBounds( pts : List<List<Double>> ) : ArrayList<Double> {
    var bounds = ArrayList<Double>().apply {
        for( coords in pts ){
            var min : Double = coords[0]
            var max : Double = coords[0]
            for( a in coords ){
                if( a > max ) max = a
                if( a < min ) min = a                
            }
            add(min)//to the end of the list
            add(max)//to the end of the list
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

//compute lp task by Maxima
//dig in the output for p o alpha coeficients
//output these

//from the p o alpha and the interval create
//the interval boundaries conditions
//conjunct with the p o alpha >= 1 condition
//for the semialgebraic set

//simplify the algebraic expression of the above (reduce)
//create intersections
