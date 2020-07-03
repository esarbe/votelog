package votelog.domain.diff

import java.lang.Math.min
import java.lang.Math.max

/**
 * Builds an list of instructions to turn list A into list B
 */
object ListDiff {

  implicit class IntOps(a: Int) {

    /**
     * Returns the non-negative modulo
     * @param m divisor for modulo operation
     * @return non-negative remainder of euclidian division of a by m
     */
    def mod(m: Int): Int = {
      val rem = a % m
      if (rem < 0) m + rem
      else rem
    }
  }

  sealed trait Op
  object Op {
    final case class Insert(oldPosition: Int, newPosition: Int) extends Op
    final case class Delete(position: Int) extends Op
  }

  /*
   * Returns a minimal list of differences between 2 lists e and f
   * requiring O(min(len(e),len(f))) space and O(min(len(e),len(f)) * D)
   * worst-case execution time where D is the number of differences.
   *
   * https://neil.fraser.name/writing/diff/myers.pdf
   *
   * As documented at http://blog.robertelder.org/diff-algorithm/
   * thanks for the amazing walk through
   *
   * Also helpful: http://simplygenius.net/Article/DiffTutorial1,
   *               http://simplygenius.net/Article/DiffTutorial2
   */

  def diff[A](e: Seq[A], f: Seq[A], i: Int = 0, j: Int = 0): List[Op] = {
    val N = e.length
    val M = f.length
    val Z = 2 * min(e.length, f.length) + 2
    val L = e.length + f.length
    if (N > 0 && M > 0) {
      val w = N - M
      val g = Array.ofDim[Int](Z)
      val p = Array.ofDim[Int](Z)
      for (h <- 0 until (L / 2 +  (L / 2 + (if (L % 2 != 0) 0 else 1)) + 1)) {
        for (r <- 0 until 2) {
          val (c, d, o, m) = if (r == 0) (g, p, 1, 1) else (p, g, 0, -1)
          for (k <- -(h - 2 * max(0, h - M)) until (h - 2 * max(0, h - N) + 1, 2 )) {
            var a = if (k == -h || k != h && c((k - 1) mod Z) < c(( k + 1) mod Z)) c((k + 1) mod Z)
               else c((k - 1) mod Z) + 1
            var b = a - k
            val (s, t) = (a, b)
            while (a < N && b < M && e((1 - o) * N + m * a + (o - 1)) == f((1 - o) * M + m * b + (o - 1))) {
              a = a + 1
              b = b + 1
            }
            c(k mod Z) = a
            val z = - (k - w)
            if (L % 2 == o && z >= -(h - o) && z <= h - o && c(k mod Z) + d(z mod Z) >= N) {
              val (d, x, y, u, v) =
                if (o == 1) (2*h-1, s, t, a, b)
                else (2*h, N-a, M-b, N-s, M-t)
              if (d > 1 || (x != u && y != v)) {
                return diff(e.slice(0, x), f.slice(0, y), i, j) ++ diff(e.slice(u, N), f.slice(v, M), i + u, j + v)
              } else if (M > N) {
                return diff(List.empty[A], f.slice(N,M), i + N, j + N)
              } else if (M < N) {
                return diff(e.slice(M,N), List.empty[A], i + M, j + M)
              } else {
                return List.empty[Op]
              }
            }
          }
        }
      }
      List.empty[Op]
    } else if ( N > 0) {
      (0 until N).map(n => Op.Delete(i + n)).toList
    } else {
      (0 until M).map(n => Op.Insert(i, j + n)).toList
    }
  }
}
