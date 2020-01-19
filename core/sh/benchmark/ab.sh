#!/usr/bin/env bash

ab -c 5 -n 5000 -e ab_benchmark.csv http://localhost:8080/feed