#!/usr/bin/env bash
if [ ! -d "public" ]; then
	mkdir public
fi

if [ ! -d "public/css" ]; then
	mkdir public/css
fi

if [ ! -d "public/js" ]; then
	mkdir public/js
fi

if [ ! -d "public/fonts" ]; then
	mkdir public/fonts
fi


sbt fastOptJS

cp webclient/src/main/resources/index.html public/
cp webclient/src/main/resources/css/* public/css/
cp -R webclient/src/main/resources/fonts/* public/fonts/
cp webclient/target/scala-*/*.js* public/js/
