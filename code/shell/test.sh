#!/usr/bin/env bash

if [[ "${params.start}" == "" ]]; then
    ansible-playbook main.yml -i host
else
    ansible-playbook main.yml -i host --start=\"${params.start}\"
fi
