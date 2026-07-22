package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.enums.UserPlan
import com.example.data.enums.UserRole
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun OnboardingScreen(
    onComplete: (role: UserRole, plan: UserPlan) -> Unit
) {
    var step1Choice by remember { mutableStateOf<String?>(null) } // "OWNER" or "EMPLOYEE"
    var step2Choice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "POS Híbrido Offline",
            color = NeonGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configuración Inicial de Estructura de Negocio",
            color = MutedGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Question 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paso 1: ¿Cómo usarás la aplicación?",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OptionTile(
                    title = "Negocio Propio (Dueño)",
                    subtitle = "Administras tus propios productos o tienda.",
                    selected = step1Choice == "OWNER",
                    onClick = {
                        step1Choice = "OWNER"
                        step2Choice = null
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OptionTile(
                    title = "Empleado / Trabajador",
                    subtitle = "Usas la app en un punto de venta o barbería.",
                    selected = step1Choice == "EMPLOYEE",
                    onClick = {
                        step1Choice = "EMPLOYEE"
                        step2Choice = null
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question 2
        AnimatedVisibility(visible = step1Choice != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Paso 2: Define la modalidad de tu trabajo",
                        color = PureWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (step1Choice == "OWNER") {
                        OptionTile(
                            title = "A1: Trabajo solo (Particular)",
                            subtitle = "Perfil: SOLO_OWNER - Control total sin empleados.",
                            selected = step2Choice == "SOLO_OWNER",
                            onClick = { step2Choice = "SOLO_OWNER" }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OptionTile(
                            title = "A2: Tengo empleados a mi cargo (Empresa)",
                            subtitle = "Perfil: MASTER_OWNER - Panel gerencial maestro.",
                            selected = step2Choice == "MASTER_OWNER",
                            onClick = { step2Choice = "MASTER_OWNER" }
                        )
                    } else if (step1Choice == "EMPLOYEE") {
                        OptionTile(
                            title = "B1: Control personal de mis ventas (Particular)",
                            subtitle = "Perfil: INDEPENDENT_WORKER - Renta de espacio/comisión.",
                            selected = step2Choice == "INDEPENDENT_WORKER",
                            onClick = { step2Choice = "INDEPENDENT_WORKER" }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OptionTile(
                            title = "B2: Trabajo vinculado a un jefe (Empresa - Gratis)",
                            subtitle = "Perfil: LINKED_WORKER - Conexión por QR con tu jefe.",
                            selected = step2Choice == "LINKED_WORKER",
                            onClick = { step2Choice = "LINKED_WORKER" }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = step2Choice != null) {
            Button(
                onClick = {
                    when (step2Choice) {
                        "SOLO_OWNER" -> onComplete(UserRole.SOLO_OWNER, UserPlan.PARTICULAR)
                        "MASTER_OWNER" -> onComplete(UserRole.MASTER_OWNER, UserPlan.EMPRESA)
                        "INDEPENDENT_WORKER" -> onComplete(UserRole.INDEPENDENT_WORKER, UserPlan.PARTICULAR)
                        "LINKED_WORKER" -> onComplete(UserRole.LINKED_WORKER, UserPlan.FREE_LINKED)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text(
                    text = "Continuar a Activación",
                    color = PureBlack,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OptionTile(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) NeonGreen else DarkSurfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(text = title, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = MutedGray, fontSize = 12.sp)
        }
    }
}
