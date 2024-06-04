#include <WiFi.h>
#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <PubSubClient.h>

// Defino el id de la placa y el id del grupo al que va a pertenecer
const int idPlaca = 124;
const int idGroup = 35;

int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;

// IP fijada para el portátil al conectarme al punto wifi del movil
String serverName = "http://192.168.169.35:8084/";
HTTPClient http;

// Credenciales de la red wifi
#define STASSID "POCO F3 P"    //"Your_Wifi_SSID"
#define STAPSK "WiFiPablo69" //"Your_Wifi_PASSWORD"




// Setup
void setup()
{
  Serial.begin(9600);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  /* Explicitly set the ESP32 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }


  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
}

String serializeActuatorStatusBody(int idRele, bool estado)
{
  DynamicJsonDocument doc(2048);

  doc["idRele"] = idRele;
  doc["estado"] = estado;
  doc["idPlaca"] = idPlaca;
  doc["idGroup"] = idGroup;
  

  String output;
  serializeJson(doc, output);
  return output;
}
String serializeSensorValueBody(int idSLuz, double valor)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Add values in the document
  //
  doc["idSLuz"] = idSLuz;
  doc["valor"] = valor;
  doc["idPlaca"] = idPlaca;
  doc["idGroup"] = idGroup;
  

  // Generate the minified JSON and send it to the Serial port.
  //
  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}
void test_response(int httpResponseCode)
{
  delay(test_delay);
  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String payload = http.getString();
    Serial.println(payload);
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}
void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}
void POST_tests()
{
  String actuator_states_body = serializeActuatorStatusBody(random(2,7), random(0,1));

  describe("Test POST with actuator state");
  String serverPath = serverName + "api/reles/";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

  String sensor_value_body = serializeSensorValueBody(random(8,14), random(350, 975));
  describe("Test POST with sensor value");
  serverPath = serverName + "api/sLuz/";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));

}

// Run the tests!
void loop()
{
  POST_tests();

}
