/*
 * test of calling Maxima
 */
package paramFAprototype

import org.junit.Assert
import org.junit.Test

class MaximaHelperTest : AbstractTest() {

    @Test
    fun testGetResultFromMaxima() {
        val commandsForMaxima = ArrayList<String>().apply {
            add("load(\"simplex\")$") // /$ ??
            add("display2d:false$") 
            add("minimize_lp(a+e+i,")
            add("[a-b-d-c-g+h+f+e+i>=1, ")
            add("a+b+d+e>=1, ")
            add("a-b-d+c+g-h-f+e+i>=1,")
            add("a-c-g+i>=0,")
            add("a+b+d-c-g-h-f+e+i>=0,")
            add("a-b-d+e>=0,")
            add("a>=0,")
            add("a+c+g+i>=0,")
            add("a+b+d+c+g+h+f+e+i>=0]);")
            add("quit();\n")
        }
        /*for( c in commandsForMaxima){
            println( c )
        }*/
        val resultStr = getResultFromMaxima(commandsForMaxima)
        /*for( c in resultStr){
            println( c )
        }*/
        val resultLine = digResultLineFromMaxima( resultStr )
        println(resultLine)
        var map : MutableMap<String,String> = getCoefMapFromResultLine( resultLine )
        print(map)
        Assert.assertEquals(map["goal"], "1/2")
        Assert.assertEquals(map["a"], "1")
    }
    
    @Test
    fun testCreateCommandsForMaximaLPTaskPar2Deg2(){
        val pmin : Double = 1.0
        val pmax : Double = 2.0
        val qmin : Double = 1.0
        val qmax : Double = 3.0
        val pin : List<Double> = listOf<Double>(1.0, 2.0, 1.0)
        val qin : List<Double> = listOf<Double>(1.0, 2.0, 3.0)
        val pout : List<Double> = listOf<Double>(1.0, 1.5, 1.5, 1.5, 2.0, 2.0)
        val qout : List<Double> = listOf<Double>(2.0, 1.0, 2.0, 3.0, 1.0, 3.0)
        val commandsForMaxima : ArrayList<String> = createCommandsForMaximaLPTaskPar2Deg2(pmin, pmax, qmin, qmax, pin, qin, pout, qout, "")
 
        val resultStr = getResultFromMaxima(commandsForMaxima)
        /*( c in resultStr){
            println( c )
        }*/
        val resultLine = digResultLinesFromMaxima( resultStr )
        println(resultLine)
        var map : MutableMap<String,String> = getCoefMapFromResultLine( resultLine )
        print(map)
        Assert.assertEquals(map["goal"], "1.333333333333333")
        Assert.assertEquals(map["a"], "0.0")
    }
}
