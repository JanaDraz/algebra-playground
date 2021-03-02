package paramFAprototype

import cc.redberry.rings.Rational
import cc.redberry.rings.Rings
import cc.redberry.rings.Rings.Q
import cc.redberry.rings.bigint.BigInteger
import cc.redberry.rings.poly.multivar.MultivariatePolynomial
import cc.redberry.rings.poly.univar.UnivariatePolynomial

typealias NumQ = Rational<BigInteger>
typealias MPolyInQ = MultivariatePolynomial<NumQ>
typealias UPolyInQ = UnivariatePolynomial<NumQ>
typealias MPolyInZ = MultivariatePolynomial<BigInteger>
typealias UMPolyInZ = UnivariatePolynomial<MPolyInZ>
typealias UPolyInZ = UnivariatePolynomial<BigInteger>

/* Several examples of the bio systems to be analyzed
 * tresholds as strings in decimal notation
 */
 
class BioSystem(   @JvmField var dim : Int,
                   @JvmField var paramCount : Int, 
                   @JvmField var varStrings : Array<String>,
                   @JvmField var parStrings : Array<String>,
                   @JvmField var derStrings : Array<String>,
                   @JvmField var secondDerStrings : Array<String>,
                   @JvmField var halfSecondDerStrings : Array<String>,
                   @JvmField var tresholdsStrings : Array<Array<String>>,
                   @JvmField var maxT : String,
                   @JvmField var taylDeg : Int ) {
    @JvmField var allVars : Array<String> = varStrings + parStrings
    @JvmField var derPols : MutableList<MPolyInQ> = mutableListOf<MPolyInQ>()
    @JvmField var secondDerPols : MutableList<MPolyInQ> = mutableListOf<MPolyInQ>()
    @JvmField var halfSecondDerPols : MutableList<MPolyInQ> = mutableListOf<MPolyInQ>()
    @JvmField var tresholdsQ : MutableList<MutableList<NumQ>> = mutableListOf<MutableList<NumQ>>()
    @JvmField var maxTQ : NumQ
    init {
        //TODO parse derivatives and tresholds from strings
        //strToMPolyInQ(poly : String, vars : Array<String>) : MPolyInQ
        for( i in 0..(dim-1)) {
            this.derPols.add( strToMPolyInQ( derStrings[i], allVars ) )
            this.secondDerPols.add( strToMPolyInQ( secondDerStrings[i], allVars ) )
            this.halfSecondDerPols.add( strToMPolyInQ( halfSecondDerStrings[i], allVars ) )
            this.tresholdsQ.add(mutableListOf<NumQ>())
            for( j in 0..(tresholdsStrings[i].size-1) ) {
                this.tresholdsQ[i].add( strToQ( tresholdsStrings[i][j] ) )
            } 
        }
        this.maxTQ = strToQ( maxT )
    }
    
    fun getDim() : Int {
        return this.dim
    }
    
    fun getVarStrings() : Array<String> {
        return this.varStrings
    }
    
    fun getParStr() : String {
        return parStrings.joinToString( separator="," )
    }
    
    fun getAllVars() : Array<String> {
        return this.allVars
    }
    
    fun getTres( variable : Int, coord : Int ) : String {
        return tresholdsStrings[ variable ][ coord ]
    }
    
    fun getMaxT() : String {
        return this.maxT
    }
    
    fun getDegTayl() : Int {
        return this.taylDeg
    }
    
    fun getDerPols() : Array<MPolyInQ> {
        return this.derPols.toTypedArray()
    }
    
    
}


/**
 * SYSTEM 001
 * Param p, ODE:
 * x' = 1
 * y' = p
 * rectangle [0,1]x[0,1]
 * facet 1: 0x[0,1]
 * facet 2: 1x[0,1]
 * p in [-2,2] ... [-1,1] by mel byt hledany inteval
 * pro ktery existuje trajektorie z facet1 do facet2
 *
 * try several steps (k = 1)
 * time for one transition k * delta
 * time fo one step delta=2
 * taylor polyn degree m=2 (in this case =1, because for constant ode the terms are 0
 */
 /**
 * System 002
 * Param p, ODE:
 * x' = x+1
 * y' = y+p
 * x'' = x' + 0 = x+1
 * y'' = y' + 0 = y+p
 * rectangle [0,1]x[0,1]
 * facet 1: 0x[0,1]
 * facet 2: 1x[0,1]
 * p in [?,?] by mel byt hledany inteval
 * pro ktery existuje trajektorie z facet1 do facet2
 *
 * try several steps (k = 1)
 * time for one transition max k * delta
 * max time fo one step delta=2
 * taylor polyn degree m=2
 *
 * try several steps (k = 2)
 * time for one transition max k * delta
 * max time fo one step delta=2
 * taylor polyn degree m=2
 *
 *
 */
 //001    var facetToFacetFormula = 
 //ex("x0",ex("y0",ex("x1",ex("y1",
 //ex("d","x0=0 and x1=1 and y0>=0 and y0<=1 and y1>=0 and y1<=1 and d>=0 and d<=2 and x1=(x0+d) and y1=(y0+p*d)")))))
//002a
/*    var facetToFacetFormula = ex("x0",ex("y0",ex("x1",ex("y1",ex(
        "d",
        "x0=0 and x1=1 and y0>=0 and y0<=1 and y1>=0 and y1<=1 and d>=0 and d<=2 " +
                "and x1=((x0*(d^2+2*d+2)/2)+d) and y1=((y0*(d^2+2*d+2)/2)+p*d)"
    )))))*/
//002b
/*    var facetToFacetFormula = ex("x0",ex("y0",
                                ex("x1",ex("y1",
                                ex("x2",ex("y2",
        ex("d",
        "x0=0 and x2=1 and y0>=0 and y0<=1 " +
                "and y1>=0 and y1<=1 " +
                "and x1>=0 and x1<=1 " +
                "and y2>=0 and y2<=1 " +
                "and d>=0 and d<=2 " +
                "and x1=((x0*(d^2+2*d+2)/2)+d) and y1=((y0*(d^2+2*d+2)/2)+p*d)" +
                "and x2=((x1*(d^2+2*d+2)/2)+d) and y2=((y1*(d^2+2*d+2)/2)+p*d)"
    )))))))*/
//002c
/*    var facetToFacetFormula = ex("x0",ex("y0",
        ex("x1",ex("y1",
            ex("x2",ex("y2",
                ex("x3", ex("y3",
                ex("d",
                    "x0=0 and x3=1 and y0>=0 and y0<=1 " +
                            "and y1>=0 and y1<=1 " +
                            "and x1>=0 and x1<=1 " +
                            "and y2>=0 and y2<=1 " +
                            "and x2>=0 and x2<=1 " +
                            "and y3>=0 and y3<=1 " +
                            "and d>=0 and d<=1 " +
                            "and x1=((x0*(d^2+2*d+2)/2)+d) and y1=((y0*(d^2+2*d+2)/2)+p*d)" +
                            "and x2=((x1*(d^2+2*d+2)/2)+d) and y2=((y1*(d^2+2*d+2)/2)+p*d)" +
                            "and x3=((x2*(d^2+2*d+2)/2)+d) and y3=((y2*(d^2+2*d+2)/2)+p*d)"
                )))))))))*/
//003a
    /*
    var facetToFacetFormula = ex("x0",ex("y0",ex("x1",ex("y1",ex(
        "d",
        "x0=0 and x1=1 and y0>=0 and y0<=1 and y1>=0 and y1<=1 and d>=0 and d<=2 " +
                "and x1=((x0*(d^2+2*d+2)/2)+d) and y1=((y0*(d^2+2*d+2)/2)+p*d)"
    )))))*/
//003b
/*    var facetToFacetFormula = ex("x0",ex("y0",
                                ex("x1",ex("y1",
                                ex("x2",ex("y2",
        ex("d",
        "x0=0 and x2=1 and y0>=0 and y0<=1 " +
                "and y1>=0 and y1<=1 " +
                "and x1>=0 and x1<=1 " +
                "and y2>=0 and y2<=1 " +
                "and d>=0 and d<=2 " +
                "and x1=((x0*(d^2+2*d+2)/2)+d) and y1=((y0*(d^2+2*d+2)/2)+p*d)" +
                "and x2=((x1*(d^2+2*d+2)/2)+d) and y2=((y1*(d^2+2*d+2)/2)+p*d)"
    )))))))*/
//003c
/*    var facetToFacetFormula = ex("x0",ex("y0",
        ex("x1",ex("y1",
            ex("x2",ex("y2",
                ex("x3", ex("y3",
                    ex("d",
                        "x0=0 and x3=1 and y0>=0 and y0<=1 " +
                                "and y1>=0 and y1<=1 " +
                                "and x1>=0 and x1<=1 " +
                                "and y2>=0 and y2<=1 " +
                                "and x2>=0 and x2<=1 " +
                                "and y3>=0 and y3<=1 " +
                                "and d>=0 and d<=1 " +
                                "and x1=((x0*(d^2+2*d+2)/2)+d) and y1=((y0*(d^2+2*d+2)/2)+p*d)" +
                                "and x2=((x1*(d^2+2*d+2)/2)+d) and y2=((y1*(d^2+2*d+2)/2)+p*d)" +
                                "and x3=((x2*(d^2+2*d+2)/2)+d) and y3=((y2*(d^2+2*d+2)/2)+p*d)"
                    )))))))))*/
/*class BioSystem(    var dim : Int,
                    var paramCount : Int, 
                    var varStrings : Array<String>,
                    var parStrings : Array<String>,
                    var derStrings : Array<String>,
                    var secondDerStrings : Array<String>,
                    var halfSecondDerStrings : Array<String>,
                    var tresholdsStrings : Array<Array<String>> ) 
*/                    
fun getBioSystemByName( name : String ) : BioSystem {
    when( name ){
        "001" -> return BioSystem( 2, 1, 
                             arrayOf( "x", "y" ),
                             arrayOf( "p" ),
                             arrayOf( "1", "p" ),
                             arrayOf( "0", "0" ),
                             arrayOf( "0", "0" ),
                             arrayOf( arrayOf( "0","1","2" ),
                                      arrayOf( "0","1","2" ) ),
                             "1", 1 )
        "002" -> return BioSystem( 2, 1,
                             arrayOf( "x", "y" ),
                             arrayOf( "p" ),
                             arrayOf( "x+1", "y+p" ),
                             arrayOf( "x+1", "y+p" ),
                             arrayOf( "0.5*x+0.5", "0.5*y+0.5*p" ),
                             arrayOf( arrayOf( "0","1","2" ),
                                      arrayOf( "0","1","2" ) ),
                             "1", 1 )
                             
        else -> return BioSystem( 2, 1, //zero dummy system
                             arrayOf( "x", "y" ),
                             arrayOf( "p" ),
                             arrayOf( "0", "0" ),
                             arrayOf( "0", "0" ),
                             arrayOf( "0", "0" ),
                             arrayOf( arrayOf( "0","1","2" ),
                                      arrayOf( "0","1","2" ) ),
                             "1", 1 )
    }
}
