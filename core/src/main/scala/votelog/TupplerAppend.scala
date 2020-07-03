/**
 * Copied from https://github.com/endpoints4s/endpoints4s
 * Copyright julienrf and tdroxler
 * The code in this file is released under the MIT License (https://opensource.org/licenses/mit-license.php)
 */

package votelog

trait TuplerAppend extends Tupler2 {

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 3.
   */
  implicit def tupler3Append[T1, T2, T3, T4]: Tupler[(T1, T2, T3), T4] { type Out = (T1, T2, T3, T4) } =
    new Tupler[(T1, T2, T3), T4] {
      type Out = (T1, T2, T3, T4)
      def apply(t: (T1, T2, T3), t4: T4): (T1, T2, T3, T4) = (t._1, t._2, t._3, t4)
      def unapply(out: (T1, T2, T3, T4)): ((T1, T2, T3), T4) = {
        val (t1, t2, t3, t4) = out
        ((t1, t2, t3), t4)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 4.
   */
  implicit def tupler4Append[T1, T2, T3, T4, T5]: Tupler[(T1, T2, T3, T4), T5] { type Out = (T1, T2, T3, T4, T5) } =
    new Tupler[(T1, T2, T3, T4), T5] {
      type Out = (T1, T2, T3, T4, T5)
      def apply(t: (T1, T2, T3, T4), t5: T5): (T1, T2, T3, T4, T5) = (t._1, t._2, t._3, t._4, t5)
      def unapply(out: (T1, T2, T3, T4, T5)): ((T1, T2, T3, T4), T5) = {
        val (t1, t2, t3, t4, t5) = out
        ((t1, t2, t3, t4), t5)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 5.
   */
  implicit def tupler5Append[T1, T2, T3, T4, T5, T6]: Tupler[(T1, T2, T3, T4, T5), T6] { type Out = (T1, T2, T3, T4, T5, T6) } =
    new Tupler[(T1, T2, T3, T4, T5), T6] {
      type Out = (T1, T2, T3, T4, T5, T6)
      def apply(t: (T1, T2, T3, T4, T5), t6: T6): (T1, T2, T3, T4, T5, T6) = (t._1, t._2, t._3, t._4, t._5, t6)
      def unapply(out: (T1, T2, T3, T4, T5, T6)): ((T1, T2, T3, T4, T5), T6) = {
        val (t1, t2, t3, t4, t5, t6) = out
        ((t1, t2, t3, t4, t5), t6)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 6.
   */
  implicit def tupler6Append[T1, T2, T3, T4, T5, T6, T7]: Tupler[(T1, T2, T3, T4, T5, T6), T7] { type Out = (T1, T2, T3, T4, T5, T6, T7) } =
    new Tupler[(T1, T2, T3, T4, T5, T6), T7] {
      type Out = (T1, T2, T3, T4, T5, T6, T7)
      def apply(t: (T1, T2, T3, T4, T5, T6), t7: T7): (T1, T2, T3, T4, T5, T6, T7) = (t._1, t._2, t._3, t._4, t._5, t._6, t7)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7)): ((T1, T2, T3, T4, T5, T6), T7) = {
        val (t1, t2, t3, t4, t5, t6, t7) = out
        ((t1, t2, t3, t4, t5, t6), t7)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 7.
   */
  implicit def tupler7Append[T1, T2, T3, T4, T5, T6, T7, T8]: Tupler[(T1, T2, T3, T4, T5, T6, T7), T8] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7), T8] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7), t8: T8): (T1, T2, T3, T4, T5, T6, T7, T8) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t8)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8)): ((T1, T2, T3, T4, T5, T6, T7), T8) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8) = out
        ((t1, t2, t3, t4, t5, t6, t7), t8)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 8.
   */
  implicit def tupler8Append[T1, T2, T3, T4, T5, T6, T7, T8, T9]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8), T9] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8), T9] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8), t9: T9): (T1, T2, T3, T4, T5, T6, T7, T8, T9) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t9)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9)): ((T1, T2, T3, T4, T5, T6, T7, T8), T9) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8), t9)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 9.
   */
  implicit def tupler9Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9), T10] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9), T10] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9), t10: T10): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t10)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9), T10) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9), t10)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 10.
   */
  implicit def tupler10Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), T11] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), T11] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), t11: T11): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t11)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), T11) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10), t11)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 11.
   */
  implicit def tupler11Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), T12] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), T12] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), t12: T12): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t12)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), T12) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11), t12)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 12.
   */
  implicit def tupler12Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), T13] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), T13] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), t13: T13): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t13)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), T13) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12), t13)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 13.
   */
  implicit def tupler13Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), T14] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), T14] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), t14: T14): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t14)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), T14) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13), t14)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 14.
   */
  implicit def tupler14Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), T15] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), T15] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), t15: T15): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t15)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), T15) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14), t15)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 15.
   */
  implicit def tupler15Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), T16] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), T16] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), t16: T16): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t16)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), T16) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15), t16)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 16.
   */
  implicit def tupler16Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), T17] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), T17] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), t17: T17): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t17)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), T17) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16), t17)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 17.
   */
  implicit def tupler17Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), T18] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), T18] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), t18: T18): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t18)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), T18) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17), t18)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 18.
   */
  implicit def tupler18Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), T19] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), T19] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), t19: T19): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t19)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), T19) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18), t19)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 19.
   */
  implicit def tupler19Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), T20] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), T20] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), t20: T20): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t20)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), T20) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19), t20)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 20.
   */
  implicit def tupler20Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), T21] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), T21] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), t21: T21): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20, t21)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), T21) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20), t21)
      }
    }

  /**
   * A [[Tupler]] that appends an element to an existing tuple of size 21.
   */
  implicit def tupler21Append[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]: Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), T22] { type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) } =
    new Tupler[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), T22] {
      type Out = (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)
      def apply(t: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), t22: T22): (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) = (t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9, t._10, t._11, t._12, t._13, t._14, t._15, t._16, t._17, t._18, t._19, t._20, t._21, t22)
      def unapply(out: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22)): ((T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), T22) = {
        val (t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21, t22) = out
        ((t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20, t21), t22)
      }
    }
}
