package com.thesis.bitperfectusb.domain.engine

import com.thesis.bitperfectusb.domain.model.AnovaResult
import com.thesis.bitperfectusb.domain.model.DescriptiveStats
import com.thesis.bitperfectusb.domain.model.WelchTTestResult
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Implements the statistical machinery behind Experiments A, B and D:
 *  - Descriptive statistics with a 95% confidence interval (Section 4.1.1)
 *  - Welch's two-sample t-test (Section 4.1.2)
 *  - One-way ANOVA (Sections 4.2.2 / 4.4.2)
 *
 * p-values are computed from the regularized incomplete beta function, the standard
 * closed-form link between the Student-t / F distributions and Beta(a,b) — see
 * Abramowitz & Stegun (1972), *Handbook of Mathematical Functions*, listed in the
 * thesis references. This avoids needing lookup tables and works for any df.
 */
class StatisticsEngine {

    // ---------------------------------------------------------------------
    // Descriptive statistics
    // ---------------------------------------------------------------------

    fun descriptiveStats(values: List<Double>, confidenceLevel: Double = 0.95): DescriptiveStats {
        require(values.isNotEmpty()) { "Cannot compute statistics on an empty sample." }
        val n = values.size
        val mean = values.average()
        val variance = if (n > 1) {
            values.sumOf { (it - mean).pow(2) } / (n - 1)
        } else 0.0
        val sd = sqrt(variance)
        val stdError = if (n > 1) sd / sqrt(n.toDouble()) else 0.0
        val alpha = 1 - confidenceLevel
        val tCrit = if (n > 1) criticalT(df = (n - 1).toDouble(), alpha = alpha) else 0.0
        val margin = tCrit * stdError
        return DescriptiveStats(
            n = n,
            mean = mean,
            stdDev = sd,
            ci95Low = mean - margin,
            ci95High = mean + margin
        )
    }

    // ---------------------------------------------------------------------
    // Welch's two-sample t-test (unequal variances assumed)
    // ---------------------------------------------------------------------

    fun welchTTest(groupA: List<Double>, groupB: List<Double>, alpha: Double = 0.05): WelchTTestResult {
        val statsA = descriptiveStats(groupA)
        val statsB = descriptiveStats(groupB)

        val varAOverN = statsA.stdDev.pow(2) / statsA.n
        val varBOverN = statsB.stdDev.pow(2) / statsB.n
        val denominator = sqrt(varAOverN + varBOverN)

        // If both groups have zero variance but different means, the groups are
        // perfectly separated — t is effectively infinite, p ≈ 0.
        if (denominator == 0.0) {
            val isSignificant = statsA.mean != statsB.mean
            val df = (statsA.n + statsB.n - 2).toDouble().coerceAtLeast(1.0)
            return WelchTTestResult(
                tStatistic = if (isSignificant) Double.MAX_VALUE else 0.0,
                degreesOfFreedom = df,
                pValue = if (isSignificant) 0.0 else 1.0,
                groupA = statsA,
                groupB = statsB,
                significant = isSignificant
            )
        }

        val t = (statsA.mean - statsB.mean) / denominator

        val df = if (statsA.n > 1 && statsB.n > 1) {
            (varAOverN + varBOverN).pow(2) /
                (varAOverN.pow(2) / (statsA.n - 1) + varBOverN.pow(2) / (statsB.n - 1))
        } else 1.0

        val p = tDistributionTwoTailedPValue(abs(t), df)

        return WelchTTestResult(
            tStatistic = t,
            degreesOfFreedom = df,
            pValue = p,
            groupA = statsA,
            groupB = statsB,
            significant = p < alpha
        )
    }

    // ---------------------------------------------------------------------
    // One-way ANOVA
    // ---------------------------------------------------------------------

    fun oneWayAnova(groups: Map<String, List<Double>>, alpha: Double = 0.05): AnovaResult {
        require(groups.size >= 2) { "ANOVA requires at least two groups." }
        val allValues = groups.values.flatten()
        val grandMean = allValues.average()
        val k = groups.size
        val nTotal = allValues.size

        var ssBetween = 0.0
        var ssWithin = 0.0
        val groupMeans = mutableMapOf<String, Double>()

        for ((label, values) in groups) {
            require(values.isNotEmpty()) { "ANOVA group '$label' has no samples." }
            val groupMean = values.average()
            groupMeans[label] = groupMean
            ssBetween += values.size * (groupMean - grandMean).pow(2)
            ssWithin += values.sumOf { (it - groupMean).pow(2) }
        }

        val dfBetween = k - 1
        val dfWithin = nTotal - k
        val msBetween = ssBetween / dfBetween
        val msWithin = if (dfWithin > 0) ssWithin / dfWithin else 0.0

        val f = if (msWithin == 0.0) {
            // All within-group variance is zero. If between-group variance also
            // exists the groups are perfectly separated → treat F as infinite.
            if (ssBetween > 0.0) Double.MAX_VALUE else 0.0
        } else msBetween / msWithin
        val p = when {
            f == Double.MAX_VALUE -> 0.0
            dfWithin > 0 -> fDistributionUpperTailPValue(f, dfBetween.toDouble(), dfWithin.toDouble())
            else -> 1.0
        }

        return AnovaResult(
            fStatistic = f,
            dfBetween = dfBetween,
            dfWithin = dfWithin,
            pValue = p,
            significant = p < alpha,
            groupMeans = groupMeans
        )
    }

    // ---------------------------------------------------------------------
    // Distribution machinery (regularized incomplete beta function)
    // ---------------------------------------------------------------------

    /** Two-tailed p-value for a t-statistic with the given degrees of freedom. */
    fun tDistributionTwoTailedPValue(t: Double, df: Double): Double {
        if (df <= 0.0) return 1.0
        val x = df / (df + t * t)
        return regularizedIncompleteBeta(x, df / 2.0, 0.5).coerceIn(0.0, 1.0)
    }

    /** Upper-tail p-value for an F-statistic — this is what ANOVA significance testing needs. */
    fun fDistributionUpperTailPValue(f: Double, df1: Double, df2: Double): Double {
        if (f <= 0.0) return 1.0
        val x = df1 * f / (df1 * f + df2)
        return (1.0 - regularizedIncompleteBeta(x, df1 / 2.0, df2 / 2.0)).coerceIn(0.0, 1.0)
    }

    /** Two-tailed critical t-value for a given df/alpha, found by bisection on the CDF. */
    fun criticalT(df: Double, alpha: Double): Double {
        var lo = 0.0
        var hi = 100.0
        repeat(200) {
            val mid = (lo + hi) / 2.0
            val p = tDistributionTwoTailedPValue(mid, df)
            if (p > alpha) lo = mid else hi = mid
        }
        return (lo + hi) / 2.0
    }

    private fun regularizedIncompleteBeta(x: Double, a: Double, b: Double): Double {
        if (x <= 0.0) return 0.0
        if (x >= 1.0) return 1.0
        val logBeta = logGamma(a + b) - logGamma(a) - logGamma(b) + a * ln(x) + b * ln(1.0 - x)
        val front = exp(logBeta)
        return if (x < (a + 1.0) / (a + b + 2.0)) {
            front * betaContinuedFraction(x, a, b) / a
        } else {
            1.0 - front * betaContinuedFraction(1.0 - x, b, a) / b
        }
    }

    private fun betaContinuedFraction(x: Double, a: Double, b: Double): Double {
        val maxIterations = 200
        val epsilon = 3.0e-9
        val minValue = 1.0e-30

        val qab = a + b
        val qap = a + 1.0
        val qam = a - 1.0
        var c = 1.0
        var d = 1.0 - qab * x / qap
        if (abs(d) < minValue) d = minValue
        d = 1.0 / d
        var h = d

        for (m in 1..maxIterations) {
            val m2 = 2 * m
            var aa = m * (b - m) * x / ((qam + m2) * (a + m2))
            d = 1.0 + aa * d
            if (abs(d) < minValue) d = minValue
            c = 1.0 + aa / c
            if (abs(c) < minValue) c = minValue
            d = 1.0 / d
            h *= d * c

            aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2))
            d = 1.0 + aa * d
            if (abs(d) < minValue) d = minValue
            c = 1.0 + aa / c
            if (abs(c) < minValue) c = minValue
            d = 1.0 / d
            val delta = d * c
            h *= delta

            if (abs(delta - 1.0) < epsilon) break
        }
        return h
    }

    /** Lanczos approximation of the log-gamma function. */
    private fun logGamma(xx: Double): Double {
        val coefficients = doubleArrayOf(
            76.18009172947146, -86.50532032941677, 24.01409824083091,
            -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5
        )
        var x = xx
        var y = xx
        var tmp = x + 5.5
        tmp -= (x + 0.5) * ln(tmp)
        var series = 1.000000000190015
        for (c in coefficients) {
            y += 1.0
            series += c / y
        }
        return -tmp + ln(2.5066282746310005 * series / x)
    }
}
