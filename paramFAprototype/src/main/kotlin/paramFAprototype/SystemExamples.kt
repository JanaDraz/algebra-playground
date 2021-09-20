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


/* Approximation of a hill function
 * on xi-th variable 
 * with name xstr
 * for a rectangle with given coords
 */
fun hillapprox( xstr: String, xi : Int, coords : Array<Int>, xtres : Array<Double>, yvals : Array<Double>) : String {
    var result : String = ""
    //find the right treshold
    var i : Int = coords[ xi ]
    //x between 2 tresholds
    var x1 : Double = xtres[ i ]
    var x2 : Double = xtres[ i+1 ]
    var y1 : Double = yvals[ i ]
    var y2 : Double = yvals[ i+1 ]
    var a : Double = (y1-y2)/(x2-x1)
    var b : Double = (x2*y2-x1*y1)/(x2-x1)
    if( b < 0 ){
        result = str(a)+"*"+xstr+"-"+str(-b)
    }else{
        result = str(a)+"*"+xstr+"+"+str(b)
    }
    return result
}

fun hillapproxVal( xstr: String, xval : Double, xtres : Array<Double>, yvals : Array<Double>) : String {
    var result : String = ""
    //find the right treshold
    var i : Int = 0
    while( i<xtres.size && xtres[i] < xval ){
        i++
    }
    //approximate the yvalue
    if( i == xtres.size ){ //yn
        result = str(yvals[ i-1 ])
    }else{//x between 2 tresholds
        var x1 : Double = xtres[ i ]
        var x2 : Double = xtres[ i+1 ]
        var y1 : Double = yvals[ i ]
        var y2 : Double = yvals[ i+1 ]
        var a : Double = (y1-y2)/(x2-x1)
        var b : Double = (x2*y2-x1*y1)/(x2-x1)
        if( b < 0 ){
            result = str(a)+"*"+xstr+"-"+str(-b)
        }else{
            result = str(a)+"*"+xstr+"+"+str(b)
        }
    }
    return result
}

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
                   @JvmField var taylDeg : Int,
                   @JvmField var prefixEquations: Map<String, String> ) {
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
    
    fun getParamCount() : Int {
        return this.paramCount
    }
    
    fun getVarStrings() : Array<String> {
        return this.varStrings
    }
    
    fun getParStrings() : Array<String> {
        return this.parStrings
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
    
    fun getMinTres( variable : Int ) : String {
        return tresholdsStrings[ variable ][ 0 ]
    }
    
    fun getMaxTres( variable : Int ) : String {
        return tresholdsStrings[ variable ][ (tresholdsStrings[ variable ].size) - 1 ]
    }
    
    fun getMinOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else
                return tresholdsStrings[ variable ][ state[ dir ] ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] ]
        }
    }
    
    fun getMaxOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else
                return tresholdsStrings[ variable ][ state[ dir ] ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] + 1 ]
        }
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
    
    fun get2DerHalfPols() : Array<MPolyInQ> {
        return this.halfSecondDerPols.toTypedArray()
    }
    
    fun getPrefixEquations() : Map<String,String> {
        return this.prefixEquations
    }
    
}

/* PIECEWISE DERIVATIVE
 */
 
class BioSystemPWMA( @JvmField var dim : Int,
                   @JvmField var paramCount : Int, 
                   @JvmField var varStrings : Array<String>,
                   @JvmField var parStrings : Array<String>,
                   @JvmField var derStringFunctions : Array<(Array<Int>)-> String>,
                   @JvmField var tresholdsStrings : Array<Array<String>>,
                   @JvmField var maxT : String,
                   @JvmField var taylDeg : Int,
                   @JvmField var prefixEquations: Map<String, String> ) {
    @JvmField var allVars : Array<String> = varStrings + parStrings
    @JvmField var tresholdsQ : MutableList<MutableList<NumQ>> = mutableListOf<MutableList<NumQ>>()
    @JvmField var maxTQ : NumQ
    init {
        //TODO parse derivatives and tresholds from strings
        //strToMPolyInQ(poly : String, vars : Array<String>) : MPolyInQ
        for( i in 0..(dim-1)) {
            this.tresholdsQ.add(mutableListOf<NumQ>())
            for( j in 0..(tresholdsStrings[i].size-1) ) {
                this.tresholdsQ[i].add( strToQ( tresholdsStrings[i][j] ) )
            } 
        }
        this.maxTQ = strToQ( maxT )
    }
    
    fun getDerStringsForRectangle( coords : Array<Int> ) : MutableList<String>{
        var resultDerStrings : MutableList<String> = mutableListOf<String>()
        //TODO parse derivatives and tresholds from strings
        //strToMPolyInQ(poly : String, vars : Array<String>) : MPolyInQ
        for( i in 0..(dim-1)) {
            resultDerStrings.add( (this.derStringFunctions[i])( coords ) )
        }
        return resultDerStrings
    }
    
    fun getDerPolsForRectangle( coords : Array<Int> ) : MutableList<MPolyInQ> {
        var resultDerPols : MutableList<MPolyInQ> = mutableListOf<MPolyInQ>()
        //TODO parse derivatives and tresholds from strings
        //strToMPolyInQ(poly : String, vars : Array<String>) : MPolyInQ
        for( i in 0..(dim-1)) {
            resultDerPols.add( strToMPolyInQ( derStringFunctions[i]( coords ), allVars ) )
        }
        return resultDerPols
    }
    
    fun getDim() : Int {
        return this.dim
    }
    
    fun getParamCount() : Int {
        return this.paramCount
    }
    
    fun getVarStrings() : Array<String> {
        return this.varStrings
    }
    
    fun getParStrings() : Array<String> {
        return this.parStrings
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
    
    fun getMinTres( variable : Int ) : String {
        return tresholdsStrings[ variable ][ 0 ]
    }
    
    fun getMaxTres( variable : Int ) : String {
        return tresholdsStrings[ variable ][ (tresholdsStrings[ variable ].size) - 1 ]
    }
    
    fun getMinOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else
                return tresholdsStrings[ variable ][ state[ dir ] ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] ]
        }
    }
    
    fun getMaxOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else
                return tresholdsStrings[ variable ][ state[ dir ] ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] + 1 ]
        }
    }
    
    fun getMaxT() : String {
        return this.maxT
    }
    
    fun getDegTayl() : Int {
        return this.taylDeg
    }
    
    fun getPrefixEquations() : Map<String,String> {
        return this.prefixEquations
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
        "001sys" -> return BioSystem( 2, 1,  //dim, num pars 
                             arrayOf( "x", "y" ), //vars
                             arrayOf( "p" ),      //pars
                             arrayOf( "1", "p" ), //der x, der y
                             arrayOf( "0", "0" ), //der2s are zero
                             arrayOf( "0", "0" ), //der2 / 2 zero again
                             arrayOf( arrayOf( "0","1","2" ), //tresholds x
                                      arrayOf( "0","1","2" ) ),//tresholds y
                             "2.0", 2,//maxT, Taylor degree
                             mutableMapOf("x" to "1", //prefixEquations
                                          "y" to "p") ) 
        "002sys" -> return BioSystem( 2, 1,//n m
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//par
                             arrayOf( "x+1", "y+p" ),
                             arrayOf( "x+1", "y+p" ),
                             arrayOf( "0.5*x+0.5", "0.5*y+0.5*p" ),
                             arrayOf( arrayOf( "0","1","2" ),
                                      arrayOf( "0","1","2" ) ),
                             "1", 1,//maxT, Taylor degree
                             mutableMapOf("x" to ("x" add "1"), //prefixEquations
                                          "y" to ("y" add "p")) )
        "003LV" -> return BioSystem( 2, 1,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-0.05*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders
                             arrayOf( arrayOf( "0","1","2" ),//tresholds x UPDATE?
                                      arrayOf( "0","1","2" ) ),//tresholds y UPDATE?
                             "2.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to (("-0.05" mul "y") add ("0.2" mul "x" mul "y"))) )                   
        "004POL" -> return BioSystem( 2, 1,//n m
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//pars
                             arrayOf( "y", 
                                      "p*y-p*x*x*y-x" ),//ders
                             arrayOf( "p*y-p*x*x*y-x", 
                                      "-2*p*y*y+p*p*y-y-2*p*p*x*x*y" ),//2nd ders
                             arrayOf( "0.5*p*y-0.5*p*x*x*y-0.5*x", 
                                      "-1*p*y*y+0.5*p*p*y-0.5*y-p*p*x*x*y" ),//half 2nd ders
                             arrayOf( arrayOf( "-2","0","2" ),//tres x
                                      arrayOf( "-6","0","6" )),//tres y
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to "y", //prefixEquations
                                          "y" to ((("p" mul "y") rem ("p" mul "x" mul "x" mul "y")) rem "x") ) )
        "005ROS" -> return BioSystem( 3, 1,//n m
                             arrayOf( "x", "y", "z" ),//vars
                             arrayOf( "c" ),//pars
                             arrayOf( "-y-z", 
                                      "x+0.2*y",
                                      "0.2+x*z-c*z" ),//ders
                             arrayOf( "-x-0.2*y-0.2-x*z-c*z", 
                                      "-0.6*y-z+0.2*x",
                                      "0.2+0.2*c+0.2*x-2*c*x*z-y*z-z*z+c*c*z+0.2*x*x*z" ),//2nd ders
                             arrayOf( "-0.5*x-0.1*y-0.1-0.5*x*z-0.5*c*z", 
                                      "-0.3*y-0.5*z+0.1*x",
                                      "0.1+0.1*c+0.1*x-c*x*z-0.5*y*z-0.5*z*z+0.5*c*c*z+0.1*x*x*z" ),//half 2nd ders
                             arrayOf( arrayOf( "-20","0","20" ),//tres x
                                      arrayOf( "-20","0","20" ),//tres y
                                      arrayOf( "0","0.1","0.2","0.3","0.4" ) ),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("-1" mul "x") rem "z"), //prefixEquations
                                          "y" to ("x" add ("0.2" mul "y")),
                                          "z" to ("0.2" add ("x" mul "z") add ("-1" mul "c" mul "z")) ) )   
        "CASE000aLVPARSQUARED" -> return BioSystem( 2, 1,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-p*p*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0","1","2" ),//tresholds x UPDATE?
                                      arrayOf( "0","1","2" ) ),//tresholds y UPDATE?
                             "2.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to (("-0.05" mul "y") add ("0.2" mul "x" mul "y"))) )
        "CASE000bLVMULTIPARS" -> return BioSystem( 2, 2,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p", "q" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-q*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0","1","2" ),//tresholds x UPDATE?
                                      arrayOf( "0","1","2" ) ),//tresholds y UPDATE?
                             "2.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to (("-0.05" mul "y") add ("0.2" mul "x" mul "y"))) )
        "CASE001SEIR" -> return BioSystem( 4, 1,//n m
                             arrayOf( "S", "E", "I", "R" ),//vars
                             arrayOf( "b" ),//pars
                             arrayOf( "L-M*S-b*S*I/N", 
                                      "b*S*I/N-(M+eps)*E",
                                      "eps*E-(G+M+A)*I",
                                      "G*I-M*R" ),//ders
                             arrayOf( "", 
                                      "",
                                      "",
                                      "" ),//2nd ders NOT APPLICABLE
                             arrayOf( "", 
                                      "",
                                      "",
                                      "" ),//half 2nd ders NOT APPLICABLE
                             arrayOf( arrayOf( "-20","0","20" ),//tres x
                                      arrayOf( "-20","0","20" ),//tres y
                                      arrayOf( "-20","0","20" ),//tres y
                                      arrayOf( "0","0.1","0.2","0.3","0.4" ) ),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("-1" mul "x") rem "z"), //prefixEquations
                                          "y" to ("x" add ("0.2" mul "y")),
                                          "z" to ("0.2" add ("x" mul "z") add ("-1" mul "c" mul "z")) ) )
        else -> return BioSystem( 2, 1, //zero dummy system
                             arrayOf( "x", "y" ),
                             arrayOf( "p" ),
                             arrayOf( "0", "0" ),
                             arrayOf( "0", "0" ),
                             arrayOf( "0", "0" ),
                             arrayOf( arrayOf( "0","1","2" ),
                                      arrayOf( "0","1","2" ) ),
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to "0.0", //prefixEquations
                                          "y" to "0.0") )
    }
}
    
    fun getBioSystemPWMAByName( name : String ) : BioSystemPWMA{
    when( name ){
        "CASE002aREPRES3D" -> return BioSystemPWMA( 3, 1,//n m
                             arrayOf( "x", "y", "z" ),//vars
                             arrayOf( "c" ),//pars
                             arrayOf( {coords -> (hillapprox("y",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapprox("z",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapprox("x",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ),//ders
                             arrayOf( arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres x
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres y
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" )),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("-1" mul "x") rem "z"), //prefixEquations
                                          "y" to ("x" add ("0.2" mul "y")),
                                          "z" to ("0.2" add ("x" mul "z") add ("-1" mul "c" mul "z")) ) )
        "CASE002bREPRES5D" -> return BioSystemPWMA( 5, 1,//n m
                             arrayOf( "u","v","x", "y", "z" ),//vars
                             arrayOf( "c" ),//pars
                             arrayOf( {coords -> (hillapprox("v",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapprox("x",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapprox("y",3,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")},
                             { coords -> (hillapprox("z",4,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapprox("u",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ),//ders
                             arrayOf( arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres u
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres v
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres x
                              arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres y
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" )),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("-1" mul "x") rem "z"), //prefixEquations
                                          "y" to ("x" add ("0.2" mul "y")),
                                          "z" to ("0.2" add ("x" mul "z") add ("-1" mul "c" mul "z")) ) )
        else -> return BioSystemPWMA( 3, 1,//n m
                             arrayOf( "x", "y", "z" ),//vars
                             arrayOf( "c" ),//pars
                             arrayOf( {coords -> (hillapprox("y",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapprox("z",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapprox("x",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ),//ders
                             arrayOf( arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres x
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres y
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462, 5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" )),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("-1" mul "x") rem "z"), //prefixEquations
                                          "y" to ("x" add ("0.2" mul "y")),
                                          "z" to ("0.2" add ("x" mul "z") add ("-1" mul "c" mul "z")) ) )
    }
}
