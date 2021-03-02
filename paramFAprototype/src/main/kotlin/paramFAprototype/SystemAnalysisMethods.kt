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
    
    return ""
}

/* Taylor 1st degree, steps
 * dirori1 according to state rectangle
 * dirori2 according to state rectangle
 */
fun getTaylor1Formula( dirori1 : Int, state : Array<Int>, dirori2 : Int, system : BioSystem, k : Int ) : String {
    var formula : String = "~~"
    var dim : Int = system.getDim()
    var dir1 : Int = getDir( dirori1 ); var ori1 : Int = getOri( dirori1 )
    var dir2 : Int = getDir( dirori2 ); var ori2 : Int = getOri( dirori2 )
        
    var existsPart : String = "" //ex(var, ex(var, ...
    var boundsPart : String = "" // var >= .. and var <= .. and .. and d>=0 and d<=maxT/k
    var taylorPart : String = "" //
    var parenthesisPart : String = "" //...))
    
    val varStrs : Array<String> = system.getVarStrings()
    var taylVars : MutableList<String> = mutableListOf<String>()
    for( step in 0..k ) {
        for( i in 0..(dim-1) ) taylVars.add( varStrs[i] + "${step}" )
    }    

    for( tv in taylVars ) existsPart += ( "ex(" + tv + "," )
    existsPart += "ex(d," //add delta var
    
    for( i in 0..(dim*(k+1)) ) parenthesisPart += ")" //variables + d

    var j : Int = 0
    //bounds for first facet
    var loBounds : Array<String> = arrayOf<String>()
    var upBounds : Array<String> = arrayOf<String>()
    for( i in 0..(dim-1) ) {
        if( i == dir1 ) {
            if( ori1 > 0 ) { //right end of rectangle
                loBounds[i] = ""
                upBounds[i] = taylVars[i]+"="+system.getTres( i, state[i] + 1 )
            } else { //ori1 < 0, left end of rectangle
                loBounds[i] = taylVars[i]+"="+system.getTres( i, state[i] )
                upBounds[i] = ""
            }
        } else {
            loBounds[i] = taylVars[i]+">="+system.getTres( i, state[i] )
            upBounds[i] = taylVars[i]+"<="+system.getTres( i, state[i] + 1 )
        }
        j += 1
    }
    //bounds inside state rectangle ... for taylor 
    for( m in 1..(k-1) ) {
        for( i in 0..(dim-1) ){
            loBounds[j] = taylVars[j]+">="+system.getTres( i, state[i] )
            upBounds[j] = taylVars[j]+"<="+system.getTres( i, state[i] + 1 )
            j += 1
        }
    }
    //bounds for second facet
    for( i in 0..(dim-1) ) {
        if( i == dir2 ) {
            if( ori2 > 0 ) { //right end of rectangle
                loBounds[j] = ""
                upBounds[j] = taylVars[j]+"="+system.getTres( i, state[i] + 1 )
            } else { //ori1 < 0, left end of rectangle
                loBounds[j] = taylVars[j]+"="+system.getTres( i, state[i] )
                upBounds[j] = ""
            }
        } else {
            loBounds[j] = taylVars[j]+">="+system.getTres( i, state[i] )
            upBounds[j] = taylVars[j]+"<="+system.getTres( i, state[i] + 1 )
        }
        j += 1
    }
    //and bounds for d
    loBounds[j] = "d>=0"
    upBounds[j] = "d<="+ (system.getMaxT().toLong()/k)
    
    for( i in 0..j ){
        boundsPart += loBounds[i] + " and " + upBounds[i] + " and "
    }
    
    //and x1=(x0+d) and y1=(y0+p*d)")))))
    //taylor part ... deg 1 = Euler
    //this functiones just for k=1
    var tayl : Array<String> = arrayOf<String>()
    var derString : String = ""
    var vars0 : Array<String> = arrayOf<String>(taylVars).copyOfRange(0, dim)
    for( i in 0..(dim-1) ) {
        derString = system.getDerPols()[i].toString(*vars0)
        tayl[i] = taylVars[dim+i] + "=(" + taylVars[i] + "+" + "d*(" + derString + "))"
    } 
    for( i in 0..(dim-2) ) {
        taylorPart += tayl[i] + " and " 
    }
    taylorPart += tayl[dim-1] //last without "and"
    
    var facetToFacetFormula = existsPart + boundsPart + taylorPart + parenthesisPart
    
    return facetToFacetFormula
}

/*Assuming the bio system includes information about the tresholds,
 * derivatives, second derivatives, 
 * and settings for the computation like the degree of Taylor polynomials,
 * delta time for one step, maximum T for a rectangle (system dependent, 
 * at the beginning user specified)
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
fun getParamSetForTransition( dirori1 : Int, state1 : Array<Int>, state2 : Array<Int>, system : BioSystem, steps : Int = 2 ) : SortedListOfDisjunctIntervals {
    //identify direction and orientation of facet between the states
    val dirori2 = getDirOriCode( state1, state2, system.getDim() )
    
    var formula = "false"
    var semiSet : String = "~"

    //for k steps
    //create formula for transition by k steps of Taylor approximation    
    formula = getQuantifiedFormulaForTransition( dirori1, state1, dirori2, system, steps )
      
    //get semialgebraic set of parameters satisfying formula
    semiSet = getQelResultForFormula( formula )
    
    //debug: print the semialgebraic set
    println( semiSet )    
    
    //convert semialgebraic set to a sorted list of disjunct interval
    return semiToIntervals( semiSet , system.getParStr() )
}

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
