package com.enthes.presentation.checkin

import com.enthes.domain.model.PainZone
import enthes.shared.generated.resources.Res
import enthes.shared.generated.resources.pain_zone_cervical
import enthes.shared.generated.resources.pain_zone_feet_heels
import enthes.shared.generated.resources.pain_zone_hands
import enthes.shared.generated.resources.pain_zone_knees
import enthes.shared.generated.resources.pain_zone_lumbar
import enthes.shared.generated.resources.pain_zone_shoulders_elbows
import org.jetbrains.compose.resources.StringResource

fun PainZone.labelRes(): StringResource = when (this) {
    PainZone.LUMBAR -> Res.string.pain_zone_lumbar
    PainZone.CERVICAL -> Res.string.pain_zone_cervical
    PainZone.KNEES -> Res.string.pain_zone_knees
    PainZone.HANDS -> Res.string.pain_zone_hands
    PainZone.FEET_HEELS -> Res.string.pain_zone_feet_heels
    PainZone.SHOULDERS_ELBOWS -> Res.string.pain_zone_shoulders_elbows
}
