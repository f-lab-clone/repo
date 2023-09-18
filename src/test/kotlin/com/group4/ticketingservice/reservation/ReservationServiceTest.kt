package com.group4.ticketingservice.reservation

import com.group4.ticketingservice.entity.Event
import com.group4.ticketingservice.entity.Reservation
import com.group4.ticketingservice.entity.User
import com.group4.ticketingservice.repository.EventRepository
import com.group4.ticketingservice.repository.ReservationRepository
import com.group4.ticketingservice.repository.UserRepository
import com.group4.ticketingservice.service.ReservationService
import com.group4.ticketingservice.utils.Authority
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.*

class ReservationServiceTest() {
    private val userRepository: UserRepository = mockk()
    private val eventRepository: EventRepository = mockk()
    private val reservationRepository: ReservationRepository = mockk()
    private val reservationService: ReservationService = ReservationService(
        userRepository = userRepository,
        eventRepository = eventRepository,
        reservationRepository = reservationRepository
    )

    val sampleUserId = 1

    val sampleUser = User(
        name = "minjun3021@qwer.com",
        email = "minjun",
        password = "1234",
        authority = Authority.USER,
        id = sampleUserId
    )

    private val sampleEvent: Event = Event(
        id = 1,
        title = "test title",
        date = OffsetDateTime.now(),
        reservationEndTime = OffsetDateTime.now(),
        reservationStartTime = OffsetDateTime.now(),
        maxAttendees = 10
    )

    private val sampleReservation: Reservation = Reservation(
        user = sampleUser,
        event = sampleEvent,
        bookedAt = OffsetDateTime.now()
    )

    @Test
    fun `ReservationService_createReservation invoke ReservationRepository_save`() {
        every { userRepository.getReferenceById(any()) } returns sampleUser
        every { eventRepository.findByIdWithPesimisticLock(any()) } returns sampleEvent
        every { eventRepository.findByIdWithOptimisicLock(any()) } returns sampleEvent

        every { eventRepository.saveAndFlush(any()) } returns sampleEvent
        every { reservationRepository.saveAndFlush(any()) } returns sampleReservation
        reservationService.createReservation(1, 1)
        verify(exactly = 1) { reservationRepository.saveAndFlush(any()) }
    }

    @Test
    fun `ReservationService_getReservation invoke ReservationRepository_findById`() {
        every { reservationRepository.findById(any()) } returns Optional.of(sampleReservation)
        reservationService.getReservation(1)
        verify(exactly = 1) { reservationRepository.findById(any()) }
    }

    @Test
    fun `ReservationService_updateReservation invoke ReservationRepository_save`() {
        every { reservationRepository.findById(any()) } returns Optional.of(sampleReservation)
        every { eventRepository.findById(any()) } returns Optional.of(sampleEvent)
        every { reservationRepository.save(any()) } returns sampleReservation
        reservationService.updateReservation(1, 1)
        verify(exactly = 1) { reservationRepository.save(any()) }
    }

    @Test
    fun `ReservationService_deleteReservation invoke ReservationRepository_deleteById`() {
        every { reservationRepository.findById(any()) } returns Optional.of(sampleReservation)
        every { reservationRepository.delete(any()) } returns Unit
        reservationService.deleteReservation(sampleUserId, 1)
        verify(exactly = 1) { reservationRepository.delete(any()) }
    }

    @Test
    fun `ReservationService_deleteReservation throw IllegalArgumentException`() {
        every { reservationRepository.findById(any()) } returns Optional.ofNullable(null)
        every { reservationRepository.delete(any()) } returns Unit

        val exception = assertThrows<IllegalArgumentException> { reservationService.deleteReservation(sampleUserId, 1) }
        assert(exception.message == "Reservation not found")
        verify(exactly = 0) { reservationRepository.delete(any()) }
    }

    @Test
    fun `ReservationService_deleteReservation throw IllegalArgumentException when deleting other's reservation`() {
        every { reservationRepository.findById(any()) } returns Optional.of(sampleReservation)
        every { reservationRepository.delete(any()) } returns Unit

        val exception = assertThrows<IllegalArgumentException> { reservationService.deleteReservation(2, 1) }
        assert(exception.message == "It's not your reservation")
    }
}
