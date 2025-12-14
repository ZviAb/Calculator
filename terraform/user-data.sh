#!/bin/bash
curl -fsSL https://get.docker.com | sh

usermod -aG docker ubuntu

systemctl start docker
systemctl enable docker

docker run -d -p 8080:8080 --name hostname-docker ${docker_image}:${app_version}

