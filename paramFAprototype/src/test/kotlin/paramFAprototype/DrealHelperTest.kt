/*
 * test of calling Dreal and Dreach
 */
package paramFAprototype

import org.junit.Assert
import org.junit.Test

class DrealHelperTest : AbstractTest() {
    
    @Test
    fun testGetResultFromDreal() {
        val commandsForDreal = ArrayList<String>().apply {
            add("(set-logic QF_NRA)")
            add("(set-info :precision 0.001)")
            add("(declare-fun x () Real)")
            add("(assert (<= 3.0 x))")
            add("(assert (<= x 64.0))")
            add("(assert (not (> (- (* 2.0 3.14159265) (* 2.0 (* x (arcsin (* (cos 0.797) (sin (/ 3.14159265 x))))))) (+ (- 0.591 (* 0.0331 x))(+ (* 0.506 (/ (- 1.26 1.0) (- 1.26 1.0))) 1.0)))))")
            add("(check-sat)\n(exit)")
        }
        val resultStr = getResultFromDreal(commandsForDreal)
        Assert.assertEquals("unsat", resultStr[0])
    }   
    
    @Test
    fun testGetResultFromDrealFacetalAbstraction() {
        val commandsForDreal = ArrayList<String>().apply {
            add("(set-logic QF_NRA_ODE)")
            add("(declare-fun predator () Real [0.1, 5.0])")
            add("(declare-fun predator_0_0 () Real [0.1, 5.0])")
            add("(declare-fun predator_0_1 () Real [0.1, 5.0])")
            add("(declare-fun predator_0_t () Real [0.1, 5.0])")
            add("(declare-fun prey () Real [0.1, 5.0])")
            add("(declare-fun prey_0_0 () Real [0.1, 5.0])")
            add("(declare-fun prey_0_1 () Real [0.1, 5.0])")
            add("(declare-fun prey_0_t () Real [0.1, 5.0])")
            add("(declare-fun time_0 () Real [0.0, 1.0])")
            add("(declare-fun time_1 () Real [0.0, 1.0])")
            add("(define-ode flow_1 ((= d/dt[predator] (- (* (* 1.0 prey) predator) (* 1.0 predator))) (= d/dt[prey] (- (* 1.0 prey) (* (* 1.33 predator) prey)))))")
            add("(assert (and (and (and (>= predator_0_0 0.5) (<= predator_0_0 0.6)) (= prey_0_0 1.05) (>= (- (* 1.0 prey_0_0) (* (* 1.33 predator_0_0) prey_0_0)) 0.0)) (and (= predator_0_t 0.6) (and (>= prey_0_t 1.05) (<= prey_0_t 1.15)) (>= (- (* (* 1.0 prey_0_t) predator_0_t) (* 1.0 predator_0_t)) 0.0)) (= [predator_0_t prey_0_t] (integral 0. time_0 [predator_0_0 prey_0_0] flow_1)) (forall_t 1 [0.0 1.0] (and (>= prey_0_t 1.05) (<= prey_0_t 1.15) (>= predator_0_t 0.5) (<= predator_0_t 0.6)))))")
            add("(check-sat)")
            add("(exit)")
        }
        val resultStr = getResultFromDreal(commandsForDreal)
        Assert.assertEquals("delta-sat with delta = 0.00100000000000000", resultStr[0])
    }   
    
        
    @Test
    fun testGetResultFromDrealFAonePar() {
        val commandsForDreal = ArrayList<String>().apply {
            add("(set-logic QF_NRA_ODE)")
            add("(declare-fun par () Real [0.1, 3.0])")
            add("(declare-fun par_0_0 () Real [0.1, 3.0])")
            add("(declare-fun par_0_1 () Real [0.1, 3.0])")
            add("(declare-fun par_0_t () Real [0.1, 3.0])")
            add("(declare-fun predator () Real [0.1, 5.0])")
            add("(declare-fun predator_0_0 () Real [0.1, 5.0])")
            add("(declare-fun predator_0_1 () Real [0.1, 5.0])")
            add("(declare-fun predator_0_t () Real [0.1, 5.0])")
            add("(declare-fun prey () Real [0.1, 5.0])")
            add("(declare-fun prey_0_0 () Real [0.1, 5.0])")
            add("(declare-fun prey_0_1 () Real [0.1, 5.0])")
            add("(declare-fun prey_0_t () Real [0.1, 5.0])")
            add("(declare-fun time_0 () Real [0.0, 1.0])")
            add("(declare-fun time_1 () Real [0.0, 1.0])")
            add("(define-ode flow_1 ((= d/dt[par] (0.0)) (= d/dt[predator] (- (* (* 1.0 prey) predator) (* 1.0 predator))) (= d/dt[prey] (- (* 1.0 prey) (* (* par predator) prey)))))")
            add("(assert (and (and (and (>= predator_0_0 0.5) (<= predator_0_0 0.6)) (= prey_0_0 1.05) (>= (- (* 1.0 prey_0_0) (* (* 1.33 predator_0_0) prey_0_0)) 0.0)) (and (= predator_0_t 0.6) (and (>= prey_0_t 1.05) (<= prey_0_t 1.15)) (>= (- (* (* 1.0 prey_0_t) predator_0_t) (* 1.0 predator_0_t)) 0.0)) (= [par_0_t predator_0_t prey_0_t] (integral 0. time_0 [par_0_0 predator_0_0 prey_0_0] flow_1)) (forall_t 1 [0.0 1.0] (and (>= par_0_t 0.1) (<= par_0_t 3.0) (>= prey_0_t 1.05) (<= prey_0_t 1.15) (>= predator_0_t 0.5) (<= predator_0_t 0.6)))))")
            add("(check-sat)")
            add("(exit)")
        }
        val resultStr = getResultFromDreal(commandsForDreal)
        Assert.assertEquals("delta-sat with delta = 0.00100000000000000", resultStr[0])
    }
    
    /*@Test
    fun testGetResultFromDreach() {
        val commandsForDreach = ArrayList<String>().apply{
            add("#define D 0.1")
            add("#define K 0.95")
            add("[0, 15] x;")
            add("[9.8] g;")
            add("[-18, 18] v;")
            add("[0, 3] time;")
            add("{ mode 1;")
            add("invt:")
            add("   (v <= 0);")
            add("   (x >= 0);")
            add("flow:")
            add("   d/dt[x] = v;")
            add("   d/dt[v] = -g + D * v^2;")
            add("jump:")
            add("   (x = 0) ==> @2 (and (x' = x) (v' = - K * v));")
            add("}")
            add("{")
            add("mode 2;")
            add("invt:")
            add("   (v >= 0);")
            add("   (x >= 0);")
            add("flow:")
            add("   d/dt[x] = v;")
            add("   d/dt[v] = -g - D * v^2;")
            add("jump:")
            add("   (v = 0) ==> @1 (and (x' = x) (v' = v));")
            add("}")
            add("init:")
            add("@1	(and (x >= 8.74) (v = 0));")
            add("goal:")
            add("@1	(and (x >= 0.45));")
        }
        val resultStr = getResultFromDreach(commandsForDreach)
        Assert.assertEquals("???", resultStr)
    }*/
    
}
