#include <WiFi.h>
#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <PubSubClient.h>

#include <DHT.h>

// Defino el id de la placa y el id del grupo al que va a pertenecer
const int idPlaca = 773;
const int idGroup = 35;

// Configuración de los Actuadores
#define Ventilador 16

int test_delay = 500; // so we don't spam the API
boolean describe_tests = true;

// IP fijada para el portátil al conectarme al punto wifi del movil
String serverName = "http://192.168.169.35:8084/";
HTTPClient http;

// Credenciales de la red wifi
#define STASSID "POCO F3 P"    //"Your_Wifi_SSID"
#define STAPSK "WiFiPablo69" //"Your_Wifi_PASSWORD"



// MQTT
WiFiClient espClient773;
PubSubClient client(espClient773);

const char *MQTT_BROKER_ADDRESS = "192.168.169.35";
const uint16_t MQTT_PORT = 1883;


const char *MQTT_CLIENT_NAME = "ESP32_" + idPlaca;

const char *topico = "Group35";
const char *user_mqtt = "mqtt";
const char *pass_mqtt = "pabloorpa";

// callback a ejecutar cuando se recibe un mensaje MQTT:
void OnMqttReceived(char *topic, byte *payload, unsigned int length){
  Serial.print("Received on ");
  Serial.print(topic);
  Serial.print(": ");

  String content = "";
  for (size_t i = 0; i < length; i++)
  {
    content.concat((char)payload[i]);
  }

  if(content == "VentiladorON"){
    int a = enciendeVentilador();
  
  } else if(content == "VentiladorOFF"){
    int a = apagaVentilador();
  } else {
    Serial.print("Mensaje no Entendido: ");
    Serial.println(content);
  }

}
// Función de inicialización de MQTT
void InitMqtt()
{
  client.setServer(MQTT_BROKER_ADDRESS, MQTT_PORT);
  client.setCallback(OnMqttReceived);
}


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

  // Una vez conectamos al WiFi, Inicializamos MQTT
  InitMqtt();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");

  // Inicializamos el ventilador
  int code = 0;
  while(code != 201){
    pinMode(Ventilador, OUTPUT);
    code = apagaVentilador();
  }
  
  
}

// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos
void ConnectMqtt()
{
  Serial.print("Starting MQTT connection...");
  if (client.connect(MQTT_CLIENT_NAME, user_mqtt, pass_mqtt))
  {
    client.subscribe(topico);
    Serial.print("Conexión MQTT establecida. Suscrito al topic: ");
    Serial.println(topico);
  }
  else
  {
    Serial.print("Failed MQTT connection, rc=");
    Serial.print(client.state());
    Serial.println(" try again in 5 seconds");

    delay(5000);
  }
}
// gestiona la comunicación MQTT
// comprueba que el cliente está conectado
// no -> intenta reconectar
// si -> llama al MQTT loop
void HandleMqtt()
{
  if (!client.connected())
  {
    ConnectMqtt();
  }
  client.loop();
}





String serializeActuatorStatusBody(int idRele, bool estado)
{
  DynamicJsonDocument doc(2048);

  doc["idRele"] = idRele;
  doc["estado"] = estado;
  doc["idPlaca"] = idPlaca;
  doc["idGroup"] = idGroup;
  doc["tipo"] = "Ventilador";
  

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

int enciendeVentilador(){
  String rele_value_body = serializeActuatorStatusBody(Ventilador, true);
    
  String serverPath = serverName + "api/reles/";
  http.begin(serverPath.c_str());
  int respuesta = http.POST(rele_value_body);
  test_response(respuesta);
  if(respuesta == 201){
    digitalWrite(Ventilador, HIGH);
  }
  return respuesta;
}

int apagaVentilador(){
  String rele_value_body = serializeActuatorStatusBody(Ventilador, false);
    
  String serverPath = serverName + "api/reles/";
  http.begin(serverPath.c_str());
  int respuesta = http.POST(rele_value_body);
  test_response(respuesta);
  if(respuesta == 201){
    digitalWrite(Ventilador, LOW);
  }
  return respuesta;
}


// Run the tests!
void loop()
{
  HandleMqtt();

}