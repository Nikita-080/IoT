#!/usr/bin/env python3
import requests
import time

from threading import Thread


alarm_working = True

def ask_server():
    global alarm_working

    headers = requests.utils.default_headers()
    headers.update(
            {
                'User-Agent': 'Embedded'
            }
        )

    while True:
        response = requests.get("https://serverapp:8080", headers=headers, verify="app.crt")
        if response.status_code == 200:
            print("Get state:", response.text)
            if response.text == "on":
                alarm_working = True
                print("Alarm state is on.")
            elif response.text == "off":
                alarm_working = False
                print("Alarm state is off.")
            else:
                if not response.text == "": 
                    print("Error! Wrong answer from server then asking for alarm state.")
        else:
            print("Error! Server status code: " + response.status_code)
        time.sleep(4)


def watch_alarm():
    headers = requests.utils.default_headers()
    headers.update(
            {
                'User-Agent': 'Embedded'
            }
        )
    detecter_channel = 12
    sound_channel = 18

    time.sleep(3)
 
    while True:
        if alarm_working:
            send = False
            while not send:
                response = requests.post("https://serverapp:8080", data={"signal":"alarm"}, headers=headers, verify="app.crt")
                if response.status_code == 200:
                    send = True
                    print("Message_delevered.")
                else:
                    print("Error! Server status code: " + response.status_code)
        time.sleep(4)


def main():
    ask_server_thread = Thread(target=ask_server)
    watch_alarm_thread = Thread(target=watch_alarm)

    ask_server_thread.start()
    watch_alarm_thread.start()

    ask_server_thread.join()
    watch_alarm_thread.join()
    

if __name__ == "__main__":
    main()
    
