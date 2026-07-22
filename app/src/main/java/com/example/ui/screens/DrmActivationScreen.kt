package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.crypto.DrmEngine
import com.example.data.enums.UserRole
import com.example.ui.components.QrCodeDisplay
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MutedGray
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite

@Composable
fun DrmActivationScreen(
    role: UserRole,
    plan: String,
    androidId: String,
    onActivateWithPin: (pin: String) -> Unit,
    onBindBossUuid: (bossUuid: String) -> Unit
) {
    val challengeText = remember(androidId, plan) {
        DrmEngine.generateChallengeText(androidId, plan)
    }

    var pinInput by remember { mutableStateOf("") }
    var bossUuidInput by remember { mutableStateOf("") }

    // Secret Keygen Trigger
    var secretTapCount by remember { mutableIntStateOf(0) }
    var showDevModal by remember { mutableStateOf(false) }
    var devPasswordInput by remember { mutableStateOf("") }
    var devPasswordError by remember { mutableStateOf(false) }
    var generatedDevPin by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Activación de Licencia",
                color = PureWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Plan: $plan - Perfil: ${role.displayName}",
                color = NeonGreen,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (role == UserRole.LINKED_WORKER) {
                // Free Linked Worker Screen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Vinculación Gratuita de Empleado",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Escanea o ingresa el UUID del código QR generado por tu jefe (MASTER_OWNER).",
                            color = MutedGray,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = bossUuidInput,
                            onValueChange = { bossUuidInput = it },
                            label = { Text("UUID del Jefe", color = MutedGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = NeonGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (bossUuidInput.isNotBlank()) {
                                    onBindBossUuid(bossUuidInput.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Vincular y Comenzar", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Paid Roles DRM Screen
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Código QR de Desafío DRM",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        QrCodeDisplay(content = challengeText, size = 180.dp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = challengeText,
                            color = MutedGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = { if (it.length <= 6) pinInput = it },
                            label = { Text("Introduce PIN de 6 dígitos", color = MutedGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = NeonGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onActivateWithPin(pinInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Activar Sistema", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Hidden Developer Keygen Trigger Area (3 rapid taps at bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                .clickable {
                    secretTapCount++
                    if (secretTapCount >= 3) {
                        showDevModal = true
                        secretTapCount = 0
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Zona de Soporte y Activación Offline",
                color = MutedGray,
                fontSize = 12.sp
            )
        }
    }

    // Secret Keygen Developer Modal
    if (showDevModal) {
        Dialog(onDismissRequest = { showDevModal = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Keygen Oculto Desarrollador", color = NeonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (generatedDevPin == null) {
                        Text("Introduce la contraseña maestra para habilitar el generador local de PIN.", color = MutedGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = devPasswordInput,
                            onValueChange = {
                                devPasswordInput = it
                                devPasswordError = false
                            },
                            label = { Text("Contraseña Maestra", color = MutedGray) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = NeonGreen
                            )
                        )

                        if (devPasswordError) {
                            Text("Contraseña maestra incorrecta", color = com.example.ui.theme.CrimsonRed, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row {
                            Button(
                                onClick = { showDevModal = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancelar", color = PureWhite) }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (devPasswordInput == DrmEngine.DEV_PASSWORD) {
                                        generatedDevPin = DrmEngine.calculateActivationPin(challengeText)
                                    } else {
                                        devPasswordError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.weight(1f)
                            ) { Text("Validar", color = PureBlack, fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        Text("PIN Generado Exitosamente:", color = PureWhite, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = generatedDevPin!!,
                                color = NeonGreen,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                pinInput = generatedDevPin!!
                                onActivateWithPin(generatedDevPin!!)
                                showDevModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Auto-aplicar PIN y Desbloquear", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
