/*
 * test of work with polynomials
 */
package paramFAprototype

import org.junit.Assert
import org.junit.Test

class SemialgebraicRealmDoubleTest : AbstractTest() {
    
    @Test
    fun testUniteWithOtherDisjunctList_mutualyDisjoint() {
        //[1/2,1)
        var i1 : IntervalDouble = IntervalDouble( 0.5, true, false, 1.0, false, false )
        //[2,+R)
        var i2 : IntervalDouble = IntervalDouble( 2.0, true, false, 0.0, false, true )
        //(-R,0]
        var i3 : IntervalDouble = IntervalDouble( 0.0, false, true, 0.0, true, false )
        
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i1,i2) )
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i3) )
        println("slodi1:"+slodi1.toString())
        println("slodi2:"+slodi2.toString())
        
        slodi1.uniteWithOtherDisjunctList( slodi2 )
        println("slodi1 after union:"+slodi1.toString())
        val unionStr = slodi1.toString()
        Assert.assertEquals("{(-R,0.0],[0.5,1.0),[2.0,+R)}",unionStr)        
    }
    
    @Test
    fun testUniteWithOtherDisjunctList_overlapping() {
        //[-1,0)
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, false, false )
        //[1,R+)
        var i2 : IntervalDouble = IntervalDouble( 1.0, true, false, 0.0, false, true )
        //(1/2,2]
        var i3 : IntervalDouble = IntervalDouble( 0.5, false, false, 2.0, true, false )
        
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i1,i2) )
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i3) )
        slodi1.uniteWithOtherDisjunctList( slodi2 )
        val unionStr = slodi1.toString()
        Assert.assertEquals("{[-1.0,0.0),(0.5,+R)}",unionStr)  
    }
    
    @Test
    fun testUniteThreeDisjunctLists_adjacent_closed_intervals() {
        //[0,1]
        var i1 : IntervalDouble = IntervalDouble( 0.0, true, false, 1.0, true, false )
        //[1,2]
        var i2 : IntervalDouble = IntervalDouble( 1.0, true, false, 2.0, true, false )
        //[-1,0]
        var i3 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, true, false )
        
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i1) )
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i2) )
        var slodi3 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>(i3) )
        slodi1.uniteWithOtherDisjunctList( slodi2 )
        val unionStr = slodi1.toString()
        Assert.assertEquals("{[0.0,2.0]}",unionStr)  
        slodi1.uniteWithOtherDisjunctList( slodi3 )
        val unionStr2 = slodi1.toString()
        Assert.assertEquals("{[-1.0,2.0]}",unionStr2)
    }
    
    @Test
    fun testAddIntervalToSLODI_disjoint() {
        //[1/2,1)
        var i1 : IntervalDouble = IntervalDouble( 0.5, true, false, 1.0, false, false )
        //[2,+R)
        var i2 : IntervalDouble = IntervalDouble( 2.0, true, false, 0.0, false, true )
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        slodi.add( i1 )
        slodi.add( i2 )
        val slodiStr = slodi.toString()
        Assert.assertEquals("{[0.5,1.0),[2.0,+R)}",slodiStr)
    }
    
    @Test
    fun testAddIntervalToSLODI_overlapping() {
        //[1,R+)
        var i2 : IntervalDouble = IntervalDouble( 1.0, true, false, 0.0, false, true )
        //(1/2,2]
        var i3 : IntervalDouble = IntervalDouble( 0.5, false, false, 2.0, true, false )
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        slodi.add( i2 )
        slodi.add( i3 )
        val slodiStr = slodi.toString()
        Assert.assertEquals("{(0.5,+R)}",slodiStr)
    }
    
    @Test
    fun testIntervalIsEmpty() {
        //[-1,0)
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, false, false )
        //(-R,0)
        var i2 : IntervalDouble = IntervalDouble( 0.0, false, true, 0.0, false, false )
        //(0,0)
        var i3 : IntervalDouble = IntervalDouble( 0.0, false, false, 0.0, false, false )
        
        Assert.assertFalse( i1.isEmpty() )
        Assert.assertFalse( i2.isEmpty() )
        Assert.assertTrue( i3.isEmpty() )
    }
    
    @Test
    fun testIntervalHasNonemptyIntersectionWith(){
        //[-1,0)
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, false, false )
        //(-R,0)
        var i2 : IntervalDouble = IntervalDouble( 0.0, false, true, 0.0, false, false )
        
        Assert.assertTrue( i1.hasNonemptyIntersectionWith( i2 ) )
    }
    
    @Test
    fun testAddIntervalToSLODI_multiple() {
        //[-1,0)
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, false, false )
        //(-R,0)
        var i2 : IntervalDouble = IntervalDouble( 0.0, false, true, 0.0, false, false )
        //(1/2,1)
        var i3 : IntervalDouble = IntervalDouble( 0.5, false, false, 1.0, false, false )
        //(0,1/2)
        var i4 : IntervalDouble = IntervalDouble( 0.0, false, false, 0.5, false, false )
        //[0,0]
        var i5 : IntervalDouble = IntervalDouble( 0.0, true, false, 0.0, true, false )
        //(1,2]
        var i6 : IntervalDouble = IntervalDouble( 1.0, false, false, 2.0, true, false )
        //(1/2,+R)
        var i7 : IntervalDouble = IntervalDouble( 0.5, false, false, 0.0, false, true )
        
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        
        val j1 = slodi.add( i1 )
        println("index where added: "+j1)
        println(slodi.toString())
        val slodiStr1 = slodi.toString()
        Assert.assertEquals("{[-1.0,0.0)}",slodiStr1)
        
        val j2 = slodi.add( i2 )
        println(j2)
        println(slodi.toString())
        val slodiStr2 = slodi.toString()
        Assert.assertEquals("{(-R,0.0)}",slodiStr2) 
                
        val j3 = slodi.add( i3 )
        println(j3)
        println(slodi.toString())
        val slodiStr3 = slodi.toString()
        Assert.assertEquals("{(-R,0.0),(0.5,1.0)}",slodiStr3)
        
        val j4 = slodi.add( i4 )
        println(j4)
        println(slodi.toString())
        val slodiStr4 = slodi.toString()
        Assert.assertEquals("{(-R,0.0),(0.0,0.5),(0.5,1.0)}",slodiStr4)
        
        val j5 = slodi.add( i5 )
        println(j5)
        println(slodi.toString())
        val slodiStr5 = slodi.toString()
        Assert.assertEquals("{(-R,0.5),(0.5,1.0)}",slodiStr5)
        
        val j6 = slodi.add( i6 )
        println(j6)
        println(slodi.toString())
        val slodiStr6 = slodi.toString()
        Assert.assertEquals("{(-R,0.5),(0.5,1.0),(1.0,2.0]}",slodiStr6)
        
        val j7 = slodi.add( i7 )
        println(j7)
        println(slodi.toString())
        val slodiStr7 = slodi.toString()
        Assert.assertEquals("{(-R,0.5),(0.5,+R)}",slodiStr7)
        
    }
    
    @Test
    fun testAddIntervalToSLODI_multiple_closed() {
        //[-1,0]
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, true, false )
        //[0,1]
        var i2 : IntervalDouble = IntervalDouble( 0.0, true, false, 1.0, true, false )
        //[1,2]
        var i3 : IntervalDouble = IntervalDouble( 1.0, true, false, 2.0, true, false )
        
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        
        val j1 = slodi.add( i1 )
        println("index where added: "+j1)
        println(slodi.toString())
        val slodiStr1 = slodi.toString()
        Assert.assertEquals("{[-1.0,0.0]}",slodiStr1)
        
        val j2 = slodi.add( i2 )
        println(j2)
        println(slodi.toString())
        val slodiStr2 = slodi.toString()
        Assert.assertEquals("{[-1.0,1.0]}",slodiStr2) 
                
        val j3 = slodi.add( i3 )
        println(j3)
        println(slodi.toString())
        val slodiStr3 = slodi.toString()
        Assert.assertEquals("{[-1.0,2.0]}",slodiStr3)
        
    }
    
    @Test
    fun testAddIntervalToSLODI_multiple_closed_mixedorder() {
        //[-1,0]
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, true, false )
        //[0,1]
        var i2 : IntervalDouble = IntervalDouble( 0.0, true, false, 1.0, true, false )
        //[1,2]
        var i3 : IntervalDouble = IntervalDouble( 1.0, true, false, 2.0, true, false )
        
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        
        val j1 = slodi.add( i2 )
        println("index where added: "+j1)
        println(slodi.toString())
        val slodiStr1 = slodi.toString()
        Assert.assertEquals("{[0.0,1.0]}",slodiStr1)
        
        val j2 = slodi.add( i3 )
        println(j2)
        println(slodi.toString())
        val slodiStr2 = slodi.toString()
        Assert.assertEquals("{[0.0,2.0]}",slodiStr2) 
                
        val j3 = slodi.add( i1 )
        println(j3)
        println(slodi.toString())
        val slodiStr3 = slodi.toString()
        Assert.assertEquals("{[-1.0,2.0]}",slodiStr3)
        
    }
    
    @Test
    fun testAddIntervalToSLODI_multiple_closed_mixedorder2() {
        //[-1,0]
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, true, false )
        //[0,1]
        var i2 : IntervalDouble = IntervalDouble( 0.0, true, false, 1.0, true, false )
        //[1,2]
        var i3 : IntervalDouble = IntervalDouble( 1.0, true, false, 2.0, true, false )
        
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble( mutableListOf<IntervalDouble>() )
        
        val j1 = slodi.add( i3 )
        println("index where added: "+j1)
        println(slodi.toString())
        val slodiStr1 = slodi.toString()
        Assert.assertEquals("{[1.0,2.0]}",slodiStr1)
        
        val j2 = slodi.add( i1 )
        println(j2)
        println(slodi.toString())
        val slodiStr2 = slodi.toString()
        Assert.assertEquals("{[-1.0,0.0],[1.0,2.0]}",slodiStr2) 
                
        val j3 = slodi.add( i2 )
        println(j3)
        println(slodi.toString())
        val slodiStr3 = slodi.toString()
        Assert.assertEquals("{[-1.0,2.0]}",slodiStr3)
        
    }
    
    
    @Test
    fun testIntervalHasNonemptyIntersectionWith2(){
        //[-1,0)
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, false, false )
        //[1,R+)
        var i2 : IntervalDouble = IntervalDouble( 1.0, true, false, 0.0, false, true )
        //[1/2,2]
        var i3 : IntervalDouble = IntervalDouble( 0.5, true, false, 2.0, true, false )
        //(0,1/2)
        var i4 : IntervalDouble = IntervalDouble( 0.0, false, false, 0.5, false, false )
        //(0,1/2]
        var i5 : IntervalDouble = IntervalDouble( 0.0, false, false, 0.5, true, false )
    
        Assert.assertTrue( i2.hasNonemptyIntersectionWith( i3 ) )
        Assert.assertFalse( i1.hasNonemptyIntersectionWith( i3 ) )
        Assert.assertFalse( i3.hasNonemptyIntersectionWith( i4 ) ) //the boundary point 1/2 not inside
        Assert.assertTrue( i3.hasNonemptyIntersectionWith( i5 ) ) //the boundary point 1/2 included
    }
    
    
    @Test
    fun testIntersectionOfListAndInterval_1() {
        //[-1,0)
        var i1 : IntervalDouble = IntervalDouble( -1.0, true, false, 0.0, false, false )
        //[1,R+)
        var i2 : IntervalDouble = IntervalDouble( 1.0, true, false, 0.0, false, true )
        //(1/2,2]
        var i3 : IntervalDouble = IntervalDouble( 0.5, false, false, 2.0, true, false )
        
        //var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i1,i2) )
        
        var intersection = IntersectionOfListAndIntervalDouble( mutableListOf<IntervalDouble>(i1,i2), i3 )
        val intersectionStr : String = intersection.toString()
        Assert.assertEquals( "{[1.0,2.0]}", intersectionStr )
    }
    
    @Test
    fun testIntersectionOfListAndInterval_2() {
        //(-R,1)
        var i1 : IntervalDouble = IntervalDouble( 0.0, false, true, 1.0, false, false )
        //(1,R+)
        var i2 : IntervalDouble = IntervalDouble( 1.0, false, false, 0.0, false, true )
        //[-1,2]
        var i3 : IntervalDouble = IntervalDouble( -1.0, true, false, 2.0, true, false )
        
        //var slodi : SortedListOfDisjunctIntervals = SortedListOfDisjunctIntervals( mutableListOf<Interval>(i1,i2) )
        
        var intersection = IntersectionOfListAndIntervalDouble( mutableListOf<IntervalDouble>(i1,i2), i3 )
        val intersectionStr : String = intersection.toString()
        Assert.assertEquals( "{[-1.0,1.0),(1.0,2.0]}", intersectionStr )
    }
    
    @Test
    fun testIntervalGetComplement(){
       //(-R,1)
        var i1 : IntervalDouble = IntervalDouble( 0.0, false, true, 1.0, false, false )
        //(1,R+)
        var i2 : IntervalDouble = IntervalDouble( 1.0, false, false, 0.0, false, true )
        //[-1,2]
        var i3 : IntervalDouble = IntervalDouble( -1.0, true, false, 2.0, true, false )
        
        var com1 : SortedListOfDisjunctIntervalsDouble = i1.getComplement()
        val com1str : String = com1.toString()
        Assert.assertEquals("{[1.0,+R)}", com1str)
        
        var com2 : SortedListOfDisjunctIntervalsDouble = i2.getComplement()
        val com2str : String = com2.toString()
        Assert.assertEquals("{(-R,1.0]}", com2str)
        
        var com3 : SortedListOfDisjunctIntervalsDouble = i3.getComplement()
        val com3str : String = com3.toString()
        Assert.assertEquals("{(-R,-1.0),(2.0,+R)}", com3str)
    }
    
    @Test
    fun testSlodiGetComplement(){
        //[10,R+)
        var i1 : IntervalDouble = IntervalDouble( 10.0, true, false, 0.0, false, true )
        //(-1,2]
        var i2 : IntervalDouble = IntervalDouble( -1.0, false, false, 2.0, true, false )
        var slodi : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i1,i2))
        val com = slodi.getComplement()
        val comstr : String = com.toString()
        Assert.assertEquals("{(-R,-1.0],(2.0,10.0)}", comstr)
    }
    
    @Test
    fun testSlodiIsEqual(){
        //tests equality after merging fitting intervals
        
        //[-10,0)
        var i1 : IntervalDouble = IntervalDouble( -10.0, true, false, 0.0, false, false )
        //[0,2) slodi1 will be [-10,2)
        var i2 : IntervalDouble = IntervalDouble( 0.0, true, false, 2.0, false, false )
        
        //[-10,1]
        var i3 : IntervalDouble = IntervalDouble( -10.0, true, false, 1.0, true, false )
        //(1,2) slodi2 will be [-10,2)
        var i4 : IntervalDouble = IntervalDouble( 1.0, false, false, 2.0, false, false )
        
        //[-10,-1) -1 is not inside slodi3
        var i5 : IntervalDouble = IntervalDouble( -10.0, true, false, -1.0, false, false )
        //(-1,2)
        var i6 : IntervalDouble = IntervalDouble( -1.0, false, false, 2.0, false, false )
        
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i1,i2))
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i3,i4)) 
        var slodi3 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i5,i6)) 
        Assert.assertEquals( true, slodi1.isEqual( slodi2 ) )
        Assert.assertEquals( false, slodi1.isEqual( slodi3 ) )
    }
    
    @Test
    fun testSlodiIsSubsetOf(){
        //[-10,0)
        var i1 : IntervalDouble = IntervalDouble( -10.0, true, false, 0.0, false, false )
        //[0,2) slodi1 will be [-10,2)
        var i2 : IntervalDouble = IntervalDouble( 0.0, true, false, 2.0, false, false )
        
        //[-10,1]
        var i3 : IntervalDouble = IntervalDouble( -10.0, true, false, 1.0, true, false )
        //(1,2] slodi2 will be [-10,2] has 2, slodi1 does not include 2
        var i4 : IntervalDouble = IntervalDouble( 1.0, false, false, 2.0, true, false )
        
        //[-10,-1) -1 is not inside slodi3
        var i5 : IntervalDouble = IntervalDouble( -10.0, true, false, -1.0, false, false )
        //(-1,2)
        var i6 : IntervalDouble = IntervalDouble( -1.0, false, false, 2.0, false, false )
        
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i1,i2))
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i3,i4)) 
        var slodi3 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i5,i6)) 
        Assert.assertEquals( true, slodi3.isSubsetOf( slodi1 ) )
        Assert.assertEquals( false, slodi2.isSubsetOf( slodi1 ) )
    }
    
    @Test
    fun testSlodiSubtractOtherDisjunctList(){
       //[-10,0)
        var i1 : IntervalDouble = IntervalDouble( -10.0, true, false, 0.0, false, false )
        //[0,2) slodi1 will be [-10,2)
        var i2 : IntervalDouble = IntervalDouble( 0.0, true, false, 2.0, false, false )
        
        //[-10,1]
        var i3 : IntervalDouble = IntervalDouble( -10.0, true, false, 1.0, true, false )
        //(1,2] slodi2 will be [-10,2] has 2, slodi1 does not include 2
        var i4 : IntervalDouble = IntervalDouble( 1.0, false, false, 2.0, true, false )
        
        //[-10,-1) -1 is not inside slodi3
        var i5 : IntervalDouble = IntervalDouble( -10.0, true, false, -1.0, false, false )
        //(-1,2)
        var i6 : IntervalDouble = IntervalDouble( -1.0, false, false, 2.0, false, false )
        
        //[-10,1]
        var i7 : IntervalDouble = IntervalDouble( -10.0, true, false, 1.0, true, false )
        //(1,2] slodi4 will be [-10,2] has 2
        var i8 : IntervalDouble = IntervalDouble( 1.0, false, false, 2.0, true, false )        
        
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i1,i2))
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i3,i4)) 
        var slodi3 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i5,i6)) 
        var slodi4 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i7,i8)) 
        
        slodi2.subtractOtherDisjunctList( slodi1 )
        slodi4.subtractOtherDisjunctList( slodi3 )
        Assert.assertEquals( "{[2.0,2.0]}", slodi2.toString() )
        Assert.assertEquals( "{[-1.0,-1.0],[2.0,2.0]}", slodi4.toString() ) 
    }
    
    @Test
    fun testSlodiIsNonempty(){
        //(0,0)
        var i1 : IntervalDouble = IntervalDouble( 0.0, false, false, 0.0, false, false )
        //(0,0) slodi1 is empty
        var i2 : IntervalDouble = IntervalDouble( 0.0, false, false, 0.0, false, false )
        
        //[-10,1]
        var i3 : IntervalDouble = IntervalDouble( -10.0, true, false, 1.0, true, false )
        //(1,2] slodi2 will be [-10,2] has 2, slodi1 does not include 2
        var i4 : IntervalDouble = IntervalDouble( 1.0, false, false, 2.0, true, false )
       
        var slodi1 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i1,i2))
        var slodi2 : SortedListOfDisjunctIntervalsDouble = SortedListOfDisjunctIntervalsDouble(mutableListOf(i3,i4)) 
        
        Assert.assertEquals( false, slodi1.isNonempty() )
        Assert.assertEquals( true, slodi2.isNonempty() )
    }
}
