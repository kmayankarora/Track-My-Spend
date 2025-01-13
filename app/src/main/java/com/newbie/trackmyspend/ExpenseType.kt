package com.newbie.trackmyspend

enum class ExpenseType(val type: Int) {
    SPEND(0),
    TRANSFER(1),
    EARNED(2)
}

enum class TransferType(val type: Int) {
    FROM(0),
    TO(1)
}