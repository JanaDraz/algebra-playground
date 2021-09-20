package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

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

/*
 * Check Existence of suitable parameters by Dreal.
 * Divide the parameter set and delete intervals where no
 * suitable valuation exists.
 * Given granularity - max times to divide, and minimal length deltaI.
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

fun encodeAndCheck( pmin: Float , pmax: Float, system : BioSystem, state1 : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, deltaI : Float = 0.01f, maxDivideSteps : Int = 10 ) : String {
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

/*
 * Sample the input interval of parameters, check by Dreal, how many
 * valuations enable the transition. If the diversity of the sampling
 * is too high (a given number between 1 and 2), we divide the interval
 * and compute for smaller intervals.
 * Arguments: diversity cutoff div, max steps to divide,  and minimal length deltaI.
*/
fun getParamSetForTransitionSampleDREAL( dirori1 : Int, state1 : Array<Int>, state2 : Array<Int>, system : BioSystem, steps : Int = 2, div : Float = 1.5f, deltaI : Float = 0.01f, maxDivideSteps : Int = 10 ) : SortedListOfDisjunctIntervals {
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
        if( method == "Sampling-Dreal" ){
            resultList.uniteWithOtherDisjunctList( getParamSetForTransitionSampleDREAL( diroriCurrent, first, states[i], system, steps ) )
        }
        
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
