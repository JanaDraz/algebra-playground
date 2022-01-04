/*
 * test of helper classes for experiments with reachability etc
 */
package paramFAprototype

import org.junit.Assert
import org.junit.Test

class SystemAnalysisHelperClassesTest : AbstractTest() {
   
    //queue item class
    
    //one rectangle data class
    
    @Test
    fun testOneRectangleData_Add(){
        var oRD : OneRectangleData = OneRectangleData( getBioSystemByName( "001sys" ), arrayOf( 0,1 ), 0, -1, SortedListOfDisjunctIntervalsDouble( mutableListOf( IntervalDouble(0.0, true, false, 2.0, true, false ))))
        var oRDS = oRD.toString()
        Assert.assertEquals("\n\tdir,or=0 -1: {[0.0,2.0]},\n", oRDS) 
        var slodi = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 3.0, true, false )))
        oRD.add( 0, 1, slodi )
        oRDS = oRD.toString()
        Assert.assertEquals("\n\tdir,or=0 -1: {[0.0,2.0]},\n\tdir,or=0 1: {[1.0,3.0]},\n", oRDS) 
        oRD.add( 0, -1, slodi )
        oRDS = oRD.toString()
        Assert.assertEquals("\n\tdir,or=0 -1: {[0.0,3.0]},\n\tdir,or=0 1: {[1.0,3.0]},\n", oRDS)
    }
    
    @Test
    fun testOneRectangleData_IsExplored(){
       val slodi1 = SortedListOfDisjunctIntervalsDouble( mutableListOf( IntervalDouble(0.0, true, false, 2.0, true, false )))  
       var oRD : OneRectangleData = OneRectangleData( getBioSystemByName( "001sys" ), arrayOf( 0,1 ), 0, -1, slodi1 )
       //slodi2 is not whole explored
       val slodi2 = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 3.0, true, false )))
       var boolRes : Boolean = oRD.isExplored( 0, -1, slodi2 )
       Assert.assertEquals(false, boolRes)
       //slodi3 is explored
       val slodi3 = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 2.0, true, false )))
       boolRes = oRD.isExplored( 0, -1, slodi3 )
       Assert.assertEquals(true, boolRes)
       //from this entryset slodi3 is not explored
       boolRes = oRD.isExplored( 0, 1, slodi3 )
       Assert.assertEquals(false, boolRes)
    }
    
    @Test
    fun testOneRectangleData_GetSubtractedSlodiForFurtherExploration(){
       val slodi1 = SortedListOfDisjunctIntervalsDouble( mutableListOf( IntervalDouble(0.0, true, false, 2.0, true, false )))
       var oRD : OneRectangleData = OneRectangleData( getBioSystemByName( "001sys" ), arrayOf( 0,1 ), 0, -1, slodi1 )
       //slodi1 is not whole explored
       val slodi2 = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 3.0, true, false )))
       val slodi3 = oRD.getSubtractedSlodiForFurtherExploration( 0, -1, slodi2 )
       val slodi3Str = slodi3.toString()
       Assert.assertEquals( "{(2.0,3.0]}", slodi3Str )  
    }
    
    //states data class
    
    @Test
    fun testGetOneRectangleParData(){
       val initialSlodi = SortedListOfDisjunctIntervalsDouble(mutableListOf(IntervalDouble(0.0, true, false, 2.0, true, false)))
       val stData = StatesData( getBioSystemByName("001sys"), arrayOf(1,1), initialSlodi, 0, 1 )
       var stringResult : String = stData.toString()
       Assert.assertEquals( "\nRectangle: [1,1], Data: \n\tdir,or=0 1: {[0.0,2.0]},\n", stringResult )
       stringResult = stData.getOneRectangleParData( arrayOf(1,1) ).toString()
       Assert.assertEquals( "\n\tdir,or=0 1: {[0.0,2.0]},\n", stringResult )
    }
    
    @Test
    fun testIsSlodiAlreadyExplored(){
       val initialSlodi = SortedListOfDisjunctIntervalsDouble(mutableListOf(IntervalDouble(0.0, true, false, 2.0, true, false)))
       val stData = StatesData( getBioSystemByName("001sys"), arrayOf(1,1), initialSlodi, 0, 1 )
       val slodi = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 3.0, true, false )))
       var boolResult : Boolean = stData.isSlodiAlreadyExplored( arrayOf(1,1), 0, 1, slodi )
       Assert.assertEquals( false, boolResult )
       boolResult = stData.isSlodiAlreadyExplored( arrayOf(1,1), 0, 1, initialSlodi )
       Assert.assertEquals( true, boolResult )
    }
    
    @Test
    fun testAddSlodiToExplored(){
       val initialSlodi = SortedListOfDisjunctIntervalsDouble(mutableListOf(IntervalDouble(0.0, true, false, 2.0, true, false)))
       val stData = StatesData( getBioSystemByName("001sys"), arrayOf(1,1), initialSlodi, 0, 1 )
       val slodi = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 3.0, true, false )))
       stData.addSlodiToExplored( getBioSystemByName("001sys"), arrayOf(1,1), 0,1, slodi )
       var strResult : String = stData.toString()
       Assert.assertEquals( "\nRectangle: [1,1], Data: \n\tdir,or=0 1: {[0.0,3.0]},\n", strResult )
       var boolResult : Boolean = stData.isSlodiAlreadyExplored( arrayOf(1,1), 0, 1, slodi )
       Assert.assertEquals( true, boolResult )
    }
    
    @Test
    fun testGetSlodiToExploreFurther(){
       val initialSlodi = SortedListOfDisjunctIntervalsDouble(mutableListOf(IntervalDouble(0.0, false, false, 0.0, false, false)))
       val stData = StatesData( getBioSystemByName("001sys"), arrayOf(1,1), initialSlodi, 0, 1 )
       val slodi = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(1.0, true, false, 3.0, true, false )))
       stData.addSlodiToExplored( getBioSystemByName("001sys"), arrayOf(1,1), 0,1, slodi )
       val slodi2 = SortedListOfDisjunctIntervalsDouble(mutableListOf( IntervalDouble(2.0, true, false, 5.0, true, false )))
       val slodi3 = stData.getSlodiToExploreFurther(arrayOf(1,1), 0,1, slodi2)
       var strResult : String = slodi3.toString()
       Assert.assertEquals( "{(3.0,5.0]}", strResult )
    }
    
    //constraint reachable class
    
    @Test
    fun testHasIntersectionWith(){
        val constraint = ConstraintReachable( 0, true, 2.0 ) //x >= 2.0
        var boolResult : Boolean = constraint.hasIntersectionWith( arrayOf(1,1), getBioSystemByName("001sys"))
        Assert.assertEquals( true, boolResult )
    }
    
    @Test
    fun testHasSharpIntersectionWith(){
        val constraint = ConstraintReachable( 0, true, 2.0 ) //x > 2.0
        var boolResult : Boolean = constraint.hasSharpIntersectionWith( arrayOf(1,1), getBioSystemByName("001sys"))
        Assert.assertEquals( false, boolResult )
    }
}
