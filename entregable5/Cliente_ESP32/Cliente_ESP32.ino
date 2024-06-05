#include <WiFi.h>
#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <PubSubClient.h>

#include <DHT.h>

// Defino el id de la placa y el id del grupo al que va a pertenecer
const int idPlaca = 124;
const int idGroup = 35;

// Configuración de los Sensores:

// Si hubiera más de un sensor de un tipo, usaria un array. Como solo hay
// un sensor de cada tipo, la implementación va a ser un poco más rudimentaria
#define sLuz 34
#define sTemp 4

DHT dht(sTemp, DHT11);


// Configuración de los Actuadores
#define rele 5
#define releVent 16


int test_delay = 500; // so we don't spam the API
boolean describe_tests = true;

// IP fijada para el portátil al conectarme al punto wifi del movil
String serverName = "http://192.168.169.35:8084/";
//String serverName = "http://192.168.56.1:8084/";
HTTPClient http;

// Credenciales de la red wifi
#define STASSID "POCO F3 P"    //"Your_Wifi_SSID"
#define STAPSK "WiFiPablo69" //"Your_Wifi_PASSWORD"
//#define STASSID "OV"    //"Your_Wifi_SSID"
//#define STAPSK "41930sanpedro2" //"Your_Wifi_PASSWORD"


// MQTT
WiFiClient espClient;
PubSubClient client(espClient);

const char *MQTT_BROKER_ADDRESS = "192.168.169.35";
const uint16_t MQTT_PORT = 1883;

const char *MQTT_CLIENT_NAME = "ESP32_" + idPlaca;

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

  if (content == "LuzON") {
    //TODO
    String rele_value_body = serializeActuatorStatusBody(rele, true);
    
    String serverPath = serverName + "api/reles/";
    http.begin(serverPath.c_str());
    int respuesta = http.POST(rele_value_body);
    test_response(respuesta);
    if(respuesta == 201){
      digitalWrite(rele, HIGH);
    }
    
  } else if (content == "LuzOFF") {
    String rele_value_body = serializeActuatorStatusBody(rele, false);
    
    String serverPath = serverName + "api/reles/";
    http.begin(serverPath.c_str());
    int respuesta = http.POST(rele_value_body);
    test_response(respuesta);
    if(respuesta == 201){
      digitalWrite(rele, LOW);
    }
  } else if(content == "VentiladorON"){
    String rele_value_body = serializeActuatorStatusBody(releVent, true);
    
    String serverPath = serverName + "api/reles/";
    http.begin(serverPath.c_str());
    int respuesta = http.POST(rele_value_body);
    test_response(respuesta);
    if(respuesta == 201){
      digitalWrite(releVent, HIGH);
    }
  
  } else if(content == "VentiladorOFF"){
    String rele_value_body = serializeActuatorStatusBody(releVent, false);
    
    String serverPath = serverName + "api/reles/";
    http.begin(serverPath.c_str());
    int respuesta = http.POST(rele_value_body);
    test_response(respuesta);
    if(respuesta == 201){
      digitalWrite(releVent, LOW);
    }
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

  pinMode(sLuz, INPUT);
  pinMode(rele, OUTPUT);
  digitalWrite(rele, LOW);
  pinMode(releVent, OUTPUT);
  digitalWrite(releVent, LOW);
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

  // Una vez conectamos al WiFi, Inicializamos MQTT
  InitMqtt();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
  
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
String serializeSensor(int idSensor, double valor, String tipo)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Add values in the document
  //
  doc["id" + tipo] = idSensor;
  doc["valor"] = valor;
  doc["idPlaca"] = idPlaca;
  doc["idGroup"] = idGroup;
  

  // Generate the minified JSON and send it to the Serial port.
  //
  String output;
  serializeJson(doc, output);
  //Serial.println(output);

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
  //describe("Test POST with sensor Luz");
  String serverPath = serverName + "api/sLuz/";
  http.begin(serverPath.c_str());
  test_response(http.POST(sLuz_value_body));

  String sTemp_value_body = serializeSensor(sTemp, dht.readTemperature(), "STemp");
  //describe("Test POST with sensor Temperatura");
  serverPath = serverName + "api/sTemp/";
  http.begin(serverPath.c_str());
  test_response(http.POST(sTemp_value_body));
}


void deserializeSensorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();

    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());



    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    // Si hemos recibido un Json con Json dentro, imprimimos el contenido del array
    if(array.size()!=0){
      for (JsonObject sensor : array)
      {
        int idSensor = sensor["idTemp"];
        double valor = sensor["valor"];
        int idPlaca = sensor["idPlaca"];
        int idGrupo = sensor["idGroup"];

        Serial.println(("Sensor deserialized: [idSensor: " + String(idSensor) + ", valor: " + String(valor) + ", idPlaca: " + String(idPlaca) + ", idGrupo: " + String(idGrupo) + "]").c_str());
      }
    // Si no, deserializamos de nuevo y esta vez no lo convertimos en Array
    }else{
      deserializeJson(doc, responseJson);
      int idSensor = doc["idTemp"];
      double valor = doc["valor"];
      int idPlaca = doc["idPlaca"];
      int idGrupo = doc["idGroup"];
      Serial.println(("Sensor deserialized: [idSensor: " + String(idSensor) + ", valor: " + String(valor) + ", idPlaca: " + String(idPlaca) + ", idGrupo: " + String(idGrupo) + "]").c_str());
    }
    
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}


void GET_tests()
{

  // No usaremos los GET para mucho, así que me limitaré a probar un par distintos del sTemp:
  describe("Test GET ultima entrada sTemp");
  String serverPath = serverName + "api/sTemp/last/" + String(sTemp);
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());

  describe("Test GET todos los Sensores Temp de un mismo grupo");
  serverPath = serverName + "api/sTemp/estado/" + String(idGroup);
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());



}
// Run the tests!
void loop()
{
  POST_tests();
  //GET_tests();
  HandleMqtt();

  Serial.println();
  Serial.println("##################################################################");
  Serial.println();
}
