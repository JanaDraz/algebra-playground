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
 * depends on xi-th variable 
 * with name xstr
 * for a rectangle with given coords
 * piecewise linear (step?)
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

/* PREFIX NOTATION
 * Approximation of a hill function
 * on xi-th variable 
 * with name xstr
 * for a rectangle with given coords
 */
fun hillapproxPrefix( xstr: String, xi : Int, coords : Array<Int>, xtres : Array<Double>, yvals : Array<Double>) : String {
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
    
    result = "(+ (* "+str(a)+" "+xstr+") "+str(b)+")"
    
    return result
}

/* Approximation of a hill function
 * depends on xi-th variable 
 * with name xstr
 * for a rectangle containing point xval
 */
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
 
class BioSystem(   @JvmField var name : String,
                   @JvmField var dim : Int,
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
    
    fun getName() : String {
        return this.name
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
    
    fun getTresCount( variable : Int ) : Int {
        return tresholdsStrings[ variable ].size
    }
    
    fun getTresCounts() : Array<Int> {
        return Array<Int>( this.dim ){ i -> getTresCount(i) }
    }
    
    fun getMinOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else if( or < 0 )
                return tresholdsStrings[ variable ][ state[ dir ] ]
            else//or==0
                return tresholdsStrings[ variable ][ state[ dir ] ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] ]
        }
    }
    
    fun getMaxOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else if( or < 0 )
                return tresholdsStrings[ variable ][ state[ dir ] ]
            else //or==0
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] + 1 ]
        }
    }
    
    fun getMaxT() : String {
        return this.maxT
    }
    
    fun setMaxT( mt : String ) {
        this.maxT = mt
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
 
class BioSystemPWMA(
                   @JvmField var name : String, 
                   @JvmField var dim : Int,
                   @JvmField var paramCount : Int, 
                   @JvmField var varStrings : Array<String>,
                   @JvmField var parStrings : Array<String>,
                   @JvmField var derStringFunctions : Array<(Array<Int>)-> String>,
                   @JvmField var tresholdsStrings : Array<Array<String>>,
                   @JvmField var maxT : String,
                   @JvmField var taylDeg : Int,
                   @JvmField var prefixEquationsFunctions: Array<(Array<Int>)-> String> ) {
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
    
    fun getPrefixDerStringsForRectangle( coords : Array<Int> ) : Map<String,String>{
        var resultDerStrings : MutableMap<String,String> = mutableMapOf<String,String>()
        //TODO parse derivatives and tresholds from strings
        //strToMPolyInQ(poly : String, vars : Array<String>) : MPolyInQ
        for( i in 0..(dim-1)) {
            resultDerStrings.put( this.varStrings[i], (this.prefixEquationsFunctions[i])( coords ) )
        }
        return resultDerStrings
    }
    
    fun getName() : String {
        return this.name
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
    
    fun getTresCount( variable : Int ) : Int {
        return tresholdsStrings[ variable ].size
    }
    
    fun getTresCounts() : Array<Int> {
        return Array<Int>( this.dim ){ i -> getTresCount(i) }
    }

    fun getMinOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else if( or < 0 )
                return tresholdsStrings[ variable ][ state[ dir ] ]
            else//or==0
                return tresholdsStrings[ variable ][ state[ dir ] ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] ]
        }
    }
    
  
    fun getRectanglesCount() : Int {
        var count : Int = 1
        for( i in 0..(this.dim-1) ){
            count = count * getTresCount(i)
        }
        return count
    }
    
    fun getMaxOnFacet( variable : Int, state : Array<Int>, dir : Int, or : Int ) : String {
        if( variable == dir ) {
            if( or > 0 )
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
            else if( or < 0 )
                return tresholdsStrings[ variable ][ state[ dir ] ]
            else
                return tresholdsStrings[ variable ][ state[ dir ] + 1 ]
        } else {
            return tresholdsStrings[ variable ][ state[ variable ] + 1 ]
        }
    }
    
    fun getMaxT() : String {
        return this.maxT
    }
    
    fun setMaxT( mt : String ) {
        this.maxT = mt
    }
    
    fun getDegTayl() : Int {
        return this.taylDeg
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
        "001sys" -> return BioSystem("001sys", 2, 1,  //dim, num pars 
                             arrayOf( "x", "y" ), //vars
                             arrayOf( "p" ),      //pars
                             arrayOf( "1", "p" ), //der x, der y
                             arrayOf( "0", "0" ), //der2s are zero
                             arrayOf( "0", "0" ), //der2 / 2 zero again
                             arrayOf( arrayOf( "0.0","1.0","2.0", "3.0" ), //tresholds x
                                      arrayOf( "0.0","1.0","2.0", "3.0" ) ),//tresholds y
                             "2.0", 2,//maxT, Taylor degree
                             mutableMapOf("x" to "1", //prefixEquations
                                          "y" to "p") ) 
        "002sys" -> return BioSystem("002sys", 2, 1,//n m
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
        "003LV" -> return BioSystem("003LV", 2, 1,//dim, num pars
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
        "004POL" -> return BioSystem("004POL", 2, 1,//n m
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
        "005ROS" -> return BioSystem("005ROS", 3, 1,//n m
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
        "CASE-EXAMPLE-LV-1PAR" -> return BioSystem("CASE-EXAMPLE-LV-1PAR", 2, 1,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-0.06*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.1","0.2","0.4","0.6" ),//tresholds x
                                      arrayOf( "0.0","0.5","0.8","1.0" ) ),//tresholds y
                             "10.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to (("-0.06" mul "y") add ("0.2" mul "x" mul "y"))) )
        "CASE000aLVPARSQUARED" -> return BioSystem("CASE000aLVPARSQUARED", 2, 1,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-p*p*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.1","0.2","0.4","0.6" ),//tresholds x
                                      arrayOf( "0.0","0.5","0.8","1.0" ) ),//tresholds y
                             "10.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to ((("-1.0" mul "p" mul "p") mul "y") add ("0.2" mul "x" mul "y"))) )
        "CASE000bLVMULTIPARS" -> return BioSystem("CASE000bLVMULTIPARS", 2, 2,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p", "q" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-q*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.1","0.2","0.4","0.6" ),//tresholds x
                                      arrayOf( "0.0","0.5","0.8","1.0" ) ),//tresholds y
                             "10.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to (("-1.0" mul "q" mul "y") add ("0.2" mul "x" mul "y"))) )
        "CASE000cLV1PARMORETRES" -> return BioSystem("CASE000cLV1PARMORETRES", 2, 1,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p" ),//pars
                             arrayOf( "0.1*x-p*x*y", 
                                      "-0.06*y+0.2*x*y" ),//ders
                             arrayOf( "0.01*x-0.15*p*x*y+p*p*y*y-0.2*p*x*x*y", 
                                      "-0.0025*y+0.04*x*y-0.04*x*x*y-0.2*p*x*y*y" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.0", "0.012", "0.024", "0.036000000000000004", "0.048", "0.06", "0.07200000000000001", "0.084", "0.096", "0.108", "0.12", "0.132", "0.14400000000000002", "0.156", "0.168", "0.18", "0.192", "0.20400000000000001", "0.216", "0.228", "0.24", "0.252", "0.264", "0.276", "0.28800000000000003", "0.3", "0.312", "0.324", "0.336", "0.34800000000000003", "0.36", "0.372", "0.384", "0.396", "0.40800000000000003", "0.42", "0.432", "0.444", "0.456", "0.468", "0.48", "0.492", "0.504", "0.516", "0.528", "0.54", "0.552", "0.5640000000000001", "0.5760000000000001", "0.588", "0.6" ),//tresholds x
                                      arrayOf( "0.0", "0.02", "0.04", "0.06", "0.08", "0.1", "0.12", "0.14", "0.16", "0.18", "0.2", "0.22", "0.24", "0.26", "0.28", "0.3", "0.32", "0.34", "0.36", "0.38", "0.4", "0.42", "0.44", "0.46", "0.48", "0.5", "0.52", "0.54", "0.56", "0.58", "0.6", "0.62", "0.64", "0.66", "0.68", "0.7000000000000001", "0.72", "0.74", "0.76", "0.78", "0.8", "0.8200000000000001", "0.84", "0.86", "0.88", "0.9", "0.92", "0.9400000000000001", "0.96", "0.98", "1.0" ) ),//tresholds y
                             "10.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("0.1" mul "x") rem ("p" mul "x" mul "y")), //prefixEquations
                                          "y" to (("-1.0" mul "0.06" mul "y") add ("0.2" mul "x" mul "y"))) )
        "CASE002aBRUSSELATOR1par" -> return BioSystem("CASE002aBRUSSELATOR1par", 2,1,//dim, num pars
                             arrayOf( "x", "y" ),//vars
                             arrayOf( "p"),//pars
                             arrayOf( "1.0+x*x*y-p*x-x", 
                                      "p*x-x*x*y" ),//ders
                             arrayOf( "0.0", 
                                      "0.0" ),//2.ders NOT APPLICABLE
                             arrayOf( "0.005*x-0.075*p*x*y+0.5*p*p*y*y-0.1*p*x*x*y", 
                                      "-0.00125*y+0.02*x*y-0.02*x*x*y-0.1*p*x*y*y" ),//half 2.ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.0", "0.006", "0.012", "0.018000000000000002", "0.024", "0.03", "0.036000000000000004", "0.042", "0.048", "0.054", "0.06", "0.066", "0.07200000000000001", "0.078", "0.084", "0.09", "0.096", "0.10200000000000001", "0.108", "0.114", "0.12", "0.126", "0.132", "0.138", "0.14400000000000002", "0.15", "0.156", "0.162", "0.168", "0.17400000000000002", "0.18", "0.186", "0.192", "0.198", "0.20400000000000001", "0.21", "0.216", "0.222", "0.228", "0.234", "0.24", "0.246", "0.252", "0.258", "0.264", "0.27", "0.276", "0.28200000000000003", "0.28800000000000003", "0.294", "0.3", "0.306", "0.312", "0.318", "0.324", "0.33", "0.336", "0.342", "0.34800000000000003", "0.354", "0.36", "0.366", "0.372", "0.378", "0.384", "0.39", "0.396", "0.402", "0.40800000000000003", "0.41400000000000003", "0.42", "0.426", "0.432", "0.438", "0.444", "0.45", "0.456", "0.462", "0.468", "0.47400000000000003", "0.48", "0.486", "0.492", "0.498", "0.504", "0.51", "0.516", "0.522", "0.528", "0.534", "0.54", "0.546", "0.552", "0.558", "0.5640000000000001", "0.5700000000000001", "0.5760000000000001", "0.582", "0.588", "0.594", "0.6" ),//100 tresholds x
                                      arrayOf( "0.0", "0.006", "0.012", "0.018000000000000002", "0.024", "0.03", "0.036000000000000004", "0.042", "0.048", "0.054", "0.06", "0.066", "0.07200000000000001", "0.078", "0.084", "0.09", "0.096", "0.10200000000000001", "0.108", "0.114", "0.12", "0.126", "0.132", "0.138", "0.14400000000000002", "0.15", "0.156", "0.162", "0.168", "0.17400000000000002", "0.18", "0.186", "0.192", "0.198", "0.20400000000000001", "0.21", "0.216", "0.222", "0.228", "0.234", "0.24", "0.246", "0.252", "0.258", "0.264", "0.27", "0.276", "0.28200000000000003", "0.28800000000000003", "0.294", "0.3", "0.306", "0.312", "0.318", "0.324", "0.33", "0.336", "0.342", "0.34800000000000003", "0.354", "0.36", "0.366", "0.372", "0.378", "0.384", "0.39", "0.396", "0.402", "0.40800000000000003", "0.41400000000000003", "0.42", "0.426", "0.432", "0.438", "0.444", "0.45", "0.456", "0.462", "0.468", "0.47400000000000003", "0.48", "0.486", "0.492", "0.498", "0.504", "0.51", "0.516", "0.522", "0.528", "0.534", "0.54", "0.546", "0.552", "0.558", "0.5640000000000001", "0.5700000000000001", "0.5760000000000001", "0.582", "0.588", "0.594", "0.6" ) ),//100 tresholds y
                             "2.0", 1 ,//maxT, Taylor degree
                             mutableMapOf("x" to (("1.0" add ("x" mul "x" mul "y")) rem (("p" mul "x") add "x")), //prefixEquations
                                          "y" to (("p" mul "x") rem ("x" mul "x" mul "y"))) )
        "CASE001aSEIR1par" -> return BioSystem("CASE001aSEIR1par", 4, 1,//n m
                             arrayOf( "w", "x", "y", "z" ),//vars SEIR
                             arrayOf( "b" ),//pars
                             arrayOf( "-1.0*b*w*y*0.0000001", 
                                      "b*w*y*0.0000001-0.2*x",
                                      "0.2*x-5.006*y",
                                      "0.2*y" ),//ders
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//2nd ders NOT APPLICABLE
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//half 2nd ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.0", "666666.6666666666", "1333333.3333333333", "2000000.0", "2666666.6666666665", "3333333.333333333", "4000000.0", "4666666.666666666", "5333333.333333333", "6000000.0", "6666666.666666666", "7333333.333333333", "8000000.0", "8666666.666666666", "9333333.333333332", "10000000.0" ),//tres w                             
                             arrayOf( "0.0", "666666.6666666666", "1333333.3333333333", "2000000.0", "2666666.6666666665", "3333333.333333333", "4000000.0", "4666666.666666666", "5333333.333333333", "6000000.0", "6666666.666666666", "7333333.333333333", "8000000.0", "8666666.666666666", "9333333.333333332", "10000000.0" ),//tres x
                             arrayOf( "0.0", "666666.6666666666", "1333333.3333333333", "2000000.0", "2666666.6666666665", "3333333.333333333", "4000000.0", "4666666.666666666", "5333333.333333333", "6000000.0", "6666666.666666666", "7333333.333333333", "8000000.0", "8666666.666666666", "9333333.333333332", "10000000.0" ),//tres y
                             arrayOf( "0","500","1000","5000","10000","1000000","2000000","3000000","4000000","5000000","6000000","7000000","8000000","9000000","10000000" )),//tres z
                             "5", 1 ,//maxT, Taylor degree
                             mutableMapOf("w" to (((("-1" mul "0.0000001") mul "b") mul "w") mul "y"), //prefixEquations
                                          "x" to (( "0.0000001" mul ("b" mul ("w" mul "y"))) rem ("0.2" mul "x")),
                                          "y" to (("0.2" mul "x") rem ("5.006" mul "y")),
                                          "z" to ("0.2" mul "y") ))
        "CASE001aSEIRnorm1par" -> return BioSystem("CASE001aSEIRnorm1par", 4, 1,//n m
                             arrayOf( "w", "x", "y", "z" ),//vars
                             arrayOf( "b" ),//pars
                             arrayOf( "-1.0*b*w*y", 
                                      "b*w*y-0.2*x",
                                      "0.2*x-1.0*y",
                                      "1.0*y" ),//ders
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//2nd ders NOT APPLICABLE
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//half 2nd ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.0", "0.06666666666666667", "0.13333333333333333", "0.2", "0.26666666666666666", "0.3333333333333333", "0.4", "0.4666666666666667", "0.5333333333333333", "0.6", "0.6666666666666666", "0.7333333333333333", "0.8", "0.8666666666666667", "0.9333333333333333", "1.0" ),//15 tres w                             
                             arrayOf( "0.0", "0.06666666666666667", "0.13333333333333333", "0.2", "0.26666666666666666", "0.3333333333333333", "0.4", "0.4666666666666667", "0.5333333333333333", "0.6", "0.6666666666666666", "0.7333333333333333", "0.8", "0.8666666666666667", "0.9333333333333333", "1.0" ),//15 tres x
                             arrayOf( "0.0", "0.06666666666666667", "0.13333333333333333", "0.2", "0.26666666666666666", "0.3333333333333333", "0.4", "0.4666666666666667", "0.5333333333333333", "0.6", "0.6666666666666666", "0.7333333333333333", "0.8", "0.8666666666666667", "0.9333333333333333", "1.0" ),//15 tres y
                             arrayOf("0.0", "0.06666666666666667", "0.13333333333333333", "0.2", "0.26666666666666666", "0.3333333333333333", "0.4", "0.4666666666666667", "0.5333333333333333", "0.6", "0.6666666666666666", "0.7333333333333333", "0.8", "0.8666666666666667", "0.9333333333333333", "1.0" )),//15 tres z
                             "5", 1 ,//maxT, Taylor degree
                             mutableMapOf("w" to ((("-1" mul "b") mul "w") mul "y"), //prefixEquations
                                          "x" to (("b" mul ("w" mul "y")) rem ("0.2" mul "x")),
                                          "y" to (("0.2" mul "x") rem ("1.0" mul "y")),
                                          "z" to ("1.0" mul "y") ))
        "CASE001aSEIRnormSimple1par" -> return BioSystem("CASE001aSEIRnormSimple1par", 4, 1,//n m
                             arrayOf( "w", "x", "y", "z" ),//vars
                             arrayOf( "b" ),//pars
                             arrayOf( "-1.0*b*w*y", 
                                      "b*w*y-0.2*x",
                                      "0.2*x-0.2*y", //1.0y->0.2y
                                      "0.2*y" ),//ders //1.0y->0.2y
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//2nd ders NOT APPLICABLE
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//half 2nd ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.0", "0.1", "0.2", "0.30000000000000004", "0.4", "0.5", "0.6000000000000001", "0.7000000000000001", "0.8", "0.9", "1.0" ),//10 tres w                             
                             arrayOf( "0.0", "0.1", "0.2", "0.30000000000000004", "0.4", "0.5", "0.6000000000000001", "0.7000000000000001", "0.8", "0.9", "1.0" ),//10 tres x
                             arrayOf( "0.0", "0.1", "0.2", "0.30000000000000004", "0.4", "0.5", "0.6000000000000001", "0.7000000000000001", "0.8", "0.9", "1.0" ),//10 tres y
                             arrayOf( "0.0", "0.1", "0.2", "0.30000000000000004", "0.4", "0.5", "0.6000000000000001", "0.7000000000000001", "0.8", "0.9", "1.0" )),//10 tres z
                             "5", 1 ,//maxT, Taylor degree
                             mutableMapOf("w" to ((("-1" mul "b") mul "w") mul "y"), //prefixEquations
                                          "x" to (("b" mul ("w" mul "y")) rem ("0.2" mul "x")),
                                          "y" to (("0.2" mul "x") rem ("0.2" mul "y")),//1.0y->0.2y
                                          "z" to ("0.2" mul "y") ))//1.0y->0.2y
        "CASE001bSEIRmpar" -> return BioSystem("CASE001bSEIRmpar", 4, 1,//n m
                             arrayOf( "w", "x", "y", "z" ),//vars
                             arrayOf( "b" ),//pars
                             arrayOf( "Lambda-Mju*w-b*w*y*0.0000001", 
                                      "b*w*y*0.0000001-(Mju+Epsilon)*x",
                                      "Epsilon*x-(Gamma+Mju+Alpha)*y",
                                      "Gamma*y-Mju*z" ),//ders
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
        "CASE001bSEIRnormMpar" -> return BioSystem("CASE001bSEIRnormMpar", 4, 2,//n m
                             arrayOf( "w", "x", "y", "z" ),//vars
                             arrayOf( "b","s" ),//pars beta, sigma, gamma?
                             arrayOf( "-1.0*b*w*y", 
                                      "b*w*y-s*x", //s around 0.2
                                      "s*x-1.0*y",
                                      "1.0*y" ),//ders
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//2nd ders NOT APPLICABLE
                             arrayOf( "0.0", 
                                      "0.0",
                                      "0.0",
                                      "0.0" ),//half 2nd ders NOT APPLICABLE
                             arrayOf( arrayOf( "0.0","0.0005","0.001","0.005","0.01","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0" ),//tres w                             
                             arrayOf( "0.0","0.0005","0.001","0.005","0.01","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0" ),//tres x
                             arrayOf( "0.0","0.0005","0.001","0.005","0.01","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0" ),//tres y
                             arrayOf( "0.0","0.0005","0.001","0.005","0.01","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0" ),//tres y
                             arrayOf( "0.0","0.0005","0.001","0.005","0.01","0.1","0.2","0.3","0.4","0.5","0.6","0.7","0.8","0.9","1.0" ) ),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             mutableMapOf("w" to ((("-1" mul "b") mul "w") mul "y"), //prefixEquations
                                          "x" to (("b" mul ("w" mul "y")) rem ("0.2" mul "x")),
                                          "y" to (("0.2" mul "x") rem ("1.0" mul "y")),
                                          "z" to ("1.0" mul "y") ) )
        else -> return BioSystem("default", 2, 1, //zero dummy system
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
        "CASE002aREPRES3D" -> return BioSystemPWMA("CASE002aREPRES3D", 3, 1,//n m
                             arrayOf( "x", "y", "z" ),//vars
                             arrayOf( "c" ),//pars //orig c=0.1, find c in [0,1]
                             arrayOf( {coords -> (hillapprox("y",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-c*x") }, 
                             { coords -> (hillapprox("z",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-c*y")}, 
                             {coords -> (hillapprox("x",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-c*z")} ),//ders
                             arrayOf( arrayOf( "0.000000", "2.011341", "2.921948", "3.692462","5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres x
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462", "5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres y
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462", "5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" )),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             //prefixEquations
                             arrayOf( {coords -> (hillapproxPrefix("y",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapproxPrefix("z",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapproxPrefix("x",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ) )
        "CASE002bREPRES5D" -> return BioSystemPWMA("CASE002bREPRES5D", 5, 1,//n m
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
                             //prefixEquations
                             arrayOf( {coords -> (hillapproxPrefix("v",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapproxPrefix("x",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapproxPrefix("y",3,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")},
                             { coords -> (hillapproxPrefix("z",4,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapproxPrefix("u",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ) )
        else -> return BioSystemPWMA("defaultPWMA", 3, 1,//n m
                             arrayOf( "x", "y", "z" ),//vars
                             arrayOf( "c" ),//pars
                             arrayOf( {coords -> (hillapprox("y",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapprox("z",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapprox("x",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ),//ders
                             arrayOf( arrayOf( "0.000000", "2.011341", "2.921948", "3.692462", "5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres x
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462", "5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" ),//tres y
                             arrayOf( "0.000000", "2.011341", "2.921948", "3.692462", "5.563709", "6.284189", "7.074716", "8.035357", "9.336224", "10.000000" )),//tres z
                             "1", 1 ,//maxT, Taylor degree
                             //prefixEquations
                             arrayOf( {coords -> (hillapproxPrefix("y",1,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*x") }, 
                             { coords -> (hillapproxPrefix("z",2,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*y")}, 
                             {coords -> (hillapproxPrefix("x",0,coords,arrayOf( 0.000000, 2.011341, 2.921948, 3.692462, 5.563709, 6.284189, 7.074716, 8.035357, 9.336224, 10.000000 ),arrayOf( 1.000000, 0.989576, 0.936192, 0.819908, 0.369553, 0.241771, 0.149892, 0.085328, 0.042196, 0.030303))+"-0.1*z")} ) )
    }
}
