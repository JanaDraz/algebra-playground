/*
 * test of work with polynomials
 */
package paramFAprototype

import org.junit.Assert
import org.junit.Test

class SemialgebraicRealmTest : AbstractTest() {
    
    @Test
    fun testGetListOfIntervalsSatisfyingCondition_linear1() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "p + 1 >= 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{[-1,+R)}", slodiStr )
    }   

    @Test
    fun testGetListOfIntervalsSatisfyingCondition_linear2() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "p - 1 < 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{(-R,1)}", slodiStr )
    }
    
    @Test
    fun testGetListOfIntervalsSatisfyingCondition_linear3() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "p + 3 = 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{[-3,-3]}", slodiStr )
    }
    
    @Test
    fun testGetListOfIntervalsSatisfyingCondition_zeroPolynomialTrue() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "0 = 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{(-R,+R)}", slodiStr )
    }
        
    @Test
    fun testGetListOfIntervalsSatisfyingCondition_zeroPolynomialFalse() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "0 > 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{}", slodiStr )
    }
    
    @Test
    fun testGetListOfIntervalsSatisfyingCondition_quadratic1() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "p^2 -p -2 <= 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{[-1,2]}", slodiStr )
    }   

    @Test
    fun testGetListOfIntervalsSatisfyingCondition_quadratic2() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "-p^2 +2*p - 1 < 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{(-R,1),(1,+R)}", slodiStr )
    }
    
    @Test
    fun testGetListOfIntervalsSatisfyingCondition_quadratic3() {
        val slodi : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "p^2 -1 = 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{[-1,-1],[1,1]}", slodiStr )
    }   
    
    @Test
    fun testSemiToIntervals() {
        var slodi : SortedListOfDisjunctIntervals = semiToIntervals("p^2 -p -2 <= 0 and -p^2 +2*p-1 < 0", "p" )
        val slodiStr : String = slodi.toString()
        println("SortedListOfDisjunctIntervals: "+slodiStr)
        Assert.assertEquals("{[-1,1),(1,2]}", slodiStr )
    }
    
    @Test
    fun testUniteWithOtherDisjunctList_mutualyDisjoint() {
        //[1/2,1)
        var i1 : Interval = Interval( QHALF, true, false, QONE, false, false )
        //[2,+R)
        var i2 : Interval = Interval( QTWO, true, false, QZERO, false, true )
        //(-R,0]
        var i3 : Interval = Interval( QZERO, false, true, QZERO, true, false )
        
        var slodi1 : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i1,i2) )
        var slodi2 : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i3) )
        println("slodi1:"+slodi1.toString())
        println("slodi2:"+slodi2.toString())
        
        slodi1.uniteWithOtherDisjunctList( slodi2 )
        println("slodi1 after union:"+slodi1.toString())
        val unionStr = slodi1.toString()
        Assert.assertEquals("{(-R,0],[1/2,1),[2,+R)}",unionStr)        
    }
    
    @Test
    fun testUniteWithOtherDisjunctList_overlapping() {
        //[-1,0)
        var i1 : Interval = Interval( QMINUSONE, true, false, QZERO, false, false )
        //[1,R+)
        var i2 : Interval = Interval( QONE, true, false, QZERO, false, true )
        //(1/2,2]
        var i3 : Interval = Interval( QHALF, false, false, QTWO, true, false )
        
        var slodi1 : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i1,i2) )
        var slodi2 : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i3) )
        slodi1.uniteWithOtherDisjunctList( slodi2 )
        val unionStr = slodi1.toString()
        Assert.assertEquals("{[-1,0),(1/2,+R)}",unionStr)  
    }
    
    @Test
    fun testAddIntervalToSLODI_disjoint() {
        //[1/2,1)
        var i1 : Interval = Interval( QHALF, true, false, QONE, false, false )
        //[2,+R)
        var i2 : Interval = Interval( QTWO, true, false, QZERO, false, true )
        var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
        slodi.add( i1 )
        slodi.add( i2 )
        val slodiStr = slodi.toString()
        Assert.assertEquals("{[1/2,1),[2,+R)}",slodiStr)
    }
    
    @Test
    fun testAddIntervalToSLODI_overlapping() {
        //[1,R+)
        var i2 : Interval = Interval( QONE, true, false, QZERO, false, true )
        //(1/2,2]
        var i3 : Interval = Interval( QHALF, false, false, QTWO, true, false )
        var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
        slodi.add( i2 )
        slodi.add( i3 )
        val slodiStr = slodi.toString()
        Assert.assertEquals("{(1/2,+R)}",slodiStr)
    }
    
    @Test
    fun testIntervalIsEmpty() {
        //[-1,0)
        var i1 : Interval = Interval( QMINUSONE, true, false, QZERO, false, false )
        //(-R,0)
        var i2 : Interval = Interval( QZERO, false, true, QZERO, false, false )
        //(0,0)
        var i3 : Interval = Interval( QZERO, false, false, QZERO, false, false )
        
        Assert.assertFalse( i1.isEmpty() )
        Assert.assertFalse( i2.isEmpty() )
        Assert.assertTrue( i3.isEmpty() )
    }
    
    @Test
    fun testIntervalHasNonemptyIntersectionWith(){
        //[-1,0)
        var i1 : Interval = Interval( QMINUSONE, true, false, QZERO, false, false )
        //(-R,0)
        var i2 : Interval = Interval( QZERO, false, true, QZERO, false, false )
        
        Assert.assertTrue( i1.hasNonemptyIntersectionWith( i2 ) )
    }
    
    @Test
    fun testAddIntervalToSLODI_multiple() {
        //[-1,0)
        var i1 : Interval = Interval( QMINUSONE, true, false, QZERO, false, false )
        //(-R,0)
        var i2 : Interval = Interval( QZERO, false, true, QZERO, false, false )
        //(1/2,1)
        var i3 : Interval = Interval( QHALF, false, false, QONE, false, false )
        //(0,1/2)
        var i4 : Interval = Interval( QZERO, false, false, QHALF, false, false )
        //[0,0]
        var i5 : Interval = Interval( QZERO, true, false, QZERO, true, false )
        //(1,2]
        var i6 : Interval = Interval( QONE, false, false, QTWO, true, false )
        //(1/2,+R)
        var i7 : Interval = Interval( QHALF, false, false, QZERO, false, true )
        
        var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>() )
        
        val j1 = slodi.add( i1 )
        println("index where added: "+j1)
        println(slodi.toString())
        val slodiStr1 = slodi.toString()
        Assert.assertEquals("{[-1,0)}",slodiStr1)
        
        val j2 = slodi.add( i2 )
        println(j2)
        println(slodi.toString())
        val slodiStr2 = slodi.toString()
        Assert.assertEquals("{(-R,0)}",slodiStr2) 
                
        val j3 = slodi.add( i3 )
        println(j3)
        println(slodi.toString())
        val slodiStr3 = slodi.toString()
        Assert.assertEquals("{(-R,0),(1/2,1)}",slodiStr3)
        
        val j4 = slodi.add( i4 )
        println(j4)
        println(slodi.toString())
        val slodiStr4 = slodi.toString()
        Assert.assertEquals("{(-R,0),(0,1/2),(1/2,1)}",slodiStr4)
        
        val j5 = slodi.add( i5 )
        println(j5)
        println(slodi.toString())
        val slodiStr5 = slodi.toString()
        Assert.assertEquals("{(-R,1/2),(1/2,1)}",slodiStr5)
        
        val j6 = slodi.add( i6 )
        println(j6)
        println(slodi.toString())
        val slodiStr6 = slodi.toString()
        Assert.assertEquals("{(-R,1/2),(1/2,1),(1,2]}",slodiStr6)
        
        val j7 = slodi.add( i7 )
        println(j7)
        println(slodi.toString())
        val slodiStr7 = slodi.toString()
        Assert.assertEquals("{(-R,1/2),(1/2,+R)}",slodiStr7)
        
    }
    
    @Test
    fun testIntersectWithOtherDisjunctList() {
        var slodi1 : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "p^2 -p -2 <= 0", "p" )
        var slodi2 : SortedListOfDisjunctIntervals = getListOfIntervalsSatisfyingCondition( "-p^2 +2*p-1 < 0", "p" )
        
        slodi1.intersectWithOtherDisjunctList( slodi2 )
        
        val intersectedStr = slodi1.toString()
        Assert.assertEquals("{[-1,1),(1,2]}",intersectedStr)
    }
    
    
    @Test
    fun testIntervalHasNonemptyIntersectionWith2(){
        //[-1,0)
        var i1 : Interval = Interval( QMINUSONE, true, false, QZERO, false, false )
        //[1,R+)
        var i2 : Interval = Interval( QONE, true, false, QZERO, false, true )
        //[1/2,2]
        var i3 : Interval = Interval( QHALF, true, false, QTWO, true, false )
        //(0,1/2)
        var i4 : Interval = Interval( QZERO, false, false, QHALF, false, false )
        //(0,1/2]
        var i5 : Interval = Interval( QZERO, false, false, QHALF, true, false )
    
        Assert.assertTrue( i2.hasNonemptyIntersectionWith( i3 ) )
        Assert.assertFalse( i1.hasNonemptyIntersectionWith( i3 ) )
        Assert.assertFalse( i3.hasNonemptyIntersectionWith( i4 ) ) //the boundary point 1/2 not inside
        Assert.assertTrue( i3.hasNonemptyIntersectionWith( i5 ) ) //the boundary point 1/2 included
    }
    
    
    @Test
    fun testIntersectionOfListAndInterval_1() {
        //[-1,0)
        var i1 : Interval = Interval( QMINUSONE, true, false, QZERO, false, false )
        //[1,R+)
        var i2 : Interval = Interval( QONE, true, false, QZERO, false, true )
        //(1/2,2]
        var i3 : Interval = Interval( QHALF, false, false, QTWO, true, false )
        
        //var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i1,i2) )
        
        var intersection = IntersectionOfListAndInterval( mutableListOf<Interval>(i1,i2), i3 )
        val intersectionStr : String = intersection.toString()
        Assert.assertEquals( "{[1,2]}", intersectionStr )
    }
    
        @Test
        fun testIntersectionOfListAndInterval_2() {
        //(-R,1)
        var i1 : Interval = Interval( QZERO, false, true, QONE, false, false )
        //(1,R+)
        var i2 : Interval = Interval( QONE, false, false, QZERO, false, true )
        //[-1,2]
        var i3 : Interval = Interval( QMINUSONE, true, false, QTWO, true, false )
        
        //var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i1,i2) )
        
        var intersection = IntersectionOfListAndInterval( mutableListOf<Interval>(i1,i2), i3 )
        val intersectionStr : String = intersection.toString()
        Assert.assertEquals( "{[-1,1),(1,2]}", intersectionStr )
    }
}
