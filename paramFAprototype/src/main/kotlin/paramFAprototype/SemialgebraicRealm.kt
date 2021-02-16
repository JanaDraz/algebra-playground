package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

//class interval [()] in R*, 
//infinity..corresponds to left/right null endpoint
  //hasNonemptyIntersectionWith(interval), 
  //isIntersectionEmptyWith(interval), intersectWith(interval), 
  //isUnitableWith(interval), uniteWith(interval)
class Interval( left: NumQ, leftClosed: Boolean, leftInf: Boolean,
                right: NumQ?, rightClosed: Boolean, rightInf: Boolean ) {
    val QZERO : NumQ = Q.mk(BigInteger.ZERO,BigInteger.ONE)                
                
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
        //set empty to (0,0):
        if( !this.lInf && !this.rInf ) {
            val lrcomparison = left.compareTo(right)
            if( lrcomparison > 0 ) this.setEmpty()
            if( (lrcomparison == 0) && (!leftClosed || !rightClosed) ) this.setEmpty()
        }
    }
    
    fun getLe(): NumQ = this.le
    fun getRi(): NumQ = this.ri
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
        return ( this.le.isZero() && this.ri.isZero() && !this.lClo && !this.rClo )
    }
    
    fun isOnePoint() : Boolean {
        return (this.isFinite() && this.isClosed() && (this.le.compareTo(this.ri) == 0) )
    }
    
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
    
    fun hasNonemptyIntersectionWith(oth : Interval) : Boolean{
        if( !this.isEmpty() && !oth.isEmpty() ){
            //none of the intervals is empty
            val thisMinusOtherL = this.compareLeft(oth)
            val thisMinusOtherR = this.compareRight(oth)
            if( thisMinusOtherL < 0 && thisMinusOtherR < 0) {
                val bccompare = this.compareThisROtherL( oth )
                if( bccompare < 0){
                    return false
                }else if( bccompare == 0 ){
                    return ( this.rClo && oth.isLClo() )
                }else{
                    return true
                }  
            }else if( thisMinusOtherL < 0 && thisMinusOtherR < 0 ) {
                val adcompare = this.compareThisLOtherR( oth )
                if( adcompare < 0){
                    return true
                }else if( adcompare == 0 ){
                    return ( this.lClo && oth.isRClo() )
                }else{
                    return false
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

//class sorted list of intervals
//methods:
//(removeEmptyIntervals)
//intersectWith(interval), intersectWith(list of intervals)
//uniteWith(interval), uniteWith(list of intervals)
//complement = complementary list of intervals
//sort by left point, (sort by right point)
//merge the overlapping neighbours

//semiToIntervals : list Interval
//divideSemiByOR
//divideSemiByAND
//parse polynomial from one polynomial condition
//get the roots of a polynomial by reduce
//get intervals where the polynomial condition holds
//sort, merge if needed, a list of intervals
//intersect two sorted lists of intervals
//
//using PolynomialParser:
//strToUPolyInZ(poly : String, variable : String) : UPolyInZ
//mutable

class SortedListOfDisjunctIntervals( @JVMField var intervals : MutableList<Interval> ) {
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
    
    fun add( interval : Interval ){
        for( i  in range(0,this.intervals.size() ) {
            var foundFirst = false
            if( intervals[i].hasNonemptyIntersectionWith( i )){
                if( foundFirst == false ){
                    foundFirst = true
                    firstIntersecting = intervals[i]
                }
                lastIntersecting = intervals[i]
                newInterval = interval.intersectWith( i )
                
            }
        }    
    }

    
}
*/
