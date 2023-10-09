package com.group4.ticketingservice.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotNull

@Entity
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    var user: User,

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    var event: Event

) : BaseTimeEntity() {

    var name: String = ""
    var phoneNumber: String = ""
    var postCode: Int = 0
    var address: String = ""
}
