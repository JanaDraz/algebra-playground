/*
 * test of work with polynomials
 */
package paramFAprototype

import org.junit.Assert
import org.junit.Test

class PolynomialParserTest : AbstractTest() {
    
    @Test
    fun testStrToPolyInQ() {
        val vars = arrayOf( "x", "y", "p" )
        val polynom = strToMPolyInQ( "x^2+5*y*p+1", vars )
        val polStr = polynom.toString(*vars)
        Assert.assertEquals("1+5*y*p+x^2", polStr)
    }   
    
    @Test
    fun testStrToPolyInQ_ZeroPolStr() {
        val vars = arrayOf( "x", "y", "p" )
        val polynom = strToMPolyInQ( "0", vars )
        val polStr = polynom.toString(*vars)
        Assert.assertEquals("0", polStr)
    }
    
}
