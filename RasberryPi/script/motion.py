import RPi.GPIO as GPIO
import time
import subprocess, os
from datetime import datetime

GPIO.setmode(GPIO.BOARD)
GPIO.setup(7, GPIO.IN)         #Read output from PIR motion sensor         #LED output pin

pin_no=7

k=1


while True:
        i=GPIO.input(pin_no)
        if i==True:
                print "Motion detected"
                now = datetime.now()
                current_time = now.strftime("%H:%M:%S")
                print current_time
                pin_no=8
                subprocess.call("raspivid -o video"+str(k)+".h264 -t 10000", shell=True)
                pin_no=7
                now = datetime.now()
                current_time = now.strftime("%H:%M:%S")
                print current_time
                k=k+1
