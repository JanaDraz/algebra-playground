package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

import java.util.Queue
import java.util.LinkedList

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/*TODO:
 * Main tasks:
 * - given coordinates of a rectangle + direction, orientation,
 *   return the set of parameters for which given transition exists
 * - the same with only the admissible parameter set
 * - the above with a sequence of transitions
 * Partial stuff:
 * - given system, state, transition info - generate the quantified
 *   formula for reduce
 * - ... to the reduce helper: add a specialized function for 
 *   parsing the output of quantifier elimination (including boolean
 *   values, (several lines output?)
 */

//what is the relative position of the two given states?
//2DirPlusOrient
//dir 0    1    2    3 ...
//do 01   23   45   67 ...
//ori -1,1 -1,1 -1,1 -1,1
//form of state array:
//dim * coords, then dir and 0/1
//assume there is only one difference, (we take the first one) 
fun getDirOriCode( state1 : Array<Int>, state2 : Array<Int>, dim : Int ) : Int {
    var dir : Int = -1
    var dif : Int = 0
    
    for( i in 0..(dim-1) ){
        if( state1[i] != state2[i] ){
            dir = i
            if( state1[i] < state2[i] ) {
                dif = 1
            } else {
                dif = -1
            }
        }
    }
    
    return 2 * dir + dif
}

//1,...,dim
fun getDir( code : Int ) : Int {
    return code / 2 //whole part
}

//-1 or 1
fun getOri( code : Int) : Int {
    if( (code % 2) != 0 ) 
        return 1 
    else 
        return -1
}

/* Taylor 2nd degree, k steps
 * - FIXME - 1st degree -> second degree
 */
fun getTaylor2Formula( dirori1 : Int, state : Array<Int>, dirori2 : Int, system : BioSystem, k : Int ) : String {
    var formula : String = "~~"
    var dim : Int = system.getDim()
    var parCount : Int = system.getParamCount()
    var dir1 : Int = getDir( dirori1 ); var ori1 : Int = getOri( dirori1 )
    var dir2 : Int = getDir( dirori2 ); var ori2 : Int = getOri( dirori2 )
        
    var existsPart : String = "" //ex(var, ex(var, ...
    var boundsPart : String = "" // var >= .. and var <= .. and .. and d>=0 and d<=maxT/k
    var taylorPart : String = "" //
    var parenthesisPart : String = "" //...))
    
    val varStrs : Array<String> = system.getVarStrings()
    var taylVars : MutableList<String> = mutableListOf<String>()
    //add variables with step labels to taylVars
    //(l+1) x dim variables x_00,..,x_nk
    for( step in 0..k ) {
        for( i in 0..(dim-1) ) taylVars.add( varStrs[i] + "${step}" )
    }    

    taylVars.add( "d" )
    
    //use the incomplete list of variables (with delta var d, without parameters),
    //to create the beginning of the quantified formula
    for( tv in taylVars ) existsPart += ( "ex(" + tv + "," )
        
    //add parameters (without labels) to taylVars
    for( par in system.getParStrings() ) {
        taylVars.add( par )
    }
        
    for( i in 0..(dim*(k+1)) ) parenthesisPart += ")" //variables + d

    var j : Int = 0
    //bounds for first facet
    //on x_00 .. x_n0
    var loBounds : MutableList<String> = mutableListOf<String>()
    var upBounds : MutableList<String> = mutableListOf<String>()
    for( i in 0..(dim-1) ) {
        if( i == dir1 ) {
            if( ori1 > 0 ) { //right end of rectangle
                loBounds.add("") //i
                upBounds.add( taylVars[i]+"="+system.getTres( i, state[i] + 1 ) )
            } else { //ori1 < 0, left end of rectangle
                loBounds.add( taylVars[i]+"="+system.getTres( i, state[i] ) )
                upBounds.add( "" )
            }
        } else {
            loBounds.add( taylVars[i]+">="+system.getTres( i, state[i] ) )
            upBounds.add( taylVars[i]+"<="+system.getTres( i, state[i] + 1 ) )
        }
        j += 1
    }
    //bounds inside state rectangle ... for taylor 
    //on x_01 .. x_n1 .. x_0k-1 .. x_nk-1
    for( m in 1..(k-1) ) {
        for( i in 0..(dim-1) ){
            loBounds.add( taylVars[j]+">="+system.getTres( i, state[i] ) )//j
            upBounds.add( taylVars[j]+"<="+system.getTres( i, state[i] + 1 ) )
            j += 1
        }
    }
    //bounds for second facet
    for( i in 0..(dim-1) ) {
        if( i == dir2 ) {
            if( ori2 > 0 ) { //right end of rectangle
                loBounds.add( "" ) //j
                upBounds.add( taylVars[j]+"="+system.getTres( i, state[i] + 1 ) )
            } else { //ori1 < 0, left end of rectangle
                loBounds.add( taylVars[j]+"="+system.getTres( i, state[i] ) )
                upBounds.add( "" )
            }
        } else {
            loBounds.add( taylVars[j]+">="+system.getTres( i, state[i] ) )
            upBounds.add( taylVars[j]+"<="+system.getTres( i, state[i] + 1 ) )
        }
        j += 1
    }
    //and bounds for d
    loBounds.add( "d>=0" ) //j
    upBounds.add( "d<=" + (system.getMaxT().toDouble()/k) )
    
    for( i in 0..j ){
        if( !loBounds[i].isEmpty() ) {
            boundsPart += loBounds[i] + " and "
        }
        if( !upBounds[i].isEmpty() ) {
            boundsPart += upBounds[i] + " and "
        }
    }
    
    for( step in 1..k ) {
        //and x1=(x0+d) and y1=(y0+p*d)")))))
        //taylor part ... deg 1 = Euler
        //this function is just for k=1, TODO NOW extend for k general
        var tayl : MutableList<String> = mutableListOf<String>() //taylor expressions per var
        var derString : String = ""
        var der2String : String = ""
    
        //variable strings for variables from the preceding step 
        //(or initial point in facet 1)
        var mutVarsOrig : MutableList<String> = mutableListOf<String>()
        for( i in ((step-1)*dim)..(step*dim-1) ) {
            mutVarsOrig.add( taylVars[i] )
        }
        //add the strings for parameters that appear in the derivatives
        //they are placed after all the vars from all steps and after d
        for( i in ((k+1)*dim + 1)..((k+1)*dim + parCount) ) {
            mutVarsOrig.add( taylVars[i] )
        }
        //immutable array of varstrings usable by rings functions
        var varsOrig : Array<String> = mutVarsOrig.toTypedArray() //arrayOf<String>( mutVars0 ) //not taylVars.copyOfRange(0, dim)
    
        //expression x1 = x0 + ... by Taylor approx 
        //second degree
        for( i in 0..(dim-1) ) {
            derString = system.getDerPols()[i].toString(*varsOrig)
            der2String = system.get2DerHalfPols()[i].toString(*varsOrig)
            tayl.add( taylVars[step*dim+i] + "=(" + taylVars[(step-1)*dim+i] + 
                        "+" + "d*(" + derString + ")" +
                        "+"+ "d*d*(" +der2String +"))" ) //ith place
        } 
    
        for( i in 0..(dim-2) ) {
            taylorPart += tayl[i] + " and " 
        }
        taylorPart += tayl[dim-1] //last for one step without "and" temporarily
        if( step != k ) {
            taylorPart += " and " //only the very last without the "and" permanently
        }
    }
    
    var facetToFacetFormula = existsPart + boundsPart + taylorPart + parenthesisPart
    
    return facetToFacetFormula
}

/* Taylor 1st degree, k steps
 * dirori1 according to state rectangle
 * dirori2 according to state rectangle
 */
fun getTaylor1Formula( dirori1 : Int, state : Array<Int>, dirori2 : Int, system : BioSystem, k : Int ) : String {
    var formula : String = "~~"
    var dim : Int = system.getDim()
    var parCount : Int = system.getParamCount()
    var dir1 : Int = getDir( dirori1 ); var ori1 : Int = getOri( dirori1 )
    var dir2 : Int = getDir( dirori2 ); var ori2 : Int = getOri( dirori2 )
        
    var existsPart : String = "" //ex(var, ex(var, ...
    var boundsPart : String = "" // var >= .. and var <= .. and .. and d>=0 and d<=maxT/k
    var taylorPart : String = "" //
    var parenthesisPart : String = "" //...))
    
    val varStrs : Array<String> = system.getVarStrings()
    var taylVars : MutableList<String> = mutableListOf<String>()
    //add variables with step labels to taylVars
    //(l+1) x dim variables x_00,..,x_nk
    for( step in 0..k ) {
        for( i in 0..(dim-1) ) taylVars.add( varStrs[i] + "${step}" )
    }    

    taylVars.add( "d" )
    
    //use the incomplete list of variables (with delta var d, without parameters),
    //to create the beginning of the quantified formula
    for( tv in taylVars ) existsPart += ( "ex(" + tv + "," )
        
    //add parameters (without labels) to taylVars
    for( par in system.getParStrings() ) {
        taylVars.add( par )
    }
        
    for( i in 0..(dim*(k+1)) ) parenthesisPart += ")" //variables + d

    var j : Int = 0
    //bounds for first facet
    //on x_00 .. x_n0
    var loBounds : MutableList<String> = mutableListOf<String>()
    var upBounds : MutableList<String> = mutableListOf<String>()
    for( i in 0..(dim-1) ) {
        if( i == dir1 ) {
            if( ori1 > 0 ) { //right end of rectangle
                loBounds.add("") //i
                upBounds.add( taylVars[i]+"="+system.getTres( i, state[i] + 1 ) )
            } else { //ori1 < 0, left end of rectangle
                loBounds.add( taylVars[i]+"="+system.getTres( i, state[i] ) )
                upBounds.add( "" )
            }
        } else {
            loBounds.add( taylVars[i]+">="+system.getTres( i, state[i] ) )
            upBounds.add( taylVars[i]+"<="+system.getTres( i, state[i] + 1 ) )
        }
        j += 1
    }
    //bounds inside state rectangle ... for taylor 
    //on x_01 .. x_n1 .. x_0k-1 .. x_nk-1
    for( m in 1..(k-1) ) {
        for( i in 0..(dim-1) ){
            loBounds.add( taylVars[j]+">="+system.getTres( i, state[i] ) )//j
            upBounds.add( taylVars[j]+"<="+system.getTres( i, state[i] + 1 ) )
            j += 1
        }
    }
    //bounds for second facet
    for( i in 0..(dim-1) ) {
        if( i == dir2 ) {
            if( ori2 > 0 ) { //right end of rectangle
                loBounds.add( "" ) //j
                upBounds.add( taylVars[j]+"="+system.getTres( i, state[i] + 1 ) )
            } else { //ori1 < 0, left end of rectangle
                loBounds.add( taylVars[j]+"="+system.getTres( i, state[i] ) )
                upBounds.add( "" )
            }
        } else {
            loBounds.add( taylVars[j]+">="+system.getTres( i, state[i] ) )
            upBounds.add( taylVars[j]+"<="+system.getTres( i, state[i] + 1 ) )
        }
        j += 1
    }
    //and bounds for d
    loBounds.add( "d>=0" ) //j
    upBounds.add( "d<=" + (system.getMaxT().toDouble()/k) )
    
    for( i in 0..j ){
        if( !loBounds[i].isEmpty() ) {
            boundsPart += loBounds[i] + " and "
        }
        if( !upBounds[i].isEmpty() ) {
            boundsPart += upBounds[i] + " and "
        }
    }
    
    for( step in 1..k ) {
        //and x1=(x0+d) and y1=(y0+p*d)")))))
        //taylor part ... deg 1 = Euler
        //this function is for k general
        var tayl : MutableList<String> = mutableListOf<String>() //taylor expressions per var
        var derString : String = ""
    
        //variable strings for variables from the preceding step 
        //(or initial point in facet 1)
        var mutVarsOrig : MutableList<String> = mutableListOf<String>()
        for( i in ((step-1)*dim)..(step*dim-1) ) {
            mutVarsOrig.add( taylVars[i] )
        }
        //add the strings for parameters that appear in the derivatives
        //they are placed after all the vars from all steps and after d
        for( i in ((k+1)*dim + 1)..((k+1)*dim + parCount) ) {
            mutVarsOrig.add( taylVars[i] )
        }
        //immutable array of varstrings usable by rings functions
        var varsOrig : Array<String> = mutVarsOrig.toTypedArray() //arrayOf<String>( mutVars0 ) //not taylVars.copyOfRange(0, dim)
    
        //expression x1 = x0 + ... by Taylor approx 
        for( i in 0..(dim-1) ) {
            derString = system.getDerPols()[i].toString(*varsOrig)
            tayl.add( taylVars[step*dim+i] + "=(" + taylVars[(step-1)*dim+i] + "+" + "d*(" + derString + "))" ) //ith place
        } 
    
        for( i in 0..(dim-2) ) {
            taylorPart += tayl[i] + " and " 
        }
        taylorPart += tayl[dim-1] //last for one step without "and" temporarily
        if( step != k ) {
            taylorPart += " and " //only the very last without the "and" permanently
        }
    }
    
    var facetToFacetFormula = existsPart + boundsPart + taylorPart + parenthesisPart
    
    return facetToFacetFormula
}

/*Assuming the bio system includes information about the tresholds,
 * derivatives, second derivatives, 
 * and settings for the computation like the degree of Taylor polynomials,
 * delta time for one step, maximum T for a rectangle (system dependent, 
 * at the beginning user specified), k steps
 */
fun getQuantifiedFormulaForTransition( dirori1 : Int, state : Array<Int>, dirori2 : Int, system : BioSystem, k : Int ) : String {
    val degTayl = system.getDegTayl()
    //delta?
    var formula : String = "~ unset"
       
    when( degTayl ) {
        1 -> formula = getTaylor1Formula( dirori1, state, dirori2, system, k )
        2 -> formula = getTaylor2Formula( dirori1, state, dirori2, system, k )
        else -> formula = "~ unsupported Taylor degree > 2"
    }
    
    return formula
}

//if set is empty, try calling this method with more steps...
//QEL Reduce, Taylor 
fun getParamSetForTransition( dirori1 : Int, state1 : Array<Int>, state2 : Array<Int>, system : BioSystem, steps : Int = 2 ) : SortedListOfDisjunctIntervals {
    //identify direction and orientation of facet between the states
    println("1 getParamSetForTransition called with dirori1="+dirori1+" state1"+state1.toString()+" steps="+steps)
    val dirori2 = getDirOriCode( state1, state2, system.getDim() )
    println("2 getParamSetForTransition dirori2=" + dirori2 )
    var formula = "false"
    var semiSet : String = "~"
    println("3 getParamSetForTransition formula=" + formula )
    //for k steps
    //create formula for transition by k steps of Taylor approximation    
    formula = getQuantifiedFormulaForTransition( dirori1, state1, dirori2, system, steps )
    println("4 getParamSetForTransition formula="+formula)
    //get semialgebraic set of parameters satisfying formula
    semiSet = getQelResultForFormula( formula )
    println("5 getParamSetForTransition semiset="+semiSet)
     
    //convert semialgebraic set to a sorted list of disjunct interval
    var slodi : SortedListOfDisjunctIntervals = semiToIntervals( semiSet , system.getParStr() )
    println( "6 getParamSetForTransition slodi result="+slodi.toString() )
    return slodi
}

/* 1 parameter
 * Check Existence of suitable parameters by Dreal.
 * Divide the parameter set and delete intervals where no
 * suitable valuation exists.
 * Given granularity - max times to divide, and minimal length deltaI.
 * THIS METHOD IS NOT IMPLEMENTED, DUMMY FOR UPPER IF FROM ANOTHER FILE?
 * DREAL CHECKING IS ELSEWHERE
*/
fun getParamSetForTransitionExistDREAL( dirori1 : Int, state1 : Array<Int>, state2 : Array<Int>, system : BioSystem, steps : Int = 2, deltaI : Float = 0.01f, maxDivideSteps : Int = 10 ) : SortedListOfDisjunctIntervals {
    //identify direction and orientation of facet between the states
    println("1 getParamSetForTransition called with dirori1="+dirori1+" state1"+state1.toString()+" steps="+steps)
    val dirori2 = getDirOriCode( state1, state2, system.getDim() )
    println("2 getParamSetForTransition dirori2=" + dirori2 )
    var formula = "false"
    var semiSet : String = "~"
    println("3 getParamSetForTransition formula=" + formula )
    //for k steps
    //create formula for transition by k steps of Taylor approximation    
    formula = getQuantifiedFormulaForTransition( dirori1, state1, dirori2, system, steps )
    println("4 getParamSetForTransition formula="+formula)
    //get semialgebraic set of parameters satisfying formula
    semiSet = getQelResultForFormula( formula )
    println("5 getParamSetForTransition semiset="+semiSet)
     
    //convert semialgebraic set to a sorted list of disjunct interval
    var slodi : SortedListOfDisjunctIntervals = semiToIntervals( semiSet , system.getParStr() )
    println( "6 getParamSetForTransition slodi result="+slodi.toString() )
    return slodi
}

/* Check one rectangle with given entry facet and exit facet,
 * pmin pmax admissible valuations bounds,
 * return set of intervals where valid valuations exist inside.
 */
fun encodeAndCheck( pmin: Double , pmax: Double, system : BioSystem, state1 : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, deltaI : Double = 0.01, maxDivideSteps : Int = 10 ) : String {
    var result : String = ""
    //encode
    var commands : ArrayList<String> = createCommandsForDrealRegularState1PminPmax( system, state1, dir1, ori1, dir2, ori2, pmin, pmax )
     
    //println( commands )
    
    //check
    var checkStr : String = (getResultFromDreal( commands ))[0]
    result = "["+pmin+","+checkStr+","+pmax +"]"+"\n"
    
    if( checkStr.contains("unsat") )
    {
        return result
    } else {//this interval contains valid params
        //recursively
        if( ( maxDivideSteps > 0 ) && ( (pmax - pmin) > deltaI ) ) {
            var result1 : String = ""
            var result2 : String = ""
            result1 = encodeAndCheck(pmin , (pmin+pmax)/2.0f, system, state1, dir1,ori1, dir2,ori2, deltaI, maxDivideSteps-1)
            result2 = encodeAndCheck((pmin+pmax)/2.0f, pmax, system, state1, dir1,ori1, dir2,ori2, deltaI, maxDivideSteps-1)
            return result1 + result2
        } else {
            return result
        }
    }
}

/* MAPPING [PMINi,PMAXi] TO [-1,1]
 * Find the set of valuations for 2 parameters. Return as
 * a semialgebraic set approximation.
 * 
 * Simple version + shifting the interval from pmin,pmax to min(pin),max(pin)
 * is just to sample the pmin,pmax x qmin,qmax interval,
 * (still without dividing it via algorithm.)
 * 
 * Better is to not shift...
 */
fun findSemialgebraicFor2ParamsSimpleShift(pmin: Double , pmax: Double, qmin: Double , qmax: Double, system : BioSystem, state1 : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, divideStepsP : Int, divideStepsQ : Int) : String {
    var result : String = ""
    var pin : MutableList<Double> = mutableListOf<Double>()  
    var pout : MutableList<Double> = mutableListOf<Double>()
    var qin : MutableList<Double> = mutableListOf<Double>()
    var qout : MutableList<Double> = mutableListOf<Double>()
    //sample the grid
    //collect pin,qin,pout,qout
    var plen : Double = pmax - pmin
    var qlen : Double = qmax - qmin
    var pdelta : Double = plen / divideStepsP.toDouble()
    var qdelta : Double = qlen / divideStepsQ.toDouble()
    var p : Double = 0.0
    var q : Double = 0.0
    var noParIn : Boolean = true
    var allParIn : Boolean = true
//    var pmini : Double = pmax //supposedly the wrong number, will change
//    var pmaxi : Double = pmin
//    var qmini : Double = qmax
//    var qmaxi : Double = qmin
    p = pmin
    for(i in 0..divideStepsP){
        q = qmin
        for(j in 0..divideStepsQ){
            //check p,q
            println( "Checking p="+p+", q="+q)
            var commands : ArrayList<String> = createCommandsForDrealRegularState2PQvalue( system, state1, dir1, ori1, dir2, ori2, p, q )
            println( commands )
            var pqin : Boolean = checkCommandsDreal( commands )
            //save p,q to the right list pin,pout,qin,qout
            if( pqin ){
                noParIn = false
                pin.add( p )//adds to the end of pin
                qin.add( q )//adds to the end of qin
//                if( p < pmini ){ pmini = p }
//                if( p > pmaxi ){ pmaxi = p }
//                if( q < qmini ){ qmini = q }
//                if( q > qmini ){ qmaxi = q }
            }else{
                allParIn = false
                pout.add( p )//adds to the end of pout
                qout.add( q )//adds to the end of qout
            }
            q = q + qdelta
        }
        p = p + pdelta
    }
    //what if pmini=pmaxi or qmini=qmaxi... widen a little bit the interval
//    val epsilon = 0.01
//    if( (pmaxi - pmini) < epsilon ){ 
//        pmini = pmini - epsilon 
//        pmaxi = pmaxi + epsilon
//    }
//    if( (qmaxi - qmini) < epsilon ){ 
//        qmini = qmini - epsilon 
//        qmaxi = qmaxi + epsilon
//    }
//    println( "pmini=$pmini, pmaxi=$pmaxi, qmini=$qmini, qmaxi=$qmaxi" )
    
    if( noParIn ){
        result = "false"
        println("pin,qin=empty")
    }else if( allParIn ){
        println("qin=all="+qin)
        println("pin=all="+pin)
        result = "(p >= "+pmin+") and (p <= "+pmax+") and (q >= "+qmin+") and (q <= "+qmax+")"
    }else{
        println("qin=nontrivial="+qin)
        println("pin=nontrivial="+pin)
        
        //call the maxima lp method to find the approximating expression
        var command : ArrayList<String> = createCommandsForMaximaLPTaskPar2Deg2(pmin, pmax, qmin, qmax, pin, qin, pout, qout, "" )
        var maximaResult : List<String> = getResultFromMaxima( command )
        var maximaResultLine : String = digResultLinesFromMaxima( maximaResult )
        //if the problem was unbounded, try with other options
        if( maximaResultLine.contains( "not bounded" ) ){
            command = createCommandsForMaximaLPTaskPar2Deg2(pmin, pmax, qmin, qmax, pin, qin, pout, qout, ", all" )
            maximaResult = getResultFromMaxima( command )
            maximaResultLine = digResultLinesFromMaxima( maximaResult )
        }
        //hope the nonnegative procedure was successfull and the problem bounded...        
        var cm : MutableMap<String,String> = getCoefMapFromResultLine( maximaResultLine )
        
        val A1 : Double = 2.0/(pmax - pmin)
        println( "A1=$A1" )
        val B1 : Double = (pmax + pmin)/ (pmax - pmin)
        println( "B1=$B1" )
        val A2 : Double = 2.0/(qmax - qmin)
        println( "A2=$A2" )
        val B2 : Double = (qmax + qmin) / (qmax - qmin) 
        println( "B2=$B2" )
        //compute the p o alpha expression
        // (p,q) -alpha-> (A1*p-B1, A2*q-B2) =: (p',q')
        // p o alpha(p',q') := (1,p',q') (a b c) (1 )
        //                               (d e f) (p')
        //                               (g h i) (q')
        // p o alpha(p',q') = a+(b+d)*p'+(c+g)*q'+(f+h)*p'q'+(e)*p'^2+(i)*q'^2
        // p o alpha(p,q) = ... substitution (is a little simpler, tbd)
        
        val sqrt3 : String = "1.73205080756888"
        var constant : String = "0.25*("+cm["a"]+")" //readMap(cm,"a")
        var linearP : String = "0.25*"+sqrt3+"*("+cm["b"]+"+"+cm["d"]+")*("+A1+"*p-"+B1+")"
        var linearQ : String = "0.25*"+sqrt3+"*("+cm["c"]+"+"+cm["g"]+")*("+A2+"*q-"+B2+")"
        var deg2PQ : String = "0.75*("+cm["f"]+"+"+cm["h"]+")*("+A1+"*p-"+B1+")*("+A2+"*q-"+B2+")"
        var quadraticP : String = "0.75*("+cm["e"]+")*("+A1+"*p-"+B1+")*("+A1+"*p-"+B1+")"
        var quadraticQ : String = "0.75*("+cm["i"]+")*("+A2+"*q-"+B2+")*("+A2+"*q-"+B2+")"
        var poalphaPQ : String = quadraticQ+"+"+quadraticP+"+"+deg2PQ+"+"+linearP+"+"+linearQ+"+"+constant
    
        //NOW SIMPLIFY THE poalpha STRING BY REDUCE
        println( "first poalpha= $poalphaPQ" )
        poalphaPQ = parseSimplifiedPolynomialExpressionResultFromReduceOutput(simplifyAPolynomialExpression( poalphaPQ ) )
    
        //create inequalities
        //  [pmin, pmax] x [qmin, qmax]
        var pIneq : String = "(p >= "+pmin+") and (p <= "+pmax+")"
        var qIneq : String = "(q >= "+qmin+") and (q <= "+qmax+")"
        //  p o alpha (p,q) >= 1
        var poalphaIneq : String = "(("+ poalphaPQ +") >= 1)"
        result = pIneq + " and " + qIneq + " and " + poalphaIneq
    }
    //output string
    return result
}

/* FUNCTION THAT JUST TRIES TO APPROXIMATE THE POINTS BY MAPPING
 * [PMIN,PMAX] TO [-1,1]
 * Find the set of valuations for 2 parameters. Return as
 * a semialgebraic set approximation.
 * 
 * Simple version is just to sample the pmin,pmax x qmin,qmax interval,
 * without dividing it via algorithm.
 */

fun findSemialgebraicFor2ParamsSimple(pmin: Double , pmax: Double, qmin: Double , qmax: Double, system : BioSystem, state1 : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, divideStepsP : Int, divideStepsQ : Int) : String {
    var result : String = ""
    var pin : MutableList<Double> = mutableListOf<Double>()  
    var pout : MutableList<Double> = mutableListOf<Double>()
    var qin : MutableList<Double> = mutableListOf<Double>()
    var qout : MutableList<Double> = mutableListOf<Double>()
    //sample the grid
    //collect pin,qin,pout,qout
    var plen : Double = pmax - pmin
    var qlen : Double = qmax - qmin
    var pdelta : Double = plen / divideStepsP.toDouble()
    var qdelta : Double = qlen / divideStepsQ.toDouble()
    var p : Double = 0.0
    var q : Double = 0.0
    var noParIn : Boolean = true
    var allParIn : Boolean = true
    p = pmin
    for(i in 0..divideStepsP){
        q = qmin
        for(j in 0..divideStepsQ){
            //check p,q
            println( "Checking p="+p+", q="+q)
            var commands : ArrayList<String> = createCommandsForDrealRegularState2PQvalue( system, state1, dir1, ori1, dir2, ori2, p, q )
            println( commands )
            var pqin : Boolean = checkCommandsDreal( commands )
            //save p,q to the right list pin,pout,qin,qout
            if( pqin ){
                noParIn = false
                pin.add( p )//adds to the end of pin
                qin.add( q )//adds to the end of qin
            }else{
                allParIn = false
                pout.add( p )//adds to the end of pout
                qout.add( q )//adds to the end of qout
            }
            q = q + qdelta
        }
        p = p + pdelta
    }
    if( noParIn ){
        result = "false"
        println("pin,qin=empty")
    }else if( allParIn ){
        println("qin=all="+qin)
        println("pin=all="+pin)
        result = "(p >= "+pmin+") and (p <= "+pmax+") and (q >= "+qmin+") and (q <= "+qmax+")"
    }else{
        println("qin=nontrivial="+qin)
        println("pin=nontrivial="+pin)
        
        //call the maxima lp method to find the approximating expression
        var command : ArrayList<String> = createCommandsForMaximaLPTaskPar2Deg2(pmin, pmax, qmin, qmax, pin, pout, qin, qout, "" )
        var maximaResult : List<String> = getResultFromMaxima( command )
        var maximaResultLine : String = digResultLinesFromMaxima( maximaResult )
        //if the problem was unbounded, try with other options
        if( maximaResultLine.contains( "not bounded" ) ){
            command = createCommandsForMaximaLPTaskPar2Deg2(pmin, pmax, qmin, qmax, pin, pout, qin, qout, ", all" )
            maximaResult = getResultFromMaxima( command )
            maximaResultLine = digResultLinesFromMaxima( maximaResult )
        }
        //hope the nonnegative procedure was successfull and the problem bounded...        
        var cm : MutableMap<String,String> = getCoefMapFromResultLine( maximaResultLine )
        val A1 : Double = 2.0/(pmax - pmin)
        println( "A1=$A1" )
        val B1 : Double = -(pmax + pmin)/ (pmax - pmin)
        println( "B1=$B1" )
        val A2 : Double = 2.0/(qmax - qmin)
        println( "A2=$A2" )
        val B2 : Double = -(qmax + qmin) / (qmax - qmin) 
        println( "B2=$B2" )
        //compute the p o alpha expression
        // (p,q) -alpha-> (A1*p+B1, A2*q+B2) =: (p',q')
        // p o alpha(p',q') := (1,p',q') (a b c) (1 )
        //                               (d e f) (p')
        //                               (g h i) (q')
        // p o alpha(p',q') = a+(b+d)*p'+(c+g)*q'+(f+h)*p'q'+(e)*p'^2+(i)*q'^2
        // p o alpha(p,q) = ... substitution (is a little simpler, tbd)
        
        val sqrt3 : String = "1.73205080756888"
        var constant : String = "0.25*("+cm["a"]+")" //readMap(cm,"a")
        var linearP : String = "0.25*"+sqrt3+"*("+cm["b"]+"+"+cm["d"]+")*("+A1+"*p+"+B1+")"
        var linearQ : String = "0.25*"+sqrt3+"*("+cm["c"]+"+"+cm["g"]+")*("+A2+"*q+"+B2+")"
        var deg2PQ : String = "3*0.25*("+cm["f"]+"+"+cm["h"]+")*("+A1+"*p+"+B1+")*("+A2+"*q+"+B2+")"
        var quadraticP : String = "3*0.25*("+cm["f"]+"+"+cm["h"]+")*("+A1+"*p+"+B1+")*("+A1+"*p+"+B1+")"
        var quadraticQ : String = "3*0.25*("+cm["f"]+"+"+cm["h"]+")*("+A2+"*q+"+B2+")*("+A2+"*q+"+B2+")"
        var poalphaPQ : String = quadraticQ+"+"+quadraticP+"+"+deg2PQ+"+"+linearP+"+"+linearQ+"+"+constant
    
        //NOW SIMPLIFY THE poalpha STRING BY REDUCE
        println( "first poalpha= $poalphaPQ" )
        poalphaPQ = parseSimplifiedPolynomialExpressionResultFromReduceOutput(simplifyAPolynomialExpression( poalphaPQ ) )
    
        //create inequalities
        //  [pmin, pmax] x [qmin, qmax]
        var pIneq : String = "(p >= "+pmin+") and (p <= "+pmax+")"
        var qIneq : String = "(q >= "+qmin+") and (q <= "+qmax+")"
        //  p o alpha (p,q) >= 1
        var poalphaIneq : String = "(("+ poalphaPQ +") >= 1)"
        result = pIneq + " and " + qIneq + " and " + poalphaIneq
    }
    //output string
    return result
}

/* If the entry is not there, return ""
 */
fun readMap(map: Map<String,String>, entry:String): String {
    var result : String = ""
    var tmp : String? = map[entry]
    tmp?.let {
        result = tmp
    }
    return result
}

/* TODO
 * Find the set of valuations for 3 parameters. Return as
 * a semialgebraic set approximation.
 */

//fun findSemialgebraicFor3Params() {
    
//}

/* TODO
 * Find the set of valuations for m parameters. Return as
 * a semialgebraic set approximation.
 */
 
//here  
//DREAL REASONING
/* Debug - the right S.L.O.D.I.
 * Find the set of valuations for one parameter. Return as
 * a sorted list of disjunct intervals.
 */
fun findSlodiFor1ParamDouble( pmin: Double , pmax: Double, system : BioSystem, state1 : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, deltaI : Double = 0.01, maxDivideSteps : Int = 10 ) : SortedListOfDisjunctIntervalsDouble {
    var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    //encode
    var commands : ArrayList<String> = createCommandsForDrealRegularState1PminPmax( system, state1, dir1, ori1, dir2, ori2, pmin, pmax )
    
    println( commands )
    
    //check
    var checkStr : String = (getResultFromDreal( commands ))[0]
        
    if( checkStr.contains("unsat") )
    {
        println("unsat appeared")
        result = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )//no valid valuation in [pmin,pmax] ... empty list
        return result
    } else {//this interval contains valid params
        //recursively
        if( ( maxDivideSteps > 0 ) && ( (pmax - pmin) > deltaI ) ) {
            var result1 : SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble(pmin , (pmin+pmax)/2.0, system, state1, dir1,ori1, dir2,ori2, deltaI, maxDivideSteps-1)
            var result2 : SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble((pmin+pmax)/2.0, pmax, system, state1, dir1,ori1, dir2,ori2, deltaI, maxDivideSteps-1)
            println("result1="+result1.toString())
            println("result2="+result2.toString())
            result = result1
            result.uniteWithOtherDisjunctList( result2 )
            println( "union="+result )
            return result
        } else {
            result.add( IntervalDouble( pmin, true, false, pmax, true, false ) )
            println(result.toString())
    
            return result
        }
    }
}

/*
 * Sample the input interval of parameters, check by Dreal, how many
 * valuations enable the transition. If the diversity of the sampling
 * is too high (a given number between 1 and 2), we divide the interval
 * and compute for smaller intervals.
 * Arguments: diversity cutoff div, max steps to divide,  and minimal length deltaI.
*/

/* Sample the pmin pmax interval, check delta-sat/unsat by dreal
 * find points of sign change, 
 * second level of sampling and binary search for
 * borders, maintain several lists
 * (- unsat list of intervals)
 * - delta-sat samples in a row forms delta-sat intervals
 * - left or right parts of the changing intervals
 * unite the above 2 types of intervals and return the result.
 * TOTO VOLAME Z REACHABILITY VELKE
 */
fun get1ParamSetForTransitionSampleDREAL( parInterval : IntervalDouble, biosystem : BioSystem, state : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, delta1 : Double /* delta1 must be > delta2 */, delta2 : Double = 0.01 ) : SortedListOfDisjunctIntervalsDouble {
    val drealVerbosity = 0
    var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    var sampleList : MutableList<Double> = mutableListOf<Double>()
    var drealResultsList : MutableList<Boolean> = mutableListOf<Boolean>()
    var pmin : Double = parInterval.getLe()
    var pmax : Double = parInterval.getRi()
    var lclo : Boolean = parInterval.isLClo()
    var rclo : Boolean = parInterval.isRClo()
    
    //sample the interval
    var sample : Double = pmin
    if( !lclo ) sample += delta1/2.0
    while( sample < pmax ){
        sampleList.add( sample )
        sample += delta1
    }
    if( rclo ) sampleList.add( pmax )
    //quick fix if the samples aren't any...
    if( sampleList.size == 0){
        sample = (pmin+pmax)/2.0
        sampleList.add( sample )
    }
    //check points by dreal
    var commands : ArrayList<String> = arrayListOf<String>()
    var checkStr : String = ""
    for( s in sampleList ){
        commands = createCommandsForDrealRegularState1Pvalue( biosystem, state, dir1, ori1, dir2, ori2, s )
        if( drealVerbosity > 1 ) println( commands )
        checkStr = (getResultFromDreal( commands ))[0]
        if( drealVerbosity > 1 ) println( checkStr )
        if( checkStr.contains("unsat") ){
            drealResultsList.add( false )
        }else{
            drealResultsList.add( true )
        }
    }
    if( drealVerbosity > 0 ) println( sampleList )
    if( drealVerbosity > 0 ) println( drealResultsList )
    //check (or not) the supposedly unsat intervals
    //var changeIntervals : MutableListOf<IntervalDouble> = mutableListOf<IntervalDouble>()
    var deltaSatIntervals : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    var beginChangeFtoTList : MutableList<Int> = mutableListOf<Int>()
    var beginChangeTtoFList : MutableList<Int> = mutableListOf<Int>()
    
    //i=0
    var lastTrue : Boolean = false
    var thisTrue : Boolean = drealResultsList[0]
    var left : Double = pmin
    var right : Double = pmin
//    println("i=0: lastTrue=$lastTrue, thisTrue=$thisTrue, left=$left, right=$right")
    //i=1...
    for( i in 1..(sampleList.size-1) ){
        lastTrue = thisTrue
        thisTrue = drealResultsList[i]
        //truth values set right for this iteration
        if( thisTrue ){
            right = sampleList[i]
        }
        //right set right if thisTrue
        if( lastTrue != thisTrue ){
            if( lastTrue ){//T to F
                 beginChangeTtoFList.add(i-1)
                 right = sampleList[i-1]
                 //right set right if not thisTrue, but lastTrue
            }else{//thisTrue //F to T
                 beginChangeFtoTList.add(i-1)
                 left = sampleList[i]
                 //the only instance when left is set anew, thisTrue, not lastTrue
            }
        }
        if( thisTrue || lastTrue){
            //update truth intervals whenever inside/or just past, 
            //the borders may change eventualy for future for iterations
            deltaSatIntervals.add( IntervalDouble(left, true, false, right, true, false ) )
//            println( "deltaSatIntervals"+deltaSatIntervals.toString() )
        }
//        println("i=$i: lastTrue=$lastTrue, thisTrue=$thisTrue, left=$left, right=$right")        
    }
    
//    println("deltaSatIntervals : "+ deltaSatIntervals.toString() )
//    println( "F to T:"+ beginChangeFtoTList )
//    println( "T to F:"+ beginChangeTtoFList )
    //delta sat intervals are in the list
    for( inter in deltaSatIntervals.getIntervals() ){
        result.add( inter )
    }
    //BINARY SEARCH for the right point to separate intervals
    //assume there is only one change in one change interval
    var left1 : Double = pmin //dummy
    var right1 : Double = pmax //dummy
    for( j in beginChangeFtoTList ){
        //test middle of the interval 
        left1 = sampleList[j]
        right1 = sampleList[j+1]
        var left = left1
        var right = right1
        var middle :  Double = 0.0//dummy
        var sat : Boolean = false
        while( ( right - left ) > delta2 ){
            middle = (left + right)/2.0
            commands = createCommandsForDrealRegularState1Pvalue( biosystem, state, dir1, ori1, dir2, ori2, middle )
            checkStr = (getResultFromDreal( commands ))[0]
            if( checkStr.contains("unsat") ){
                left = middle
            }else{
                right = middle
            }
            //^set left, right of interval where to search next
        }
        //right - left < delta2
        //left F, right T
        //add the delta sat part to the result
        result.add( IntervalDouble( left, false, false, right1, true, false ) )
    }
    for( j in beginChangeTtoFList ){
        //test middle of the interval 
        left1 = sampleList[j]
        right1 = sampleList[j+1]
        var left = left1
        var right = right1
        var middle :  Double = 0.0//dummy
        var sat : Boolean = false
        while( ( right - left ) > delta2 ){
            middle = (left + right)/2.0
            commands = createCommandsForDrealRegularState1Pvalue( biosystem, state, dir1, ori1, dir2, ori2, middle )
            checkStr = (getResultFromDreal( commands ))[0]
            if( checkStr.contains("unsat") ){
                right = middle
            }else{
                left = middle
            }
            //^set left, right of interval where to search next
        }
        //right - left < delta2
        //left T, right F
        //add the delta sat part to the result
        result.add( IntervalDouble( left1, true, false, right, false, false ) )
    }
    if( drealVerbosity > 0 ) println(result)
    return result
}
 
 
/* Sample the pmin pmax interval, check delta-sat/unsat by dreal
 * find points of sign change, 
 * second level of sampling and binary search for
 * borders, maintain several lists
 * (- unsat list of intervals)
 * - delta-sat samples in a row forms delta-sat intervals
 * - left or right parts of the changing intervals
 * unite the above 2 types of intervals and return the result.
 */
fun get1ParamSetForTransitionSampleDREAL(pmin : Double, pmax : Double, biosystem : BioSystem, state : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, delta1 : Double /* delta1 must be > delta2 */, delta2 : Double = 0.01 ) : SortedListOfDisjunctIntervalsDouble {
    val drealVerbosity = 0
    var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    var sampleList : MutableList<Double> = mutableListOf<Double>()
    var drealResultsList : MutableList<Boolean> = mutableListOf<Boolean>()
    
    //sample the interval
    var sample : Double = pmin
    while( sample < pmax ){
        sampleList.add( sample )
        sample += delta1
    }
    sampleList.add( pmax )
    //check points by dreal
    var commands : ArrayList<String> = arrayListOf<String>()
    var checkStr : String = ""
    for( s in sampleList ){
        commands = createCommandsForDrealRegularState1Pvalue( biosystem, state, dir1, ori1, dir2, ori2, s )
        if( drealVerbosity > 1 ) println( commands )
        checkStr = (getResultFromDreal( commands ))[0]
        if( drealVerbosity > 1 ) println( checkStr )
        if( checkStr.contains("unsat") ){
            drealResultsList.add( false )
        }else{
            drealResultsList.add( true )
        }
    }
    if( drealVerbosity > 0 ) println( sampleList )
    if( drealVerbosity > 0 ) println( drealResultsList )
    //check (or not) the supposedly unsat intervals
    //var changeIntervals : MutableListOf<IntervalDouble> = mutableListOf<IntervalDouble>()
    var deltaSatIntervals : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    var beginChangeFtoTList : MutableList<Int> = mutableListOf<Int>()
    var beginChangeTtoFList : MutableList<Int> = mutableListOf<Int>()
    
    //i=0
    var lastTrue : Boolean = false
    var thisTrue : Boolean = drealResultsList[0]
    var left : Double = pmin
    var right : Double = pmin
//    println("i=0: lastTrue=$lastTrue, thisTrue=$thisTrue, left=$left, right=$right")
    //i=1...
    for( i in 1..(sampleList.size-1) ){
        lastTrue = thisTrue
        thisTrue = drealResultsList[i]
        //truth values set right for this iteration
        if( thisTrue ){
            right = sampleList[i]
        }
        //right set right if thisTrue
        if( lastTrue != thisTrue ){
            if( lastTrue ){//T to F
                 beginChangeTtoFList.add(i-1)
                 right = sampleList[i-1]
                 //right set right if not thisTrue, but lastTrue
            }else{//thisTrue //F to T
                 beginChangeFtoTList.add(i-1)
                 left = sampleList[i]
                 //the only instance when left is set anew, thisTrue, not lastTrue
            }
        }
        if( thisTrue || lastTrue){
            //update truth intervals whenever inside/or just past, 
            //the borders may change eventualy for future for iterations
            deltaSatIntervals.add( IntervalDouble(left, true, false, right, true, false ) )
//            println( "deltaSatIntervals"+deltaSatIntervals.toString() )
        }
//        println("i=$i: lastTrue=$lastTrue, thisTrue=$thisTrue, left=$left, right=$right")        
    }
    
//    println("deltaSatIntervals : "+ deltaSatIntervals.toString() )
//    println( "F to T:"+ beginChangeFtoTList )
//    println( "T to F:"+ beginChangeTtoFList )
    //delta sat intervals are in the list
    for( inter in deltaSatIntervals.getIntervals() ){
        result.add( inter )
    }
    //BINARY SEARCH for the right point to separate intervals
    //assume there is only one change in one change interval
    var left1 : Double = pmin //dummy
    var right1 : Double = pmax //dummy
    for( j in beginChangeFtoTList ){
        //test middle of the interval 
        left1 = sampleList[j]
        right1 = sampleList[j+1]
        var left = left1
        var right = right1
        var middle :  Double = 0.0//dummy
        var sat : Boolean = false
        while( ( right - left ) > delta2 ){
            middle = (left + right)/2.0
            commands = createCommandsForDrealRegularState1Pvalue( biosystem, state, dir1, ori1, dir2, ori2, middle )
            checkStr = (getResultFromDreal( commands ))[0]
            if( checkStr.contains("unsat") ){
                left = middle
            }else{
                right = middle
            }
            //^set left, right of interval where to search next
        }
        //right - left < delta2
        //left F, right T
        //add the delta sat part to the result
        result.add( IntervalDouble( left, false, false, right1, true, false ) )
    }
    for( j in beginChangeTtoFList ){
        //test middle of the interval 
        left1 = sampleList[j]
        right1 = sampleList[j+1]
        var left = left1
        var right = right1
        var middle :  Double = 0.0//dummy
        var sat : Boolean = false
        while( ( right - left ) > delta2 ){
            middle = (left + right)/2.0
            commands = createCommandsForDrealRegularState1Pvalue( biosystem, state, dir1, ori1, dir2, ori2, middle )
            checkStr = (getResultFromDreal( commands ))[0]
            if( checkStr.contains("unsat") ){
                right = middle
            }else{
                left = middle
            }
            //^set left, right of interval where to search next
        }
        //right - left < delta2
        //left T, right F
        //add the delta sat part to the result
        result.add( IntervalDouble( left1, true, false, right, false, false ) )
    }
    if( drealVerbosity > 0 ) println(result)
    return result
}

fun getParamSetForSequenceOfRectangles( dirori : Int, states : Array<Array<Int>>, system : BioSystem, steps : Int, method : String ) : SortedListOfDisjunctIntervals {
    var resultList = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
        
    var first : Array<Int> = states[0]
    var diroriCurrent : Int = dirori
    for( i in 1..(states.size-1) ){
        if( method == "QEL-Reduce" ){
            resultList.uniteWithOtherDisjunctList( getParamSetForTransition( diroriCurrent, first, states[i], system, steps ) )
        }
        if( method == "Existence-Dreal" ){
            resultList.uniteWithOtherDisjunctList( getParamSetForTransitionExistDREAL( diroriCurrent, first, states[i], system, steps ) )
        }
       /* if( method == "Sampling-Dreal" ){
            resultList.uniteWithOtherDisjunctList( getParamSetForTransitionSampleDREAL( diroriCurrent, first, states[i], system, steps ) )
        }*/
        
        diroriCurrent = getDirOriCode( first, states[i], system.getDim() )
        first = states[i]
    }    
    
    return resultList
}

/* function original without method
fun getParamSetForSequenceOfRectangles( dirori : Int, states : Array<Array<Int>>, system : BioSystem, steps : Int ) : SortedListOfDisjunctIntervals {
    var resultList = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
        
    var first : Array<Int> = states[0]
    var diroriCurrent : Int = dirori
    for( i in 1..(states.size-1) ){
        resultList.uniteWithOtherDisjunctList( getParamSetForTransition( diroriCurrent, first, states[i], system, steps ) )
        
        diroriCurrent = getDirOriCode( first, states[i], system.getDim() )
        first = states[i]
    }    
    
    return resultList
}
*/

fun getUpperNeighbourOn( variable : Int, state : Array<Int> ) : Array<Int> {
    val mutableArray = state.toMutableList()
    mutableArray[variable]++
    return mutableArray.toTypedArray()
}

fun getLowerNeighbourOn( variable : Int, state : Array<Int> ) : Array<Int> {
    val mutableArray = state.toMutableList()
    mutableArray[variable]--
    return mutableArray.toTypedArray()
}


fun getListOfSuccessorsAndParameterSets( state : Array<Int>, entryVar : Int, entryDir : Int, pmin : Double, pmax : Double, biosystem : BioSystem, delta1 : Double, delta2 : Double ) : List< Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble > > {
    var result : MutableList< Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble > > = mutableListOf<Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble >> ()
    
    //take all the possible 2n facets (staying forever is not usable for reachability)
    //determine the successors with nonempty set of parameters
    //put them into the list of results
    val n : Int = biosystem.getDim()
    for( vari in 0..(n-1) ){
        //upper neighbour if exists
        if( state[vari] < (biosystem.getTresCount(vari)-1) ){
            var slodi1 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( pmin, pmax, biosystem, state, entryVar, entryDir, vari, 1, delta1, delta2 )
            if( slodi1.isNonempty() ){
                result.add( Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble >(getUpperNeighbourOn( vari, state ), slodi1) )
            }
        }
        //lower neighbour if exists
        if( state[vari] > 0 ){
            var slodi2 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( pmin, pmax, biosystem, state, entryVar, entryDir, vari, -1, delta1, delta2 )
            if( slodi2.isNonempty() ){
                result.add( Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble >(getLowerNeighbourOn( vari, state ), slodi2) )
            }
        }
    }
    
    return result
}


/* version combined with the QDA abstraction (calling Python a alternative to
 * a very long computation of a state by DREAL
 * version with FUTURES, despite the prototype functions this does not
 */
fun getListOfSuccessorsAndParameterSetsCOMBINEDfutures( stateAndPar : QueueItem, biosystem : BioSystem, delta1 : Double, delta2 : Double ) : List< QueueItem > {
    var result : MutableList< QueueItem > = mutableListOf<QueueItem> ()
    
    //take all the possible 2n facets (staying forever is not usable for reachability)
    //determine the successors with nonempty set of parameters
    //put them into the list of results
    val n : Int = biosystem.getDim()
    var state : Array<Int> = stateAndPar.getR()
    var entryDir : Int = stateAndPar.getDir()
    var entryOr : Int = stateAndPar.getOr()
    var slodi : SortedListOfDisjunctIntervalsDouble = stateAndPar.getSlodi()
    
    for( inter in slodi.getIntervals() ){
        /*HERE starts the part that is in this combined version threaded
         * and if the thread takes too long, it would be stopped and 
         * replaced by QDA Python script */
         
        //atomic int var for checking which was first (FA 1, QDA 2)
        //two resultsets FA and QDA
        val resultFA = mutableListOf<QueueItem>()
        val resultQDA = mutableListOf<QueueItem>()
        val firstComplete = AtomicInteger( 0 )
        
        val future = CompletableFuture.anyOf(
            CompletableFuture.supplyAsync( Supplier {
                for( vari in 0..(n-1) ){
                    println("FA: vari=$vari")
                    //upper neighbour if exists [i,i+1]->[i+1,i+2] all have to be tresholds
                    if( state[vari] < (biosystem.getTresCount(vari)-2) ){
                        var slodi1 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( inter, biosystem, state, entryDir, entryOr, vari, 1, delta1, delta2 )
                        if( slodi1.isNonempty() ){
                            resultFA.add( QueueItem( getUpperNeighbourOn( vari, state ), vari, -1, slodi1 ) )
                        }
                    }
                    //lower neighbour if exists
                    if( state[vari] > 0 ){
                        var slodi2 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( inter, biosystem, state, entryDir, entryOr, vari, -1, delta1, delta2 )
                        if( slodi2.isNonempty() ){
                            resultFA.add( QueueItem( getLowerNeighbourOn( vari, state ), vari, 1, slodi2 ) )
                        }
                    }
                }
                println("FA: complete")
                firstComplete.set( 1 )
            } ),
            CompletableFuture.supplyAsync( Supplier {
                println("QDA: start")
                //call the Python script that computes QDA 
                //successors and par sets list
                val biosystemStr = biosystem.getName()
                val rStr = rectangleHash( state )
                val eDirStr = entryDir.toString()
                var eOriStr : String = ""
                if( entryOr == 1) { 
                    eOriStr = "1" //entryOr.toString()
                }else{ //entryOr ==-1, we want "0" for Python
                    eOriStr = "0"
                }
                val pminStr = inter.getLe().toString()
                val pmaxStr = inter.getRi().toString()
                val deltaStr = delta1.toString()
                val maxTstr = biosystem.getMaxT()
                val resultQDA = getResultFor1paramFromPython( biosystemStr, rStr, eDirStr, eOriStr, pminStr, pmaxStr, deltaStr, maxTstr )
                println("QDA: complete")
                firstComplete.set( 2 )
            } ) 
        ).thenApply{
            //depending on the value of the atomic var, assign the result 
            //that has been computed completely
            if( firstComplete.get() == 1 ){
                println("F")
                result = resultFA
            } else if (firstComplete.get() == 2){
                println("Q")
                result = resultQDA
            } else {
                println("N")
            }
        }
        /*HERE ends the replaced code for the combined version*/
        //assign the completed resultset to result
    }
    
    return result
}

/* version combined with the QDA abstraction (calling Python a alternative to
 * a very long computation of a state by DREAL
 * a version using Executors and timeout
 */
fun getListOfSuccessorsAndParameterSetsCOMBINEDtimeout( stateAndPar : QueueItem, biosystem : BioSystem, delta1 : Double, delta2 : Double ) : List< QueueItem > {
    var result : MutableList< QueueItem > = mutableListOf<QueueItem> ()
    
    //take all the possible 2n facets (staying forever is not usable for reachability)
    //determine the successors with nonempty set of parameters
    //put them into the list of results
    val n : Int = biosystem.getDim()
    var state : Array<Int> = stateAndPar.getR()
    var entryDir : Int = stateAndPar.getDir()
    var entryOr : Int = stateAndPar.getOr()
    var slodi : SortedListOfDisjunctIntervalsDouble = stateAndPar.getSlodi()
    
    for( inter in slodi.getIntervals() ){
        /*HERE starts the part that is in this combined version threaded
         * and if the thread takes too long, it would be stopped and 
         * replaced by QDA Python script */
         
        //atomic int var for checking which was first (FA 1, QDA 2)
        //two resultsets FA and QDA
        val resultFA = mutableListOf<QueueItem>()
        var resultQDA = mutableListOf<QueueItem>()
        val firstComplete = AtomicInteger( 0 )
        
        val executor = Executors.newFixedThreadPool(1)
        
        val future = CompletableFuture.supplyAsync( Supplier {
                for( vari in 0..(n-1) ){
                    //println("FA: vari=$vari")
                    //upper neighbour if exists [i,i+1]->[i+1,i+2] all have to be tresholds
                    if( state[vari] < (biosystem.getTresCount(vari)-2) ){
                        var slodi1 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( inter, biosystem, state, entryDir, entryOr, vari, 1, delta1, delta2 )
                        if( slodi1.isNonempty() ){
                            resultFA.add( QueueItem( getUpperNeighbourOn( vari, state ), vari, -1, slodi1 ) )
                        }
                    }
                    //lower neighbour if exists
                    if( state[vari] > 0 ){
                        var slodi2 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( inter, biosystem, state, entryDir, entryOr, vari, -1, delta1, delta2 )
                        if( slodi2.isNonempty() ){
                            resultFA.add( QueueItem( getLowerNeighbourOn( vari, state ), vari, 1, slodi2 ) )
                        }
                    }
                }
                //Thread.sleep(5*60*1000)//5 minutes sleep, not now
                //println("FA: complete")
                if( firstComplete.get() == 0 ){ firstComplete.set( 1 ) }
            }, executor )
            /*future.thenApply{
                //depending on the value of the atomic var, assign the result 
                //that has been computed completely
                if( firstComplete.get() == 1 ){
                    println("F")
                    result.addAll( resultFA )
                } 
            }*/
            executor.shutdown()
            executor.awaitTermination(60, TimeUnit.SECONDS )
            if( firstComplete.get() == 1 ){
                    println("F")
                    result.addAll( resultFA )
                } else {
                    println("Q")
                    //println("QDA: start")
                    //call the Python script that computes QDA 
                    //successors and par sets list
                    val biosystemStr = biosystem.getName()
                    val rStr = rectangleHash( state )
                    val eDirStr = entryDir.toString()
                    var eOriStr : String = ""
                    if( entryOr == 1) { 
                        eOriStr = "1" //entryOr.toString()
                    }else{ //entryOr ==-1, we want "0" for Python
                        eOriStr = "0"
                    }
                    val pminStr = inter.getLe().toString()
                    val pmaxStr = inter.getRi().toString()
                    val deltaStr = delta1.toString()
                    val maxTstr = biosystem.getMaxT()
                    resultQDA = getResultFor1paramFromPython( biosystemStr, rStr, eDirStr, eOriStr, pminStr, pmaxStr, deltaStr, maxTstr )
                    //println("QDA: complete")
                    
                    result.addAll( resultQDA )
                }
        }
        /*HERE ends the replaced code for the combined version*/
        //assign the completed resultset to result
    
    return result
}

/* version only with the QDA abstraction (calling Python)
 */
fun getListOfSuccessorsAndParameterSetsQDA( stateAndPar : QueueItem, biosystem : BioSystem, delta1 : Double, delta2 : Double ) : List< QueueItem > {
    var result : MutableList< QueueItem > = mutableListOf<QueueItem> ()
    
    //take all the possible 2n facets (staying forever is not usable for reachability)
    //determine the successors with nonempty set of parameters
    //put them into the list of results
    val n : Int = biosystem.getDim()
    var state : Array<Int> = stateAndPar.getR()
    var entryDir : Int = stateAndPar.getDir()
    var entryOr : Int = stateAndPar.getOr()
    var slodi : SortedListOfDisjunctIntervalsDouble = stateAndPar.getSlodi()
    
    for( inter in slodi.getIntervals() ){
        /*HERE starts the part that is in the combined version threaded
         * replaced by QDA Python script */
         
        println("QDA: start")
        //call the Python script that computes QDA 
        //successors and par sets list
        val biosystemStr = biosystem.getName()
        val rStr = rectangleHash( state )
        val eDirStr = entryDir.toString()
        var eOriStr : String = ""
        if( entryOr == 1) { 
            eOriStr = "1" //entryOr.toString()
        }else{ //entryOr ==-1, we want "0" for Python
            eOriStr = "0"
        }
        val pminStr = inter.getLe().toString()
        val pmaxStr = inter.getRi().toString()
        val deltaStr = delta1.toString()
        val maxTstr = biosystem.getMaxT()
        val resultQDA = getResultFor1paramFromPython( biosystemStr, rStr, eDirStr, eOriStr, pminStr, pmaxStr, deltaStr, maxTstr )
        println("QDA: complete")
        result = resultQDA
        /*HERE ends the replaced code for the QDA version*/
        //assign the completed resultset to result
    }
    
    return result
}

fun getListOfSuccessorsAndParameterSets( stateAndPar : QueueItem, biosystem : BioSystem, 
        delta1 : Double, delta2 : Double ) : List< QueueItem > {
    var result : MutableList< QueueItem > = mutableListOf<QueueItem> ()
    
    //take all the possible 2n facets (staying forever is not usable for reachability)
    //determine the successors with nonempty set of parameters
    //put them into the list of results
    val n : Int = biosystem.getDim()
    var state : Array<Int> = stateAndPar.getR()
    var entryDir : Int = stateAndPar.getDir()
    var entryOr : Int = stateAndPar.getOr()
    var slodi : SortedListOfDisjunctIntervalsDouble = stateAndPar.getSlodi()
    
    for( inter in slodi.getIntervals() ){
        /*HERE starts the part that will be threaded in the COMBINED version 
         * of this method */
             for( vari in 0..(n-1) ){
                //upper neighbour if exists [i,i+1]->[i+1,i+2] all have to be tresholds
                if( state[vari] < (biosystem.getTresCount(vari)-2) ){
                    var slodi1 : SortedListOfDisjunctIntervalsDouble =       get1ParamSetForTransitionSampleDREAL( inter, biosystem, state, entryDir, entryOr, vari, 1, delta1, delta2 )
                    if( slodi1.isNonempty() ){
                        result.add( QueueItem( getUpperNeighbourOn( vari, state ), vari, -1, slodi1 ) )
                    }
                }
                //lower neighbour if exists
                if( state[vari] > 0 ){
                    var slodi2 : SortedListOfDisjunctIntervalsDouble =            get1ParamSetForTransitionSampleDREAL( inter, biosystem, state, entryDir, entryOr, vari, -1, delta1, delta2 )
                    if( slodi2.isNonempty() ){
                        result.add( QueueItem( getLowerNeighbourOn( vari, state ), vari, 1, slodi2 ) )
                    }
                }
            }
        
        /*HERE ends the code that will be replaced in COMBINED*/
    }
    
    return result
}

fun findParamValuesForReachabilityOfBFromA( pmin : Double, pmax : Double, biosystem : BioSystem, stateA : Array<Int>, entryDir : Int, entryOr : Int,constraintsB : List<ConstraintReachable>, delta1 : Double /* delta1 must be > delta2 */, delta2 : Double = 0.01 ) : SortedListOfDisjunctIntervalsDouble {
   val reachVerbosity = 1

   //result is zero
   var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
      
   //put the initial rectangle in queue if it does not have intersection with B
   //otherwise result is [pmin,pmax]
   if( intersectionNonemptyListOfConstraints( stateA, constraintsB, biosystem ) ){
       result.add( IntervalDouble( pmin, true, false, pmax, true, false ) )
       if( reachVerbosity > 0 ) println( "A cap B nonempty, result="+result.toString() )
   }else{
       val initialParSlodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf( IntervalDouble(pmin, true, false, pmax, true, false) ) )
       val visitedStates = StatesData( biosystem, stateA, initialParSlodi, entryDir, entryOr )
       val queueStates: Queue< QueueItem > = LinkedList< QueueItem >()
       
       if( reachVerbosity > 1) println("    Queue size "+queueStates.size )
       queueStates.add( QueueItem( stateA, entryDir, entryOr, initialParSlodi ) )
       if( reachVerbosity > 0) println("Adding state "+displayState(stateA)+" with params "+initialParSlodi.toString())
       if( reachVerbosity > 1) println("    Queue size "+queueStates.size )
       //while the queue is not empty
       var checkpoint : Int = 0
       while( !queueStates.isEmpty() ){
            
            if( reachVerbosity > 1) println("Visited states data: ${checkpoint}:"+visitedStates.toString())
            checkpoint++
            //take a state out of queue
            var qitem : QueueItem = queueStates.remove()
            if( reachVerbosity > 1 ) println( "Removing state from queue "+displayQueueItem( qitem ) )
            if( reachVerbosity > 1) println("   Queue size "+queueStates.size )
            var state : Array<Int> = qitem.getR()
            var parset : SortedListOfDisjunctIntervalsDouble = qitem.getSlodi()
            var entryDirQI : Int = qitem.getDir()
            var entryOrQI : Int = qitem.getOr()

            for( succQI in getListOfSuccessorsAndParameterSetsCOMBINEDtimeout( qitem, biosystem, delta1, delta2) ){
                var succState : Array<Int> = succQI.getR()
                var succDir : Int = succQI.getDir()
                var succOr : Int = succQI.getOr()
                var succParset : SortedListOfDisjunctIntervalsDouble = succQI.getSlodi()
                visitedStates.markAsVisited( succState )
                
                if( reachVerbosity > 1 ) println( "Successor state "+displayQueueItem( succQI ) )
                /*put the successors in queue if 
                    - they do not intersect with B
                    - their rectangle and parset is worth further exploring
                their parameters are intersection of state pars with transition pars
                otherwise union their params with actual result
                */
                if( intersectionNonemptyListOfConstraints( succState, constraintsB, biosystem ) ){
                    result.uniteWithOtherDisjunctList( succParset )
                    println("Uniting result with "+succParset.toString())
                }else{
                    if( !visitedStates.isSlodiAlreadyExplored( succState, succDir, succOr, succParset ) ){
                        var newSlodi : SortedListOfDisjunctIntervalsDouble = visitedStates.getSlodiToExploreFurther( succState, succDir, succOr, succParset )
                        queueStates.add( QueueItem( succState, succDir, succOr, newSlodi ) ) 
                        if( reachVerbosity > 0) println("Adding state "+displayState(succState)+" with params "+succParset.toString() )
                        if( reachVerbosity > 0) println("   Queue size "+queueStates.size )
                    }
                }
                visitedStates.addSlodiToExplored( biosystem, succState, succDir, succOr, succParset )
            }
        }
        println("Visited states data: checkpoint=${checkpoint}:"+visitedStates.toString())
    }
    
    //output the (union of) results 
    if( reachVerbosity > 0 ) println( "Result of reach A,B: "+result.toString() )
    return result
}

fun displayState( state : Array<Int> ) : String {
    var result : String = "["
    for( i in 0..(state.size-2) ){
        result += "${state[i]},"
    }
    result += "${state[state.size-1]}]"
    return result
}

fun displayQueueItem( qi : QueueItem ) : String {
    var result : String = displayState(qi.getR())
    result += ", entry dir ${qi.getDir()}, ori ${qi.getOr()}"
    result += ", params ${qi.getSlodi().toString()}"
    return result
}

fun displaySPpair( pa : Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble > ) : String{
    var result : String = displayState( pa.first )+", "+pa.second.toString()
    return result
}
