import serial
from time import sleep

ser = serial.Serial(
    port='/dev/serial0',
    baudrate = 9600,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE,
    bytesize=serial.EIGHTBITS,
    timeout=0
)
if (ser.isOpen()):
    ser.close()
ser.open()

for i in range(100):
    print(i)
    ser.write(("L1P" + str(i) + "\r").encode("ASCII"))
    sleep(0.1)
    
sleep (1)
ser.write(b"L1\r")
ser.close()

    
