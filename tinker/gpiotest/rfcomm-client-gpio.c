// Client side C/C++ program to demonstrate Socket programming
#include <stdio.h> 
#include <sys/socket.h> 
#include <arpa/inet.h>
#include <netinet/in.h> 
#include <netdb.h>
#include <unistd.h>
#include <time.h>
#include <string.h> 
#include <stdlib.h>
#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/rfcomm.h>

#define UUID_STR "00001101-0000-1000-8000-00805f9b34fb"
#define DEVICE_NAME "linaro-alip"

int open_rfcomm_socket(const char *uuid_str) {
    inquiry_info *ii = NULL;
    int max_rsp, num_rsp;
    int dev_id, sock, len, flags;
    char addr[19] = { 0 };
    char name[248] = { 0 };
    char dest[18] = "58:11:22:61:FF:73";

    dev_id = hci_get_route(NULL);
    sock = hci_open_dev(dev_id);
    if (dev_id < 0 || sock < 0) {
        perror("Opening socket");
        exit(1);
    }

    len = 8;
    max_rsp = 255;
    flags = IREQ_CACHE_FLUSH;
    ii = (inquiry_info *)malloc(max_rsp * sizeof(inquiry_info));

    num_rsp = hci_inquiry(dev_id, len, max_rsp, NULL, &ii, flags);
    if (num_rsp < 0) {
        perror("Inquiry failed");
        exit(1);
    }

    for (int i = 0; i < num_rsp; i++) {
        ba2str(&(ii + i)->bdaddr, addr);
        memset(name, 0, sizeof(name));
        if (hci_read_remote_name(sock, &(ii + i)->bdaddr, sizeof(name), name, 0) < 0)
            strcpy(name, "unknown");

        printf("%s %s\n", name, addr);
        if (strcmp(name, DEVICE_NAME) == 0) {
            struct sockaddr_rc addr2 = {0};
            addr2.rc_family = AF_BLUETOOTH;
            addr2.rc_channel = (uint8_t)1;
            str2ba( addr, &addr2.rc_bdaddr );

            // allocate a socket
            sock = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
            int connection_status = connect(sock, (struct sockaddr *)&addr2, sizeof(addr2));
            if (connection_status == 0) {
                printf("Connected to device: %s (%s)\n", name, addr);
                free(ii);
                return sock;
            }
        }
    }

    free(ii);
    close(sock);
    return -1; // Device not found
}

int sendcmd( int sock, char* command, int delay )
{
    int valread;
    char buffer[1024] = {0};

    write(sock , command, strlen(command));
    valread = read( sock, buffer, 1024 );
    buffer[valread] = '\0';
    printf( "%s\n", buffer );
    sleep( delay );
    return 0;
}

int led_test( int sock )
{
    int i;

    for ( i = 0; i < 5; i++ ) {
        sendcmd( sock, "led_on", 1 );
        sendcmd( sock, "led_off", 1 );
    }
    return 0;
}

int lcd_test( int sock )
{
    int i;

    for ( i = 0; i < 3; i++ ) {
        sendcmd( sock, "lcd_red", 1 );
        sendcmd( sock, "lcd_green", 1 );
        sendcmd( sock, "lcd_blue", 1 );
        sendcmd( sock, "lcd_yellow", 1 );
        sendcmd( sock, "lcd_cyan", 1 );
        sendcmd( sock, "lcd_purple", 1 );
        sendcmd( sock, "lcd_white", 1 );
        sendcmd( sock, "lcd_black", 1 );
    }
    sendcmd( sock, "Hello world !\nIt works !\n", 1 );
    return 0;
}

int servo( int sock )
{
    int i;

    for ( i = 0; i < 3; i++ ) {
        sendcmd( sock, "servo_middle", 1 );
        sendcmd( sock, "servo_right", 1 );
        sendcmd( sock, "servo_left", 1 );
        sendcmd( sock, "servo_middle", 1 );
        sendcmd( sock, "servo_left", 1 );
        sendcmd( sock, "servo_right", 1 );
        sendcmd( sock, "servo_middle", 1 );
        sendcmd( sock, "servo_left", 1 );
    }
    sendcmd( sock, "servo_stop", 1 );
    return 0;
}

int main(int argc, char const *argv[])
{
    int sock;

    // Open an RFCOMM socket using device discovery
    sock = open_rfcomm_socket(UUID_STR);

    if( sock != -1 ) {
       // send commands
       sendcmd( sock, "start", 1 );
       led_test( sock );
       lcd_test( sock );
       servo( sock );
       sendcmd( sock, "bye", 0 );
       close( sock );
    } else {
        printf("Device not found or connection failed.\n");
    }

    return 0;
}
