package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

/*
val QZERO : NumQ = Q.mk(BigInteger.ZERO,BigInteger.ONE)
val QMINUSONE : NumQ = Q.getNegativeOne()
val QONE : NumQ = Q.mk(BigInteger.ONE,BigInteger.ONE)
val QTWO : NumQ = Q.mk(BigInteger.TWO,BigInteger.ONE)
val QHALF : NumQ = Q.mk(BigInteger.ONE,BigInteger.TWO)
*/

fun compare2Doubles( a: Double, b : Double) : Int {
    var result : Int
    if( (a-b)>0.0 ){
        result = 1
    }else if( (a-b) < 0.0 ){
        result = -1
    }else{
        result = 0
    }
    return result    
}

//class interval [()] in R*, 
//infinity..corresponds to left/right null endpoint
  //hasNonemptyIntersectionWith(interval), 
  //isIntersectionEmptyWith(interval), intersectWith(interval), 
  //isUnitableWith(interval), uniteWith(interval)
class IntervalDouble( left: Double, leftClosed: Boolean, leftInf: Boolean,
                right: Double, rightClosed: Boolean, rightInf: Boolean ) {
                
    @JvmField var le : Double = 0.0
    @JvmField var ri : Double = 0.0
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
            val lrcomparison = left-right
            if( lrcomparison > 0.0 ) this.setEmpty()
            if( (lrcomparison == 0.0) && (!leftClosed || !rightClosed) ) this.setEmpty()
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
    
    fun getLe(): Double = this.le
    fun getRi(): Double = this.ri
    
    fun simpleSetLe( left: Double) { this.le = left }
    fun simpleSetRi( right: Double) { this.ri = right }
    fun simpleSetLClo( lc : Boolean ) { this.lClo = lc }
    fun simpleSetRClo( rc : Boolean) { this.rClo = rc }
    fun simpleSetLInf( li : Boolean ) { this.lInf = li }
    fun simpleSetRInf( ri : Boolean) { this.rInf = ri }
        
    fun setLe(left: Double) { this.le = left; this.setRInf( false ) }
    fun setRi(right: Double) {this.ri = right; this.setLInf( false )}
    
    fun isLClo(): Boolean = this.lClo
    fun isRClo(): Boolean = this.rClo
    fun setLClo( lc : Boolean ) { this.lClo = lc }
    fun setRClo( rc : Boolean ) { this.rClo = rc }
    fun setLValClo(left : Double, lc : Boolean ) { 
        this.setLe(left); this.setLClo( lc ); this.setLInf(false) 
        //set empty to (0,0):
        if( !this.lInf && !this.rInf ) {
            val lrcomparison = this.le.compareTo(this.ri)
            if( lrcomparison > 0 ) this.setEmpty()
            if( (lrcomparison == 0) && (!lc || !this.rClo) ) this.setEmpty()
        }
    }
    fun setRValClo(right : Double, rc : Boolean ) { 
        this.setRi(right); this.setRClo( rc ); this.setRInf(false) 
        //set empty to (0,0):
        if( !this.lInf && !this.rInf ) {
            val lrcomparison = this.le.compareTo(this.ri)
            if( lrcomparison > 0 ) this.setEmpty()
            if( (lrcomparison == 0) && (!this.lClo || !rc) ) this.setEmpty()
        }
    }

    fun setEmpty(){
        this.le = 0.0
        this.ri = 0.0
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
        this.le = if (lI) 0.0 else this.le
    }
    fun setRInf( rI : Boolean ) { 
        this.rInf = rI
        this.rClo = if( rI ) false else this.rClo 
        this.ri = if( rI ) 0.0 else this.ri
    }    

    fun isLOpen(): Boolean = !this.lClo
    fun isROpen(): Boolean = !this.rClo
    
    fun isOpen(): Boolean = !this.lClo && !this.rClo
    fun isClosed(): Boolean = this.lClo && this.rClo
    fun isFinite(): Boolean = !this.lInf || !this.rInf
    
    //positive length is computed only for finite intervals
    //infinite get -1
    fun length(): Double {
        if( !this.lInf && !this.rInf )
            return this.ri-this.le
        else
            return -1.0
    }
    
    fun isEmpty(): Boolean {
        //the moment an interval becomes empty, we set le,ri to 0s
        //andl closed to false, and infinite to false
        return ( this.le==0.0 && this.ri==0.0 && (!this.lClo || !this.rClo) && !this.lInf && !this.rInf)
    }
    
    fun isOnePoint() : Boolean {
        return (this.isFinite() && this.isClosed() && (this.le == this.ri) )
    }
    
    /* intersection = itself, 
     * is a subset
     */
    fun isSubsetOfSLODI( slodi : SortedListOfDisjunctIntervalsDouble ) : Boolean {
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf( this ) )
        slodi2.uniteWithOtherDisjunctList(slodi)
        return slodi2.isEqual( slodi )
    }
    
    /*compareLeft
     * return <0 if my left is smaller than others left
     * return =0 if lefts are the same
     * return >0 if my left is bigger than others left
     */
    fun compareLeft(oth : IntervalDouble) : Int {
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
                return compare2Doubles( this.le, oth.getLe() )
            }
        }
    }
    
    
    /*compareRight
     * return <0 if my right is smaller than others right
     * return =0 if rights are the same
     * return >0 if my right is bigger than others right
     */
    fun compareRight(oth : IntervalDouble) : Int {
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
                return compare2Doubles( this.ri, oth.getRi())
            }
        }
    }
    
    fun compareThisLOtherR( oth : IntervalDouble ) : Int {
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
                return compare2Doubles( this.le, oth.getRi() )
            }
        }
    }
    
    fun compareThisROtherL( oth : IntervalDouble ) : Int {
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
                return compare2Doubles( this.ri, oth.getLe() )
            }
        }
    }
    
    //intersection of two intervals this and oth
    fun intersectWith( oth : IntervalDouble ) : IntervalDouble {
        if( this.hasNonemptyIntersectionWith( oth ) ){
            var left : Double
            var right : Double
            var leftClosed : Boolean
            var rightClosed : Boolean
            var leftInf : Boolean
            var rightInf : Boolean
            
            val thisMinusOthL = this.compareLeft( oth )
            if( thisMinusOthL >= 0 ){
                //this left
                if( this.isLInf() ){ left = 0.0; leftClosed = false; leftInf = true }
                else { left = this.le; leftClosed = this.lClo; leftInf = false }
            } else {
                //other left
                if( oth.isLInf() ){ left = 0.0; leftClosed = false; leftInf = true }
                else { left = oth.le; leftClosed = oth.lClo; leftInf = false }
            }
            
            val thisMinusOthR = this.compareRight( oth )
            if( thisMinusOthR <= 0 ){
                //this right
                if( this.isRInf() ){ right = 0.0; rightClosed = false; rightInf = true }
                else { right = this.ri; rightClosed = this.rClo; rightInf = false }
            } else {
                //other right
                if( oth.isRInf() ){ right = 0.0; rightClosed = false; rightInf = true }
                else { right = oth.ri; rightClosed = oth.rClo; rightInf = false }
            }
            
            return IntervalDouble( left, leftClosed, leftInf, right, rightClosed, rightInf )
            
        } else {
            return IntervalDouble( 0.0, false, false, 0.0, false, false )    
        }
    }
    
    /* {a,b},{c,d} has intersection?
     * 
     */
    fun hasNonemptyIntersectionWith(oth : IntervalDouble) : Boolean{
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
    fun isPointInside( point : Double ) : Boolean {
        if( this.isEmpty() ) return false
        else {
            var fromLeft : Boolean
            var fromRight : Boolean
            //is point bounded from left by interval?
            if( this.isLInf()) { 
                fromLeft = true
            } else {
                val pointMinusLeft : Int = compare2Doubles( point, this.le)
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
                val rightMinusPoint : Int = compare2Doubles( this.ri, point )
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
    
    /* Complement of an interval is one interval or two 
     * disjunct intervals or empty set.
     */
    fun getComplement() : SortedListOfDisjunctIntervalsDouble {
        var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>())
        if( !lInf ){
            result.add( IntervalDouble( 0.0, false, true, this.le, !this.isLClo(), false ) )
        }
        if( !rInf ){
            result.add( IntervalDouble( this.ri, !this.isRClo(), false, 0.0, false, true  ) )
        }
        return result
    }
    
    /* Two intervals are the same if they have all the characeteristics
     * the same.
     */
    fun isEqual( other : IntervalDouble ): Boolean{
        var result : Boolean = true
        if( this.le != other.getLe() ) result = false
        if( this.isLClo() != other.isLClo() ) result = false
        if( this.isLInf() != other.isLInf() ) result = false
        if( this.ri != other.getRi() ) result = false
        if( this.isRClo() != other.isRClo() ) result = false
        if( this.isRInf() != other.isRInf() ) result = false
        return result
    }
}

/* Helper class intersection list intersects one interval,
 * optional parameter startIndex inicates the index in the list
 * from which to start (0 by default).
 * Assumption: the list is a sorted list of disjunct intervals
 */
class IntersectionOfListAndIntervalDouble(@JvmField var listI : MutableList<IntervalDouble>,
    @JvmField var interval: IntervalDouble, @JvmField var startIndex : Int = 0){
        @JvmField var foundFirst = false
        @JvmField var firstIndex : Int = -1
        @JvmField var firstIntersecting : IntervalDouble = IntervalDouble(0.0, false, false,0.0, false, false)
        @JvmField var lastIntersecting : IntervalDouble = IntervalDouble(0.0, false, false,0.0, false, false)
        @JvmField var intersectingList : MutableList<IntervalDouble> = mutableListOf<IntervalDouble>()
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
                var trimFirst : IntervalDouble = intersectingList[0].intersectWith( interval )
                intersectingList[0] = trimFirst
                //trim last if neccessary
                var trimLast : IntervalDouble = intersectingList[ intersectingList.size-1 ].intersectWith( interval )
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
        
        fun getIntersectingList() : MutableList<IntervalDouble>{
            return this.intersectingList
        }
        fun getIntersectingListOfIndices() : MutableList<Int>{
            return this.intersectingListOfIndices
        }
        fun getFirstIntersectingInterval() : IntervalDouble{
            return this.firstIntersecting
        }
        fun getLastIntersectingInterval() : IntervalDouble{
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

class SortedListOfDisjunctIntervalsDouble( @JvmField var intervals : MutableList<IntervalDouble> ) {
    //var intervals : List<Interval> = List<Interval>(0)
    
    /* set the set of intervals to include the union of inters
     * merged where possible
     */
    fun setIntervals( inters : List<IntervalDouble> ){
        //remove original intervals
        this.intervals.clear()
        //add all the new ones, one by one
        for( inter in inters ){
            this.add( inter )
        }
    }
    
    fun getIntervals() : MutableList<IntervalDouble>{
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
    fun add( interval : IntervalDouble, startIndex : Int = 0 ) : Int{
        //println("-----------------Adding an interval "+interval.toString()+" to SLODI "+this.toString() )
        val iL = IntersectionOfListAndIntervalDouble(this.intervals, interval, startIndex )
        //println("intersection: "+iL.toString())
        var newI : Int = -1
        
        if(iL.isFoundFirst()){
            //println("intersection found")
            //there is a list of intersecting intervals that have to be replaced by one
            //possibly bigger than the new added interval
            var left : Double = iL.getFirstIntersectingInterval().getLe()
            var right : Double = iL.getLastIntersectingInterval().getRi()
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
                                IntervalDouble( left, leftCl, leftInf, right, rightCl, rightInf ))
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

    fun mergable( ab : IntervalDouble, cd : IntervalDouble ) : Boolean {
        return (ab.compareThisROtherL( cd ) == 0 ) && ( ab.isRClo() || cd.isLClo() )
    }

    //assuming i is in good range
    //assuming the added interval is disjoint to all the others
    fun moveFront( j : Int, interval : IntervalDouble ) : Boolean {
        if( j < 1 ) {
            //no more index to move to
            return false
        } else {//there actually is an interval on position j-1
            var jMinusOneInterval : IntervalDouble = this.intervals[ j-1 ]
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
        var newInterval : IntervalDouble = IntervalDouble(0.0, false, false, 0.0, false, false)
        var newI : Int = i
        var listToRemove : MutableList<IntervalDouble> = mutableListOf<IntervalDouble>()
        
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
    fun getIntersectionWithInterval(interval : IntervalDouble) : MutableList<IntervalDouble> {
        val iL = IntersectionOfListAndIntervalDouble(this.intervals, interval)
        //trim first and last interval in the returned (assuming disjoint sorted) list
        var returnedList = iL.getIntersectingList()
        returnedList[0] = returnedList[0].intersectWith( interval )
        returnedList[returnedList.size-1] = returnedList[returnedList.size-1].intersectWith( interval )
        return returnedList
    } 

    /*Modifies the this.intervals list
     */
    fun intersectWithInterval(interval : IntervalDouble){
        this.intervals.retainAll(getIntersectionWithInterval(interval))//delete the rest
    }
      
    /* Does not modify this.intervals.
     * intersection of two unions of sequences of disjoint intervals
     * subsequently intersect  intersecting parts with intervals from oth
     */
    fun getIntersectionWithOtherDisjunctList( oth : SortedListOfDisjunctIntervalsDouble ) : MutableList<IntervalDouble> {
        var i : Int = 0
        var resultList : MutableList<IntervalDouble> = mutableListOf<IntervalDouble>()
        //something that shlould generally work (even with a future tree representation)
        //find and save intersection with every interval from the list, 
        //search for the next one 
        //add the intersections to a list of results
        for( inter in oth.getIntervals() ){
            val iL = IntersectionOfListAndIntervalDouble(this.intervals, inter, i)
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
    fun intersectWithOtherDisjunctList( oth : SortedListOfDisjunctIntervalsDouble){
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
    fun getUnionWithOtherDisjunctList( oth : SortedListOfDisjunctIntervalsDouble ) : MutableList<IntervalDouble> {
        //search for each of the newly added intervals in the list, 
        //add it, go to next one
        //starting from the index firstIndex from the last search
        var i : Int = 0
        var resultList : MutableList<IntervalDouble> = mutableListOf<IntervalDouble>()
        
        for( inter in oth.getIntervals() ){
            //val j : Int = resultList.add( inter )
           // if( j>i ) i = j
        }
        return resultList
        
    }
    
    /* Modifies this.intervals
     */
    fun uniteWithOtherDisjunctList( oth : SortedListOfDisjunctIntervalsDouble){
        //this.intervals = this.getUnionWithOtherDisjunctList( oth )
        //println("other:"+oth.toString())
        for( inter in oth.getIntervals() ) {
            //println("adding "+inter.toString() )
            this.add(inter)
        }
    }
    
    /* Does not modify this.intervals
     * Complement of A (a union):
     * intersection of complements of the intervals
     */
    fun getComplement() : SortedListOfDisjunctIntervalsDouble {
        var result : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        
        result.add( IntervalDouble( 0.0, false, true, 0.0, false, true ) )//R
        for( inter in this.intervals ){
                result.intersectWithOtherDisjunctList( inter.getComplement() )
        }
        
        return result
    }
    
    /* Should modify the intervals if needed
     */
    fun mergeFittingIntervals(){
        var toDelete : MutableList<Int> = mutableListOf<Int>()
        var newIntervals : MutableList<IntervalDouble> = mutableListOf<IntervalDouble>()
        var oldCount : Int = this.intervals.size
        var left : Int = 0
        var right : Int = 0
        for( i in this.intervals){
            newIntervals.add( IntervalDouble( i.getLe(), i.isLClo(), i.isLInf(),
                                              i.getRi(), i.isRClo(), i.isRInf() ) )
        }
        //modify the new intervals
        if( newIntervals.size > 0){
            //var nleft : Int = newIntervals[0].getLe()
            //var nlinf : Boolean = newIntervals[0].getLInf()
            //var nlclo : Boolean = newIntervals[0].getLClo()
            
            var nright : Double = newIntervals[0].getRi()
            var nrinf : Boolean = newIntervals[0].isRInf()
            var nrclo : Boolean = newIntervals[0].isRClo()
            
            //last i left mergable interval
            var lasti : Int = 0
            
            //go through the intervals after that
            for( j in 1..(newIntervals.size-1) ){
                //if join j, R=L and ] or [
                if( nright == newIntervals[j].getLe() &&
                    (nrclo || newIntervals[j].isLClo() ) ){
                    toDelete.add( j )
                    nright = newIntervals[j].getRi()
                    nrinf = newIntervals[j].isRInf()
                    nrclo = newIntervals[j].isRClo()
                    newIntervals[lasti].setRi( nright )
                    newIntervals[lasti].setRClo( nrclo )
                    newIntervals[lasti].setRInf( nrinf )
                }else{//if not join j, begin new interval
                    newIntervals[lasti].setRi( nright )
                    newIntervals[lasti].setRClo( nrclo )
                    newIntervals[lasti].setRInf( nrinf )
                    lasti = j
                    nright = newIntervals[j].getRi()
                    nrclo = newIntervals[j].isRClo()
                    nrinf = newIntervals[j].isRInf()
                }
                //modify the adjoining intervals and delete the unnecessary
                for( ind in toDelete.asReversed() ){
                    newIntervals.removeAt( ind )
                }
            }
        }
        this.intervals = newIntervals
    }
    
    /* all the intervals are equal */
    fun isEqual( oth : SortedListOfDisjunctIntervalsDouble ) : Boolean {
        var result : Boolean = true
        //println("this before merge "+this.toString())
        //println("oth before merge "+oth.toString())
        
        //merge consecutive ]( and )[ in both lists
        this.mergeFittingIntervals()
        oth.mergeFittingIntervals()
        //println("this after merge "+this.toString())
        //println("oth after merge "+oth.toString())
        
        var otherIntervals : List<IntervalDouble> = oth.getIntervals()
        if( this.intervals.size != otherIntervals.size ){
            return false
        }else{
            //iterate over both lists and
            for(i in 0..(this.intervals.size-1) ){
                if( !this.intervals[i].isEqual(otherIntervals[i]) )
                    result = false
            }
        }
        return result 
    }
    
    /* All the intervals are intersections of themselves with oth.
     */
    fun isSubsetOf( oth : SortedListOfDisjunctIntervalsDouble ) : Boolean {
        var result : Boolean = true
        for( inter in this.intervals ){
            if( !inter.isSubsetOfSLODI( oth ) ) result = false
        }
        return result
    }
    
    /* Modifies this.intervals
     * subtract B from A:
     * intersect A with complement of B
     */
    fun subtractOtherDisjunctList( oth : SortedListOfDisjunctIntervalsDouble ){
        var complementOther : SortedListOfDisjunctIntervalsDouble = oth.getComplement()
        this.intersectWithOtherDisjunctList( complementOther )
    }
    
    fun isNonempty() : Boolean{
        var empty : Boolean = true
        for( inter in this.intervals ){
            if( !inter.isEmpty() ){
                empty = false
            }
        }
        return !empty
    }
}

/* semiToIntervals : list Interval
 * formula = semialgebraic formula in dnf, nonquantified, for parameters
 * false -> (0,0) notinf, notinf ...  empty list, no interval
 * true -> (0,0) Inf, Inf ... list of one interval
 */
fun semiToIntervalsDouble( formula : String, varStr : String ) : SortedListOfDisjunctIntervalsDouble {
    var resultList = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
    
    if( formula.contains("false") ) {
        //let resultList be empty
    } else if( formula.contains("true") ) {
        resultList.add( IntervalDouble( 0.0, false, true, 0.0, false, true ) ) //(-R,+R)
    } else {
        val formulaWithoutParenthesis = removeParenthesis( formula )
        val listOfAlternatives : List<String> = divideSemiByOR( formulaWithoutParenthesis )
        println( listOfAlternatives )
        for( alt in listOfAlternatives) {
            val listOfConjuncts : List<String> = divideSemiByAND( alt )
            var partialResultList = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
            println( listOfConjuncts )
            for( i in 0..(listOfConjuncts.size - 1) ){
                println( " Slodi for this conjunct co: "+listOfConjuncts[i])
                val slodi = getListOfIntervalsSatisfyingConditionDouble( listOfConjuncts[i], varStr )
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

/* eval using Rings */
/*fun evalPolAt( polyn : UPolyInQ, sample : NumQ ) : NumQ {
    //convert Z polyn to Q polyn, and evaluate at sample
    return polyn.evaluate( sample )
}*/

/* Get sign of a polynomial at point var=sample.
 * <0 ... sign -1
 * =0 ... sign 0
 * >0 ... sign 1
 */
fun getSignPolAtDouble( polyn : UPolyInQ, sample : Double ) : Int {
    val qVal = polyn.evaluate( strToQ(str(sample)) )
    //is it possible to do the comparison of constant to zero in rings?
    return sign( qVal.compareTo(QZERO) )
}


fun covertListOfIntervalsToDouble( listOfIntervals : MutableList<Interval> ) : MutableList<IntervalDouble> {
    var resultList = mutableListOf<IntervalDouble>()
    for( inter in listOfIntervals ){
        resultList.add( convertIntervalToDouble( inter ) )
    }
    return resultList
}

//left: T, leftClosed: Boolean, leftInf: Boolean,
//right: T, rightClosed: Boolean, rightInf: Boolean
fun convertIntervalToDouble( inter : Interval ) : IntervalDouble {
    var left : Double = inter.getLe().toString().toDouble()
    var right : Double = inter.getRi().toString().toDouble()
    return IntervalDouble(left,inter.isLClo(), inter.isLInf(), right, inter.isRClo(), inter.isRInf())
}

/* get intervals where the polynomial condition holds
 * (for one variable ... in the real line)
 * (sort, merge if needed, a list of intervals)
 * assuming zero rhs
 */
fun getListOfIntervalsSatisfyingConditionDouble( oneCondition : String, varStr : String ) : SortedListOfDisjunctIntervalsDouble {
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
    
    return SortedListOfDisjunctIntervalsDouble( covertListOfIntervalsToDouble(listForResult) )
}


//using PolynomialParser:
//strToUPolyInZ(poly : String, variable : String) : UPolyInZ

//using ReduceHelper:
//get roots
