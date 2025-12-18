package com.scsa.abr.domain.model

enum class NavigationState {
    IDLE,
    GATHERING,
    CALCULATING,
    TURNING,
    MOVING,
}

enum class NavigationAlgorithmType {
    GRADIENT_DESCENT,
    ITERATIVE_HEADING_CORRECTION
}

data class NavigationMove(
    val direction: NavigationDirection,
    val amount: Int
) {
    override fun toString(): String {
        return "$direction:$amount"
    }
}

enum class NavigationDirection {
    FORWARD,
    LEFT,
    RIGHT,
    BACKWARD
}