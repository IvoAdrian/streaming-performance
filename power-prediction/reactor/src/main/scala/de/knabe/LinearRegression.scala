package de.knabe

// Source: https://algs4.cs.princeton.edu/14analysis/LinearRegression.java.html
// Shortened and translated to Scala by Ivo Adrian Knabe

/** ****************************************************************************
 * Compute least squares solution to y = beta * x + alpha.
 * Simple linear regression.
 *
 * *****************************************************************************/


/**
 * The {@code LinearRegression} class performs a simple linear regression
 * on an set of <em>n</em> data points (<em>y<sub>i</sub></em>, <em>x<sub>i</sub></em>).
 * That is, it fits a straight line <em>y</em> = &alpha; + &beta; <em>x</em>,
 * (where <em>y</em> is the response variable, <em>x</em> is the predictor variable,
 * &alpha; is the <em>y-intercept</em>, and &beta; is the <em>slope</em>)
 * that minimizes the sum of squared residuals of the linear regression model.
 * It also computes associated statistics, including the coefficient of
 * determination <em>R</em><sup>2</sup> and the standard deviation of the
 * estimates for the slope and <em>y</em>-intercept.
 * Performs a linear regression on the data points {@code (y[i], x[i])}.
 *
 *
 * @param  x the values of the predictor variable
 * @param  y the corresponding values of the response variable
 * @throws IllegalArgumentException if the lengths of the two arrays are not equal
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
class LinearRegression(val x: Array[Double], val y: Array[Double]) {
  private val xn = x.length
  private val yn = y.length
  if (xn != yn){
    throw new IllegalArgumentException(s"array lengths are not equal: xn=$xn, yn=$yn")
  }
  private val n = xn
  // first pass
  private var sumx = 0.0
  private var sumy = 0.0
  private var sumx2 = 0.0
  for (i <- 0 until n) {
    sumx += x(i)
    sumx2 += x(i) * x(i)
    sumy += y(i)
  }
  private val xbar: Double = sumx / n
  private val ybar: Double = sumy / n
  // second pass: compute summary statistics
  private var xxbar = 0.0
  private var yybar = 0.0
  private var xybar = 0.0
  for (i <- 0 until n) {
    xxbar += (x(i) - xbar) * (x(i) - xbar)
    yybar += (y(i) - ybar) * (y(i) - ybar)
    xybar += (x(i) - xbar) * (y(i) - ybar)
  }
  /**
   * Returns the slope &beta; of the best of the best-fit line <em>y</em> = &alpha; + &beta; <em>x</em>.
   *
   * @return the slope &beta; of the best-fit line <em>y</em> = &alpha; + &beta; <em>x</em>
   */
  private val slope = xybar / xxbar
  /**
   * Returns the <em>y</em>-intercept &alpha; of the best of the best-fit line <em>y</em> = &alpha; + &beta; <em>x</em>.
   *
   * @return the <em>y</em>-intercept &alpha; of the best-fit line <em>y = &alpha; + &beta; x</em>
   */
  private val intercept = ybar - slope * xbar


  /**
   * Returns the expected response {@code y} given the value of the predictor
   * variable {@code x}.
   *
   * @param  x the value of the predictor variable
   * @return the expected response { @code y} given the value of the predictor
   *                                       variable { @code x}
   */
  def predict(x: Double): Double = slope * x + intercept

}
