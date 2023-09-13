package com.group4.ticketingservice.event

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.group4.ticketingservice.JwtAuthorizationEntryPoint
import com.group4.ticketingservice.config.SecurityConfig
import com.group4.ticketingservice.controller.EventController
import com.group4.ticketingservice.dto.EventCreateRequest
import com.group4.ticketingservice.dto.EventDeleteRequest
import com.group4.ticketingservice.entity.Event
import com.group4.ticketingservice.entity.User
import com.group4.ticketingservice.service.EventService
import com.group4.ticketingservice.util.DateTimeConverter
import com.group4.ticketingservice.utils.Authority
import com.group4.ticketingservice.utils.TokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@ExtendWith(MockKExtension::class)
@WebMvcTest(
    EventController::class,
    includeFilters = arrayOf(
        ComponentScan.Filter(value = [(SecurityConfig::class), (TokenProvider::class), (JwtAuthorizationEntryPoint::class)], type = FilterType.ASSIGNABLE_TYPE)
    )
)
class EventControllerTest(
    @Autowired val mockMvc: MockMvc
) {
    @MockkBean
    private lateinit var eventService: EventService

    object testFields {
        const val testName = "minjun"
        const val testUserId = 1L
        const val testUserName = "minjun3021@qwer.com"
        const val testUserRole = "USER"
        const val password = "123456789"
    }

    val sampleUser = User(
        name = "james",
        email = "james@example.com",
        password = "12345678",
        authority = Authority.USER
    )
    private val sampleEvent: Event = Event(
        id = 1,
        title = "test title",
        date = OffsetDateTime.now(),
        reservationEndTime = OffsetDateTime.now(),
        reservationStartTime = OffsetDateTime.now(),
        maxAttendees = 10

    )
    private val sampleEventCreateRequest: EventCreateRequest = EventCreateRequest(
        title = "test title",
        date = OffsetDateTime.now(),
        reservationEndTime = OffsetDateTime.now(),
        reservationStartTime = OffsetDateTime.now(),
        maxAttendees = 10
    )
    private val sampleEventDeleteRequest: EventDeleteRequest = EventDeleteRequest(
        id = 1
    )
    private val gson: Gson = GsonBuilder().registerTypeAdapter(OffsetDateTime::class.java, DateTimeConverter()).create()

    @Test
    fun `GET events should return event`() {
        every { eventService.getEvent(any()) } returns sampleEvent
        mockMvc.perform(
            get("/events/${sampleEvent.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(sampleEvent.id))
    }

    @Test
    fun `GET events should return not found`() {
        every { eventService.getEvent(any()) } returns null
        mockMvc.perform(
            get("/events/${sampleEvent.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `GET List of events should return list of events`() {
        every { eventService.getEvents() } returns listOf(sampleEvent)
        mockMvc.perform(
            get("/events/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(sampleEvent.id))
    }

    @Test
    fun `GET List of events should return empty list`() {
        every { eventService.getEvents() } returns listOf()
        mockMvc.perform(
            get("/events/")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNoContent)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isEmpty)
    }
}
