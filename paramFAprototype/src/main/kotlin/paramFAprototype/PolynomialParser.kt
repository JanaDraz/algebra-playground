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

/*
 * Parse rational (Q) number from a decimal string,
 * either there is a decimal point
 * or we create the ratio of denumerator / 1 
 */
fun strToQ(number : String) : NumQ {
    val uvZCoder = Rings.UnivariateRingZ.mkCoder("x")
    var num : BigInteger; var den : BigInteger
   
    //FIXME? dodat sem nejake checkovani jestli je jen jedna tecka a jinak same cislice
    if( "." in number) {
       //does not contain decimal point
       val parts = number.split(".")
       num = uvZCoder.parse(parts[0] + parts[1]).cc()
       val expon = parts[1].length
       den = BigInteger.TEN.pow(expon)
    }else{
       //contains decimal point 
       num = uvZCoder.parse(number).cc()
       den = BigInteger.ONE
    }
    
    val qnum = Q.mk(num,den)
    return qnum
}

/*
 * parse the term vars^k_i into a polynomial representation
 */
fun stringTermToMPolyInQ(term : String, vars : Array<String>) : MPolyInQ {
    val mvQCoder = Rings.MultivariateRingQ(vars.size).mkCoder(*vars)
    return mvQCoder.parse(term)
}

/*
 * parse Q coeff * term into a polynomial representation
 */
fun  stringCoeffTimesTermToMPolyInQ(qxterm : String, vars : Array<String>) : MPolyInQ {
    //first we set the result to constant "one" polynomial
    var mqpoly = Rings.MultivariateRingQ(vars.size).getOne()
    var qnumber : MPolyInQ
    var strqnumber : String
    val muls = qxterm.split("*")
    println("muls*")
    println(muls)
    for (m in muls){
        //is first letter a digit? (then m is a Q number)
        if(m[0].isDigit()){
            //parse Q number into the polynomial ring
            strqnumber = strToQ(m).toString()
            qnumber = Rings.MultivariateRingQ(vars.size).mkCoder(*vars).parse(strqnumber)
            //multiply result by the parsed number
            mqpoly = Rings.MultivariateRingQ(vars.size).multiply(mqpoly,qnumber)
        //otherwise m is a term
        }else{
            //parse a polynomial term without Q number, multiply the result by it
            mqpoly = Rings.MultivariateRingQ(vars.size).multiply(mqpoly,stringTermToMPolyInQ(m,vars))
        }
    }
    return mqpoly
}

/*
 * Read a multivariate polynomial with Q coefficients and 
 * given variables from string.
 * - eliminate spaces
 * - divide into chunks according to + and -
 * - divide into number {0-9.}^n and vars^k_i term by "*"
 * - get the Q representation of the number
 * - parse the term into a polynomial representation
 * - put all back together into a polynomial representation  
 */
fun strToMPolyInQ(poly : String, vars : Array<String>) : MPolyInQ{
    //first we set the result to zero polynomial
    var mqpoly = Rings.MultivariateRingQ(vars.size).getZero()//mkCoder(*vars).parse("0")
    //get rid of all the whitespaces in the input string
    val poly2 = poly.filter({c -> !(c.isWhitespace())})
    val plusOne = Rings.MultivariateRingQ(vars.size).getOne()
    val minusOne = Rings.MultivariateRingQ(vars.size).getNegativeOne()
    //regex split into terms +
    val chunks = poly2.split("+")
    println("chunks+:")
    println(chunks)
    for(chunk in chunks){
        //if contains -, deal with it
        if(chunk.contains('-')){
            //we suppose the form is like a-b-c or -a-b etc., hoping for no parenthesis
            val chunksm = chunk.split("-")
            println("chunksm-")
            println(chunksm)
            var currentnegative = false//: Boolean = (chunksm[0].isEmpty())
            for(chu in chunksm){
                if(!chu.isEmpty()){
                  var mulby = if( currentnegative ) { minusOne } else { plusOne }
                  mulby = Rings.MultivariateRingQ(vars.size).multiply( mulby,
                    stringCoeffTimesTermToMPolyInQ(chu,vars) )
                  mqpoly = Rings.MultivariateRingQ(vars.size).add( mqpoly, mulby)
                }
                currentnegative = true //all the chunks after the 0th one begin with -
            }
        //no -... one term, add to the result
        } else {
            mqpoly = Rings.MultivariateRingQ(vars.size).add( mqpoly,
                stringCoeffTimesTermToMPolyInQ(chunk,vars))
        }
    }
    return mqpoly
}

/*
 * Read a univariate polynomial with Z coefficients and 
 * given variable string from string.
 */
 fun strToUPolyInZ(poly : String, variable : String) : UPolyInZ{
    val uvZCoder = Rings.UnivariateRingZ.mkCoder(variable)
    return uvZCoder.parse(poly)
}

/*
 * Read a univariate polynomial with Q coefficients and 
 * given variable string from string (where the polynomial
 * is written with coefficients in Z).
 */
 fun strToUPolyInQ(poly : String, variable : String) : UPolyInQ{
    val uvQCoder = Rings.UnivariateRingQ.mkCoder(variable)
    return uvQCoder.parse(poly)
}
