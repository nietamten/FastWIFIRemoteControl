
#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library (you most likely already have this in your sketch)

//#include <DNSServer.h>            //Local DNS Server used for redirecting all requests to the configuration portal
//#include <ESP8266WebServer.h>     //Local WebServer used to serve the configuration portal
//#include <WiFiManager.h>          //https://github.com/tzapu/WiFiManager WiFi Configuration Magic
#include "ESPAsyncUDP.h"

//WiFiManager wifiManager;
AsyncUDP udpSend;
AsyncUDP udpRec;
IPAddress    apIP(10, 10, 10, 1);                               // Private network address: local & gateway

   bool rec = false;

volatile               byte v;
volatile            byte a;
volatile bool change = false;
volatile bool front = false;


#define R1 12
#define R2 13
#define M1 5
#define M2 4 


void ride()
{
      digitalWrite(M2, LOW);
      digitalWrite(M1, LOW);

    if(a>64&&a<194)
    {
      if(v>250)
        digitalWrite(M1, HIGH);
       else
        analogWrite(M1, (float)((float)v/(float)(255))*(float)PWMRANGE);              
      front =false;
    }
    else
    {
      if(v>250)
        digitalWrite(M2, HIGH);
       else
        analogWrite(M2, (float)((float)v/(float)(255))*(float)PWMRANGE); 
        front = true;
    }
}

void setup() {

analogWriteFreq(500);

  //if(!wifiManager.autoConnect()) {
    //ESP.reset();
    //delay(1000);
  //} 


//for(int i=0;i<4;i++)
//{
//pinMode(i,OUTPUT);
//digitalWrite(i,LOW);
//}

digitalWrite(R1,LOW);
pinMode(R1,OUTPUT);
digitalWrite(R2,LOW);
pinMode(R2,OUTPUT);

digitalWrite(M1,LOW);
pinMode(M1,OUTPUT);
digitalWrite(M2,LOW);
pinMode(M2,OUTPUT);

delay(100);

//digitalWrite(2,LOW);

  WiFi.mode(WIFI_AP_STA);
  WiFi.softAPConfig(apIP, apIP, IPAddress(255, 255, 255, 0));   // subnet FF FF FF 00
  WiFi.softAP("autko");


  if(udpSend.listen(9998)) {
    if(udpRec.listen(9999)) {
     
      udpRec.onPacket([](AsyncUDPPacket packet) {

        rec = true;

        if(packet.length() == 5 && packet.data()[0] == 'x' && packet.data()[3] == 'y')
        {

          byte xorv = 0;
          for(int i=0;i<4;i++)
            xorv ^= packet.data()[i]; 
          if(xorv == packet.data()[4])
          {
//digitalWrite(2,HIGH);
             v = packet.data()[1];
             a = packet.data()[2];

            ride();

            byte va;
            {
              if(a<64)
              {
                va = a;
              }
              else if(a<128)
              {
                va = 64-(a-64);
              }
              else if(a<192)
              {
                va = a-128;
              }
              else
              {
                va = 64-(a-192);
              }
            }
            
            if(a<128)
            {
              //right
              digitalWrite(R1, LOW);
              analogWrite(R2, (float)((float)va/(float)(64))*(float)PWMRANGE);
            }
            else
            {
              //left
              digitalWrite(R2, LOW);
              analogWrite(R1, (float)((float)va/(float)(64))*(float)PWMRANGE);
            }
            
          }
        }
        
      });


      //digitalWrite(2,LOW);
      //analogWrite(2, 500);    
    }else{
      ESP.reset(); 
    }
  }else{
    ESP.reset();
  }
}

int c = 0;

void loop() {


    rec = false;
    delay(500);
    uint16 t = analogRead(A0);
    uint8_t msg[3];
    msg[0] = 'o';
    *((uint16*)(&msg[1])) = t;
    //msg[1] = ((uint8_t*)t)[0];
    //msg[2] = ((uint8_t*)t)[1]; 
    
    udpSend.broadcast(msg,3) ;
    if(!rec)
    {
      c++;
      if(c>1)
      {
        digitalWrite(R1, LOW);    
        digitalWrite(R2, LOW);;    
        digitalWrite(M1, LOW);    
        digitalWrite(M2, LOW);    
      }
      else
        c=0;
    }
  
}
