package paramFAprototype

import java.io.File

fun getResultFromDreal(commandsForDreal: ArrayList<String>): List<String> {
    val tempFile = File.createTempFile("input", ".smt2")
    tempFile.writeText(commandsForDreal.joinToString(separator = "\n"))
    val process = Runtime.getRuntime().exec(arrayOf("/home/jfabriko/PROGRAMOVANI/dReal-3.16.06.02-linux/bin/dReal", "--visualize", 
        tempFile.absolutePath))
    val output1 = process.inputStream.bufferedReader().readLines()

    return output1 //digResultFromReduceOutput(output1)
}

/*
 * filename should be an absolute path to dreal commands
 */
fun getResultFromDrealFile( filename : String) : List<String> {
    val process = Runtime.getRuntime().exec(arrayOf("/home/jfabriko/PROGRAMOVANI/dReal-3.16.06.02-linux/bin/dReal", "--visualize", 
        filename ))
    val output1 = process.inputStream.bufferedReader().readLines()

    return output1
}

/* DReach does not cooperate well with this project yet...*/
fun getResultFromDreach(commandsForDreach: ArrayList<String>): List<String> {
    val tempFile = File.createTempFile("input", ".drh")
    tempFile.writeText(commandsForDreach.joinToString(separator = "\n"))
    val process = Runtime.getRuntime().exec(arrayOf("/home/jfabriko/PROGRAMOVANI/dReal-3.16.06.02-linux/bin/dReach",
        tempFile.absolutePath))
    val output1 = process.inputStream.bufferedReader().readLines()

    return output1 //digResultFromReduceOutput(output1)
}
/*
 * pmin, pmax Float
 */
fun createCommandsForDrealRegularState1PminPmax( biosystem : BioSystem, state : Array<Int>, dir1: Int, ori1 : Int, dir2 : Int, ori2 : Int, pminF: Float, pmaxF : Float ) : ArrayList<String> {
    var dim : Int = biosystem.getDim()
    var maxT : String = biosystem.getMaxT()
    var parCount : Int = biosystem.getParamCount()
    
    val parStrs : Array<String> = biosystem.getParStrings()
    val varStrs : Array<String> = biosystem.getVarStrings()

    var allVars : MutableList<String> = mutableListOf<String>()
    allVars.addAll( parStrs )
    allVars.addAll( varStrs )
    var allVars_0 : MutableList<String> = mutableListOf<String>()
    var allVars_t : MutableList<String> = mutableListOf<String>()
    for( v in allVars ){
        allVars_0.add( v+"_0")
        allVars_t.add( v+"_t")
    }
    
    var pmin : String = pminF.toString()
    var pmax : String = pmaxF.toString()
    
    var equations : MutableMap<String,String> = mutableMapOf()
    equations.put(parStrs[0], "0.0")
    equations.putAll( biosystem.getPrefixEquations() )
        
    
    val command = ArrayList<String>().apply {
        // beginning
        add("(set-logic QF_NRA_ODE)")
        // now add params
        for( p in parStrs){
                declareVariable(p, pmin, pmax)
                declareVariable(p + "_0", pmin, pmax)
                declareVariable(p + "_t", pmin, pmax)
        }
        // now add vars
        for( i in 0..(dim-1) ){
                var v : String = varStrs[i]
                var vmin : String = biosystem.getMinTres( i )
                var vmax : String = biosystem.getMaxTres( i )
                //min max treshold in system
                declareVariable(v, vmin, vmax)
                //first facet condition
                var vfacetmin : String = biosystem.getMinOnFacet( i, state, dir1, ori1 )
                var vfacetmax : String = biosystem.getMaxOnFacet( i, state, dir1, ori1 )
                declareVariable(v + "_0", vfacetmin, vfacetmax)
                //min max treshold in system
                declareVariable(v + "_t", vmin, vmax)
        }
        // now add time
        declareVariable("time_0", "0.0", maxT)
        // define the flow, set d par /dt = 0.0
        declareODE( equations )//ode flow
        add(assertStr(conjunction(
                getDerivativePointsInsideCondition( biosystem, state, dir1, ori1, "_0" ),
                integral0( allVars_t, allVars_0, "time_0"),//integrals
                forallT(str(0.0), "time_0", getInRectangleCondition( biosystem, state ) ),
                getOutsideFacetCondition( biosystem, state, dir2, ori2, "_t"),//goal _t
                getDerivativePointsOutsideCondition( biosystem, state, dir2, ori2, "_t" )
            )))
        // end
        add("(check-sat)")
        add("(exit)")
    }
    
    return command
}


/*
 * _0
 * rectangular abstraction condition 
 * on the first facet
 */
fun getDerivativePointsInsideCondition( biosystem : BioSystem, state : Array<Int>, dir1 : Int, ori1 : Int, suffix : String ) : String {
    var result : String =""
    //rectangular condition at x_t deriv pointing outside
    var dim : Int = biosystem.getDim()
    val varStrs : Array<String> = biosystem.getVarStrings()
    var deriv : String = ""
    var der : String? = (biosystem.getPrefixEquations())[varStrs[dir1]]
    der?.let {
        deriv = der
    }
    for( i in 0..(dim-1) ) {
        deriv = deriv.replace( varStrs[i], varStrs[i]+suffix )
    }
    //TODO vic parametru
    deriv = deriv.replace( "p", "p"+suffix )
    if(ori1 > 0) {//right end of rectangle
        result = deriv.le("0.0")
    } else {//left end of rectangle
        result = deriv.ge("0.0")
    }
    return result
}

/*
 * _t
 * rectangular abstraction condition 
 * on the second facet
 */
fun getDerivativePointsOutsideCondition( biosystem : BioSystem, state : Array<Int>, dir2 : Int, ori2 : Int, suffix : String ) : String {
    var result : String =""
    //rectangular condition at x_t deriv pointing outside
    var dim : Int = biosystem.getDim()
    val varStrs : Array<String> = biosystem.getVarStrings()
    var deriv : String = ""
    var der : String? = (biosystem.getPrefixEquations())[varStrs[dir2]]
    der?.let {
        deriv = der
    }
    for( i in 0..(dim-1) ) {
        deriv = deriv.replace( varStrs[i], varStrs[i]+suffix )
    }
    //TODO vic parametru
    deriv = deriv.replace( "p", "p"+suffix )
    if(ori2 > 0) {//right end of rectangle
        result = deriv.ge("0.0")
    } else {//left end of rectangle
        result = deriv.le("0.0")
    }
    return result
}

/*
 * parameterSpace is an interval [a,b]
 */
fun createCommandsForDrealRegularState1Interval( biosystem : BioSystem, state : Array<Int>, dirori1 : Int, dirori2 : Int, parameterSpace : Interval ) : ArrayList<String> {
    var dim : Int = biosystem.getDim()
    var maxT : String = biosystem.getMaxT()
    var parCount : Int = biosystem.getParamCount()
    
    var dir1 : Int = getDir( dirori1 ); var ori1 : Int = getOri( dirori1 )
    var dir2 : Int = getDir( dirori2 ); var ori2 : Int = getOri( dirori2 )
    
    val parStrs : Array<String> = biosystem.getParStrings()
    val varStrs : Array<String> = biosystem.getVarStrings()

    var allVars : MutableList<String> = mutableListOf<String>()
    allVars.addAll( parStrs )
    allVars.addAll( varStrs )
    var allVars_0 : MutableList<String> = mutableListOf<String>()
    var allVars_t : MutableList<String> = mutableListOf<String>()
    for( v in allVars ){
        allVars_0.add( v+"_0_0")
        allVars_t.add( v+"_0_t")
    }
    
    var pmin : String = parameterSpace.getLe().toString()
    var pmax : String = parameterSpace.getRi().toString()
    
    var equations : MutableMap<String,String> = mutableMapOf()
    equations.putAll( biosystem.getPrefixEquations() )
    equations.put(parStrs[0], "0.0")
    
    
    val command = ArrayList<String>().apply {
        // beginning
        add("(set-logic QF_NRA_ODE)")
        // now add params
        for( p in parStrs){
                declareVariable(p, pmin, pmax)
                declareVariable(p + "_0_0", pmin, pmax)
                declareVariable(p + "_0_1", pmin, pmax)
                declareVariable(p + "_0_t", pmin, pmax)
        }
        // now add vars PMIN PMAX SPATNE
        for( v in varStrs){
                declareVariable(v, pmin, pmax)
                declareVariable(v + "_0_0", pmin, pmax)
                declareVariable(v + "_0_1", pmin, pmax)
                declareVariable(v + "_0_t", pmin, pmax)
        }
        // now add time
        declareVariable("time_0", "0.0", maxT)
        declareVariable("time_1", "0.0", maxT)
        // define the flow, set d par /dt = 0.0
        declareODE( equations )//ode flow
        add(assertStr(conjunction(
                getParameterSpaceCondition1Par( parStrs[0], pmin, pmax, "_0_0" ),//par_0_0
                getEntryFacetCondition( biosystem, state, dir1, ori1 ),//var_0_0
                getParameterSpaceCondition1Par( parStrs[0], pmin, pmax, "_0_t" ),//par_0_t
                getExitFacetCondition( biosystem, state, dir2, ori2 ),//var_0_t
                integral0( allVars_t, allVars_0, "time_0"),//integrals
                forallT(str(0.0), maxT, conjunction(getParameterSpaceCondition1Par( parStrs[0], pmin, pmax, "_0_t" ), 
                                                    getInRectangleCondition( biosystem, state ) ))
            )))
        // end
        add("(check-sat)")
        add("(exit)")
    }
    
    return command
}

/*
 * Now just 1 parameter in an interval
 * we do just closed intervals
 * This  will be useful once we do more than one parameter
 * TODO
 */
fun getParameterSpaceCondition1Par( parStr : String, pmin : String, pmax : String, suffix : String ) : String {
    return conjunction( (parStr+suffix).ge( pmin ), (parStr+suffix).le( pmax ) )
}

fun getParameterSpaceCondition() : String {
    var result : String = ""
        
    return result
}



/*
 * Bounds for first facet
 * on x_00 .. x_n0
 * and derivative \cdot orientation condition from RA
 */    
fun getEntryFacetCondition( system : BioSystem, state : Array<Int>, dir1 : Int, ori1 : Int ) : String {
    var dim : Int = system.getDim()
    //there will be 2*dim-1 conditions on being inside facet
    //and 1 condition regarding the sign of the derivative from RA
    var bounds : Array<String> = Array<String>( 2* dim ){i -> ""}
    
    val varStrs : Array<String> = system.getVarStrings()
    val suffix : String = "_0_0"
    var j : Int = 0 //which bound is current
    for( i in 0..(dim-1) ) {
        if( i == dir1 ) {
            if( ori1 > 0 ) { //right end of rectangle
                bounds[j] = (varStrs[i]+suffix).eq( system.getTres( i, state[i] + 1 ) )
            } else { //ori1 < 0, left end of rectangle
                bounds[j] = (varStrs[i]+suffix).eq( system.getTres( i, state[i] ) )
            }
            j++
        } else {
            bounds[j] = (varStrs[i]+suffix).ge( system.getTres( i, state[i] ) )
            j++
            bounds[j] = (varStrs[i]+suffix).le( system.getTres( i, state[i] + 1 ) )
            j++
        }
    }
    //rectangular condition at x_0_0 deriv pointing inside
    var deriv : String = ""
    var der : String? = (system.getPrefixEquations())[varStrs[dir1]]
    der?.let {
        deriv = der
    }
    
    for( i in 0..(dim-1) ) {
        deriv = deriv.replace( varStrs[i], varStrs[i]+suffix )
    }
    if(ori1 > 0) {//right end of rectangle
        bounds[j] = deriv.le("0.0")
    } else {//left end of rectangle
        bounds[j] = deriv.ge("0.0")
    }
    return conjunction( *bounds )
}

/*
 * exploit the fact that the polytopes are rectangles and the trajectory
 * is in the rectangle for whole time of integration
 * it is sufficient to check that the dir2 th variable has value outside
 * the rectangle on the right side of the rectangle
 */
fun  getOutsideFacetCondition( biosystem : BioSystem, state : Array<Int>, dir2 : Int, ori2 : Int, suffix : String ) : String {
    var result : String = ""
    val varStrs : Array<String> = biosystem.getVarStrings()

    if( ori2 > 0 ) { //right end of rectangle
                result = (varStrs[dir2]+suffix).ge( biosystem.getTres( dir2, state[dir2] + 1 ) )
    } else { //ori2 < 0, left end of rectangle
                result = (varStrs[dir2]+suffix).le( biosystem.getTres( dir2, state[dir2] ) )
    }
    return result
} 

/*
 * Bounds for second facet
 * on x_01 .. x_n1
 * and derivative \cdot orientation condition from RA
 */ 
fun getExitFacetCondition( system : BioSystem, state : Array<Int>, dir2 : Int, ori2 : Int ) : String {
    var dim : Int = system.getDim()
    //there will be 2*dim-1 conditions on being inside facet
    //and 1 condition regarding the sign of the derivative from RA
    var bounds : Array<String> = Array<String>( 2* dim ){i -> ""}
    
    val varStrs : Array<String> = system.getVarStrings()
    val suffix : String = "_0_t"
    var j : Int = 0 //which bound is current
    for( i in 0..(dim-1) ) {
        if( i == dir2 ) {
            if( ori2 > 0 ) { //right end of rectangle
                bounds[j] = (varStrs[i]+suffix).eq( system.getTres( i, state[i] + 1 ) )
            } else { //ori2 < 0, left end of rectangle
                bounds[j] = (varStrs[i]+suffix).eq( system.getTres( i, state[i] ) )
            }
            j++
        } else {
            bounds[j] = (varStrs[i]+suffix).ge( system.getTres( i, state[i] ) )
            j++
            bounds[j] = (varStrs[i]+suffix).le( system.getTres( i, state[i] + 1 ) )
            j++
        }
    }
    //rectangular condition at x_0_0 deriv pointing outside
    var deriv : String = ""
    var der : String? = (system.getPrefixEquations())[varStrs[dir2]]
    der?.let {
        deriv = der
    }
    for( i in 0..(dim-1) ) {
        deriv = deriv.replace( varStrs[i], varStrs[i]+suffix )
    }
    if(ori2 > 0) {//right end of rectangle
        bounds[j] = deriv.ge("0.0")
    } else {//left end of rectangle
        bounds[j] = deriv.le("0.0")
    }
    return conjunction( *bounds )
}

/*
 * Bounds for the whole rectangle
 * on x_0t .. x_nt
 */ 
fun getInRectangleCondition( system : BioSystem, state : Array<Int> ) : String {
    var dim : Int = system.getDim()
    //there will be 2*dim conditions on being in the rectangle 
    var bounds : Array<String> = Array<String>( 2* dim ){i -> ""}
    
    val varStrs : Array<String> = system.getVarStrings()
    val suffix : String = "_t" //"_0_t"
    var j : Int = 0 //which bound is current
    for( i in 0..(dim-1) ) {
        bounds[j] = (varStrs[i]+suffix).ge( system.getTres( i, state[i] ) )
        j++
        bounds[j] = (varStrs[i]+suffix).le( system.getTres( i, state[i] + 1 ) )
        j++
    }
    
    return conjunction( *bounds )
}


fun MutableList<String>.declareVariable(name: String, min: String, max: String) {
    add("(declare-fun $name () Real [$min, $max])")
}

fun MutableList<String>.declareVariable(name: String, min: Double, max: Double) {
    add("(declare-fun $name () Real [$min, $max])")
}

fun MutableList<String>.declareODE(equations: Map<String, String>) {
    add("(define-ode flow_1 ${
        equations.entries.joinToString(prefix = "(", postfix = ")", separator = " ") {
            "(= d/dt[${it.key}] ${it.value})"
        }
    })")
}

fun not(clause: String): String {
    return "(not $clause)"
}

fun conjunction(vararg clauses: String): String {
    return clauses.joinToString(separator = " ", prefix = "(and ", postfix = ")")
}

fun disjunction(vararg clauses: String): String {
    return clauses.joinToString(separator = " ", prefix = "(or ", postfix = ")")
}

fun integral0(variables: List<String>, initial: List<String>, time: String): String {
    return variables.joinToString(prefix = "[", postfix = "]", separator = " ") eq "(integral 0. $time ${initial.joinToString(prefix = "[", postfix = "]", separator = " ")} flow_1)"
}

fun forallT(time0: String, time1: String, condition: String): String {
    return "(forall_t 1 [$time0 $time1] $condition)"
}

fun inInterval(variable: String, from: Double, to: Double) = conjunction(variable ge str(from), variable le str(to))

infix fun String.ge(other: String) = "(>= $this $other)"
infix fun String.le(other: String) = "(<= $this $other)"
infix fun String.eq(other: String) = "(= $this $other)"
infix fun String.add(other: String) = "(+ $this $other)"
infix fun String.rem(other: String) = "(- $this $other)"
infix fun String.mul(other: String) = "(* $this $other)"
infix fun String.div(other: String) = "(/ $this $other)"
infix fun String.pow(other: String) = "(^ $this $other)"

fun assertStr(f: String) = "(assert $f)"

fun str(d: Double) = d.toString()
