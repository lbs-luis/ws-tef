package com.wstef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wstef.api.PaymentData
import com.wstef.api.WebSocketManager
import com.wstef.ui.theme.WstefTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WstefTheme {
                PagamentoRootScreen()
            }
        }
    }


    // Azul Google padrão
    private val blue = Color(0xFF1A73E8)
    // Verde de recebimento (Google Pay)
    private val green = Color(0xFF34A853)
    // Neutros
    private val white = Color.White
    private val black = Color.Black
    private val gray = Color(0xFFF5F5F5)

    @Composable
    fun PagamentoRootScreen() {
        var isConnected by remember { mutableStateOf(false) }
        var paymentData by remember { mutableStateOf(PaymentData()) }
        var wsManager by remember { mutableStateOf<WebSocketManager?>(null) }
        var ipInput by remember { mutableStateOf("") }

        if (!isConnected) {
            CardConectar(
                ipInput = ipInput,
                onIpChange = { ipInput = it },
                onConectar = {
                    if(ipInput.isNotEmpty()){
                        wsManager = WebSocketManager("ws://$ipInput:6109",
                            onPaymentUpdate = { data ->
                                paymentData = data
                            },
                            onDisconnect = {
                                isConnected = false
                                wsManager = null
                            }
                        )
                        isConnected = true
                    }
                }
            )
        } else {
            PagamentoScreen(paymentData)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CardConectar(
        ipInput: String,
        onIpChange: (String) -> Unit,
        onConectar: () -> Unit
    ) {
        val blue = Color(0xFF1A73E8)
        val white = Color.White
        val gray = Color(0xFFF5F5F5)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gray),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(340.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = white)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(28.dp)
                ) {
                    Text(
                        text = "Conectar",
                        color = blue,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    // Campo com prefixo fixo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(32.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ws://",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextField(
                            value = ipInput,
                            onValueChange = onIpChange,
                            singleLine = true,
                            placeholder = { Text("192.168.15.2") },
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ":6109",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onConectar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = blue)
                    ) {
                        Text("Conectar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Composable
    fun PagamentoScreen(paymentData: PaymentData) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gray),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(340.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .background(white, RoundedCornerShape(16.dp))
                    .padding(28.dp)
            ) {
                Text(
                    text = "Pagamento",
                    color = blue,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                CampoArredondado(
                    label = "Valor Total",
                    value = "R$ ${paymentData.valorTotal}",
                    background = gray,
                    textColor = black
                )
                Spacer(modifier = Modifier.height(14.dp))
                CampoArredondado(
                    label = "Descontos",
                    value = "- R$ ${paymentData.descontos}",
                    background = gray,
                    textColor = black
                )
                Spacer(modifier = Modifier.height(14.dp))
                CampoArredondado(
                    label = "Total a Pagar",
                    value = "R$ ${paymentData.totalPagar}",
                    background = green.copy(alpha = 0.15f),
                    textColor = green,
                    bold = true
                )
            }
        }
    }

    @Composable
    fun CampoArredondado(
        label: String,
        value: String,
        background: Color,
        textColor: Color,
        bold: Boolean = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(background, RoundedCornerShape(32.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = textColor,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
            Text(
                text = value,
                color = textColor,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                fontSize = if (bold) 20.sp else 16.sp
            )
        }
    }
}
