package paramFAprototype

import cc.redberry.rings.Rings
import cc.redberry.rings.bigint.BigInteger
import paramFAprototype.NumQ

abstract class AbstractTest {
    protected val uniQCoder = Rings.UnivariateRingQ.mkCoder("x")
    protected val mv2QCoder = Rings.MultivariateRingQ(2).mkCoder("x", "y")
    protected val mv3QCoder = Rings.MultivariateRingQ(3).mkCoder("x", "y", "z")

    protected val uniZCoder = Rings.UnivariateRingZ.mkCoder("x")
    protected val mv2ZCoder = Rings.MultivariateRingZ(2).mkCoder("x", "y")
    protected val mv3ZCoder = Rings.MultivariateRingZ(3).mkCoder("x", "y", "z")
    
    protected val DEFAULT_PRECISION_FOR_TEST = NumQ(Rings.Q.ring, BigInteger.ONE, BigInteger.valueOf(100000))

    protected fun anyElementsContainsNumberWithPrecision(list: Iterable<NumQ>, number: NumQ, precision: NumQ): Boolean {
        for (el in list) {
            if (el.subtract(number).abs() <= precision)
                return true
        }
        return false
    }
}
