package com.group4.ticketingservice.service

import com.group4.ticketingservice.entity.Reservation
import com.group4.ticketingservice.repository.EventRepository
import com.group4.ticketingservice.repository.ReservationRepository
import com.group4.ticketingservice.repository.UserRepository
import com.group4.ticketingservice.utils.exception.CustomException
import com.group4.ticketingservice.utils.exception.ErrorCodes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class ReservationService @Autowired constructor(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository
) {
    @Transactional
    fun createReservation(eventId: Int, userId: Int, name: String, phoneNumber: String, postCode: Int, address: String): Reservation {
        val user = userRepository.getReferenceById(userId)
        val event = eventRepository.findByIdWithPesimisticLock(eventId) ?: throw CustomException(ErrorCodes.ENTITY_NOT_FOUND)

        val now = OffsetDateTime.now()
        if (now.isBefore(event.reservationStartTime) || now.isAfter(event.reservationEndTime)) throw CustomException(ErrorCodes.DATE_NOT_ALLOWED)

        val reservation = Reservation(user = user, event = event)
        reservation.apply {
            this.name = name
            this.phoneNumber = phoneNumber
            this.postCode = postCode
            this.address = address
        }
        if (event.maxAttendees > event.totalAttendees) {
            event.totalAttendees += 1
            eventRepository.saveAndFlush(event)

            reservationRepository.saveAndFlush(reservation)
        } else {
            throw CustomException(ErrorCodes.EVENT_ALREADY_RESERVED_ALL)
        }
        return reservation
    }

    fun getReservation(reservationId: Int): Reservation {
        return reservationRepository.findById(reservationId).orElseThrow {
            CustomException(ErrorCodes.ENTITY_NOT_FOUND)
        }
    }

    fun updateReservation(reservationId: Int, eventId: Int): Reservation {
        val reservation: Reservation = reservationRepository.findById(reservationId).orElseThrow {
            CustomException(ErrorCodes.ENTITY_NOT_FOUND)
        }
        val event = eventRepository.findById(eventId).orElseThrow {
            CustomException(ErrorCodes.ENTITY_NOT_FOUND)
        }
        reservation.event = event

        return reservationRepository.save(reservation)
    }

    fun deleteReservation(userId: Int, id: Int) {
        val reservation = reservationRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCodes.ENTITY_NOT_FOUND)
        if (reservation.user.id != userId) throw CustomException(ErrorCodes.FORBIDDEN)
        reservationRepository.delete(reservation)
    }

    fun getReservations(userId: Int, pageable: Pageable): Page<Reservation> {
        return reservationRepository.findByUserId(userId, pageable)
    }
}
