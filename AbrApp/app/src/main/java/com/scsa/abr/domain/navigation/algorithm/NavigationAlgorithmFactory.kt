package com.scsa.abr.domain.navigation.algorithm

import com.scsa.abr.domain.model.NavigationAlgorithmType

object NavigationAlgorithmFactory {
    fun createAlgorithm(navigationAlgorithmType: NavigationAlgorithmType): NavigationAlgorithm {
        return when (navigationAlgorithmType) {
            NavigationAlgorithmType.GRADIENT_DESCENT -> GradientDescentAlgorithm()
            NavigationAlgorithmType.ITERATIVE_HEADING_CORRECTION -> IterativeHeadingCorrectionAlgorithm()
        }
    }
}