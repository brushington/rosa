/* Copyright 2009-2013 EPFL, Lausanne
 *
 * Author: Ravi
 * Date: 20.11.2013
 **/

import leon.collection._
import leon._ 

object HeapSort {
 
  sealed abstract class Heap {
    val rank : Int = this match {
      case Leaf() => 0
      case Node(_, l, r) => 
        1 + max(l.rank, r.rank)
    }
  }
  case class Leaf() extends Heap
  case class Node(value:Int, left: Heap, right: Heap) extends Heap

  def max(i1 : Int, i2 : Int) = if (i1 >= i2) i1 else i2

  def hasLeftistProperty(h: Heap) : Boolean = h match {
    case Leaf() => true
    case Node(_,l,r) => 
      hasLeftistProperty(l) && 
      hasLeftistProperty(r) && 
      l.rank >= r.rank 
  }

  def heapSize(t: Heap): Int = { t match {
    case Leaf() => 0
    case Node(v, l, r) => heapSize(l) + 1 + heapSize(r)
  }} ensuring(_ >= 0)

  private def merge(h1: Heap, h2: Heap) : Heap = {
    require(hasLeftistProperty(h1) && hasLeftistProperty(h2))
    (h1,h2) match {
      case (Leaf(), _) => h2
      case (_, Leaf()) => h1
      case (Node(v1, l1, r1), Node(v2, l2, r2)) =>
        if(v1 > v2)
          Node(v1, l1, merge(r1, h2)) // FIXME: Forgot to put the heap of lowest rank on the left 
                                      //        (failed to use makeN)
        else
          Node(v2, l2, merge(h1, r2)) // FIXME: Same as above
    }
  } ensuring { res => 
    hasLeftistProperty(res) && 
    heapSize(h1) + heapSize(h2) == heapSize(res)
  }
/*
  private def makeN(value: Int, left: Heap, right: Heap) : Heap = {
    require(hasLeftistProperty(left) && hasLeftistProperty(right))
    if(left.rank >= right.rank)
      Node(value, left, right)
    else
      Node(value, right, left)
  } ensuring { hasLeftistProperty(_) }
*/
  def insert(element: Int, heap: Heap) : Heap = {
    require(hasLeftistProperty(heap))

    merge(Node(element, Leaf(), Leaf()), heap)

  } ensuring { res =>
    hasLeftistProperty(res) && 
    heapSize(res) == heapSize(heap) + 1
  }

  def findMax(h: Heap) : Option[Int] = {
    h match {
      case Node(m,_,_) => Some(m)
      case Leaf() => None()
    }
  }

  def removeMax(h: Heap) : Heap = {
    require(hasLeftistProperty(h))
    h match {
      case Node(_,l,r) => merge(l, r)
      case l => l
    }
  }

} 
