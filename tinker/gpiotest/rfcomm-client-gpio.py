#!/usr/bin/python3

import sys
import bluetooth
import time

# bd_addr = "58:11:22:61:FF:73"

# port = 1
uuid = "00001101-0000-1000-8000-00805f9b34fb"
service_matches = bluetooth.find_service( uuid = uuid )

if len(service_matches) == 0:
    print("couldn't find the FooBar service")
    sys.exit(0)

first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]

print("connecting to \"%s\" on %s" % (name, host))
sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
sock.connect((host, port))

sock.send("start".encode())
print(sock.recv(1024))

for i in range( 5 ):
    sock.send("led_on".encode())
    print(sock.recv(1024))
    time.sleep( 1 )
    sock.send("led_off".encode())
    print(sock.recv(1024))
    time.sleep( 1 )

for i in range( 5 ):
    sock.send("lcd_red".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_green".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_blue".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_yellow".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_cyan".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_purple".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_white".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("lcd_black".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )

sock.send( "Hello world !\nIt works !\n".encode() )
print(sock.recv(1024))

for i in range( 5 ):
    sock.send("servo_middle".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_right".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_left".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_middle".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_left".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_right".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_middle".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )
    sock.send("servo_left".encode())
    print(sock.recv(1024))
    time.sleep( 0.8 )

sock.send("servo_stop".encode())
print(sock.recv(1024))
sock.send("bye".encode())
print(sock.recv(1024))

sock.close()
