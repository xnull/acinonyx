package acinonyx.integtest

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MyTest extends FunSuite{

  test("check"){
    assert(1 == 1)
  }
}
