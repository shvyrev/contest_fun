#!/usr/bin/env bash

for USERS in 1 5 10 15 20 25 30 35 40
do
  echo "Runnning with $USERS users"
	for run in {1..2}
   do
        wrk --threads=$USERS --connections=$USERS -H "Content-Type:application/json" -d60s http://localhost:8080/feed
	done
done

#wrk -t12 -c1000 --latency -H "Content-Type:application/json" -d30m http://localhost:8080/feed