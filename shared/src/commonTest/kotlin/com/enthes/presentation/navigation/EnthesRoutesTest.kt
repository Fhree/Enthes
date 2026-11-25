package com.enthes.presentation.navigation

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnthesRoutesTest {

    @Test
    fun dashboardRouteSerializesAndDeserializes() {
        val encoded = Json.encodeToString(DashboardRoute)
        val decoded = Json.decodeFromString<DashboardRoute>(encoded)

        assertEquals(DashboardRoute, decoded)
    }

    @Test
    fun checkinWizardRouteDefaultsToNullActivityId() {
        val route = CheckinWizardRoute()

        assertNull(route.activityId)

        val encoded = Json.encodeToString(route)
        val decoded = Json.decodeFromString<CheckinWizardRoute>(encoded)

        assertEquals(route, decoded)
    }

    @Test
    fun checkinWizardRoutePreservesActivityIdAcrossSerialization() {
        val route = CheckinWizardRoute(activityId = "activity-123")

        val encoded = Json.encodeToString(route)
        val decoded = Json.decodeFromString<CheckinWizardRoute>(encoded)

        assertEquals(route, decoded)
        assertEquals("activity-123", decoded.activityId)
    }
}
