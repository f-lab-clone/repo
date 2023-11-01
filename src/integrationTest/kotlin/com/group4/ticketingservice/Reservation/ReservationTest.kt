package com.group4.ticketingservice.Reservation

import com.group4.ticketingservice.AbstractIntegrationTest
import com.group4.ticketingservice.dto.QueueResponseDTO
import com.group4.ticketingservice.entity.Event
import com.group4.ticketingservice.entity.User
import com.group4.ticketingservice.repository.EventRepository
import com.group4.ticketingservice.repository.ReservationRepository
import com.group4.ticketingservice.repository.UserRepository
import com.group4.ticketingservice.service.ReservationService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate
import java.time.Duration.ofHours
import java.time.OffsetDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=create"])
class ReservationTest @Autowired constructor(
    val reservationService: ReservationService,
    val userRepository: UserRepository,
    val reservationRepository: ReservationRepository,
    val eventRepository: EventRepository
) : AbstractIntegrationTest() {

    object testFields {
        const val testName = "minjun"
        const val testUserName = "minjun3021@qwer.com"
        const val password = "1234"
    }
    val sampleUser = User(
        name = testFields.testName,
        email = testFields.testUserName,
        password = BCryptPasswordEncoder().encode(testFields.password),

        phone = "010-1234-5678"
    )

    private val sampleEvent = Event(
        name = "test title",
        startDate = OffsetDateTime.now(),
        endDate = OffsetDateTime.now(),
        reservationEndTime = OffsetDateTime.now() + ofHours(2),
        reservationStartTime = OffsetDateTime.now() - ofHours(2),
        maxAttendees = 100
    )

    private val queueSuccess: QueueResponseDTO = QueueResponseDTO(
        status = true,
        message = "",
        data = null
    )

    @BeforeEach fun addUserAndEvent() {
        userRepository.save(sampleUser)
        eventRepository.save(sampleEvent)

        val event = eventRepository.findById(1).get()
        event.totalAttendees = 0

        eventRepository.save(event)
    }

    @AfterEach fun clear() {
        reservationRepository.deleteAllInBatch()
    }

    @RepeatedTest(3)
    @Test
    fun `ReservationService_createReservation should not exceed the limit in the concurrency test`() {
        val restTemplate: RestTemplate = mockk()
        every { restTemplate.exchange(any() as String, HttpMethod.DELETE, null, QueueResponseDTO::class.java) } returns ResponseEntity.ok(queueSuccess)
        reservationService.restTemplate = restTemplate
        val threadCount = 1000
        val executorService = Executors.newFixedThreadPool(32)
        val countDownLatch = CountDownLatch(threadCount)

        for (i in 0 until threadCount) {
            executorService.submit {
                try {
                    reservationService.createReservation(1, 1, "", "", 1, "")
                } finally {
                    countDownLatch.countDown()
                }
            }
        }

        countDownLatch.await()

        val count: Long = reservationRepository.count()

        assertThat(count).isEqualTo(100)
    }
}
