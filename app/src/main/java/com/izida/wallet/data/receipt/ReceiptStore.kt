package com.izida.wallet.data.receipt

import com.izida.wallet.domain.receipt.PaymentReceipt
import java.util.concurrent.CopyOnWriteArrayList

class ReceiptStore {
    private val receipts = CopyOnWriteArrayList<PaymentReceipt>()

    fun save(receipt: PaymentReceipt) {
        receipts.removeIf { it.transactionId == receipt.transactionId }
        receipts.add(0, receipt)
    }

    fun all(): List<PaymentReceipt> = receipts.toList()
}
