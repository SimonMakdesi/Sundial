package com.makdesi.sundial.theme

import android.content.Context
import android.provider.Settings

/** Motion timings from the design contract (§2.3 of the plan, CSS of the demo). */
object Motion {
    const val PALETTE_MS = 1100      // bg / ink crossfade
    const val HORIZON_MS = 1400      // horizon band / wash crossfade
    const val LAYER_MS = 450         // home <-> settings fade + scale
    const val BREATH_MS = 3600       // breath circle loop
    const val RITUAL_CHOICES_DELAY_MS = 3400L
    const val NEAR_INSTANT_MS = 80   // everything, under reduced motion
}

/** True when the system asks for reduced motion (animator scale off). */
fun reducedMotion(context: Context): Boolean =
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) == 0f
