/*


--- Esta placa se va a programar como actuador Bombilla, por tanto:

    -Su función se limita a encender y apagar la bombilla
    -Esto implica que no contará con sensores
    -El código se simplificará lo máximo posible para ello
    -Se planea recibir comandos MQTT, por lo que se implementará esta funcionalidad



--- NOTA:

    -Dada la naturaleza del ESP8266, si quisieramos poner más de un actuador del mismo tipo (2 bombillas por ejemplo) tendríamos que hacer lo siguiente:
    - 1º -- Declarar un array con los nombres de los puertos (D5, D17, etc);
    - 2º -- Declarar un SEGUNDO ARRAY con los números de los puertos (5, 17, etc);
    - Esto se haría para poder pasar el número correspondiente como idRele y a su vez poder activar y desactivar correctamente el rele

    - Para encender todos los actuadores de un mismo tipo, habría que recorrer el array y por cada elemento, ejecutar el código dentro de "enciendeLuz()" o "apagaLuz()"


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
const int idPlaca = 57;
const int idGroup = 35;
#define rele D5

// Configuración para los servidores:
HTTPClient http;
WiFiClient espClient57;       //Cliente para peticiones a la api
WiFiClient mqtt_espClient57;  // Cliente para MQTT
String api = "http://192.168.169.35:8084/api/reles";
PubSubClient client(mqtt_espClient57);


const char *MQTT_BROKER_ADDRESS = "192.168.169.35";
const uint16_t MQTT_PORT = 1883;

const char *MQTT_CLIENT_NAME = "ESP8266_" + idPlaca;

const char *topico = "Group35";

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

  if (content == "LuzON") {
    
    enciendeLuz();

  } else if (content == "LuzOFF") {
    
    apagaLuz();

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
  Serial.begin(9600);
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
  // Inicialización de los actuadores:

  
  pinMode(rele, OUTPUT);
  apagaLuz();
}

// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos
void ConnectMqtt()
{
  Serial.print("Starting MQTT connection...");
  if (client.connect(MQTT_CLIENT_NAME))
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

  String output;
  serializeJson(doc, output);
  return output;
}

void enciendeLuz(){
  String rele_value_body = serializeActuatorStatusBody(5, true);
  http.begin(espClient57, api);
  http.addHeader("Content-Type", "application/json");
  int httpCode = http.POST(rele_value_body);
  String payload = http.getString();
  http.end();

  if (httpCode == 201) {
    Serial.printf("HTTP POST code: %d\n", httpCode);
    Serial.println("Response payload: " + payload);
    digitalWrite(rele, HIGH);
  } else {
    Serial.printf("HTTP POST failed, error: %s\n", http.errorToString(httpCode).c_str());
  }
}

void apagaLuz(){
  String rele_value_body = serializeActuatorStatusBody(5, false);
    http.begin(espClient57, api);
    http.addHeader("Content-Type", "application/json");
    int httpCode = http.POST(rele_value_body);
    String payload = http.getString();
    http.end();

    if (httpCode == 201) {
      Serial.printf("HTTP POST code: %d\n", httpCode);
      Serial.println("Response payload: " + payload);
      digitalWrite(rele, LOW);
    } else {
      Serial.printf("HTTP POST failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
}

// Run the tests!
void loop()
{
  HandleMqtt();
}
