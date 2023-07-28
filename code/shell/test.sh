#!/usr/bin/env bash

if [[ "${params.start}" == "" ]]; then
    ansible-playbook main.yml -i host
else
    ansible-playbook main.yml -i host --start=\"${params.start}\"
fi

if [ -f "/path/to/file" ]; then
    echo "File \"/path/to/file\" exists"
fi
