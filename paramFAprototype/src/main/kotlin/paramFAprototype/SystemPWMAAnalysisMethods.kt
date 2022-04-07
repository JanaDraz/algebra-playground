package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

import java.util.Queue
import java.util.LinkedList

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
fun get1ParamSetForTransitionSampleDREALforPWMA( parInterval : IntervalDouble, biosystem : BioSystemPWMA, state : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, delta1 : Double /* delta1 must be > delta2 */, delta2 : Double = 0.01 ) : SortedListOfDisjunctIntervalsDouble {
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
        commands = createCommandsForDrealRegularState1PvalueForPWMA( biosystem, state, dir1, ori1, dir2, ori2, s )
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
            commands = createCommandsForDrealRegularState1PvalueForPWMA( biosystem, state, dir1, ori1, dir2, ori2, middle )
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
            commands = createCommandsForDrealRegularState1PvalueForPWMA( biosystem, state, dir1, ori1, dir2, ori2, middle )
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
fun get1ParamSetForTransitionSampleDREALforPWMA(pmin : Double, pmax : Double, biosystem : BioSystemPWMA, state : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, delta1 : Double /* delta1 must be > delta2 */, delta2 : Double = 0.01 ) : SortedListOfDisjunctIntervalsDouble {
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
        commands = createCommandsForDrealRegularState1PvalueForPWMA( biosystem, state, dir1, ori1, dir2, ori2, s )
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
            commands = createCommandsForDrealRegularState1PvalueForPWMA( biosystem, state, dir1, ori1, dir2, ori2, middle )
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
            commands = createCommandsForDrealRegularState1PvalueForPWMA( biosystem, state, dir1, ori1, dir2, ori2, middle )
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

fun getListOfSuccessorsAndParameterSetsForPWMA( state : Array<Int>, entryVar : Int, entryDir : Int, pmin : Double, pmax : Double, biosystem : BioSystemPWMA, delta1 : Double, delta2 : Double ) : List< Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble > > {
    var result : MutableList< Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble > > = mutableListOf<Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble >> ()
    
    //take all the possible 2n facets (staying forever is not usable for reachability)
    //determine the successors with nonempty set of parameters
    //put them into the list of results
    val n : Int = biosystem.getDim()
    for( vari in 0..(n-1) ){
        //upper neighbour if exists
        if( state[vari] < (biosystem.getTresCount(vari)-1) ){
            var slodi1 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREALforPWMA( pmin, pmax, biosystem, state, entryVar, entryDir, vari, 1, delta1, delta2 )
            if( slodi1.isNonempty() ){
                result.add( Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble >(getUpperNeighbourOn( vari, state ), slodi1) )
            }
        }
        //lower neighbour if exists
        if( state[vari] > 0 ){
            var slodi2 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREALforPWMA( pmin, pmax, biosystem, state, entryVar, entryDir, vari, -1, delta1, delta2 )
            if( slodi2.isNonempty() ){
                result.add( Pair< Array<Int>, SortedListOfDisjunctIntervalsDouble >(getLowerNeighbourOn( vari, state ), slodi2) )
            }
        }
    }
    
    return result
}

fun getListOfSuccessorsAndParameterSetsForPWMA( stateAndPar : QueueItem, biosystem : BioSystemPWMA, 
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
                    var slodi1 : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREALforPWMA( inter, biosystem, state, entryDir, entryOr, vari, 1, delta1, delta2 )
                    if( slodi1.isNonempty() ){
                        result.add( QueueItem( getUpperNeighbourOn( vari, state ), vari, -1, slodi1 ) )
                    }
                }
                //lower neighbour if exists
                if( state[vari] > 0 ){
                    var slodi2 : SortedListOfDisjunctIntervalsDouble =  get1ParamSetForTransitionSampleDREALforPWMA( inter, biosystem, state, entryDir, entryOr, vari, -1, delta1, delta2 )
                    if( slodi2.isNonempty() ){
                        result.add( QueueItem( getLowerNeighbourOn( vari, state ), vari, 1, slodi2 ) )
                    }
                }
            }
        
        /*HERE ends the code that will be replaced in COMBINED*/
    }
    
    return result
}

fun findParamValuesForReachabilityOfBFromAforPWMA( pmin : Double, pmax : Double, biosystem : BioSystemPWMA, stateA : Array<Int>, entryDir : Int, entryOr : Int,constraintsB : List<ConstraintReachable>, delta1 : Double /* delta1 must be > delta2 */, delta2 : Double = 0.01 ) : SortedListOfDisjunctIntervalsDouble {
   val reachVerbosity = 1

   //result is zero
   var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
      
   //put the initial rectangle in queue if it does not have intersection with B
   //otherwise result is [pmin,pmax]
   if( intersectionNonemptyListOfConstraintsPWMA( stateA, constraintsB, biosystem ) ){
       result.add( IntervalDouble( pmin, true, false, pmax, true, false ) )
       if( reachVerbosity > 0 ) println( "A cap B nonempty, result="+result.toString() )
   }else{
       val initialParSlodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf( IntervalDouble(pmin, true, false, pmax, true, false) ) )
       val visitedStates = StatesDataPWMA( biosystem, stateA, initialParSlodi, entryDir, entryOr )
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
            
            /*The queue can be very long. At the moment when the state comes up,
             * it is very possible that similar rectangle and entry and parset 
             * has come up several times before and the parset that was not explored
             * when the state was put in queue is explored now. Therefore second
             * check of isSlodiAlreadyExplored takes place here.
             */ //NOPE, visited is updated, doesn't start properly with the first rectangle, leave it without this check.
            //if( !visitedStates.isSlodiAlreadyExplored( state, entryDirQI, entryOrQI, parset ) ){

                for( succQI in getListOfSuccessorsAndParameterSetsForPWMA( qitem, biosystem, delta1, delta2) ){
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
                    //HERE SET "SHARP" OR "" TO DIFFERENTIATE CONTACT ON FACET AND INSIDE RECTANGLE
                    if( intersectionNonemptyListOfConstraintsPWMA( succState, constraintsB, biosystem ) ){
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
            //}
        }
        println("Visited states data: checkpoint=${checkpoint}:"+visitedStates.toString())
    }
    
    //output the (union of) results 
    if( reachVerbosity > 0 ) println( "Result of reach A,B: "+result.toString() )
    return result
}
