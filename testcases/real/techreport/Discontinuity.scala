import leon.real._
import RealOps._

object Discontinuity {

  //Robustness Analysis of Finite Precision Implementations, arxiv extended version,
  // E. Goubault, S. Putot
  def simpleInterpolator(e: Real): Real = {
    require(0.0 <= e && e <= 100.0 && e +/- 0.00001)

    val r1: Real = 0.0
    val const: Real = 2.25  // else this would be pre-evaluated by the parser
    val r2: Real = 5 * const
    val const2: Real = 1.1
    val r3: Real = r2 + 20 * const2  //same here

    if (e < 5)
      e * 2.25 + r1
    else if (e < 25)
      (e - 5) * 1.1 + r2
    else
      r3
  } ensuring (res => -2.25e-5 <= res && res <= 33.26 && res +/- 3.5e-5)


  def squareRoot(i: Real): Real = {
    require(1.0 <= i && i <= 2.0 && i +/- 0.001)

    val sqrt2: Real = 1.414213538169860839843750

    if (i >= 2)
      sqrt2 * (1.0 + (i/2 - 1) * (0.5 - 0.125 * ( i/2 - 1)))
    else
      1 + (i - 1) * (0.5 + (i-1) * (-0.125 + (i - 1) * 0.0625))
  
  } ensuring (res => 1 <= res && res <= 1.4531 && res +/- 0.03941)


  def cubicSpline(x: Real): Real = {
    require(-2 <= x && x <= 2)

    if (x <= -1) {
      0.25 * (x + 2)* (x + 2)* (x + 2)
    } else if (x <= 0) {
      0.25*(-3*x*x*x - 6*x*x +4)
    } else if (x <= 1) {
      0.25*(3*x*x*x - 6*x*x +4)
    } else {
      0.25*(2 - x)*(2 - x)*(2 - x)
    }

  }  ensuring (res => res +/- 2.3e-8)


  def squareRoot3(x: Real): Real = {
    require(0 < x && x < 10 && x +/- 1e-10 )
    if (x < 1e-5) 1 + 0.5 * x
    else sqrt(1 + x)
  } ensuring( res => res +/- 1e-10) //valid

  def squareRoot3Invalid(x: Real): Real = {
    require(0 < x && x < 10 && x +/- 1e-10 )
    if (x < 1e-4) 1 + 0.5 * x
    else sqrt(1 + x)
  } ensuring( res => res +/- 1e-10) //invalid

 

  def linearFit(x: Real, y: Real): Real = {
    require(-4 <= x && x <= 4 && -4 <= y && y <= 4)

    if (x <= 0) {
      if (y <= 0) {
        0.0958099 - 0.0557219*x - 0.0557219*y
      } else {
        -0.0958099 + 0.0557219*x - 0.0557219*y
      }

    } else { // x >= 0
      if (y <= 0) {
        -0.0958099 - 0.0557219*x + 0.0557219*y
      } else {
        0.0958099 + 0.0557219*x + 0.0557219*y  
      }
    }

  }


  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  def quadraticFit(x: Real, y: Real): Real = {
    require(-4 <= x && x <= 4 && -4 <= y && y <= 4)

    if (x <= 0) {
      if (y <= 0) {
        -0.0495178 - 0.188656*x - 0.0502969*x*x - 0.188656*y + 0.0384002*x*y - 0.0502969*y*y
      } else {
        0.0495178 + 0.188656*x + 0.0502969*x*x - 0.188656*y + 0.0384002*x*y + 0.0502969*y*y
      }

    } else { // x >= 0
      if (y <= 0) {
        0.0495178 - 0.188656*x + 0.0502969*x*x + 0.188656*y + 0.0384002*x*y + 0.0502969*y*y
      } else {
        -0.0495178 + 0.188656*x - 0.0502969*x*x + 0.188656*y + 0.0384002*x*y - 0.0502969*y*y
      }
    }
  }

  def quadraticFitErr(x: Real, y: Real): Real = {
    require(-4 <= x && x <= 4 && -4 <= y && y <= 4 && x +/- 0.001 && y +/- 0.001)
    
    if (x <= 0) {
      if (y <= 0) {
        -0.0495178 - 0.188656*x - 0.0502969*x*x - 0.188656*y + 0.0384002*x*y - 0.0502969*y*y
      } else {
        0.0495178 + 0.188656*x + 0.0502969*x*x - 0.188656*y + 0.0384002*x*y + 0.0502969*y*y
      }

    } else { // x >= 0
      if (y <= 0) {
        0.0495178 - 0.188656*x + 0.0502969*x*x + 0.188656*y + 0.0384002*x*y + 0.0502969*y*y
      } else {
        -0.0495178 + 0.188656*x - 0.0502969*x*x + 0.188656*y + 0.0384002*x*y - 0.0502969*y*y
      }
    }
  }

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  def quadraticFit2(x: Real, y: Real): Real = {
    require(-4 <= x && x <= 4 && -4 <= y && y <= 4)

    if (x > y) {
      0.238604 - 0.143624*x + 0.0137*x*x + 0.143624*y + 0.00605411*x*y + 0.0137*y*y
    } else {
      0.238604 + 0.143624*x + 0.0137*x*x - 0.143624*y + 0.00605411*x*y + 0.0137*y*y
    }
  }

  def quadraticFit2Err(x: Real, y: Real): Real = {
    require(-4 <= x && x <= 4 && -4 <= y && y <= 4 && x +/- 0.001 && y +/- 0.001)
    
    if (x > y) {
      0.238604 - 0.143624*x + 0.0137*x*x + 0.143624*y + 0.00605411*x*y + 0.0137*y*y
    } else {
      0.238604 + 0.143624*x + 0.0137*x*x - 0.143624*y + 0.00605411*x*y + 0.0137*y*y
    }
  }

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


  def styblinski(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5)
    
    if (y <= 0)
      if (x <= 0)
        -1.4717 + 2.83079*x + 0.786996*x*x + 2.83079*y - 1.07939*1e-16*x*y + 0.786996*y*y
      else 
        -1.4717 - 2.33079*x + 0.786996*x*x + 2.83079*y + 9.1748*1e-16*x*y + 0.786996*y*y
    else 
      if (x <= 0)
        -1.4717 + 2.83079*x + 0.786996*x*x - 2.33079*y + 3.23816*1e-16*x*y + 0.786996*y*y
      else 
        -1.4717 - 2.33079*x + 0.786996*x*x - 2.33079*y + 1.72702*1e-15*x*y + 0.786996*y*y
  }

  def styblinskiErr(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5 && x +/- 0.001 && y +/- 0.001)
    
    if (y <= 0)
      if (x <= 0)
        -1.4717 + 2.83079*x + 0.786996*x*x + 2.83079*y - 1.07939*1e-16*x*y + 0.786996*y*y
      else 
        -1.4717 - 2.33079*x + 0.786996*x*x + 2.83079*y + 9.1748*1e-16*x*y + 0.786996*y*y
    else 
      if (x <= 0)
        -1.4717 + 2.83079*x + 0.786996*x*x - 2.33079*y + 3.23816*1e-16*x*y + 0.786996*y*y
      else 
        -1.4717 - 2.33079*x + 0.786996*x*x - 2.33079*y + 1.72702*1e-15*x*y + 0.786996*y*y
  }

  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

  // this is the styblinski function a little modified, y^3 instead of y^4
  
  def sortOfStyblinski(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5)

    if (y < x)
      -2.34606 + 0.230969*x + 0.280371*x*x + 0.979579*y + 0.000353913*x*y - 0.842642*y*y
    else
      -3.03249 + 0.230969*x + 0.266765*x*x + 0.979579*y - 0.000353913*x*y - 0.757358*y*y

  }

  def sortOfStyblinskiErr(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5 && x +/- 0.001 && y +/- 0.001)

    if (y < x)
      -2.34606 + 0.230969*x + 0.280371*x*x + 0.979579*y + 0.000353913*x*y - 0.842642*y*y
    else
      -3.03249 + 0.230969*x + 0.266765*x*x + 0.979579*y - 0.000353913*x*y - 0.757358*y*y

  }


  //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 
  // w= 1.0, step = 0.01
  def jetApprox(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5)

    if (y <= 0)
      if (x <= 0)
        -0.0939097 + 0.313122*x + 0.20583 *x*x + 0.00111573*y - 0.00514265*x*y + 
          0.00380476 *x*x*y - 0.000567088 *y*y + 0.000568992*x*y*y

      else 
        -0.0758695 - 0.300328*x + 0.168573 *x*x + 0.00633419*y - 0.00910356*x*y +
          0.0125128 *x*x*y + 0.000567088 *y*y + 0.000568992*x*y*y

    else 
      if (x <= 0)
        -0.0939097 + 0.313122*x + 0.20583 *x*x + 0.00604201*y + 0.0147615*x*y + 
          0.0087808 *x*x*y - 0.000567088 *y*y + 0.000568992*x*y*y
 
      else
        -0.0758695 - 0.300328*x + 0.168573 *x*x + 0.00140791*y + 0.0108006*x*y +
         0.00753679 *x*x*y + 0.000567088 *y*y + 0.000568992*x*y*y

  } 

  // w= 1.0, step = 0.01
  def jetApproxErr(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5 && x +/- 0.001 && y +/- 0.001)

    if (y <= 0)
      if (x <= 0)
        -0.0939097 + 0.313122*x + 0.20583 *x*x + 0.00111573*y - 0.00514265*x*y + 
          0.00380476 *x*x*y - 0.000567088 *y*y + 0.000568992*x*y*y

      else 
        -0.0758695 - 0.300328*x + 0.168573 *x*x + 0.00633419*y - 0.00910356*x*y +
          0.0125128 *x*x*y + 0.000567088 *y*y + 0.000568992*x*y*y

    else 
      if (x <= 0)
        -0.0939097 + 0.313122*x + 0.20583 *x*x + 0.00604201*y + 0.0147615*x*y + 
          0.0087808 *x*x*y - 0.000567088 *y*y + 0.000568992*x*y*y
 
      else
        -0.0758695 - 0.300328*x + 0.168573 *x*x + 0.00140791*y + 0.0108006*x*y +
         0.00753679 *x*x*y + 0.000567088 *y*y + 0.000568992*x*y*y

  } 


  // w = 1.0, step = 0.01
   def jetApproxGoodFit(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5)

    if (y < x)
      -0.317581 + 0.0563331*x + 0.0966019*x*x + 0.0132828*y + 0.0372319*x*y + 0.00204579*y*y

    else
      -0.330458 + 0.0478931*x + 0.154893*x*x + 0.0185116*y - 0.0153842*x*y - 0.00204579*y*y

  }

   def jetApproxGoodFitErr(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5 && x +/- 0.001 && y +/- 0.001)

    if (y < x)
      -0.317581 + 0.0563331*x + 0.0966019*x*x + 0.0132828*y + 0.0372319*x*y + 0.00204579*y*y

    else
      -0.330458 + 0.0478931*x + 0.154893*x*x + 0.0185116*y - 0.0153842*x*y - 0.00204579*y*y

  }


  def jetApproxBadFit(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5)

    if (y < x)
      -0.270322 - 0.135107*x + 0.141303*x*x + 0.0256895*y - 
        0.0207437*x*y + 0.0140767*x*x*y + 0.00719123*y*y - 0.0014995*x*y*y

    else
      -0.299952 + 0.138877*x + 0.177148*x*x - 0.0533731*y - 
        0.0601704*x*y - 0.00674474*x*x*y + 0.0161593*y*y + 0.00530538*x*y*y
  }

  def jetApproxBadFitErr(x: Real, y: Real): Real = {
    require(-5 <= x && x <= 5 && -5 <= y && y <= 5 && x +/- 0.001 && y +/- 0.001)

    if (y < x)
      -0.270322 - 0.135107*x + 0.141303*x*x + 0.0256895*y - 
        0.0207437*x*y + 0.0140767*x*x*y + 0.00719123*y*y - 0.0014995*x*y*y

    else
      -0.299952 + 0.138877*x + 0.177148*x*x - 0.0533731*y - 
        0.0601704*x*y - 0.00674474*x*x*y + 0.0161593*y*y + 0.00530538*x*y*y
  }
 
}