#!/bin/bash
sudo docker run -it --device /dev/video0 -p 8080:8080  iot-smart-parking-overwatch --name iot-smart-parking-overwatch