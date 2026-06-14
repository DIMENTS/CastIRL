package com.castIRL.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring

/**
 * Material 3 Expressive motion tokens, hand-rolled for stability.
 *
 * SPATIAL springs drive position / size / rotation / corner-morph — they BOUNCE
 * (low damping, overshoot) for a lively, physical feel.
 *
 * EFFECTS springs drive color / opacity / elevation — they SETTLE (critically
 * damped, no overshoot) so a live stat or color cross-fade never wobbles.
 *
 * Token values mirror MotionScheme.expressive().
 */
object MotionTokens {

    // --- Spatial (bounce) ---
    fun <T> spatialFast(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.6f, stiffness = 800f)

    fun <T> spatialDefault(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.8f, stiffness = 380f)

    fun <T> spatialSlow(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 0.8f, stiffness = 200f)

    // --- Effects (settle) ---
    fun <T> effectsFast(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 3800f)

    fun <T> effectsDefault(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 1600f)

    fun <T> effectsSlow(): FiniteAnimationSpec<T> =
        spring(dampingRatio = 1f, stiffness = 800f)
}
