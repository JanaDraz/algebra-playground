/*
 * Parameters for Facetal Abstraction Prototype Implementation
 * 
 * (First version of this Kotlin source file was generated by the Gradle 'init' task.)
 */
package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

class App {
    val greeting: String
        get() {
            return "Hello world."
        }
}

fun exampleLVP(){
    //just one parameter 3 rectangles find out the directions of flow
    var systemLVexample : BioSystem = getBioSystemByName( "CASE-EXAMPLE-LV-1PAR" )
    var divideP : Int = 5
    
    var slodiCA : SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVexample, arrayOf(2,1), 0, 1, 0, -1, 0.01, divideP )//deltaI 0.01, maxdivide steps 10
    
    var slodiAB: SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVexample, arrayOf(1,1), 0, 1, 0, -1, 0.01, divideP )
    
    var slodiBA: SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVexample, arrayOf(0,1), 1, 1, 0, 1, 0.01, divideP )
    systemLVexample.setMaxT( "20.0" ) //state with the equilibrium for some pars, the solutions slow down
    
    var slodiAC: SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVexample, arrayOf(1,1), 0, -1, 0, 1, 0.01, divideP )
    
    println("CASE-EXAMPLE-LV-1PAR p:") //DREAL REASONING
    println("C->A for p in "+slodiCA.toString()) //C->A for p in {[0.16250000000000003,0.5]}
    println("A->B for p in "+slodiAB.toString()) //A->B for p in {[0.2,0.5]}
    println("B->A for p in "+slodiBA.toString()) //B->A for p in {[0.1,0.15000000000000002]}
    println("A->C for p in "+slodiAC.toString()) //A->C for p in {[0.1,0.15000000000000002]}
}

fun exampleLVP_sampling(){
    //just one parameter 3 rectangles find out the directions of flow
    var systemLVexample : BioSystem = getBioSystemByName( "CASE-EXAMPLE-LV-1PAR" )
    var delta1 : Double = 0.1
    var delta2 : Double = 0.001
    
    var slodiCA : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVexample, arrayOf(2,1), 0, 1, 0, -1, delta1, delta2 )
    
    var slodiAB: SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVexample, arrayOf(1,1), 0, 1, 0, -1, delta1, delta2 )
    
    var slodiBA: SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVexample, arrayOf(2,1), 0, 1, 0, -1, delta1, delta2 )
    systemLVexample.setMaxT( "20.0" ) //state with the equilibrium for some pars, the solutions slow down
    
    var slodiAC: SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVexample, arrayOf(2,1), 0, 1, 0, -1, delta1, delta2 )

    println("CASE-EXAMPLE-LV-1PAR p:")           //v results for whole intervals method (DREAL REASONING from exampleLVP) below
    println("C->A for p in "+slodiCA.toString()) //C->A for p in {[0.16250000000000003,0.5]}
    println("A->B for p in "+slodiAB.toString()) //A->B for p in {[0.2,0.5]}
    println("B->A for p in "+slodiBA.toString()) //B->A for p in {[0.1,0.15000000000000002]}
    println("A->C for p in "+slodiAC.toString()) //A->C for p in {[0.1,0.15000000000000002]}
/* results for SAMPLING methods below
 CASE-EXAMPLE-LV-1PAR p: 0.1,0.01 (deltas)
C->A for p in {(0.16250000000000003,0.5]}
A->B for p in {(0.20625000000000002,0.5]}
B->A for p in {(0.16250000000000003,0.5]}
A->C for p in {(0.16250000000000003,0.5]}
* 
CASE-EXAMPLE-LV-1PAR p: 0.01, 0.001 (different deltas)
C->A for p in {(0.16718750000000002,0.5]}
A->B for p in {(0.20703125,0.5]}
B->A for p in {(0.16718750000000002,0.5]}
A->C for p in {(0.16640625000000003,0.5]}
 */
}


fun caseStudyLVPPSquared(){
 //p,p^2 one parameter synthesis, DREAL REASONING
    
    var systemLVp : BioSystem = getBioSystemByName( "CASE000aLVPARSQUARED" )
    var divideP : Int = 5
    
    //result as a slodi
//    var slodi_CA : SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVp, arrayOf(2,1), 0, 1, 0, -1, 0.01, divideP )//deltaI 0.01, maxdivide steps
//    var slodi_AB: SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVp, arrayOf(1,1), 0, 1, 0, -1, 0.01, divideP )
    //try to catch even the slower evolution of the solutions
//    systemLVp.setMaxT( "20.0" )
//    var slodi_BA: SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVp, arrayOf(0,1), 1, 1, 0, 1, 0.01, divideP )//may need 1,1 ingoing facet
    systemLVp.setMaxT( "30.0" )
    var slodi_AC: SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 0.5, systemLVp, arrayOf(1,1), 0, -1, 0, 1, 0.01, divideP )
    
    println("CASE000a LV PARSQUARED p:")
//    println("C->A for p in "+slodi_CA.toString())//C->A for p in {[0.1875,0.5]}
//    println("A->B for p in "+slodi_AB.toString())//A->B for p in {[0.17500000000000002,0.5]}
//    println("B->A for p in "+slodi_BA.toString())//B->A for p in {[0.1,0.125]}
    //^for dir1=0,or1=-1
    println("A->C for p in "+slodi_AC.toString())//A->C for p in {} ... jeste delsi cas?
    
    //several transitions - some kind of reachability
    //p, p^2 vs p, q - look for behaviours that can differ for par valuations
    //as moving the centre of the loop, under and above (left,right)
    //of the state rectangle p=0.1 vs p=0.2, q=0.01 vs q=0.04,5,6
}

fun caseStudyLVPPSquared_QDA(){
 //p,p^2 one parameter synthesis QDA
    
    var systemLVp : BioSystem = getBioSystemByName( "CASE000aLVPARSQUARED" )
    var delta1 : Double = 0.001
    var delta2 : Double = 0.001

    println("CASE000a LV PARSQUARED p QDA, d1=$delta1, d2=$delta2:")
    
    //NOTE: Input facet orientation for Python QDA is 0 and 1 (not -1 and 1)
    
    val succsOfC_forCA : List<String> = getResultFor1paramONLYFromPython( "CASE000aLVPARSQUARED", "[2,1]", "0", "1", "0.1", "0.5", "0.001", "10.0" )//maxt was 30 for the older output
    println("Succs of C (for CA):")
    println(succsOfC_forCA)
    
    val succsOfA_forAB : List<String> = getResultFor1paramONLYFromPython( "CASE000aLVPARSQUARED", "[1,1]", "0", "1", "0.1", "0.5", "0.001", "10.0" )//maxt was 30 for the older output
    println("Succs of A (for AB):")
    println(succsOfA_forAB)
    
    val succsOfB_forBA : List<String> = getResultFor1paramONLYFromPython( "CASE000aLVPARSQUARED", "[0,1]", "1", "1", "0.1", "0.5", "0.001", "30.0")
    println("Succs of B (for BA):")
    println(succsOfB_forBA)
    
    val succsOfA_forAC : List<String> = getResultFor1paramONLYFromPython( "CASE000aLVPARSQUARED", "[1,1]", "0", "0", "0.1", "0.5", "0.001", "40.0" )//maxt was 30 for the older output
    println("Succs of A (for AC):")
    println(succsOfA_forAC)

//to try if not enough time
//    systemLVp.setMaxT( "30.0" ) //BA
//    systemLVp.setMaxT( "40.0" ) //AC
 
}

fun caseStudyLVPPSquared_sampling(){
 //p,p^2 one parameter synthesis SAMPLING
    
    var systemLVp : BioSystem = getBioSystemByName( "CASE000aLVPARSQUARED" )
    var delta1 : Double = 0.01
    var delta2 : Double = 0.001
    
    //result as a slodi
    var slodi_CA : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVp, arrayOf(2,1), 0, 1, 0, -1, delta1, delta2 )

    var slodi_AB: SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVp, arrayOf(1,1), 0, 1, 0, -1, delta1, delta2 )

    //try to catch even the slower evolution of the solutions
    systemLVp.setMaxT( "30.0" )
    var slodi_BA: SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVp, arrayOf(0,1), 1, 1, 0, 1, delta1, delta2 )//may need 1,1 ingoing facet

    systemLVp.setMaxT( "40.0" )                         //the same method with Interval input is used in big reachability
    var slodi_AC: SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL( 0.1, 0.5, systemLVp, arrayOf(1,1), 0, -1, 0, 1, delta1, delta2 )
    
    println("CASE000a LV PARSQUARED p sampling, d1=$delta1, d2=$delta2:")
//SAMPLING results below:                           //whole intervals (DREAL REASONING) for comparison:
    println("C->A for p in "+slodi_CA.toString()) //C->A for p in {[0.1875,0.5]}
    println("A->B for p in "+slodi_AB.toString()) //A->B for p in {[0.17500000000000002,0.5]}
    println("B->A for p in "+slodi_BA.toString())   //B->A for p in {[0.1,0.125]}
    //^for dir1=0,or1=-1
    println("A->C for p in "+slodi_AC.toString())   //A->C for p in {} ... jeste delsi cas?
    
    //several transitions - some kind of reachability
    //p, p^2 vs p, q - look for behaviours that can differ for par valuations
    //as moving the centre of the loop, under and above (left,right)
    //of the state rectangle p=0.1 vs p=0.2, q=0.01 vs q=0.04,5,6
    
    //sampling results delta1=0.1,delta2=0.01:
    /*CASE000a LV PARSQUARED p sampling:
     * times 10,10,20,30
    C->A for p in {(0.1875,0.5]}
    A->B for p in {(0.18125000000000002,0.5]}
    B->A for p in {}
    A->C for p in {} 
    * times 10,10,30,40 stejny vysledek 
    CASE000a LV PARSQUARED p sampling:
    C->A for p in {(0.1875,0.5]}
    A->B for p in {(0.18125000000000002,0.5]}
    B->A for p in {}
    A->C for p in {}
    * */
    //sampling results delta1=0.01,delta2=0.001:
    //times 10,10,20,30:
    /*
     CASE000a LV PARSQUARED p sampling:
    C->A for p in {(0.18875000000000006,0.5]}
    A->B for p in {(0.18500000000000005,0.5]}
    B->A for p in {}
    A->C for p in {}
     */
}

fun caseStudyLVPQ(){
 //p,q two parameters synthesis
    var systemLVpq : BioSystem = getBioSystemByName( "CASE000bLVMULTIPARS" )
    //sample parameter space, check by dreal one transition
    //approximate the set of delta-sat par vals
    //findSemialgebraicFor2ParamsSimple(pmin: Double , pmax: Double, qmin: Double , qmax: Double, system : BioSystem, state1 : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, divideStepsP : Int, divideStepsQ : Int)
    
    //admissible valuations p in [0.1,0.5], q in [0.01,0.06]
    var divideP : Int = 100
    var divideQ : Int = 10
    
    var semiAlgStrCA : String = findSemialgebraicFor2ParamsSimpleShift(0.1, 0.5, 0.01, 0.06, systemLVpq, arrayOf(2,1), 0, 1, 0, -1, divideP, divideQ )
    var semiAlgStrAB : String = findSemialgebraicFor2ParamsSimpleShift(0.1, 0.5, 0.01, 0.06, systemLVpq, arrayOf(1,1), 0, 1, 0, -1, divideP, divideQ ) 
    //try to catch even the slower evolution of the solutions
    systemLVpq.setMaxT( "20.0" )
    var semiAlgStrBA : String = findSemialgebraicFor2ParamsSimpleShift(0.1, 0.5, 0.01, 0.06, systemLVpq, arrayOf(0,1), 1, 1, 0, 1, divideP, divideQ ) //may need 1,1 ingoing facet
    var semiAlgStrAC : String = findSemialgebraicFor2ParamsSimpleShift(0.1, 0.5, 0.01, 0.06, systemLVpq, arrayOf(1,1), 0, -1, 0, 1, divideP, divideQ )
    
    println("CASE000b LV MULTIPARS p,q:")
    println( "C->A for p,q satisfying "+semiAlgStrCA )
    println( "A->B for p,q satisfying "+semiAlgStrAB )
    println( "B->A for p,q satisfying "+semiAlgStrBA )
    println( "A->C for p,q satisfying "+semiAlgStrAC )
    /* CASE000b LV MULTIPARS p,q: B->A is 1,1 ingoing, timemax for B->A and A->C was 20.0, divide p 100x ,q 10x: VYSLEDKY-PQ-100-10.txt     */
    /* CASE000b LV MULTIPARS p,q: B->A is 1,1 ingoing, timemax for B->A and A->C was 20.0, divide p,q 10x - in .ods     */
    /*
     CASE000b LV MULTIPARS p,q: B->A is 0,-1 ingoing, timemax for B->A and A->C was 20.0, divide p,q 5x:
C->A for p,q satisfying (p >= 0.1) and (p <= 0.5) and (q >= 0.01) and (q <= 0.06) and (( ( - 444089209850064375000000000000000000000000000000*p         - 2312964634635748200000000000000000000000000000000*p*q         + 426510678626832488418891216574850000000000000000*p                                                              2         + 1776356839400269680000000000000000000000000000000*q         + 854501654619830731910991183458072000000000000000*q         + 19999999999999909217358922172000513583563788220247298384423097)       /20000000000000000000000000000000000000000000000000000000000000) >= 1)
A->B for p,q satisfying (p >= 0.1) and (p <= 0.5) and (q >= 0.01) and (q <= 0.06) and (( ( - 31249999999999961250000000000000000000000000000000*p         + 3622665293346165000000000000000000*p*q         + 23749999999999970985149540160687325000000000000000*p                                                 2         + 106581410364014568000000000000000000*q         - 8402591701751023601467883534172600*q         - 2062499999999994957554365762973125270911059191897)       /2000000000000000000000000000000000000000000000000) >= 1)
B->A for p,q satisfying (p >= 0.1) and (p <= 0.5) and (q >= 0.01) and (q <= 0.06) and (( (691731770833333125000000000000000000000000000000*p         - 1953124999999998000000000000000000000000000000000*p*q         - 467122395833333593384100868698350000000000000000*p                                                               2         + 15624999999999936000000000000000000000000000000000*q         - 273437499999995898677081231719920000000000000000*q         + 92919921875000027051219770386355157865131918733)       /50000000000000000000000000000000000000000000000) >= 1)
A->C for p,q satisfying (p >= 0.1) and (p <= 0.5) and (q >= 0.01) and (q <= 0.06) and (( (13020833333333343750000000000000000000000000000000*p         + 70288425285875535000000000000000000*p*q         - 9895833333333342147976372989806225000000000000000*p                                                 2         + 290405559682043280000000000000000000*q         - 38844956058354934938096129525378600*q         + 1859375000000002014357764493835096484771680688401)       /1000000000000000000000000000000000000000000000000) >= 1)

*/
}

fun caseStudyLVReachability(){
    var systemLVReach1 : BioSystem = getBioSystemByName( "CASE000cLV1PARMORETRES" )
    //init
    //constraints
    //call reachability
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( 0.1, 0.5, systemLVReach1, arrayOf(40,10), 1,-1, listOf<ConstraintReachable>( ConstraintReachable(0,false, 0.2)), delta1, delta2 )
    println( slodi )
}

fun caseStudyLVSmallReachability(){
    var systemLVReach1 : BioSystem = getBioSystemByName( "CASE-EXAMPLE-LV-1PAR" )
    //init
    //constraints
    //call reachability
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( 0.1, 0.5, systemLVReach1, arrayOf(1,1), /*0,0,*/1,-1, listOf<ConstraintReachable>( ConstraintReachable(0,true, 0.5)), delta1, delta2 )
    println( slodi )
}
//11 00 init:
//Result of reach A,B: {[0.1,0.20625000000000002)}
//{[0.1,0.20625000000000002)}

/* ...bonus (non maf, non polyn)
 * Brusselator case study on parameter b values for reachability.
 * 100 x 100 pieces
 * A = [a1,a2] = [10,43] (real point [0.63,2.6] upper y turning point of limit cycle for b=3) -->
 * B = [b1,b2] = [62,14] (real point [3.75,0.88] up x turning point of limit cycle for b=3) from up to left
 * C = [c1,c2] = [16,28] (real point [1,1.7], the central point of spiral for b=1.7)
D=[0.375,3.5] [6, 58] |^
E=[3,0.9] [49, 15] \v
F=[3,2.25] [49, 37] <--
 * For b = 3 wanted reachability situation:   A->B,   (B->A),   A-x->C, (B-x->C)
 * For b = 1.7 wanted reachability situation: A-x->B, (B-x->A), A->C,   (B->C)
 * minimal input addmissible valuatoins: [1.7,3.0]
 * run for FA-1min->QDA
 */
fun caseBRUSSELATORreachability_sampling(){
    //just one parameter 3 rectangles find out the directions of flow
    var systemBRU : BioSystem = getBioSystemByName( "CASE002aBRUSSELATOR1par" )
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    val pmin = 1.7
    val pmax = 3.0    
    
    val stateA : Array<Int> = arrayOf( 10,43 )
    val stateC : Array<Int> = arrayOf( 16,28 )
    val stateD : Array<Int> = arrayOf( 6,58 )
    val stateX : Array<Int> = arrayOf( 49,13 )
    //A overapproximated by: y >= 4.0
    val constraintsA : List<ConstraintReachable> = listOf<ConstraintReachable>( /*ConstraintReachable( 0, false, 1.0 ),*/ ConstraintReachable( 1, true, 4.0 ) )
    //B overapproximated by: x >= 3.5 and y <= 1.0
    val constraintsB : List<ConstraintReachable> = listOf<ConstraintReachable>( ConstraintReachable( 0, true, 3.5 ), ConstraintReachable( 1, false, 1.0 ) )
    //C overapproximated by: x in [0.9,1.1] and y in [1.6,1.8]
    val constraintsC : List<ConstraintReachable> = listOf<ConstraintReachable>( ConstraintReachable( 0, true, 0.9 ), ConstraintReachable( 0, false, 1.1 ), ConstraintReachable( 1, true, 1.6 ), ConstraintReachable( 1, false, 1.8 ) )
    
    val constraintsD : List<ConstraintReachable> = listOf<ConstraintReachable>( /*ConstraintReachable( 0, false, 1.0 ),*/ ConstraintReachable( 1, true, 3.0 ) )
    
    /*val slodiAB : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( pmin, pmax, systemBRU, stateA, 0, -1, constraintsB, delta1, delta2 )
    println("CASE-BRU-1PAR b, partial result for A->B:")  
    println("A->B for b in "+slodiAB.toString())
    
    val slodiAC : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( pmin, pmax, systemBRU, stateA, 0, -1, constraintsC, delta1, delta2 )
    println("CASE-BRU-1PAR b both reachabilities:")           
    println("A->B for b in "+slodiAB.toString())
    println("A->C for b in "+slodiAC.toString()) */
    
    //question - is it possible to escape C and go above y=4?
    val slodiCA : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( pmin, pmax, systemBRU, stateC, 1, -1, constraintsA, delta1, delta2 )
    
    println("***divide here")
    
    //question - does D escape C and go above y=3?
    val slodiXD : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( pmin, pmax, systemBRU, stateD, 0, 1, constraintsD, delta1, delta2 )
}


fun caseStudySEIR(){
    //CASE001aSEIR1par
    //add the initial rectangle state
    var systemSEIRp : BioSystem = getBioSystemByName( "CASE001aSEIR1par" )
     
    //For which b in [0.1,100] the number of S is decreasing?
    //all b in [0.1,100] maxT = 20 (days)
//    var dSlodi : SortedListOfDisjunctIntervalsDouble = findSlodiFor1ParamDouble( 0.1, 100.0, systemSEIRp, arrayOf(13,0,0,0), 0, 0, 0, -1, 0.01, 2 )
    
    // (and first to increase is E)? maxT = 10
//    var dSlodi : SortedListOfDisjunctIntervalsDouble = get1ParamSetForTransitionSampleDREAL(0.1, 10.0, systemSEIRp, arrayOf(13,0,0,0), 0, 0, 1, 1, 0.5, 0.01)
    
//    println( dSlodi.toString() )
    //findSlodiFor1ParamDouble( 0.1, 10.0, systemSEIRp, arrayOf(13,0,0,0), 0, 0, 1, 1, 0.01, 2 )
    
    //CASE001bSEIRmpar
    
}

fun caseStudySEIR_SimpleNormReachability(){
    //simple SEIR model normed, 1 parameter = beta
    val systemSEIR1par : BioSystem = getBioSystemByName("CASE001aSEIRnormSimple1par")
    //fraction of infected is >= 0.5, vars: 0 s 1 e 2 i 3 r
    val constraintr : ConstraintReachable = ConstraintReachable( 2, true, 0.5 )
    val pmin : Double = 0.1
    val pmax : Double = 1.0
    //compute reachability
    
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01 // 0.05 for init=4005
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( pmin, pmax, systemSEIR1par, arrayOf(9,0,0,0), /*0,0,*/3,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    //var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromA( pmin, pmax, systemSEIR1par, arrayOf(4,0,0,5), /*0,0,*/3,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    println( slodi )
}
//?zkouset? init 8010 3,-1 with combined timeout reach A,B


//init 9000 3,-1
//combined method with QDA, reach A,B:


//init 2000 0,0
//Result of reach A,B: {[0.19375,1.0]}
//{[0.19375,1.0]}


fun caseStudyRepressilator3D(){
    val systemRep3d1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002aREPRES3D")
    val constraintr : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//>=7.5
    val pmin : Double = 0.1 //-5.0 /*-1.0*/ //0.0 //0.1
    val pmax : Double = 0.15 //-1.0 /*3.0*/  //3.0 //1.0
    //compute reachability  //TODO s mensim delta1
    
    var delta1 : Double = 0.01
    var delta2 : Double = 0.01
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRep3d1par, arrayOf(2,1,1), 0,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    println( slodi )
}

//the same case study as caseStudyRepressilator3D, but PWMA with added treshold x=7.5 and 
//added approximated hill value at x=7.5, hill=0.11
fun caseStudyRepressilator3DPlusXtres(){
    val systemRep3d1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002aREPRES3DPlusXtres")
    val constraintr : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//>=7.5
    val pmin : Double = 0.1 //-5.0 /*-1.0*/ //0.0 //0.1
    val pmax : Double = 0.15 //-1.0 /*3.0*/  //3.0 //1.0
    //compute reachability  //TODO s mensim delta1
    
    var delta1 : Double = 0.001
    var delta2 : Double = 0.001
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRep3d1par, arrayOf(2,1,1), 0,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    println( slodi )
}

/*[-1,3] -> result zhruba [-1,0.149...]
 * val systemRep3d1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002aREPRES3D")
    val constraintr : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//>=7.5
    val pmin : Double = -1.0//0.0//0.1
    val pmax : Double = 3.0 //3.0//1.0
    //compute reachability  //TODO [-1,1]
    
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRep3d1par, arrayOf(2,1,1), 0,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    println( slodi )
 * 
 */

/*mozna jeste nebyly constanty +0.1 v obehu... pro jistotu jeste jednou
 * val systemRep3d1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002aREPRES3D")
    val constraintr : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//>=7.5
    val pmin : Double = 0.0//0.1
    val pmax : Double = 3.0//1.0
    //compute reachability  //TODO [-1,1]
    
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRep3d1par, arrayOf(2,1,1), 0,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
 * 
 * Result: {[0.0,0.1]} yes it is the same 
 */

/*  too many tresholds
 fun caseStudyRepressilator5D(){
    val systemSEIR1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002bREPRES5D")
    val constraintr : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//>=7.5
    val pmin : Double = 0.0//0.1
    val pmax : Double = 3.0//1.0
    //compute reachability
    
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemSEIR1par, arrayOf(2,1,1,1,1), 0,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    println( slodi )
}*/

//still too many tresholds (6^5 state invariants)
 fun caseStudyRepressilator5DSimple(){
    val systemREP5D : BioSystemPWMA = getBioSystemPWMAByName("CASE002bREPRES5DSimple")
    val constraintr : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//>=7.5
    val pmin : Double = 0.0//0.1
    val pmax : Double = 3.0//1.0
    //compute reachability
    
    var delta1 : Double = 0.1
    var delta2 : Double = 0.01
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemREP5D, arrayOf(2,1,1,1,1), 0,-1, listOf<ConstraintReachable>( constraintr), delta1, delta2 )
    println( slodi )
}

 fun caseStudyRepressilator5DVerySimple(){
    val systemRepres5d1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002bREPRES5DVerySimple")
    
    val pmin : Double = 3.0  //-5.0//0.0//0.1
    val pmax : Double = 5.0 //0.0//3.0//1.0
    //compute reachability
    
    var delta1 : Double = 0.1
    var delta2 : Double = 0.05
    
    val constraint1 : ConstraintReachable = ConstraintReachable( 0, true, 7.5 )//x0 >= 7.5
    //above is reachable for whole [0,3] input par interval
    //constraint1 means a whole facet of the phase space cube, where x0 between 7.07 and 10
        
    val constraint2 : List<ConstraintReachable> = listOf<ConstraintReachable>( 
                                ConstraintReachable( 0, true, 7.5 ),
                                ConstraintReachable( 1, true, 7.5 ),
                                ConstraintReachable( 2, true, 7.5 ),
                                ConstraintReachable( 3, true, 7.5 ),
                                ConstraintReachable( 4, true, 7.5 ) )
    
    //above is reachable?? for which pars??
    //constraint2 means the corner opposite to 00000 (state 22222)
    
    
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    //00000 - a corner
    //(11111 - the centre)
    //22222 - the opposite corner
    /*var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRepres5d1par, arrayOf(0,0,0,0,0), 0,-1, listOf<ConstraintReachable>( constraint1 ), delta1, delta2 )
    println( slodi ) */
    
    //after we explore pars, try reaching the corner:
    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRepres5d1par, arrayOf(0,0,0,0,0), 0,-1, constraint2, delta1, delta2 )
    println( slodi )
}


 fun caseStudyRepressilator5DVerySimpleUntrivialParset(){
    val systemRepres5d1par : BioSystemPWMA = getBioSystemPWMAByName("CASE002bREPRES5DVerySimple")
    
    val pmin : Double = 0.0
    val pmax : Double = 0.5
    //compute reachability
    
    var delta1 : Double = 0.01
    var delta2 : Double = 0.01
    
    val constraint2221x : List<ConstraintReachable> = listOf<ConstraintReachable>( 
                                ConstraintReachable( 0, true, systemRepres5d1par.getTres(0, 2 ).toDouble() ), //to be sure of the same values
                                ConstraintReachable( 1, true, systemRepres5d1par.getTres(1, 2 ).toDouble() ), //use the precise formulation of value
                                ConstraintReachable( 2, true, systemRepres5d1par.getTres(2, 2 ).toDouble() ), //from impl of ConstraintReachable
                                ConstraintReachable( 3, true, systemRepres5d1par.getTres(3, 1 ).toDouble() ) )
    
    //above is reachable?? for which pars??
    //constraint2 means the corner opposite to 00000 (state 22222)
    
    
    //begin on facet r=0, 0<ei<epsilon, max-epsilon<s<max
    //00000 - a corner
    //(11111 - the centre)
    //22222 - the opposite corner

    var slodi : SortedListOfDisjunctIntervalsDouble = findParamValuesForReachabilityOfBFromAforPWMA( pmin, pmax, systemRepres5d1par, arrayOf(0,0,0,0,0), 0,-1, constraint2221x, delta1, delta2 )
    println( slodi )
}

fun main(args: Array<String>) {
    
//done    exampleLVP()

//done    exampleLVP_sampling()
    
//done    caseStudyLVPPSquared()

//done    caseStudyLVPPSquared_sampling()
//co neslo v caseStudyLVPPSquared_sampling pomoci FA, tu s QDA:
//jeste s casy 10 10 30 40 jako pq 
//done    caseStudyLVPPSquared_QDA()

//    caseStudyLVPQ()

    //nedojelo do konce jen s FA
    //30.3.2022 jet s timetoutem FA->QDA: Result {[0.133984375,0.2],[0.21274414062500002,0.30000000000000004],[0.30625,0.5]}
//done    caseStudyLVReachability()

    /**
     * the two small reachabilities with whole initial rectangles follow
     */
    //done -with QDA and -with the combined FA/QDA timeout 1min, always FA
//done    caseStudyLVSmallReachability()
    
    //28.3.2022 - seir jeste pojede, ted zkusime neco mensiho hybridne s pythonem... prijde zrychleni?
    //29.3.2022 - now try combined seir, timeout is 1min... yes
    //init different from 90000... (init=40005 nedojelo do asi tak 2 dni necelych, ani s QDA timeout)
    //jede s initem=4005 a s delta2=0.05 a s heuristikou na invariantni sumu coordu stavu, dojel za skoro 2 dny
    //original init=9000, delta2=0.01 with heuristics (because of computation time comparison with/without heur - longer with heur...)
//done    caseStudySEIR_SimpleNormReachability()
    
    //28.3.2022 dobehlo rychle, ale zadani nebylo ok
    //29.3.2022 input interval [0,3] : Result of reach A,B: {[0.0,0.1]}
    //30.3.2022 input interval [-1,1]: Result of reach A,B; {[-1,0.149...}
    //30.3.2022 jeste jednou [0,3] pro srovnani s above (mozna nebyly konstanty 0.1+produkcni pritomne) OK Result of reach A,B: {[0.0,0.1]}
    //1.4.2022 s [-5,-1] pro ohraniceni valid pars zleva cele valid
    //1.4.2022 s [0.1,0.15] s delta1=delta2=0.01 pro zaplneni mezery delky 0.1 ve vypoctu param[0,3] (s delta1=0.1) OK valid all
//done    caseStudyRepressilator3D()
    //with added x=7.5 (this was added by Pithya, want a comparison on the same model / pro delta 0.01 zase stejny vysl REP3D_addX75.txt [0.1,0.15])
    //jeste s 0.001 delta stejny vysledek [0.1,0.15]
    //JEDE se SHARP PWMA a 0.001
//done    caseStudyRepressilator3DPlusXtres()
    
    //30.3.2022 bezelo celou noc a stejne moc dlouha fronta, znova s mene tresholdy (4^5 nebo 6x6x6x6x6 misto 10x...)
    //caseStudyRepressilator5D()
    //30.3.2022 4x4x... tresholds, constraint >= 0.75 is satisfied for init for the small num of states 
    //30.3.2022 again with different init: 00000 for whole [0,3] is reachable 
    //31.3.2022 init A=00000  B=2xxxx with params in [-10,10] ... moc velky parset 
    //31.3.2022 init A=00000  B=2xxxx with params in [-5,0] ... all valid
    //init A=00000 B=2221x, params=?, Pithya says [-10,0.249...] left bound is irrelevant (-\infty) < JEDE s admissible [-0.5,0.5]
    //se SHARP PWMA
//done    caseStudyRepressilator5DVerySimpleUntrivialParset()

    //30.3.2022 6x6x... tresholdy, init: 21111 runs more than 24h, ~5000 states, and increasing
//too much tres    caseStudyRepressilator5DSimple()


    //31.3.2022 Brusselator try with only FA
    //runs with FA only, how would the result change with combined FA -timeout-> QDA and the same settings?
    //31.3.2022 points CA XD with FA nereach (with QDA timeout JET? z analyzy jestli je avg time na stav vetsi nez 1 min? jet a porovnat vysledky...)
//    caseBRUSSELATORreachability_sampling()

    //bits and odds:
    //simplifyAPolynomialExpression( "(1.0 + 2.1)*x-(20*p-1.0008)*(0.0000001-0.0000009)*x")
    
    //var reduceOutput : List<String> = simplifyAPolynomialExpression( "(p>=0.1)and(((0+0.05263157894736788)*(20.0*p+-3.0000000000000004)*(66.66666666666667*q+-1.6666666666666667)+(0+0.05263157894736788)*(20.0*p+-3.0000000000000004)*(66.66666666666667*q+-1.6666666666666667)+(0+0.05263157894736788)*(20.0*p+-3.0000000000000004)*(66.66666666666667*q+-1.6666666666666667)+(0+3.31423485147146e-17)*(20.0*p+-3.0000000000000004)+(0+0.1473684210526302)*(66.66666666666667*q+-1.6666666666666667)+0)>=1)")    
        
    //var simplPol : String = parseSimplifiedPolynomialExpressionResultFromReduceOutput( reduceOutput )
    //println( simplPol ) 
 
//    caseStudySEIR()
    //val polynoms : List<String> = listOf("x^2+1","x+1","x^3+x","0","p^2-p-2","2*x+1")
    
    //for( pol in polynoms )
/**/
    val polCA = "0.75*((-8.042949022840241e-15))*(40.0*p*p-1.4)*(40.0*p*p-1.4)+0.75*(0.0)*(5.0*p-1.4999999999999998)*(5.0*p-1.4999999999999998)+0.75*(0+0.0)*(5.0*p-1.4999999999999998)*(40.0*p*p-1.4)+0.25*1.73205080756888*(0+0.0)*(5.0*p-1.4999999999999998)+0.25*1.73205080756888*(0+(-2.563950248511606e-15))*(40.0*p*p-1.4)+0.25*(4.000000000000052)-1.0"
    val polAB = "0.75*(3.478698810492165e-15)*(40.0*p*p-1.4)*(40.0*p*p-1.4)+0.75*(7.244237072457574e-45)*(5.0*p-1.4999999999999998)*(5.0*p-1.4999999999999998)+0.75*(0+6.319236576629892e-30)*(5.0*p-1.4999999999999998)*(40.0*p*p-1.4)+0.25*1.73205080756888*(0+(-4.378095526308221e-30))*(5.0*p-1.4999999999999998)+0.25*1.73205080756888*(0+(-8.973825869790225e-15))*(40.0*p*p-1.4)+0.25*(4.00000000000006)-1.0"
    val polBA = "0.75*(0.04105090316463364)*(40.0*p*p-1.4)*(40.0*p*p-1.4)+0.75*(0.7819219641977574)*(5.0*p-1.4999999999999998)*(5.0*p-1.4999999999999998)+0.75*(0+(-0.3583809004535952))*(5.0*p-1.4999999999999998)*(40.0*p*p-1.4)+0.25*1.73205080756888*(0+(-1.029289712761777))*(5.0*p-1.4999999999999998)+0.25*1.73205080756888*(0+0.2358788926375568)*(40.0*p*p-1.4)+0.25*(0.3387285948311652)-1.0"
    val polAC = "0.75*(0.0301522689596759)*(40.0*p*p-1.4)*(40.0*p*p-1.4)+0.75*(0.7538067239578337)*(5.0*p-1.4999999999999998)*(5.0*p-1.4999999999999998)+0.75*(0+(-0.3015226895861085))*(5.0*p-1.4999999999999998)*(40.0*p*p-1.4)+0.25*1.73205080756888*(0+(-0.9139420814812506))*(5.0*p-1.4999999999999998)+0.25*1.73205080756888*(0+0.1827884162985326)*(40.0*p*p-1.4)+0.25*(0.2767978290341266)-1.0"
    println( "CA" )
    println( getRoots( polCA ) )//will print out the result
    println( "AB" )
    println( getRoots( polAB ) )//will print out the result
    println( "BA" )
    println( getRoots( polBA ) )//will print out the result
    println( "AC" )
    println( getRoots( polAC ) )//will print out the result
/**/
        
//    var examplesStrs = arrayOf("001sys","002sys","003LV","004POL","005ROS")
//    var numero = 1
//    var parRangesMin = arrayOf(-5.0, -5.0, 0.0, 0.1, 5.7)
//    var parRangesMax = arrayOf(5.0,  5.0,  3.0, 4.0, 14.0)
//        
//    for( numero in 0..4 ) {
//        var example1 : BioSystem = getBioSystemByName( examplesStrs[numero] )
//        println("Biosystem "+numero+" loaded.")
    
    //getParamSetForSequenceOfRectangles( dirori : Int, states : Array<Array<Int>>, system : BioSystem, steps : Int, method : String ) : SortedListOfDisjunctIntervals
    //var parSet : SortedListOfDisjunctIntervals = getParamSetForSequenceOfRectangles( 0, arrayOf( arrayOf(0,0), arrayOf(1,0) ), example1, 1,"QEL-Reduce")
    //println( parSet.getIntervals() )
    /*
   
    var commands : ArrayList<String> = 
    createCommandsForDrealRegularState1PminPmax( example1, arrayOf(0,0), 0,-1,0,1, 0.0f, 2.1f )
     
    println( commands )
    
    var drealResult = getResultFromDreal( commands )

    println( drealResult )*/
//      var minp : Double = parRangesMin[ numero ]
//        var maxp : Double = parRangesMax[ numero ]
//            
//        if( numero < 4 )
//            println( encodeAndCheck( minp , maxp, example1, arrayOf(0,0), 0,-1,0,1, 0.1, 10 ) )
//        else
//            println( encodeAndCheck( minp , maxp, example1, arrayOf(0,0,0), 0,-1,0,1, 0.1, 10 ) )
//    }
    /*var commandsFile : String = "/home/jfabriko/PROGRAMOVANI/rings-pokusy/sgradlem/drealInput5.smt2"
    println( getResultFromDrealFile( commandsFile ) )
    */
    
    //var parSet3d : SortedListOfDisjunctIntervals = getParamSetForSequenceOfRectangles( 0, arrayOf( arrayOf(0,0,0), arrayOf(1,0,0) ), example1, 1,"QEL-Reduce")
    //println( parSet3d.getIntervals() )
    //println( commands )
    //println( drealResult )
    //2 steps:
    //parSet = getParamSetForSequenceOfRectangles( 0, arrayOf( arrayOf(0,0), arrayOf(1,0) ), example1, 2, "QEL-Reduce" )
    //println( parSet.getIntervals() )
    
    //print hello world
    //println( App().greeting )
    
}
