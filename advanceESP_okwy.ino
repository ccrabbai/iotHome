#include <EEPROM.h>
#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>

class IoT {
  public:
    IoT() {
      getResponse = "";
      setEepromData("");
      setProcessedData("0");
      setRequestData("");
      setSensorData("");
    }
    void eeprom_begin (unsigned int size) {
      EEPROM.begin(size);
    }

    long eeprom_Read(unsigned int address, long len) {
      if (address >= 0 && address < 512) {
        EEPROM.get(address, len);
        return len;
      }
      return 0;
    }

    boolean eeprom_Write(unsigned int address, long value) {
      if (address >= 0 && address < 512) {
        EEPROM.put(address, value);
        EEPROM.commit();
        if (value == eeprom_Read(address, value) ) {
          EEPROM.end();
          return true;
        }
        else {
          EEPROM.end();
          return false;
        }
      }
      return false;
    }

    void IoTwifi_begin (const char ssid[], const char password[]) {
      WiFi.mode(WIFI_STA);
      WiFi.begin(ssid, password);
    }

    wl_status_t IoTwifi_Status (void) {
      return WiFi.status();
    }

    boolean IoT_httpRequest (void) {
      HTTPClient http;
      //Serial.println("Request data before Get: " +getRequestData());
      http.begin("http://navgation.herokuapp.com/data/" + getSensorData());
      int httpCode = http.GET();
      if (httpCode > 0) {
        //Serial.printf("[HTTP] sent: %d\n", httpCode);
        if (httpCode == HTTP_CODE_OK) {
          getResponse = "";
          getResponse = http.getString();
          // Serial.print("unprocessed response Data: ");
          //Serial.println( getResponse);
          http.end();
          process_http(getResponse);
          //Serial.println("unprocessed response Data: " + http_Data() );
          // Serial.println("response value: " + getProcessedData());
          return true;
        }
        // else Serial.printf("[HTTP] GET... failed, error: %s\n", http.errorToString(httpCode).c_str());
      }
      http.end();
      return false;
    }

    void process_http(String mss) {
      String data1 = mss;
      if (data1.startsWith("[") && data1.endsWith("]")) {
        data1.replace("[" , "");
        data1.replace("]" , "");
        data1.replace("," , "");
        setProcessedData(data1);
      }
    }
    String http_Data (void) {
      return getResponse;
    }
    void setEepromData (String mss) {
      eepromData = mss;
    }
    String getEepromData (void) {
      return eepromData;
    }
    String getProcessedData (void) {
      return ProcessedData;
    }
    void setProcessedData (String mss) {
      ProcessedData = mss;
    }
    void setRequestData (String mss) {
      requestData = mss;
    }
    String getRequestData (void) {
      return requestData;
    }
    void setSensorData(String sensor) {
      sensorData = sensor;}
      String getSensorData(void) {
        return sensorData;
      }

private:
      String getResponse, sensorData;
      String eepromData, ProcessedData, requestData;
    };

#define lightBulb D2
#define sleepLight D3
#define fan D4
#define sleepFan D5
#define networkPin D7
    const int switchSize = 4;
    const int arraySize = 9;
    const int pin[] = {4, 0, 2, 14};
    const char url[] = "Galaxy2020";
    const char pass[] = "sulc6886";

    extern "C" {
#include "user_interface.h"
#include "wpa2_enterprise.h"
}  
    IoT ig;

    void memTest () {
      ig.eeprom_begin(512);
      long socks = 0;
      socks = ig.eeprom_Read(0, socks);
      if (socks >= 0 && socks <= 123456) {
        ig.setEepromData(String(socks));
      }
      else ig.setEepromData("0");
    }

    void lightUp (String light) {
      Serial.println("Eeprom value: " + ig.getEepromData());
      //Serial.println ("http_data value: " + ig.getProcessedData());
      if ( ig.getEepromData() == ig.getProcessedData() ) {
        return;
      }
     const int ArraySize = light.length()+1;
    char lights[ArraySize];
    light.toCharArray(lights, ArraySize);
    char a = lights[0];
    char b = lights[1];
      if (String(a) == String("1")) {
        digitalWrite(sleepLight, LOW);
        digitalWrite(lightBulb, HIGH);
      }
      else if (String(a) == String("2")) {
        digitalWrite(lightBulb, LOW);
        digitalWrite(sleepLight, HIGH);
      }
      else {
        digitalWrite(lightBulb, LOW);
        digitalWrite(sleepLight, LOW);
      }

      if (String(b) == String("1")) {
        digitalWrite(sleepFan, LOW);
        digitalWrite(fan, HIGH);
      }
      else if (String(b) == String("2")) {
        digitalWrite(fan, LOW);
        digitalWrite(sleepFan, HIGH);
      }
      else {
        digitalWrite(fan, LOW);
        digitalWrite(sleepFan, LOW);
      }
    }

boolean saveData (void) {
  String buff = "";
  ig.setRequestData("");
  for (int i = 0; i < switchSize; i++) {
    int state = digitalRead(pin[i]);
    //ig.setRequestData(ig.getRequestData() + state);   // setData() saves 000011
    if (i<=1){
      if (state == 1 & i== 0){
        buff += 1;
        }
       else if (state == 1 & i==1){
          buff += 2;
        }
        
      }
     else{
      if (state == 1 & i== 2){
        buff += 1;
        }
       else if (state == 1 & i==3){
          buff += 2;
        }
       
      }
    }
  
  //Serial.print("Request Data: ");
  //Serial.println(ig.getRequestData());
  if (ig.getEepromData() != ig.getProcessedData()) {
    long data  = buff.toInt();

    Serial.print("long buffer: ");
    Serial.println(data);
    ig.eeprom_begin(512);
    boolean state = ig.eeprom_Write(0, data);
    return state;   // eeprom data is like 123456
  }
  return false;
}

void pinInit() {
  for (int i = 0; i < arraySize; i++) {
    pinMode (pin[i], OUTPUT);
    digitalWrite(pin[i], LOW);
  }
  pinMode (networkPin, OUTPUT);
  digitalWrite(networkPin, LOW);
}


void connectToNetwork() {
  int counter = 0;
  boolean check = true;
  while (check) {
    wifi_station_clear_enterprise_username();
    wifi_station_clear_enterprise_password();
    Serial.println("Attempting to connect");
    ig.IoTwifi_begin(url, pass);
    while ( (ig.IoTwifi_Status() != WL_CONNECTED) && (counter < 10) ) {
      counter++;
      Serial.println(counter);
      delay(1000);
      Serial.println("Waiting for connection");
    }
    if (ig.IoTwifi_Status() == WL_CONNECTED) {
      Serial.println("CONNECTION SUCCESSFUL");
      digitalWrite(networkPin, HIGH);
      check = false;
      counter = 0;
    }
    else counter = 0;
  }
  wifi_station_clear_enterprise_username();
  wifi_station_clear_enterprise_password();
}


void setup() {
  pinInit();
  memTest();
  lightUp(ig.getEepromData());
  Serial.begin(9600);
  pinMode(A0, INPUT);
  saveData();
  connectToNetwork();
}
int counter2 = 0;
boolean check2 = false;

void loop() {
  // put your main code here, to run repeatedly:
  while (check2) {
    connectToNetwork();
    check2 = false;
    counter2 = 0;
  }
  double temp = analogRead(A0);
  temp = (temp*5)/10.23;
  ig.setSensorData(String(temp) );
  if (ig.IoTwifi_Status() == WL_CONNECTED) {
    ig.setEepromData(ig.getProcessedData());
    if ( ig.IoT_httpRequest()) {
      lightUp(ig.getProcessedData());
      boolean state = saveData();
      digitalWrite(networkPin, HIGH);
        }else {digitalWrite(networkPin, LOW); counter2++; //Serial.println("Could not make a request to the server");
        }
    }else {digitalWrite(networkPin, LOW); counter2++; //Serial.println("Failed to connect to network");
    }
    if (counter2 >= 4){
      wifi_station_disconnect();
      check2 = true;
      delay(500);}
      //else { wifi_set_sleep_type(MODEM_SLEEP_T); }

}

