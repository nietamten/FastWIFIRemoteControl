package com.example.niewazne.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;


class Joy extends View {

    Paint paint;
    float x, y, posX, posY;

    String temp = "000";

    final private float rP = (float)0.78;

    private int maxVelocity = 100; //254

    public Joy(Context context, AttributeSet attrs) {
        super(context, attrs);
//        maxVelocity = Remote.settings.getMax();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
    }

    private int color = getResources().getColor(R.color.black);

    public void SetColor(boolean connected, int temp){

        if (temp == 1024){
            this.temp = "< 87 C";
        }else {
            double resistance = (double) temp / (double) 0.1218880665;
            double themperature = ResistanceToThemperature(resistance);
            this.temp = Double.toString(themperature)+" C";
        }

        if(connected)
            color = getResources().getColor(R.color.green);
        else
            color = getResources().getColor(R.color.red);

        invalidate();
    }


    //https://stackoverflow.com/questions/839899/how-do-i-calculate-a-point-on-a-circle-s-circumference
//https://stackoverflow.com/questions/9970281/java-calculating-the-angle-between-two-points-in-degrees

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE ||
                event.getAction() == MotionEvent.ACTION_DOWN) {

            posX = event.getX() - getMeasuredWidth() / 2;
            posY = event.getY() - getMeasuredHeight() / 2;

            float r = (min(getMeasuredHeight(), getMeasuredWidth())/2)*rP;
            float lineLen = (float) sqrt(pow(getMeasuredWidth() / 2 - event.getX(), 2) + pow(getMeasuredHeight() / 2 - event.getY(), 2));
            float a = -(float) Math.atan2(posX, posY)+(float)Math.PI/2;

            if(lineLen <= r) {
                x = event.getX();
                y = event.getY();
            }
            else{
                x = (float) (getMeasuredWidth() / 2  + r * cos(a));
                y = (float) (getMeasuredHeight() / 2  + r * sin(a));
                lineLen = r;
            }

            byte[] buf = new byte[5];
            buf[0] =  (byte)'x';
            buf[1] = (byte)((int)((lineLen/r)*maxVelocity)&0xff);
            buf[2] = (byte)((int)(((Math.atan2(posX, posY)+Math.PI)/(2*Math.PI))*maxVelocity)&0xff);
            buf[3] = (byte)'y';
            byte xor = 0;
            for(int i=0;i<4;i++)
                xor ^= buf[i];
            buf[4] = xor;


            Log.d("X",String.valueOf((int)(buf[2]&0xff)));

            Remote.SetDataToSend(buf);

            invalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            byte[] buf = new byte[5];

            buf[0] =  (byte)'x';
            buf[1] = 0;
            buf[2] = 0;
            buf[3] = (byte)'y';
            byte xor = 0;
            for(int i=0;i<4;i++)
                xor ^= buf[i];
            buf[4] = xor;
            Remote.SetDataToSend(buf);

            x = getMeasuredWidth() / 2;
            y = getMeasuredHeight() / 2;
            invalidate();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float r = (min(getMeasuredHeight(), getMeasuredWidth())/2)*rP;

        paint.setColor(color);
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, r, paint);
        paint.setColor(getResources().getColor(R.color.black));
        canvas.drawCircle(x, y,  r * (float) 0.1, paint);
        paint.setColor(getResources().getColor(R.color.white));
        canvas.drawLine(0, getMeasuredHeight() / 2, getMeasuredWidth(), getMeasuredHeight() / 2, paint);
        canvas.drawLine(getMeasuredWidth() / 2, 0, getMeasuredWidth() / 2, getMeasuredHeight(), paint);
        paint.setColor(getResources().getColor(R.color.black));
        paint.setTextSize(44);
        canvas.drawText(temp,100,100,paint);
    }

    private double ResistanceToThemperature(double resistance){
        resistance /=1000;

        double upRes = 10000000;
        double upTherm = 0;
        double downRes = -1000000;
        double downTherm = 0;

        for (double[] o : ntc){

            if(o[1]>resistance && o[1] < upRes) {
                upRes = o[1];
                upTherm = o[0];
            }
            if(o[1]<resistance && o[1] > downRes) {
                downRes = o[1];
                downTherm = o[0];
            }
        }

        return (upTherm+downTherm)/2;
    }

    final private static double[][] ntc={
            {-20 ,959.0500 },
            {-19 ,906.0117  },
            {-18 ,856.2883  },
            {-17 ,809.6506  },
            {-16 ,765.8865  },
            {-15 ,724.8000  },
            {-14 ,685.6507  },
            {-13 ,648.8929  },
            {-12 ,614.3649  },
            {-11 ,581.9169  },
            {-10 ,551.4100  },
            {-30 ,1733.2000 },
            {-29 ,1630.4080 },
            {-28 ,1534.4770 },
            {-27 ,1444.9030 },
            {-26 ,1361.2200 },
            {-25 ,1283.0000 },
            {-24 ,1209.3270 },
            {-23 ,1140.4240 },
            {-22 ,1075.9490 },
            {10  ,199.9900 },
            {11  ,190.5578 },
            {12  ,181.6319 },
            {13  ,173.1822 },
            {14  ,165.1804 },
            {15  ,157.6000 },
            {16  ,150.4253 },
            {17  ,143.6234 },
            {18  ,137.1726 },
            {-21 ,1015.588 },
            {19  ,131.0528 },
            {20  ,125.2450 },
            {21  ,119.6582 },
            {22  ,114.3559 },
            {23  ,109.3221 },
            {24  ,104.5415 },
            {25  ,100.0000 },
            {26  ,95.8191 },
            {27  ,91.8392 },
            {28  ,88.0494 },
            {29  ,84.4395 },
            {30  ,81.0000 },
            {-9  ,522.6908  },
            {-8  ,495.6674  },
            {-7  ,470.2286  },
            {-6  ,446.2714  },
            {-5  ,423.7000  },
            {-4  ,402.0560  },
            {-3  ,381.6658  },
            {-2  ,362.4488  },
            {-1  ,344.3301  },
            {31  ,77.6238 },
            {32  ,74.4091 },
            {33  ,71.3472 },
            {34  ,68.4301 },
            {35  ,65.6500 },
            {36  ,62.9838 },
            {37  ,60.4420 },
            {38  ,58.0181 },
            {39  ,55.7060 },
            {0   ,27.2400},
            {1   ,11.0397},
            {2   ,95.7506},
            {3   ,81.3157},
            {4   ,67.6820},
            {5   ,54.8000},
            {6   ,42.5827},
            {7   ,31.0321},
            {8   ,20.1080},
            {40  ,53.5000 },
            {41  ,51.3708 },
            {42  ,49.3391 },
            {43  ,47.3998 },
            {44  ,45.5483 },
            {45  ,43.7800 },
            {46  ,42.0555 },
            {47  ,40.4092 },
            {48  ,38.8369 },
            {9   ,209.7724},
            {49  ,37.3350},
            {50  ,35.8999},
            {51  ,34.6160},
            {52  ,33.3855},
            {53  ,32.2059},
            {54  ,31.0748},
            {55  ,29.9900},
            {56  ,28.9053},
            {57  ,27.8660},
            {58  ,26.8700},
            {59  ,25.9153},
            {60  ,25.0000},
            {61  ,24.1099},
            {62  ,23.2565},
            {63  ,22.4381},
            {64  ,21.6531},
            {65  ,20.9000},
            {66  ,20.1741},
            {67  ,19.4774},
            {68  ,18.8087},
            {69  ,18.1666},
            {70  ,17.5500},
            {71  ,16.9459},
            {72  ,16.3659},
            {73  ,15.8089},
            {74  ,15.2739},
            {90  ,9.1000 },
            {91  ,8.8171 },
            {92  ,8.5444 },
            {93  ,8.2816 },
            {94  ,8.0283 },
            {95  ,7.7840 },
            {96  ,7.5538 },
            {97  ,7.3316 },
            {98  ,7.1172 },
            {99  ,6.9100 },
            {100 ,6.7100 },
            {101 ,6.5265 },
            {102 ,6.3490 },
            {103 ,6.1772 },
            {104 ,6.0109 },
            {105 ,5.8500 },
            {106 ,5.6832 },
            {107 ,5.5221 },
            {108 ,5.3663 },
            {109 ,5.2156 },
            {110 ,5.0700 },
            {111 ,4.9291 },
            {112 ,4.7928 },
            {113 ,4.6610 },
            {114 ,4.5334 },
            {115 ,4.4100 },
            {116 ,4.2906 },
            {117 ,4.1751 },
            {118 ,4.0632 },
            {119 ,3.9549 },
            {120 ,3.8500 },
            {121 ,3.7410 },
            {122 ,3.6357 },
            {123 ,3.5338 },
            {124 ,3.4353 },
            {125 ,3.3400 },
            {75  ,14.7600 },
            {76  ,14.2813 },
            {77  ,13.8206 },
            {78  ,13.3774 },
            {79  ,12.9507 },
            {80  ,12.5400 },
            {81  ,12.1347 },
            {82  ,11.7447 },
            {83  ,11.3694 },
            {84  ,11.0080 },
            {85  ,10.6600 },
            {86  ,10.3243},
            {126 ,3.2550 },
            {87  ,10.0010},
            {127 ,3.1726 },
            {88  ,9.6895 },
            {128 ,3.0927 },
            {89  ,9.3893 },
            {129 ,3.0152 },
            {130 ,2.9400 },
            {131 ,2.8634 },
            {132 ,2.7893 },
            {133 ,2.7173 },
            {134 ,2.6476 },
            {135 ,2.5800 },
            {136 ,2.5144 },
            {137 ,2.4507 },
            {138 ,2.3890 },
            {139 ,2.3291 },
            {140 ,2.2710 },
            {141 ,2.2135 },
            {142 ,2.1577 },
            {143 ,2.1035 },
            {144 ,2.0510 },
            {145 ,2.0000 },
            {146 ,1.9513 },
            {147 ,1.9040 },
            {148 ,1.8580 },
            {149 ,1.8134 },
            {150 ,1.7700 },
            {151 ,1.7319 },
            {152 ,1.6947 },
            {153 ,1.6586 },
            {154 ,1.6233 },
            {155 ,1.5890 },
            {156 ,1.5520 },
            {170 ,1.1220 },
            {171 ,1.0956 },
            {172 ,1.0699 },
            {173 ,1.0449 },
            {174 ,1.0206 },
            {175 ,0.9970 },
            {176 ,0.9757 },
            {177 ,0.9550 },
            {178 ,0.9348 },
            {179 ,0.9151 },
            {180 ,0.8960 },
            {181 ,0.8750 },
            {182 ,0.8547 },
            {183 ,0.8349 },
            {184 ,0.8157 },
            {185 ,0.7970 },
            {186 ,0.7806 },
            {187 ,0.7646 },
            {188 ,0.7490 },
            {189 ,0.7338 },
            {190 ,0.7190 },
            {191 ,0.7029 },
            {192 ,0.6873 },
            {193 ,0.6721 },
            {194 ,0.6574 },
            {195 ,0.6430 },
            {196 ,0.6302 },
            {197 ,0.6177 },
            {198 ,0.6055 },
            {199 ,0.5936 },
            {200 ,0.5820 },
            {201 ,0.5717 },
            {202 ,0.5617 },
            {203 ,0.5519 },
            {204 ,0.5423 },
            {205 ,0.5330 },
            {206 ,0.5225 },
            {207 ,0.5122 },
            {208 ,0.5022 },
            {209 ,0.4925 },
            {157 ,1.5160 },
            {158 ,1.4811 },
            {159 ,1.4471 },
            {160 ,1.4140 },
            {161 ,1.3812 },
            {162 ,1.3494 },
            {163 ,1.3184 },
            {164 ,1.2883 },
            {165 ,1.2590 },
            {166 ,1.2301 },
            {167 ,1.2019 },
            {168 ,1.1745 },
            {169 ,1.1479 },
            {250 ,0.2300 },
            {251 ,0.2262 },
            {252 ,0.2223 },
            {253 ,0.2186 },
            {254 ,0.2150 },
            {255 ,0.2114 },
            {256 ,0.2079 },
            {257 ,0.2045 },
            {258 ,0.2011 },
            {259 ,0.1978 },
            {260 ,0.1946 },
            {261 ,0.1914 },
            {262 ,0.1883 },
            {263 ,0.1853 },
            {264 ,0.1823 },
            {265 ,0.1794 },
            {266 ,0.1765 },
            {267 ,0.1737 },
            {268 ,0.1710 },
            {269 ,0.1683 },
            {270 ,0.1656 },
            {271 ,0.1630 },
            {272 ,0.1604 },
            {210 ,0.4830 },
            {211 ,0.4733 },
            {212 ,0.4639 },
            {213 ,0.4547 },
            {214 ,0.4457 },
            {215 ,0.4370 },
            {216 ,0.4284 },
            {217 ,0.4200 },
            {218 ,0.4118 },
            {219 ,0.4038 },
            {220 ,0.3960 },
            {221 ,0.3884 },
            {222 ,0.3810 },
            {223 ,0.3739 },
            {224 ,0.3668 },
            {225 ,0.3600 },
            {226 ,0.3533 },
            {227 ,0.3467 },
            {228 ,0.3403 },
            {229 ,0.3341 },
            {230 ,0.3280 },
            {231 ,0.3220 },
            {232 ,0.3161 },
            {233 ,0.3104 },
            {234 ,0.3048 },
            {235 ,0.2993 },
            {236 ,0.2940 },
            {237 ,0.2888 },
            {238 ,0.2836 },
            {239 ,0.2786 },
            {240 ,0.2737 },
            {241 ,0.2689 },
            {242 ,0.2642 },
            {243 ,0.2596 },
            {244 ,0.2551 },
            {245 ,0.2507 },
            {246 ,0.2464 },
            {247 ,0.2422 },
            {248 ,0.2380 },
            {249 ,0.2340 },
            {273 ,0.1579 },
            {274 ,0.1555 },
            {275 ,0.1531 },
            {276 ,0.1507 },
            {277 ,0.1484 },
            {278 ,0.1461 },
            {279 ,0.1439 },
            {280 ,0.1417 },
            {281 ,0.1396 },
            {282 ,0.1375 },
            {283 ,0.1354 },
            {284 ,0.1334 },
            {285 ,0.1314 },
            {286 ,0.1295 },
            {287 ,0.1275 },
            {288 ,0.1257 },
            {289 ,0.1238 },
            {290 ,0.1220 },
            {291 ,0.1202 },
            {292 ,0.1185 },
            {293 ,0.1168 },
            {294 ,0.1151 },
            {295 ,0.1134 },
            {296 ,0.1118 },
            {297 ,0.1102 },
            {298 ,0.1086 },
            {299 ,0.1071 },
            {300 ,0.1056 }
    };

}

