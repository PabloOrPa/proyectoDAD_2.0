/*


--- Esta placa se va a programar como centralita, por tanto:

    -Su función se limita a la recolección de datos
    -Esto implica que no contará con actuadores
    -El código se simplificará lo máximo posible para ello
    -Puesto que no se planea recibir ningún comando MQTT útil, esta funcionalidad se eliminará de esta placa en concreto


*/


#include <WiFi.h>
#include <HTTPClient.h>
#include "ArduinoJson.h"

#include <DHT.h>

// Defino el id de la placa y el id del grupo al que va a pertenecer
const int idPlaca = 124;
const int idGroup = 35;

// Configuración de los Sensores:

#define sLuz 34
#define sTemp 4

DHT dht(sTemp, DHT11);


int test_delay = 500; // Para no spamear la API
boolean describe_tests = true;

// IP del servidor
String serverName = "http://192.168.169.35:8084/";
HTTPClient http;

// Credenciales de la red wifi
#define STASSID "POCO F3 P"    //"Wifi_SSID"
#define STAPSK "WiFiPablo69" //"Wifi_PASSWORD"

// Setup
void setup()
{
  Serial.begin(9600);

  pinMode(sLuz, INPUT);
  dht.begin();

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


String serializeSensor(int idSensor, double valor, String tipo)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Valores del sensor:
  doc["id" + tipo] = idSensor;
  doc["valor"] = valor;
  doc["idPlaca"] = idPlaca;
  doc["idGroup"] = idGroup;
  

  // Genera el Json y lo devuelve
  String output;
  serializeJson(doc, output);

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
  
  String sLuz_value_body = serializeSensor(sLuz, analogRead(sLuz), "SLuz");
  String serverPath = serverName + "api/sLuz/";
  http.begin(serverPath.c_str());
  test_response(http.POST(sLuz_value_body));

  String sTemp_value_body = serializeSensor(sTemp, dht.readTemperature(), "STemp");
  serverPath = serverName + "api/sTemp/";
  http.begin(serverPath.c_str());
  test_response(http.POST(sTemp_value_body));
}



// Run the tests!
void loop()
{
  POST_tests();

  Serial.println();
  Serial.println("##################################################################");
  Serial.println();
}
