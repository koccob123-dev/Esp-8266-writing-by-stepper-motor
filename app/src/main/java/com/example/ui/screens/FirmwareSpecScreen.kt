package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StepperGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FirmwareSpecScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // API Spec Header Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("firmware_spec_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ESP8266 REST API SPECIFICATION",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                }

                Text(
                    text = "The ESP8266 acts as an Access Point (SSID: WriteBot_AP, default IP: 192.168.4.1). It runs an HTTP server responding on port 80 to the following endpoints:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Endpoints Breakdown
        EndpointCard(
            method = "GET",
            path = "/api/status",
            description = "Returns current X/Y coordinate, pen state, and machine status.",
            examplePayload = null,
            exampleResponse = """{"status":"idle","x":0.0,"y":0.0,"pen":"up","speed":50}"""
        )

        EndpointCard(
            method = "POST",
            path = "/api/home",
            description = "Homes both X and Y axes to (0,0) and lifts pen UP.",
            examplePayload = "{}",
            exampleResponse = """{"status":"ok","message":"homed"}"""
        )

        EndpointCard(
            method = "POST",
            path = "/api/stop",
            description = "Emergency Stop: instantly disables steppers and lifts servo pen.",
            examplePayload = "{}",
            exampleResponse = """{"status":"ok","message":"stopped"}"""
        )

        EndpointCard(
            method = "POST",
            path = "/api/pen",
            description = "Lifts or lowers the MG90S servo pen mechanism.",
            examplePayload = """{"state": "down"}  // or "up"""",
            exampleResponse = """{"status":"ok","pen":"down"}"""
        )

        EndpointCard(
            method = "POST",
            path = "/api/move",
            description = "Moves toolhead by relative delta X and Y in millimeters.",
            examplePayload = """{"x": 10.0, "y": 5.0, "speed": 50}""",
            exampleResponse = """{"status":"ok","x":10.0,"y":5.0}"""
        )

        EndpointCard(
            method = "POST",
            path = "/api/draw",
            description = "Processes a batch of machine toolpath commands.",
            examplePayload = """{
  "commands": [
    {"x":0, "y":0, "pen":0},
    {"x":10, "y":0, "pen":1},
    {"x":10, "y":10, "pen":1},
    {"x":0, "y":10, "pen":1}
  ]
}""",
            exampleResponse = """{"status":"ok","count":4}"""
        )

        // Arduino Firmware Code Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = StepperGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ARDUINO C++ FIRMWARE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = StepperGreen
                        )
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("WriteBot Firmware", ESP8266_ARDUINO_CODE)
                            clipboard.setPrimaryClip(clip)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("copy_firmware_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COPY", color = NeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF070A0E))
                        .border(1.dp, DarkOutline, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = ESP8266_ARDUINO_CODE.take(800) + "\n\n// ... (Tap COPY for full ready-to-flash Arduino sketch)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFA5D6A7),
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EndpointCard(
    method: String,
    path: String,
    description: String,
    examplePayload: String?,
    exampleResponse: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkOutline))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (method == "GET") Color(0xFF0277BD) else Color(0xFF2E7D32))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = method,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = path,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = description,
                fontSize = 12.sp,
                color = TextSecondary
            )

            if (examplePayload != null) {
                Text(
                    text = "Request Body:",
                    fontSize = 10.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF090D12))
                        .padding(8.dp)
                ) {
                    Text(
                        text = examplePayload,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFFD54F)
                    )
                }
            }

            Text(
                text = "Response:",
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF090D12))
                    .padding(8.dp)
            ) {
                Text(
                    text = exampleResponse,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF81C784)
                )
            }
        }
    }
}

private const val ESP8266_ARDUINO_CODE = """/*
 * WriteBot Controller - ESP8266 NodeMCU Firmware
 * Controls XY Plotter with 2x 28BYJ-48 Steppers + ULN2003 & 1x MG90S Servo
 */
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <ArduinoJson.h>
#include <Servo.h>

// Wi-Fi Access Point Credentials
const char* apSSID = "WriteBot_AP";
const char* apPassword = ""; // Open hotspot or set password

ESP8266WebServer server(80);
Servo penServo;

// Pin Definitions
const int SERVO_PIN = D4; // GPIO2
// Stepper X (ULN2003)
const int X_IN1 = D1; const int X_IN2 = D2; const int X_IN3 = D5; const int X_IN4 = D6;
// Stepper Y (ULN2003)
const int Y_IN1 = D7; const int Y_IN2 = D8; const int Y_IN3 = D0; const int Y_IN4 = D3;

// Machine Position State
float currentX = 0.0;
float currentY = 0.0;
bool isPenDown = false;
int penUpAngle = 45;
int penDownAngle = 90;
const float STEPS_PER_MM = 64.0;

void setPen(bool down) {
  isPenDown = down;
  penServo.write(down ? penDownAngle : penUpAngle);
  delay(150);
}

void setup() {
  Serial.begin(115200);
  penServo.attach(SERVO_PIN);
  setPen(false);

  // Configure AP
  WiFi.mode(WIFI_AP);
  WiFi.softAPConfig(IPAddress(192, 168, 4, 1), IPAddress(192, 168, 4, 1), IPAddress(255, 255, 255, 0));
  WiFi.softAP(apSSID, apPassword);
  Serial.println("WriteBot AP Ready: 192.168.4.1");

  // REST Endpoints
  server.on("/api/status", HTTP_GET, []() {
    StaticJsonDocument<200> doc;
    doc["status"] = "idle";
    doc["x"] = currentX;
    doc["y"] = currentY;
    doc["pen"] = isPenDown ? "down" : "up";
    doc["speed"] = 50;
    String res;
    serializeJson(doc, res);
    server.send(200, "application/json", res);
  });

  server.on("/api/home", HTTP_POST, []() {
    setPen(false);
    currentX = 0.0; currentY = 0.0;
    server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"homed\"}");
  });

  server.on("/api/stop", HTTP_POST, []() {
    setPen(false);
    server.send(200, "application/json", "{\"status\":\"ok\",\"message\":\"stopped\"}");
  });

  server.on("/api/pen", HTTP_POST, []() {
    if (!server.hasArg("plain")) { server.send(400); return; }
    StaticJsonDocument<100> doc;
    deserializeJson(doc, server.arg("plain"));
    const char* state = doc["state"];
    setPen(strcmp(state, "down") == 0);
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  });

  server.on("/api/move", HTTP_POST, []() {
    if (!server.hasArg("plain")) { server.send(400); return; }
    StaticJsonDocument<200> doc;
    deserializeJson(doc, server.arg("plain"));
    float dx = doc["x"];
    float dy = doc["y"];
    currentX += dx;
    currentY += dy;
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  });

  server.on("/api/draw", HTTP_POST, []() {
    if (!server.hasArg("plain")) { server.send(400); return; }
    DynamicJsonDocument doc(4096);
    deserializeJson(doc, server.arg("plain"));
    JsonArray commands = doc["commands"];
    for (JsonObject cmd : commands) {
      float tx = cmd["x"];
      float ty = cmd["y"];
      int pen = cmd["pen"];
      setPen(pen == 1);
      currentX = tx;
      currentY = ty;
    }
    server.send(200, "application/json", "{\"status\":\"ok\"}");
  });

  server.begin();
}

void loop() {
  server.handleClient();
}
"""
