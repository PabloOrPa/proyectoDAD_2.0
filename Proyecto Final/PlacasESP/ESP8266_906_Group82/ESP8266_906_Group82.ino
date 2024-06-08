/*


--- Esta placa se va a programar como Centralita y Actuador Ventilador, por tanto:

    -Su función es mixta: Recopila datos y los postea + Recibe mensajes MQTT y actúa conforme a ello
    -Esto implica que contará con sensor + actuador
    -El código se simplificará lo máximo posible para ello
    -Se planea recibir comandos MQTT, por lo que se implementará esta funcionalidad



--- NOTA:

    -Debido a la falta de materiales, para esta placa usaré un Potenciómetro a modo de DHT11, por lo que el
     instanciado y tratamiento del "sensor" será diferente a la del real (para ver dicho sensor en acción, 
     consultar el código del ESP32_124_Centralita)

     -Puesto que sólo vamos a usar un actuador y un sensor, todas las adaptaciones para asignar el id a cada 
     elemento que hay que hacer en los ESP8266, se va a omitir y simplificar. Para un ejemplo de ello, consultar
     los 2 arrays del ESP8266_57_Bombilla


*/


#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <PubSubClient.h>

int test_delay = 1000; //so we don't spam the API
boolean describe_tests = true;

#define STASSID "POCO F3 P"    //"Your_Wifi_SSID"
#define STAPSK "WiFiPablo69" //"Your_Wifi_PASSWORD"

// Informacion de placa y actuador:
const int idPlaca = 906;
const int idGroup = 82;

// Declaramos sensores:
#define sTemp A0
// Declaramos actuadores:
#define releVent D0


// Configuración para los servidores:
HTTPClient http;
WiFiClient espClient906;       //Cliente para peticiones a la api
WiFiClient mqtt_espClient906;  // Cliente para MQTT
String apiReles = "http://192.168.169.35:8084/api/reles";
String apiSTemp = "http://192.168.169.35:8084/api/sTemp/";
PubSubClient client(mqtt_espClient906);

const char *MQTT_BROKER_ADDRESS = "192.168.169.35";
const uint16_t MQTT_PORT = 1883;

const char *MQTT_CLIENT_NAME = "ESP8266_" + idPlaca;

const char *topico = "Group82";
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
  Serial.println(content);

  if (content == "VentiladorON") {
    
    int a = enciendeVentilador();

  } else if (content == "VentiladorOFF") {
    
    int a = apagaVentilador();
    
  }  else {
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

//Setup
void setup()
{
  Serial.begin(9660);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");

  http.setTimeout(5000);

  InitMqtt();
  // Inicialización de los sensores y actuadores:

  
  int code = 0;
  // Mientras la subida no sea satisfactoria, se queda intentando hacer el post 
  // para evitar conflictos con la lógica de negocio
  pinMode(releVent, OUTPUT);
  while(code != 201){
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

int enciendeVentilador(){
  String rele_value_body = serializeActuatorStatusBody(0, true);
  http.begin(espClient906, apiReles);
  http.addHeader("Content-Type", "application/json");
  int httpCode = http.POST(rele_value_body);
  String payload = http.getString();
  http.end();

  if (httpCode == 201) {
    Serial.printf("HTTP POST code: %d\n", httpCode);
    Serial.println("Response payload: " + payload);
    digitalWrite(releVent, HIGH);
  } else {
    Serial.printf("HTTP POST failed, error: %s\n", http.errorToString(httpCode).c_str());
  }
  return httpCode;
}

int apagaVentilador(){
  String rele_value_body = serializeActuatorStatusBody(0, false);
    http.begin(espClient906, apiReles);
    http.addHeader("Content-Type", "application/json");
    int httpCode = http.POST(rele_value_body);
    String payload = http.getString();
    http.end();

    if (httpCode == 201) {
      Serial.printf("HTTP POST code: %d\n", httpCode);
      Serial.println("Response payload: " + payload);
      digitalWrite(releVent, LOW);
    } else {
      Serial.printf("HTTP POST failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
    return httpCode;
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

void POST(){
  String sTemp_value_body = serializeSensor(0, analogRead(sTemp)/21.0, "STemp");// La lectura del "sensor de temperatura" es un valor entre 0 y 1024 dividido por 21 para tener un rango de entre 0 a 48 grados
  http.begin(espClient906, apiSTemp);
  test_response(http.POST(sTemp_value_body));
}

// Run the tests!
void loop()
{
  POST();
  HandleMqtt();
}
