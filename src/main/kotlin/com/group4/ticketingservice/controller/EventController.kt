package com.group4.ticketingservice.controller

import com.group4.ticketingservice.dto.EventCreateRequest
import com.group4.ticketingservice.dto.EventResponse
import com.group4.ticketingservice.entity.Event
import com.group4.ticketingservice.service.EventService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventController @Autowired constructor(
    val eventService: EventService
) {

    // TimeE2ETest를 위한 임시 EndPoint입니다.
    @PostMapping
    fun createEvent(
        @RequestBody @Valid
        request: EventCreateRequest
    ): ResponseEntity<EventResponse> {
        val event = eventService.createEvent(
            request.title!!,
            request.date!!,
            request.reservationStartTime!!,
            request.reservationEndTime!!,
            request.maxAttendees!!
        )

        val response = EventResponse(
            id = event.id!!,
            title = event.title,
            date = event.date,
            reservationStartTime = event.reservationStartTime,
            reservationEndTime = event.reservationEndTime,
            maxAttendees = event.maxAttendees
        )

        val headers = HttpHeaders()
        headers.set("Content-Location", "/events/%d".format(event.id!!))

        return ResponseEntity(response, headers, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getEvent(
        request: HttpServletRequest,
        @PathVariable id: Int
    ): ResponseEntity<EventResponse?> {
        val foundEvent = eventService.getEvent(id)?.let {
            EventResponse(
                id = it.id!!,
                title = it.title,
                date = it.date,
                reservationStartTime = it.reservationStartTime,
                reservationEndTime = it.reservationEndTime,
                maxAttendees = it.maxAttendees
            )
        } ?: kotlin.run {
            null
        }

        val headers = HttpHeaders()
        headers.set("Content-Location", request.requestURI)

        return ResponseEntity(foundEvent, headers, HttpStatus.OK)
    }

    @GetMapping
    fun getEvents(
        request: HttpServletRequest,
        @RequestParam(required = false) title: String?,
        @PageableDefault(size = 10, sort = ["date", "id"]) pageable: Pageable
    ): ResponseEntity<Page<Event>> {
        val page = eventService.getEvents(title, pageable)

        val headers = HttpHeaders()
        headers.set("Content-Location", request.requestURI)

        return ResponseEntity(page, headers, HttpStatus.OK)
    }
}
