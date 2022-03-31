package paramFAprototype

//maybe not used:
fun stateHash( rectangle : Array<Int>, entryDir : Int, entryOr : Int ) : String {
    var result : String = displayState( rectangle )
    result += " ${entryDir} ${entryOr}"
    return rectangleHash( rectangle )+" "+entryHash( entryDir, entryOr )
}

//will be used in StatesData as String keys
//for the maps visited and exploredPars
fun rectangleHash(rectangle : Array<Int> ) : String {
    return displayState( rectangle )
}

//will be used in OneRectangleData as String keys
//for map exploredPars
fun entryHash( entryDir : Int, entryOr : Int ) : String {
    return "${entryDir} ${entryOr}"
}

class QueueItem( rectangle : Array<Int>, entryDir : Int, entryOr : Int, 
                 slodiInput : SortedListOfDisjunctIntervalsDouble ){
    @JvmField var r : Array<Int>
    @JvmField var dir : Int
    @JvmField var or : Int
    @JvmField var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    
    init {
        r = rectangle
        dir = entryDir
        or = entryOr
        slodi = slodiInput
    }
    
    fun getR() : Array<Int> {
        return this.r
    }
    
    fun getDirOr() : Pair<Int, Int>{
        return Pair<Int,Int>(dir, or)
    }
    
    fun getDir() : Int {
        return dir
    }
    
    fun getOr() : Int {
        return or
    }
    
    fun getSlodi() : SortedListOfDisjunctIntervalsDouble {
        return slodi
    }
}

fun getDefaultOneRectangleDataValue() : OneRectangleData {
        return OneRectangleData( getBioSystemByName("001LV"), arrayOf(0,0), 0, 0, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) )
    }


fun getDefaultOneRectangleDataValuePWMA() : OneRectangleDataPWMA {
        return OneRectangleDataPWMA( getBioSystemPWMAByName("defaultPWMA"), arrayOf(0,0), 0, 0, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) )
    }

class OneRectangleData( biosystem : BioSystem, state : Array<Int>, 
                    entryDir :Int , entryOr : Int, 
                    initialSlodi : SortedListOfDisjunctIntervalsDouble ){
    
    var entrysExploredPars : MutableMap< String, SortedListOfDisjunctIntervalsDouble > = mutableMapOf()
    
    init{
        this.entrysExploredPars[entryHash(entryDir,entryOr)] = initialSlodi
    }
    
    override fun toString() : String {
        var result : String = "\n"
        for( (k,v) in entrysExploredPars ){
            result += "\tdir,or="+k+": "
            result += v.toString()+",\n"
        }
        return result
    }
    
    /* Add new slodi of explored parameters into the data about
     * given entryset.
     */
    fun add( entryDir : Int, entryOr : Int, 
             slodi : SortedListOfDisjunctIntervalsDouble ){
        if( entrysExploredPars.containsKey( entryHash(entryDir, entryOr) ) ){
            getMapItemAtKey( entryHash(entryDir, entryOr), entrysExploredPars, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) ).uniteWithOtherDisjunctList( slodi )
            //this.exploredPars[Pair(entryDir, entryOr)].uniteWithOtherDisjunctList( slodi )
        }else{
            this.entrysExploredPars[entryHash(entryDir, entryOr)] = slodi
        }
    }
    
    /* The given slodi is already explored if it is a subset of exploredPars
     * for the given entryset.
     */
    fun isExplored( entryDir : Int, entryOr : Int, 
        slodi : SortedListOfDisjunctIntervalsDouble ) : Boolean{
        if( entrysExploredPars.containsKey( entryHash(entryDir,entryOr) ) ){
            return slodi.isSubsetOf( getMapItemAtKey( entryHash(entryDir, entryOr), entrysExploredPars, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) ) )
        }else{
            return false
        }
    }
    
    /* Subtract the already explored slodi from given slodi
     * and return the difference.
     */
    fun getSubtractedSlodiForFurtherExploration( entryDir : Int, entryOr : Int, 
            slodi : SortedListOfDisjunctIntervalsDouble) : SortedListOfDisjunctIntervalsDouble {
        //println("  slodi in:"+slodi.toString())
        if( entrysExploredPars.containsKey(entryHash(entryDir,entryOr)) ){
            var slodi2 : SortedListOfDisjunctIntervalsDouble = getMapItemAtKey( entryHash(entryDir, entryOr), entrysExploredPars, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) )
            //println("  subtracting "+slodi2)
            slodi.subtractOtherDisjunctList( slodi2 )
        }
        //println("  slodi out:"+slodi.toString())
        return slodi
    }
}

class OneRectangleDataPWMA( biosystem : BioSystemPWMA, state : Array<Int>, 
                    entryDir :Int , entryOr : Int, 
                    initialSlodi : SortedListOfDisjunctIntervalsDouble ){
    
    var entrysExploredPars : MutableMap< String, SortedListOfDisjunctIntervalsDouble > = mutableMapOf()
    
    init{
        this.entrysExploredPars[entryHash(entryDir,entryOr)] = initialSlodi
    }
    
    override fun toString() : String {
        var result : String = "\n"
        for( (k,v) in entrysExploredPars ){
            result += "\tdir,or="+k+": "
            result += v.toString()+",\n"
        }
        return result
    }
    
    /* Add new slodi of explored parameters into the data about
     * given entryset.
     */
    fun add( entryDir : Int, entryOr : Int, 
             slodi : SortedListOfDisjunctIntervalsDouble ){
        if( entrysExploredPars.containsKey( entryHash(entryDir, entryOr) ) ){
            getMapItemAtKey( entryHash(entryDir, entryOr), entrysExploredPars, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) ).uniteWithOtherDisjunctList( slodi )
            //this.exploredPars[Pair(entryDir, entryOr)].uniteWithOtherDisjunctList( slodi )
        }else{
            this.entrysExploredPars[entryHash(entryDir, entryOr)] = slodi
        }
    }
    
    /* The given slodi is already explored if it is a subset of exploredPars
     * for the given entryset.
     */
    fun isExplored( entryDir : Int, entryOr : Int, 
        slodi : SortedListOfDisjunctIntervalsDouble ) : Boolean{
        if( entrysExploredPars.containsKey( entryHash(entryDir,entryOr) ) ){
            return slodi.isSubsetOf( getMapItemAtKey( entryHash(entryDir, entryOr), entrysExploredPars, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) ) )
        }else{
            return false
        }
    }
    
    /* Subtract the already explored slodi from given slodi
     * and return the difference.
     */
    fun getSubtractedSlodiForFurtherExploration( entryDir : Int, entryOr : Int, 
            slodi : SortedListOfDisjunctIntervalsDouble) : SortedListOfDisjunctIntervalsDouble {
        //println("  slodi in:"+slodi.toString())
        if( entrysExploredPars.containsKey(entryHash(entryDir,entryOr)) ){
            var slodi2 : SortedListOfDisjunctIntervalsDouble = getMapItemAtKey( entryHash(entryDir, entryOr), entrysExploredPars, SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() ) )
            //println("  subtracting "+slodi2)
            slodi.subtractOtherDisjunctList( slodi2 )
        }
        //println("  slodi out:"+slodi.toString())
        return slodi
    }
}

class StatesData( biosystem : BioSystem, initialState : Array<Int>, 
                  initialParSlodi: SortedListOfDisjunctIntervalsDouble,
                  dir : Int, ori : Int ){
    //visited rectangles
    var visited : MutableMap< String, Boolean > = mutableMapOf< String, Boolean >() 
    var rectanglesExploredPars : MutableMap< String, OneRectangleData > = mutableMapOf< String, OneRectangleData>()
    
    init {
        this.visited = mutableMapOf< String, Boolean >()
        this.visited[ rectangleHash(initialState) ] = true
        this.rectanglesExploredPars[ rectangleHash(initialState) ] = OneRectangleData( biosystem, initialState, dir, ori, initialParSlodi )
    }
    
    override fun toString() : String {
        var result : String = "\n"
        //per visited states
        for( (k,v) in rectanglesExploredPars )
        {
            //entrysets and explored parsets
            result += "Rectangle: "+ k 
            result += ", Data: "+v.toString()
        }
        return result
    }
    
    fun visited( state : Array<Int> ) : Boolean {
        return getMapItemAtKey( rectangleHash(state), visited, false )
    }
    
    fun markAsVisited( state : Array<Int> ){
        this.visited[ rectangleHash(state) ] = true
    }
    
    fun getOneRectangleParData( state : Array<Int> ) : OneRectangleData {
        return getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValue() )
    }
    
    fun isSlodiAlreadyExplored( state : Array<Int> , entryDir : Int, entryOr : Int, 
                                slodi : SortedListOfDisjunctIntervalsDouble ) : Boolean {
        return getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValue() ).isExplored( entryDir, entryOr, slodi )
    }
    
    fun addSlodiToExplored( biosystem : BioSystem, state : Array<Int> , entryDir : Int, entryOr : Int, 
                            slodi : SortedListOfDisjunctIntervalsDouble ) {
        if( rectanglesExploredPars.containsKey(rectangleHash(state)) ){
            getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValue() ).add( entryDir, entryOr, slodi )
        }else{
            rectanglesExploredPars[ rectangleHash(state) ] = OneRectangleData( biosystem, state, entryDir, entryOr, slodi)
        }
    }
    
    fun getSlodiToExploreFurther( state : Array<Int> , entryDir : Int, entryOr : Int, 
                                  slodi : SortedListOfDisjunctIntervalsDouble ) : SortedListOfDisjunctIntervalsDouble {
        return getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValue() ).getSubtractedSlodiForFurtherExploration( entryDir, entryOr, slodi )
    }
    
}

class StatesDataPWMA( biosystem : BioSystemPWMA, initialState : Array<Int>, 
                  initialParSlodi: SortedListOfDisjunctIntervalsDouble,
                  dir : Int, ori : Int ){
    //visited rectangles
    var visited : MutableMap< String, Boolean > = mutableMapOf< String, Boolean >() 
    var rectanglesExploredPars : MutableMap< String, OneRectangleDataPWMA > = mutableMapOf< String, OneRectangleDataPWMA>()
    
    init {
        this.visited = mutableMapOf< String, Boolean >()
        this.visited[ rectangleHash(initialState) ] = true
        this.rectanglesExploredPars[ rectangleHash(initialState) ] = OneRectangleDataPWMA( biosystem, initialState, dir, ori, initialParSlodi )
    }
    
    override fun toString() : String {
        var result : String = "\n"
        //per visited states
        for( (k,v) in rectanglesExploredPars )
        {
            //entrysets and explored parsets
            result += "Rectangle: "+ k 
            result += ", Data: "+v.toString()
        }
        return result
    }
    
    fun visited( state : Array<Int> ) : Boolean {
        return getMapItemAtKey( rectangleHash(state), visited, false )
    }
    
    fun markAsVisited( state : Array<Int> ){
        this.visited[ rectangleHash(state) ] = true
    }
    
    fun getOneRectangleParData( state : Array<Int> ) : OneRectangleDataPWMA {
        return getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValuePWMA() )
    }
    
    fun isSlodiAlreadyExplored( state : Array<Int> , entryDir : Int, entryOr : Int, 
                                slodi : SortedListOfDisjunctIntervalsDouble ) : Boolean {
        return getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValuePWMA() ).isExplored( entryDir, entryOr, slodi )
    }
    
    fun addSlodiToExplored( biosystem : BioSystemPWMA, state : Array<Int> , entryDir : Int, entryOr : Int, 
                            slodi : SortedListOfDisjunctIntervalsDouble ) {
        if( rectanglesExploredPars.containsKey(rectangleHash(state)) ){
            getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValuePWMA() ).add( entryDir, entryOr, slodi )
        }else{
            rectanglesExploredPars[ rectangleHash(state) ] = OneRectangleDataPWMA( biosystem, state, entryDir, entryOr, slodi)
        }
    }
    
    fun getSlodiToExploreFurther( state : Array<Int> , entryDir : Int, entryOr : Int, 
                                  slodi : SortedListOfDisjunctIntervalsDouble ) : SortedListOfDisjunctIntervalsDouble {
        return getMapItemAtKey( rectangleHash(state), rectanglesExploredPars, getDefaultOneRectangleDataValuePWMA() ).getSubtractedSlodiForFurtherExploration( entryDir, entryOr, slodi )
    }
    
}

/* Class for representing the constraints (there will appear a list of such)
 * defining the region whose reachability we want to analyse.
 * one constraint is on one variable of the system, 
 * it states that the variable is GreaterOrEqual or LessOrEqual
 * (true / false ge)
 * than the value (value).
 */
class ConstraintReachable( variable : Int, ge : Boolean, value : Double ) {
                
    @JvmField var variable : Int = 0
    @JvmField var ge : Boolean = false
    @JvmField var value : Double = 0.0
    
    init {
        this.variable = variable
        this.ge = ge
        this.value = value
    }

    override fun toString() : String {
        //var resultStr = biosystem.getVarStrings()[variable] 
        var resultStr = "$variable-th variable"
        if( ge ) resultStr += ">=" else resultStr += "<="
        resultStr += value.toString()
        return resultStr
    }
    
    fun hasIntersectionWith( state : Array<Int>, biosystem : BioSystem ) : Boolean {
        val lower : Double = biosystem.getTres(variable, state[variable] ).toDouble()
        val upper : Double = biosystem.getTres(variable, state[variable] + 1).toDouble()
        
        if( ge ){
            return upper >= value //contact on facet
        } else {
            return lower <= value //contact on facet
        }
    }
    
    fun hasIntersectionWith( state : Array<Int>, biosystem : BioSystemPWMA ) : Boolean {
        val lower : Double = biosystem.getTres(variable, state[variable] ).toDouble()
        val upper : Double = biosystem.getTres(variable, state[variable] + 1).toDouble()
        
        if( ge ){
            return upper >= value //contact on facet
        } else {
            return lower <= value //contact on facet
        }
    }
    
    fun hasSharpIntersectionWith( state : Array<Int>, biosystem : BioSystem ) : Boolean {
        val lower : Double = biosystem.getTres(variable, state[variable] ).toDouble()
        val upper : Double = biosystem.getTres(variable, state[variable] + 1).toDouble()
        
        if( ge ){
            return upper > value //contact inside rectangle
        } else {
            return lower < value //contact inside rectangle
        }
    }
}

fun intersectionNonemptyListOfConstraints( stateA : Array<Int>, constraintsB : List<ConstraintReachable>, biosystem : BioSystem ) : Boolean {
    var result : Boolean = true
    for( constraint in constraintsB ){
        result = result && constraint.hasIntersectionWith( stateA, biosystem )
    }
    return result
}

fun intersectionNonemptyListOfConstraintsPWMA( stateA : Array<Int>, constraintsB : List<ConstraintReachable>, biosystem : BioSystemPWMA ) : Boolean {
    var result : Boolean = true
    for( constraint in constraintsB ){
        result = result && constraint.hasIntersectionWith( stateA, biosystem )
    }
    return result
}


/* Template function for getting a value with given key from a map,
 * if the key is not present, a default value is returned. 
 */
fun <K,T> getMapItemAtKey( givenKey : K, givenMap : Map<K,T>, defaultValue : T ) : T{
    var value : T = defaultValue
    val questionableVal : T? = givenMap[ givenKey ]
    questionableVal?.let {
        value = questionableVal
    }
    return value
}
