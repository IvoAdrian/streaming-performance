#!/bin/bash
#read all keys from redis and store them in txt file
set -ue
redis-cli keys "*" > keys.txt


while read -r key; do
x = `redis-cli TYPE $key`
	if [[x == "string"]]
	then 
	redis-cli GET $key
	elif [[x == "hash"]]
	then
	 redis-cli HGETALL $key
	else
	redis-cli LRANGE $key 0  -1
done < keys.txt



