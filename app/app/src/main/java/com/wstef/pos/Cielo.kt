package com.wstef.cielo

import android.content.Context
import android.util.Log
import cielo.orders.domain.CheckoutRequest
import cielo.orders.domain.Credentials
import cielo.orders.domain.Order
import cielo.sdk.order.OrderManager
import cielo.sdk.order.ServiceBindListener
import cielo.sdk.order.payment.PaymentCode
import cielo.sdk.order.payment.PaymentError
import cielo.sdk.order.payment.PaymentListener
import org.json.JSONObject


data class PaymentResult(
    val transacaoId: String?,
    val nsu: String?,
    val autorizacao: String?,
    val valor: Long
)

interface PaymentResultListener {
    fun onPaymentComplete(result: PaymentResult)
    fun onPaymentError(message: String)
    fun onPaymentCancelled()
}

class Cielo(
    private val context: Context,
    private val clientId: String,
    private val accessToken: String,
    private val listener: PaymentResultListener
) {
    private var orderManager: OrderManager? = null

    private val paymentListener = object : PaymentListener {
        override fun onStart() {
            Log.d("CieloPayment", "Pagamento iniciado")
        }

        override fun onPayment(paidOrder: Order) {
            val payment = paidOrder.payments.firstOrNull()

            if (payment != null && paidOrder.pendingAmount == 0L) {

                paidOrder.markAsPaid()
                orderManager?.updateOrder(paidOrder)

                val result = PaymentResult(
                    transacaoId = payment.paymentFields["paymentTransactionId"],
                    nsu = payment.cieloCode,
                    autorizacao = payment.authCode,
                    valor = payment.amount ?: 0L
                )

                listener.onPaymentComplete(result)
            } else if (paidOrder.pendingAmount > 0L) {
                listener.onPaymentError("Pagamento parcial ou pendente.")
            } else {
                listener.onPaymentError("Pagamento aprovado, mas dados da transação ausentes.")
            }
            orderManager?.unbind()
        }

        override fun onCancel() {
            listener.onPaymentCancelled()
            orderManager?.unbind()
        }

        override fun onError(error: PaymentError) {
            listener.onPaymentError("Erro: ${error.description}")
            orderManager?.unbind()
        }
    }

    fun startPayment(
        valorEmCentavos: Long,
        paymentCode: PaymentCode
    ) {
        if (orderManager == null)
            orderManager = OrderManager(Credentials(clientId, accessToken), context)

        orderManager!!.bind(context, object : ServiceBindListener {
            override fun onServiceBound() {
                val order = orderManager!!.createDraftOrder("PEDIDO")
                order!!.addItem("SKU1", "Pagamento", valorEmCentavos, 1, "UN")
                orderManager!!.placeOrder(order)

                val request = CheckoutRequest.Builder()
                    .orderId(order.id)
                    .amount(valorEmCentavos)
                    .paymentCode(paymentCode)
                    .build()

                orderManager!!.checkoutOrder(request, paymentListener)
            }

            override fun onServiceBoundError(throwable: Throwable) {
                listener.onPaymentError("Erro ao conectar: ${throwable.message}")
            }

            override fun onServiceUnbound() {}
        })
    }
}
