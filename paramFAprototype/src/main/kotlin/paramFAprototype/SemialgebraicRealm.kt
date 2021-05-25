package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

val QZERO : NumQ = Q.mk(BigInteger.ZERO,BigInteger.ONE)
val QMINUSONE : NumQ = Q.getNegativeOne()
val QONE : NumQ = Q.mk(BigInteger.ONE,BigInteger.ONE)
val QTWO : NumQ = Q.mk(BigInteger.TWO,BigInteger.ONE)
val QHALF : NumQ = Q.mk(BigInteger.ONE,BigInteger.TWO)

//class interval [()] in R*, 
//infinity..corresponds to left/right null endpoint
  //hasNonemptyIntersectionWith(interval), 
  //isIntersectionEmptyWith(interval), intersectWith(interval), 
  //isUnitableWith(interval), uniteWith(interval)
class Interval( left: NumQ, leftClosed: Boolean, leftInf: Boolean,
                right: NumQ, rightClosed: Boolean, rightInf: Boolean ) {
                
    @JvmField var le : NumQ = QZERO
    @JvmField var ri : NumQ = QZERO
    @JvmField var lClo : Boolean = false
    @JvmField var rClo : Boolean = false
    @JvmField var lInf : Boolean = false
    @JvmField var rInf : Boolean = false
    
    init {
        this.lClo = leftClosed
        this.rClo = rightClosed
        this.lInf = leftInf
        this.rInf = rightInf
        this.le = left
        this.ri = right
        //set empty to (0,0):
        if( !this.lInf && !this.rInf ) {
            val lrcomparison = left.compareTo(right)
            if( lrcomparison > 0 ) this.setEmpty()
            if( (lrcomparison == 0) && (!leftClosed || !rightClosed) ) this.setEmpty()
        }
    }
    
    override fun toString() : String {
        var resultStr : String = ""
        if( this.lInf ) {
            resultStr += "(-R"
        } else if( this.lClo ) {
            resultStr += "["
            resultStr += this.le.toString()
        } else{
            resultStr += "("
            resultStr += this.le.toString()
        }
        resultStr += ","
        if( this.rInf ) {
            resultStr += "+R)"
        } else if( this.rClo ) {
            resultStr += this.ri.toString()
            resultStr += "]"
        } else{
            resultStr += this.ri.toString()
            resultStr += ")"
        }
        return resultStr
    }
    
    fun getLe(): NumQ = this.le
    fun getRi(): NumQ = this.ri
    
    fun simpleSetLe( left: NumQ) { this.le = left }
    fun simpleSetRi( right: NumQ) { this.ri = right }
    fun simpleSetLClo( lc : Boolean ) { this.lClo = lc }
    fun simpleSetRClo( rc : Boolean) { this.rClo = rc }
    fun simpleSetLInf( li : Boolean ) { this.lInf = li }
    fun simpleSetRInf( ri : Boolean) { this.rInf = ri }
        
    fun setLe(left: NumQ) { this.le = left; this.setRInf( false ) }
    fun setRi(right: NumQ) {this.ri = right; this.setLInf( false )}
    
    fun isLClo(): Boolean = this.lClo
    fun isRClo(): Boolean = this.rClo
    fun setLClo( lc : Boolean ) { this.lClo = lc }
    fun setRClo( rc : Boolean ) { this.rClo = rc }
    fun setLValClo(left : NumQ, lc : Boolean ) { 
        this.setLe(left); this.setLClo( lc ); this.setLInf(false) 
        //set empty to (0,0):
        if( !this.lInf && !this.rInf ) {
            val lrcomparison = this.le.compareTo(this.ri)
            if( lrcomparison > 0 ) this.setEmpty()
            if( (lrcomparison == 0) && (!lc || !this.rClo) ) this.setEmpty()
        }
    }
    fun setRValClo(right : NumQ, rc : Boolean ) { 
        this.setRi(right); this.setRClo( rc ); this.setRInf(false) 
        //set empty to (0,0):
        if( !this.lInf && !this.rInf ) {
            val lrcomparison = this.le.compareTo(this.ri)
            if( lrcomparison > 0 ) this.setEmpty()
            if( (lrcomparison == 0) && (!this.lClo || !rc) ) this.setEmpty()
        }
    }

    fun setEmpty(){
        this.le = QZERO
        this.ri = QZERO
        this.lClo = false
        this.rClo = false
        this.lInf = false
        this.rInf = false   
    }

    fun isLInf(): Boolean = this.lInf
    fun isRInf(): Boolean = this.rInf
    fun setLInf( lI : Boolean ) { 
        this.lInf = lI 
        this.lClo = if (lI) false else this.lClo 
        this.le = if (lI) QZERO else this.le
    }
    fun setRInf( rI : Boolean ) { 
        this.rInf = rI
        this.rClo = if( rI ) false else this.rClo 
        this.ri = if( rI ) QZERO else this.ri
    }    

    fun isLOpen(): Boolean = !this.lClo
    fun isROpen(): Boolean = !this.rClo
    
    fun isOpen(): Boolean = !this.lClo && !this.rClo
    fun isClosed(): Boolean = this.lClo && this.rClo
    fun isFinite(): Boolean = !this.lInf || !this.rInf
    
    //positive length is computed only for finite intervals
    //infinite get -1
    fun length(): NumQ {
        if( !this.lInf && !this.rInf )
            return this.ri.subtract(this.le)
        else
            return Q.mk(BigInteger.NEGATIVE_ONE,BigInteger.ONE)
    }
    
    fun isEmpty(): Boolean {
        //the moment an interval becomes empty, we set le,ri to 0s
        //andl closed to false, and infinite to false
        return ( this.le.isZero() && this.ri.isZero() && !this.lClo && !this.rClo && !this.lInf && !this.rInf)
    }
    
    fun isOnePoint() : Boolean {
        return (this.isFinite() && this.isClosed() && (this.le.compareTo(this.ri) == 0) )
    }
    
    /*compareLeft
     * return <0 if my left is smaller than others left
     * return =0 if lefts are the same
     * return >0 if my left is bigger than others left
     */
    fun compareLeft(oth : Interval) : Int {
        //this - other
        if( this.isLInf() ){//-inf
            if( oth.isLInf() ){
                return 0
            } else {
                return -1
            }
        }else{
            if( oth.isLInf() ){//-inf
                return 1
            } else {
                return this.le.compareTo( oth.getLe() ) 
            }
        }
    }
    
    
    /*compareRight
     * return <0 if my right is smaller than others right
     * return =0 if rights are the same
     * return >0 if my right is bigger than others right
     */
    fun compareRight(oth : Interval) : Int {
        //this - other
        if( this.isRInf() ){
            if( oth.isRInf() ){//+inf
                return 0
            } else {
                return 1
            }
        }else{
            if( oth.isRInf() ){//+inf
                return -1
            } else {
                return this.ri.compareTo( oth.getRi() ) 
            }
        }
    }
    
    fun compareThisLOtherR( oth : Interval ) : Int {
        //this - other
        if( this.isLInf() ){//-inf
            if( oth.isRInf() ){//+inf
                return -1
            } else {
                return -1
            }
        }else{
            if( oth.isRInf() ){//+inf
                return -1
            } else {
                return this.le.compareTo( oth.getRi() ) 
            }
        }
    }
    
    fun compareThisROtherL( oth : Interval ) : Int {
        //this - other
        if( this.isRInf() ){//+inf
            if( oth.isLInf() ){//-inf
                return 1
            } else {
                return 1
            }
        }else{
            if( oth.isLInf() ){//-inf
                return 1
            } else {
                return this.ri.compareTo( oth.getLe() ) 
            }
        }
    }
    
    //intersection of two intervals this and oth
    fun intersectWith( oth : Interval ) : Interval {
        if( this.hasNonemptyIntersectionWith( oth ) ){
            var left : NumQ
            var right : NumQ
            var leftClosed : Boolean
            var rightClosed : Boolean
            var leftInf : Boolean
            var rightInf : Boolean
            
            val thisMinusOthL = this.compareLeft( oth )
            if( thisMinusOthL >= 0 ){
                //this left
                if( this.isLInf() ){ left = QZERO; leftClosed = false; leftInf = true }
                else { left = this.le; leftClosed = this.lClo; leftInf = false }
            } else {
                //other left
                if( oth.isLInf() ){ left = QZERO; leftClosed = false; leftInf = true }
                else { left = oth.le; leftClosed = oth.lClo; leftInf = false }
            }
            
            val thisMinusOthR = this.compareRight( oth )
            if( thisMinusOthR <= 0 ){
                //this right
                if( this.isRInf() ){ right = QZERO; rightClosed = false; rightInf = true }
                else { right = this.ri; rightClosed = this.rClo; rightInf = false }
            } else {
                //other right
                if( oth.isRInf() ){ right = QZERO; rightClosed = false; rightInf = true }
                else { right = oth.ri; rightClosed = oth.rClo; rightInf = false }
            }
            
            return Interval( left, leftClosed, leftInf, right, rightClosed, rightInf )
            
        } else {
            return Interval( QZERO, false, false, QZERO, false, false )    
        }
    }
    
    /* {a,b},{c,d} has intersection?
     * 
     */
    fun hasNonemptyIntersectionWith(oth : Interval) : Boolean{
        if( !this.isEmpty() && !oth.isEmpty() ){
            //none of the intervals is empty
            val thisMinusOtherL = this.compareLeft(oth)
            val thisMinusOtherR = this.compareRight(oth)
            if( thisMinusOtherL <= 0 && thisMinusOtherR <= 0) { //a<=c,b<=d
                val bccompare = this.compareThisROtherL( oth )
                if( bccompare < 0){ //b<c no intersection
                    return false
                }else if( bccompare == 0 ){ //b=c
                    return ( this.rClo && oth.isLClo() ) // b][c true, else false
                }else{ //b>c overlapping 
                    return true
                }  
            }else if( thisMinusOtherL >= 0 && thisMinusOtherR >= 0 ) {//a>=c,b>=d
                val adcompare = this.compareThisLOtherR( oth )
                if( adcompare < 0){// a<d overlapping
                    return true
                }else if( adcompare == 0 ){//a=d
                    return ( this.lClo && oth.isRClo() ) // d][a true, else false
                }else{
                    return false// a>d no intersection
                }
            }else{
                return true                
            }
         } else {
            //either one of the intervals is empty
            return false
        }    
    }
    
    //meant for finite points only
    fun isPointInside( point : NumQ ) : Boolean {
        if( this.isEmpty() ) return false
        else {
            var fromLeft : Boolean
            var fromRight : Boolean
            //is point bounded from left by interval?
            if( this.isLInf()) { 
                fromLeft = true
            } else {
                val pointMinusLeft : Int = point.compareTo( this.le )
                if( pointMinusLeft > 0 ) {
                    fromLeft = true
                } else if( pointMinusLeft == 0 ) {
                    fromLeft = this.isLClo()
                } else {
                    fromLeft = false
                }
            }
            //is point bounded from right by interval?
            if( this.isRInf()) { 
                fromRight = true
            } else {
                val rightMinusPoint : Int = this.ri.compareTo(point)
                if( rightMinusPoint > 0 ) {
                    fromRight = true
                } else if( rightMinusPoint == 0 ) {
                    fromRight = this.isRClo()
                } else {
                    fromRight = false
                }
            }
            return fromLeft && fromRight
        }
    }
}

/* Helper class intersection list intersects one interval,
 * optional parameter startIndex inicates the index in the list
 * from which to start (0 by default).
 * Assumption: the list is a sorted list of disjunct intervals
 */
class IntersectionOfListAndInterval(@JvmField var listI : MutableList<Interval>,
    @JvmField var interval: Interval, @JvmField var startIndex : Int = 0){
        @JvmField var foundFirst = false
        @JvmField var firstIndex : Int = -1
        @JvmField var firstIntersecting : Interval = Interval(QZERO, false, false, QZERO, false, false)
        @JvmField var lastIntersecting : Interval = Interval(QZERO, false, false, QZERO, false, false)
        @JvmField var intersectingList : MutableList<Interval> = mutableListOf<Interval>()
        @JvmField var intersectingListOfIndices : MutableList<Int> = mutableListOf<Int>()
                
        init {        
            for( i  in startIndex..(listI.size-1) ) {
                if( listI[i].hasNonemptyIntersectionWith( interval )){
                    intersectingListOfIndices.add( i )
                    if( foundFirst == false ){
                        foundFirst = true
                        firstIndex = i
                        firstIntersecting = listI[i]
                    }
                    lastIntersecting = listI[i]
                    intersectingList.add(listI[i])
                }                     
            }
            if( foundFirst ){
                //trim first if neccessary
                var trimFirst : Interval = intersectingList[0].intersectWith( interval )
                intersectingList[0] = trimFirst
                //trim last if neccessary
                var trimLast : Interval = intersectingList[ intersectingList.size-1 ].intersectWith( interval )
                intersectingList[ intersectingList.size-1 ] = trimLast
            }
            //intersectingListOfIndices.sortByDescending{it.int}
        }
        
        override fun toString() : String {
            var resultStr : String = "{"
            for( j in 0..(this.intersectingList.size-1) ){
                resultStr += this.intersectingList[j].toString()
                if( j < (this.intersectingList.size -1) ) resultStr += ","
            }
            resultStr += "}"        
            return resultStr
        }
        
        fun getIntersectingList() : MutableList<Interval>{
            return this.intersectingList
        }
        fun getIntersectingListOfIndices() : MutableList<Int>{
            return this.intersectingListOfIndices
        }
        fun getFirstIntersectingInterval() : Interval{
            return this.firstIntersecting
        }
        fun getLastIntersectingInterval() : Interval{
            return this.lastIntersecting
        }
        fun isFoundFirst() : Boolean{
            return this.foundFirst
        }
        fun getFirstIndex() : Int{
            return this.firstIndex
        }
    }



/*
 * class sorted list of intervals
*/
//methods: TODO?: (in parenthesis... should already be taken care of inside the relevant procedures)
//(removeEmptyIntervals)
//complement = complementary list of intervals
//(merge the overlapping neighbours)

class SortedListOfDisjunctIntervals( @JvmField var intervals : MutableList<Interval> ) {
    //var intervals : List<Interval> = List<Interval>(0)
    
    /* set the set of intervals to include the union of inters
     * merged where possible
     */
    fun setIntervals( inters : List<Interval> ){
        //remove original intervals
        this.intervals.clear()
        //add all the new ones, one by one
        for( inter in inters ){
            this.add( inter )
        }
    }
    
    fun getIntervals() : MutableList<Interval>{
        return this.intervals
    }
    
    override fun toString() : String {
        var resultString : String = "{"
        for( i in 0..( this.intervals.size - 1 ) ) {
            resultString += this.intervals[i].toString()
            if( i < (this.intervals.size -1 ) ) {
                resultString += ","
            }
        }
        resultString += "}"
        return resultString
    }
    
    /* Adds the interval to this.intervals
     * the return value of index where the new interval crafted from 
     * existing intersecting intervals in the list and the input interval
     * appears in the disjunct list. 
     */
    fun add( interval : Interval, startIndex : Int = 0 ) : Int{
        //println("-----------------Adding an interval "+interval.toString()+" to SLODI "+this.toString() )
        val iL = IntersectionOfListAndInterval(this.intervals, interval, startIndex )
        //println("intersection: "+iL.toString())
        var newI : Int = -1
        
        if(iL.isFoundFirst()){
            //println("intersection found")
            //there is a list of intersecting intervals that have to be replaced by one
            //possibly bigger than the new added interval
            var left : NumQ = iL.getFirstIntersectingInterval().getLe()
            var right : NumQ = iL.getLastIntersectingInterval().getRi()
            var leftCl : Boolean = iL.getFirstIntersectingInterval().isLClo()
            var rightCl : Boolean = iL.getLastIntersectingInterval().isRClo()
            var leftInf : Boolean = iL.getFirstIntersectingInterval().isLInf()
            var rightInf : Boolean = iL.getLastIntersectingInterval().isRInf()
            
            if( interval.compareLeft( iL.getFirstIntersectingInterval()) < 0 ){
                left = interval.getLe()
                leftCl = interval.isLClo()
                leftInf = interval.isLInf()  
            }
            if( interval.compareRight(iL.getLastIntersectingInterval()) > 0 ){
                right = interval.getRi()
                rightCl = interval.isRClo()
                rightInf = interval.isRInf()    
            }
            //all intersecting intervals but the first one out
            for( i in iL.getIntersectingListOfIndices().sortedDescending() ){
                this.intervals.removeAt( i )
            }
            //this.intervals.removeAll(iL.getIntersectingList())
            //println("slodi remove intersecting="+this.toString())
            //replace the first one
            this.intervals.add( iL.getFirstIndex(),
                                Interval( left, leftCl, leftInf, right, rightCl, rightInf ))
            //println("slodi added="+this.toString())
            newI = iL.getFirstIndex()
            
        } else {//old intervals have no intersection with new interval 
            //println("intersection not found")
            //still need to find the right place for the new interval though...
            //backwards? until the interval fits, given it is disjoint
            var j = this.intervals.size //would be the last element in the list
            while( (j > 0)  && moveFront(j, interval) ) //possible and right to move to the beginning
            {
                j--
            }
            if( j < 0 ) j=0
            //println("insert at j="+j)
            this.intervals.add( j, interval )
            //println("slodi added="+this.toString())
            newI = j
        }
        
        var left : Boolean = false
        var right : Boolean = false
        if( newI > 0){
            left = mergable( this.intervals[ newI - 1], interval )
        } 
        if( newI < (this.intervals.size - 1) ){
            right = mergable( interval, this.intervals[ newI + 1] )
        }
        if( left || right ) {
            return merge( newI, left, right )
        } else {//no need to merge, all set
            return newI
        }
    }

    fun mergable( ab : Interval, cd : Interval ) : Boolean {
        return (ab.compareThisROtherL( cd ) == 0 ) && ( ab.isRClo() || cd.isLClo() )
    }

    //assuming i is in good range
    //assuming the added interval is disjoint to all the others
    fun moveFront( j : Int, interval : Interval ) : Boolean {
        if( j < 1 ) {
            //no more index to move to
            return false
        } else {//there actually is an interval on position j-1
            var jMinusOneInterval : Interval = this.intervals[ j-1 ]
            val iCompareM = interval.compareLeft( jMinusOneInterval )
            
            if( iCompareM < 0 ){
                return true
            }else if( iCompareM > 0 ){
                return false
            }else{//the left points are equal
                if( interval.isLClo() && jMinusOneInterval.isLOpen() ){ // [,( situation
                    return true
                }else{
                    return false
                }
            }
        }
    }
    
    /*alters the intervals
     */
    fun merge( i : Int, left : Boolean, right : Boolean) : Int {
        var newInterval : Interval = Interval(QZERO, false, false, QZERO, false, false)
        var newI : Int = i
        var listToRemove : MutableList<Interval> = mutableListOf<Interval>()
        
        //println( "i="+i+" left="+left+" right="+right )
        
        if( left ) {
            newI = i-1
            this.intervals[newI].simpleSetRi( this.intervals[i].getRi() )
            this.intervals[newI].simpleSetRClo( this.intervals[i].isRClo() )
            //println("slodi merge 1="+this.toString())  
            this.intervals[newI].simpleSetRInf( this.intervals[ i ].isRInf())
            //println("slodi merge 2="+this.toString())
                      
            listToRemove.add( this.intervals[i] )
        }
        //listToRemove.add( this.intervals[i] )
        if( right ) {
            this.intervals[newI].simpleSetRi( this.intervals[i+1].getRi() )
            this.intervals[newI].simpleSetRClo( this.intervals[i+1].isRClo() )
            //println("slodi merge 3="+this.toString())
            this.intervals[newI].simpleSetRInf( this.intervals[ i+1 ].isRInf() )
            //println("slodi merge 4="+this.toString())
            listToRemove.add( this.intervals[i+1] )
        }
        this.intervals.removeAll( listToRemove )
        //println("slodi removed merged="+this.toString())
        //println("newInterval="+newInterval.toString())
        //this.intervals.add( newI, newInterval )
        //println("slodi set merged="+this.toString())
        
        return newI
    }
    
    /*Does not modify this.intervals
     */
    fun getIntersectionWithInterval(interval : Interval) : MutableList<Interval> {
        val iL = IntersectionOfListAndInterval(this.intervals, interval)
        //trim first and last interval in the returned (assuming disjoint sorted) list
        var returnedList = iL.getIntersectingList()
        returnedList[0] = returnedList[0].intersectWith( interval )
        returnedList[returnedList.size-1] = returnedList[returnedList.size-1].intersectWith( interval )
        return returnedList
    } 

    /*Modifies the this.intervals list
     */
    fun intersectWithInterval(interval : Interval){
        this.intervals.retainAll(getIntersectionWithInterval(interval))//delete the rest
    }
      
    /* Does not modify this.intervals.
     * intersection of two unions of sequences of disjoint intervals
     * subsequently intersect  intersecting parts with intervals from oth
     */
    fun getIntersectionWithOtherDisjunctList( oth : SortedListOfDisjunctIntervals ) : MutableList<Interval> {
        var i : Int = 0
        var resultList : MutableList<Interval> = mutableListOf<Interval>()
        //something that shlould generally work (even with a future tree representation)
        //find and save intersection with every interval from the list, 
        //search for the next one 
        //add the intersections to a list of results
        for( inter in oth.getIntervals() ){
            val iL = IntersectionOfListAndInterval(this.intervals, inter, i)
            if(iL.isFoundFirst()){
                i = iL.getFirstIndex()
                //normal add, we know the input were two sorted lists of disjunct intervals
                //therefore the output is a union of consecutive disjoint sets represented
                //as lists of sorted disjoint intervals, it is sufficient to simply
                //put these together in one result list
                resultList.addAll(iL.getIntersectingList() )
            }
        }
        return resultList
    }
    
    /* Modifies this.intervals.
     */
    fun intersectWithOtherDisjunctList( oth : SortedListOfDisjunctIntervals){
        this.intervals = this.getIntersectionWithOtherDisjunctList( oth )
    }
    
    /*Does not modify this.intervals.
     * add all the intervals subsequently (using indices to not have 
     * to go through the whole list when searching for place where to add 
     * the next dijunct interval from the list.
     * 
     * !!just now this method is garbage...
     * !!!!!still garbage big garbage
     */
    fun getUnionWithOtherDisjunctList( oth : SortedListOfDisjunctIntervals ) : MutableList<Interval> {
        //search for each of the newly added intervals in the list, 
        //add it, go to next one
        //starting from the index firstIndex from the last search
        var i : Int = 0
        var resultList : MutableList<Interval> = mutableListOf<Interval>()
        
        for( inter in oth.getIntervals() ){
            //val j : Int = resultList.add( inter )
           // if( j>i ) i = j
        }
        return resultList
        
    }
    
    fun uniteWithOtherDisjunctList( oth : SortedListOfDisjunctIntervals){
        //this.intervals = this.getUnionWithOtherDisjunctList( oth )
        //println("other:"+oth.toString())
        for( inter in oth.getIntervals() ) {
            //println("adding "+inter.toString() )
            this.add(inter)
        }
    }
}

fun removeParenthesis( str : String ) : String {
    //var re = "[()]".toRegex()
    //return re.replace( str, "" )
    val result = str.filter{ it -> (it != '('  && it !=')' )} 
    println("Bez zavorek = "+ result)
    return result
}

/* semiToIntervals : list Interval
 * formula = semialgebraic formula in dnf, nonquantified, for parameters
 * false -> (0,0) notinf, notinf ...  empty list, no interval
 * true -> (0,0) Inf, Inf ... list of one interval
 */
fun semiToIntervals( formula : String, varStr : String ) : SortedListOfDisjunctIntervals {
    var resultList = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
    
    if( formula.contains("false") ) {
        //let resultList be empty
    } else if( formula.contains("true") ) {
        resultList.add( Interval( QZERO, false, true, QZERO, false, true ) ) //(-R,+R)
    } else {
        val formulaWithoutParenthesis = removeParenthesis( formula )
        val listOfAlternatives : List<String> = divideSemiByOR( formulaWithoutParenthesis )
        println( listOfAlternatives )
        for( alt in listOfAlternatives) {
            val listOfConjuncts : List<String> = divideSemiByAND( alt )
            var partialResultList = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
            println( listOfConjuncts )
            for( i in 0..(listOfConjuncts.size - 1) ){
                println( " Slodi for this conjunct co: "+listOfConjuncts[i])
                val slodi = getListOfIntervalsSatisfyingCondition( listOfConjuncts[i], varStr )
                println( " One conjunct result: "+ slodi.toString() )
                if( i == 0 ){
                    partialResultList = slodi
                } else {
                    //intersect with partial result (AND)
                    partialResultList.intersectWithOtherDisjunctList( slodi )
                    println( " intersected with partialRes: "+partialResultList.toString() )
                }
            }
            //unite the partial result with current resultList (OR)
            resultList.uniteWithOtherDisjunctList( partialResultList )
            println("One alternative result: "+ resultList.toString() )
        }
    }
    
    return resultList
}

/* string to inequality code:
 *   -2 <    -1 <=   0 =   1 >=   2 >
 */
fun strToIneqCode( str : String ) : Int {
    when( str ){
        "<=" -> return -1
        "<" -> return -2
        ">=" -> return 1
        ">" -> return 2
        "=" -> return 0
        else -> return -3 //something is not ok
    }
}

/* divideSemiByOR : list String
 */
fun divideSemiByOR( formula : String, orString : String = "or" ) : List<String> {
    return formula.split( orString )
}

/* divideSemiByAND : list String
 */
fun divideSemiByAND( formula : String, andString : String = "and") : List<String> {
    return formula.split( andString )
}

/* class helper for parsing one polynomial condition
 * into polynomial, eq or ineq sign, rhs
 */
class SemialgebraicInfoFromCondition (@JvmField var oneCondition : String){
    @JvmField var polyn : String = ""; 
    @JvmField var eqineq : Int = -3; 
    @JvmField var rhs : String = "0" //supposing this won't change
    init{
        //parse the condition into
        //polynomial, eq or ineq, zero
        //divide by spaces, last nonempty = 0, last but one nonempty = eqineq,
        //squash the remainder back together, its a polynomial
        var listOfStr = oneCondition.split(" ")
        var listOfNonemptyStr : MutableList<String> = mutableListOf<String>()
        for( str in listOfStr ) {
            if( str.isNotEmpty() ) {
                listOfNonemptyStr.add(str)
            }
        }
        if( listOfNonemptyStr.size > 2 ){
            this.rhs = listOfNonemptyStr[listOfNonemptyStr.size - 1]
            // -2 <    -1 <=   0 =   1 >=   2 >
            this.eqineq = strToIneqCode(listOfNonemptyStr[listOfNonemptyStr.size - 2])
            for( i in 0..(listOfNonemptyStr.size-3) ){
                polyn = polyn + listOfNonemptyStr[i]
            }
        }
    }
    
    fun getPolyn() : String {
        return this.polyn
    }
    fun getEqIneq() : Int {
        return this.eqineq
    }
    fun getRHS() : String {
        return this.rhs
    }
}

/* eval using Rings */
/*fun evalPolAt( polyn : UPolyInQ, sample : NumQ ) : NumQ {
    //convert Z polyn to Q polyn, and evaluate at sample
    return polyn.evaluate( sample )
}*/

fun sign( value : Int ) : Int {
    if( value < 0) {
        return -1
    } else if( value == 0 ) {
        return 0
    } else {
        return 1
    }
}

/* Get sign of a polynomial at point var=sample.
 * <0 ... sign -1
 * =0 ... sign 0
 * >0 ... sign 1
 */
fun getSignPolAt( polyn : UPolyInQ, sample : NumQ ) : Int {
    val qVal = polyn.evaluate( sample )
    //is it possible to do the comparison of constant to zero in rings?
    return sign( qVal.compareTo(QZERO) )
}

/* get intervals where the polynomial condition holds
 * (for one variable ... in the real line)
 * (sort, merge if needed, a list of intervals)
 * assuming zero rhs
 */
fun getListOfIntervalsSatisfyingCondition( oneCondition : String, varStr : String ) : SortedListOfDisjunctIntervals {
    val info = SemialgebraicInfoFromCondition( oneCondition )
    println("Info.polyn: "+info.getPolyn())
    println("Info.eqIneq: "+info.getEqIneq())
    println("Info.RHS: "+info.getRHS())
    val roots = getRoots( info.getPolyn() ) //sorted list of Q roots of the polynomial [] for zero polyn
    println("Roots: "+roots)
    val polyn = strToUPolyInQ( info.getPolyn(), varStr )
    println("Polyn: "+polyn)
    val signsList = getListOfSigns( roots, polyn ) //[0] for zero polynomial
    println("SignsList: "+signsList)
    var listForResult = mutableListOf<Interval>()
    //decide from signs and the eqineq id which intervals to take into the result
    //we suppose the condition is <=> 0 (with zero rhs)
    val en : Int= info.getEqIneq()
    
    var addToActual : Boolean = false
    var actualInterval : Interval = Interval( QZERO, false, false, QZERO, false, false )
    
    //first open interval:
    when( en ){
        //<>
        -2, 2 -> if( signsList[0] * en > 0 ) {
                     if( roots.size > 0) {
                         listForResult.add( Interval( QZERO, false, true, roots[0], false, false ) )
                     } else { //holds for whole R
                         listForResult.add( Interval( QZERO, false, true, QZERO, false, true ) )
                     }
                 }
        //<=>
        -1, 1 -> if( signsList[0] * en >= 0 ) {
                     if( roots.size > 0) { // (-R,root0)
                         actualInterval = Interval( QZERO, false, true, roots[0], false, false ) 
                         addToActual = true
                     } else { //holds for whole R
                         listForResult.add( Interval( QZERO, false, true, QZERO, false, true ) )
                     } 
                 }
        //= 
        0  ->    if( signsList[0] == 0 ) { 
                     //only zero polynomial is zero on one (and all) interval of nonzero length
                     //holds for whole R
                     listForResult.add( Interval( QZERO, false, true, QZERO, false, true ) )
                 }
    }
    
    if( roots.size > 0 ) {
        //left boundary points + middle intervals:
        for( i in 0..(roots.size - 2)){
            when( en ){
            //<>
            -2, 2 -> {//deal with the point:
                     //the point cannot be part of interval and since $en is global
                     //to this loop, it is not part of the former interval
                     //(there cannot be any actualInterval waiting to be
                     //prolonged through this point) .. do nothing
                
                     //deal with the interval: (for such $en no merging of the intervals)
                     if( signsList[i+1] * en > 0 ) {
                         listForResult.add( Interval( roots[i], false, false, roots[i+1], false, false ) )
                     }
                 }
            //<=>
            -1, 1 -> {//deal with the point:
                     //which is a root, so it satisfies the ineq ><=0
                     if( addToActual ){//prolonging existing interval to ?,ri]
                         actualInterval.setRValClo( roots[i], true )
                     } else {//beginning new interval [ri,ri]
                         addToActual = true
                         actualInterval = Interval( roots[i], true, false, roots[i], true, false )
                     }
                     //deal with the interval:
                     if( signsList[i+1] * en >= 0 ) { 
                         //add this line segment to actual interval ?,ri+1)
                         //we know that addToActual must be true now, <>= holds at ri
                         actualInterval.setRValClo(roots[i+1],false)
                     } else {//do not add this line segment to the interval
                         //if there is an actual interval under construction, 
                         //output it now
                         if( addToActual ){
                             listForResult.add( actualInterval )
                             addToActual = false
                         }
                     }
                 }
            //= 
            0 ->     {//deal with the point:
                     //either the set of zero values is the set of roots
                     //add [ri,ri], no prolonging of intervals ...
                     if( signsList[0] != 0 ) { //the case of nonzero polynomial
                         listForResult.add( Interval( roots[i], true, false, roots[i], true, false ) )
                     }
                     //deal with the interval:
                     //... or the whole R is a zero set of the (zero) polynomial
                     //it has been taken care of before the for loop, do nothing 
                 }
            }//when
        }//for
        
        //last point roots[ roots.size - 1 ] :
        val lastRoot : NumQ = roots[ roots.size -1 ]
        val lastSign : Int = signsList[ signsList.size - 1 ]
        when( en ){
        //<>
        -2, 2 -> {//do nothing, again point is not part of a solution to <> ineq 
             }
        //<=>
        -1, 1 -> {//point is a root, so it satisfies the ineq ><=0
                 if( addToActual ){//prolonging existing interval to ?,ri]
                     actualInterval.setRValClo( lastRoot, true )
                 } else {//beginning new interval [ri,ri]
                     addToActual = true
                     actualInterval = Interval( lastRoot, true, false, lastRoot, true, false )
                 }
             }
        //= 
        0    ->  {//either the set of zero values is the set of roots
                 //add [ri,ri], no prolonging of intervals ... 
                 if( signsList[0] != 0 ) { //the case of nonzero polynomial
                     listForResult.add( Interval( lastRoot, true, false, lastRoot, true, false ) )
                 }
                 //...or polyn is zero and has been already dealt with, so do nothing
             }
        }  

        //last interval (roots[ roots.size - 1 ],+R) :
        when( en ){
        //<>
        -2, 2 -> //deal with the interval: (for such $en no merging of the intervals)
                 if( lastSign * en > 0 ) {// (ri,+R)
                     listForResult.add( Interval( lastRoot, false, false, QZERO, false, true ) )
                 }
        //<=>
        -1, 1 -> if( lastSign * en >= 0 ) { 
                     //add this halfline to actual interval ?,ri+1)
                     //we know that addToActual must be true now, <>= holds at ri
                     actualInterval.setRInf( true ) // ?,+R)
                     listForResult.add( actualInterval )
                 } else {//do not add this halfline to the interval
                     //if there is an actual interval under construction, 
                     //output it now
                     if( addToActual ){
                         listForResult.add( actualInterval )
                         addToActual = false
                     }
                 }
        //= 
        0  ->    {//the zero polynomial case was dealt with before, this
                 //halfline is no root, so nothing to do
             }
        }//when - adding last interval
    }//if there is at least one root of the polynomial in the list of roots
    
    return SortedListOfDisjunctIntervals( listForResult )
}

/* Get list of signs of a polynomial in n+1 intervals given by
 * n roots of the polynomial.
 * TODO: zero polynomial: output [0]
 */
fun getListOfSigns( roots : List<NumQ>, polyn : UPolyInQ ): List<Int> {
    var resultList : List<Int> = listOf<Int>()
    if( roots.size > 0) {
        //first (unbounded from the left) interval
        //get sample by bound -1
        var r1 : NumQ = roots[0]
        var r2 : NumQ = r1
        var sample = r1.add( QMINUSONE )
        resultList += listOf( getSignPolAt( polyn, sample ) )
    
        //middle (finite) intervals
        //get sample by avg of the bounds
        for( i in 0..(roots.size-2) ){
            r1 = roots[i]
            r2 = roots[i+1]
            sample = (r1.add(r2)).multiply( QHALF )
            resultList += listOf( getSignPolAt( polyn, sample ) )
        }    
        //last (unbounded from the right) interval
        //get sample by bound +1
        r2 = roots[ roots.size-1 ]
        sample = r2.add( QONE )
        resultList += listOf( getSignPolAt( polyn, sample ) )
    } else {
        //just evaluate at zero, to see what sign the polynomial has everywhere
        //the sign list will have exactly one element
        //in case of zero polynomial, this will be QZERO
        resultList += listOf( getSignPolAt( polyn, QZERO ) )
    }
    
    return resultList
}

//using PolynomialParser:
//strToUPolyInZ(poly : String, variable : String) : UPolyInZ

//using ReduceHelper:
//get roots
