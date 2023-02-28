#!/usr/bin/env bash

echo 'start ping tool'
addr=$1

if [[ "${addr}" == "" ]]; then
    addr="baidu.com"
fi

ping $addr
