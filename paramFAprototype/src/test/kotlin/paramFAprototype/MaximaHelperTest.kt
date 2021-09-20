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
    
}
