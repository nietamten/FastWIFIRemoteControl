
#include <ESP8266WiFi.h>          //ESP8266 Core WiFi Library (you most likely already have this in your sketch)
#include "ESPAsyncUDP.h"

AsyncUDP udpSend;
AsyncUDP udpRec;

bool rec = false;

volatile byte v; 
volatile byte a;
volatile bool change = false;
volatile bool front = false;


#define SERVO 12
#define MDIR 13
#define MPWM 14 


void ride()
{
    if(a>64&&a<194)
      digitalWrite(MDIR, LOW);
    else
      digitalWrite(MDIR, HIGH);

    if(v>250)
      digitalWrite(MPWM, HIGH);
    else
      analogWrite(MPWM, (float)((float)v/(float)(255))*(float)PWMRANGE);  
}

void setup() {

  analogWriteFreq(50);


  pinMode(SERVO,OUTPUT);
  pinMode(MDIR,OUTPUT);
  pinMode(MPWM,OUTPUT);
  digitalWrite(SERVO, LOW);    
  digitalWrite(MDIR, LOW);    
  digitalWrite(MPWM, LOW); 

  //Serial.begin(115200);
  //Serial.println("start 1");
  
  IPAddress local_IP(192,168,4,22);
  IPAddress gateway(192,168,4,9);
  IPAddress subnet(255,255,255,0);

  //WiFi.mode(WIFI_AP_STA);
  WiFi.softAPConfig(local_IP, gateway, subnet);
  //WiFi.softAPConfig(apIP, apIP, IPAddress(255, 255, 255, 0));   // subnet FF FF FF 00
  WiFi.softAP("autko");


  if(udpSend.listen(9998)) {
    if(udpRec.listen(9999)) {
     
      udpRec.onPacket([](AsyncUDPPacket packet) {

      //Serial.println("rec ");

        rec = true;

        if( packet.length() == 5 && 
            packet.data()[0] == 'x' && 
            packet.data()[3] == 'y')
        {
          //Serial.println("rec test 1 ok");
          
          byte xorv = 0;
          for(int i=0;i<4;i++)
            xorv ^= packet.data()[i]; 
          if(xorv == packet.data()[4])
          {
            //Serial.println("rec test 2 ok");
            
            //digitalWrite(2,HIGH);
            v = packet.data()[1];
            a = packet.data()[2];
            
            ride();

            byte va;
            if(a <= 64)
              va = 64-a;
            else if (a <= 128)
              va = a-64;
            else if (a <= 192)
              va = a-64;
            else
              va = 128-(a-192);

            float prop = (float)va/(float)(128);
            //unsigned int activeRange = (PWMRANGE/20);
            //float servo = (float)activeRange+((float)activeRange*prop);

            //wyznaczone eksperymentalnie dla MG996R
            unsigned int activeRange = 231;
            unsigned int servo = (float)36+231-((float)activeRange*prop);

            analogWrite(SERVO, servo);
            //Serial.println(servo);
            //Serial.println(va);
          }
        }
        
      });
 
    }else{
      ESP.reset(); 
    }
  }else{
    ESP.reset();
  }
 
}

int c = 0;


void loop() {
/*
  Serial.begin(115200);
  for(uint16 i=0;i<65535;i++)
   {
    analogWrite(SERVO, i);
    Serial.println(i);
    delay(300);
   }
    return;
*/
    rec = false;
    delay(500);
    uint16 t = analogRead(A0);
    uint8_t msg[3];
    msg[0] = 'o';
    *((uint16*)(&msg[1])) = t;
    //msg[1] = ((uint8_t*)t)[0];
    //msg[2] = ((uint8_t*)t)[1]; 
    
    //udpRec.broadcastTo(msg,3,9998) ;
    udpSend.writeTo(msg,3,IPAddress(192,168,4, 255),9998) ;
 
    //Serial.println("send");
    if(!rec)
    {
      //Serial.println("!res");
      c++;
      if(c>1)
      {
        digitalWrite(SERVO, LOW);    
        digitalWrite(MDIR, LOW);    
        digitalWrite(MPWM, LOW);    
      }
      else
        c=0;
    }
  
}
